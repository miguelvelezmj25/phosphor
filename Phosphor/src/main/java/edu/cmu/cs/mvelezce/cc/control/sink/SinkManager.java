package edu.cmu.cs.mvelezce.cc.control.sink;

import edu.cmu.cs.mvelezce.cc.instrumenter.SinkInstrumenter;
import edu.columbia.cs.psl.phosphor.PreMain;
import edu.columbia.cs.psl.phosphor.control.standard.StandardControlFlowStack;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public final class SinkManager {

  private static final Map<Class, Integer> CLASSES_TO_INTS = new HashMap<>();
  private static final Map<Field, Integer> FIELDS_TO_INTS = new HashMap<>();
  private static final Map<SinkData, Integer> SINK_DATAS_TO_INTS = new HashMap<>();
  private static final byte[] NEW_LINE_BYTES = "\n".getBytes();

  private static int classCount = 0;
  private static int fieldCount = 0;
  private static int sinkDataCount = 0;

  private static String programName;

  private SinkManager() {
    System.out.println();
  }

  public static void preProcessSinks(String programName) {
    SinkManager.programName = programName;
  }

  public static void postProcessSinks() {
    long start = System.nanoTime();
    saveData();
    saveClassesToInts();
    saveFieldsToInts();
    System.out.println("Sink processing took " + (System.nanoTime() - start) / 1E9 + " s");
  }

  private static void saveClassesToInts() {
    File outputFile =
        new File("../../../examples/control/" + SinkManager.programName + "/classes.ser");
    try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
      for (Map.Entry<Class, Integer> entry : CLASSES_TO_INTS.entrySet()) {
        dos.writeBytes(entry.getKey().getName());
        dos.write(NEW_LINE_BYTES);
        dos.writeInt(entry.getValue());
        dos.write(NEW_LINE_BYTES);
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private static void saveFieldsToInts() {
    File outputFile =
        new File("../../../examples/control/" + SinkManager.programName + "/fields.ser");
    try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
      for (Map.Entry<Field, Integer> entry : FIELDS_TO_INTS.entrySet()) {
        dos.writeBytes(entry.getKey().getName());
        dos.write(NEW_LINE_BYTES);
        dos.writeInt(entry.getValue());
        dos.write(NEW_LINE_BYTES);
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private static <T> void saveData() {
    File outputFile =
        new File("../../../examples/control/" + SinkManager.programName + "/data.ser");
    try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
      Class[] classes = PreMain.getInstrumentation().getAllLoadedClasses();

      for (Class clazz : classes) {
        // TODO might be able to not look at some classes here
        boolean hasData = false;

        Field[] fields = clazz.getFields();

        for (Field field : fields) {
          String fieldName = field.getName();

          if (!fieldName.startsWith(SinkInstrumenter.CC_STATIC_FIELD_PREFIX)) {
            continue;
          }

          edu.columbia.cs.psl.phosphor.struct.harmony.util.Set<SinkData<T>> data =
              (edu.columbia.cs.psl.phosphor.struct.harmony.util.Set<SinkData<T>>) field.get(null);

          for (SinkData<T> sinkData : data) {
            hasData = true;
            Integer index = SINK_DATAS_TO_INTS.get(sinkData);

            if (index == null) {
              SINK_DATAS_TO_INTS.put(sinkData, SinkManager.sinkDataCount);
              index = SinkManager.sinkDataCount;
            }

            dos.writeInt(SinkManager.classCount);
            dos.write(NEW_LINE_BYTES);
            dos.writeInt(SinkManager.fieldCount);
            dos.write(NEW_LINE_BYTES);
            dos.writeInt(index);
            dos.write(NEW_LINE_BYTES);

            SinkManager.sinkDataCount++;
          }

          if (hasData) {
            FIELDS_TO_INTS.put(field, SinkManager.fieldCount);
            SinkManager.fieldCount++;
          }
        }

        if (hasData) {
          CLASSES_TO_INTS.put(clazz, SinkManager.classCount);
          SinkManager.classCount++;
        }
      }
    } catch (IOException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> SinkData<T> getSinkData(
      StandardControlFlowStack<T> stack, Taint<T> dataTaints) {
    // TODO Should we cache these taints?
    return new SinkData<T>(stack.copyTag(), dataTaints);
  }

  //  synchronized void checkTaint(
  //          ControlTaintTagStack controlTaintTagStack, Object taintedObject, String id) {
  //    try {
  //      this.fos.write(id.getBytes());
  //      this.fos.write(NEW_LINE_BYTES);
  //      this.writeTaints(controlTaintTagStack.getTaintHistory().peek());
  //      this.fos.write(NEW_LINE_BYTES);
  //      this.writeTaints(taintedObject);
  //      this.fos.write(NEW_LINE_BYTES);
  //    } catch (IOException e) {
  //      // TODO throw error that the taint cannot be written.
  //    }
  //  }
  //
  //  private void writeTaints(Object taintedObject) throws IOException {
  //    if (taintedObject == null) {
  //      this.fos.write(EMPTY);
  //    } else if (taintedObject instanceof Taint) {
  //      this.writeTaintLabels((Taint) taintedObject);
  //    } else if (taintedObject instanceof TaintedWithObjTag) {
  //      Taint taint = (Taint) ((TaintedWithObjTag) taintedObject).getPHOSPHOR_TAG();
  //      this.writeTaintLabels(taint);
  //    } else {
  //      throw new RuntimeException("What is this thing now? " + taintedObject.getClass());
  //      //      this.fos.write(EMPTY);
  //    }
  //  }
  //
  //  private static <T> void processTaintLabels(Taint<T> taint) {
  //    if (taint == null) {
  //      //      this.fos.write(EMPTY);
  //      return;
  //    }
  //
  //    Object[] labels = taint.getLabels();
  //
  //    //    for (int i = 0; i < (labels.length - 1); i++) {
  //    //      this.fos.write(String.valueOf(labels[i]).intern().getBytes());
  //    //      this.fos.write(COMMA_BYTES);
  //    //    }
  //
  //    //    this.fos.write(String.valueOf(labels[labels.length - 1]).intern().getBytes());
  //    for (Object label : labels) {
  //      System.out.println(label);
  //    }
  //  }
}
