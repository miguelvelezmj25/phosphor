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

  public static final byte[] NEW_LINE_BYTES = "\n".getBytes();
  public static final byte[] LABEL_SEP_BYTES = ":".getBytes();
  public static final int LABELS_END = Integer.MIN_VALUE;

  private static final Map<Class, Integer> CLASSES_TO_INTS = new HashMap<>();
  private static final Map<Field, Integer> FIELDS_TO_INTS = new HashMap<>();
  private static final Map<Taint, Integer> TAINTS_TO_INTS = new HashMap<>();
  private static int classCount = 0;
  private static int fieldCount = 0;
  private static int taintCount = 0;

  private static String programName;

  private SinkManager() {
    System.out.println();
  }

  public static void preProcessSinks(String programName) {
    SinkManager.programName = programName;
  }

  public static void postProcessSinks() {
    long START = System.nanoTime();
    long start = System.nanoTime();
    saveData();
    long end = System.nanoTime();
    System.out.println("save data " + (end - start) / 1E9 + " s");
    start = System.nanoTime();
    saveClasses();
    end = System.nanoTime();
    System.out.println("save classes " + (end - start) / 1E9 + " s");
    start = System.nanoTime();
    saveField();
    end = System.nanoTime();
    System.out.println("save fields " + (end - start) / 1E9 + " s");
    start = System.nanoTime();
    saveTaints();
    end = System.nanoTime();
    System.out.println("save taints " + (end - start) / 1E9 + " s");
    long END = System.nanoTime();
    System.out.println("Sink processing took " + (END - START) / 1E9 + " s");
  }

  private static void saveClasses() {
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

  private static void saveField() {
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

  private static void saveTaints() {
    File outputFile =
        new File("../../../examples/control/" + SinkManager.programName + "/taints.ser");
    try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
      for (Map.Entry<Taint, Integer> entry : TAINTS_TO_INTS.entrySet()) {
        Taint taint = entry.getKey();

        if (taint != null) {
          for (Object label : taint.getLabels()) {
            dos.writeInt((Integer) label);
            dos.write(LABEL_SEP_BYTES);
          }
        }

        dos.writeInt(LABELS_END);
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

          edu.columbia.cs.psl.phosphor.struct.harmony.util.Set<SinkData<T>> sinkData =
              (edu.columbia.cs.psl.phosphor.struct.harmony.util.Set<SinkData<T>>) field.get(null);

          for (SinkData<T> entry : sinkData) {
            hasData = true;
            dos.writeInt(SinkManager.classCount);
            dos.write(NEW_LINE_BYTES);
            dos.writeInt(SinkManager.fieldCount);
            dos.write(NEW_LINE_BYTES);

            Taint<T> control = entry.getControl();
            Integer taintIndex = TAINTS_TO_INTS.get(control);

            if (taintIndex == null) {
              TAINTS_TO_INTS.put(control, SinkManager.taintCount);
              taintIndex = SinkManager.taintCount;
              SinkManager.taintCount++;
            }

            dos.writeInt(taintIndex);
            dos.write(NEW_LINE_BYTES);

            Taint<T> data = entry.getData();
            taintIndex = TAINTS_TO_INTS.get(data);

            if (taintIndex == null) {
              TAINTS_TO_INTS.put(data, SinkManager.taintCount);
              taintIndex = SinkManager.taintCount;
              SinkManager.taintCount++;
            }

            dos.writeInt(taintIndex);
            dos.write(NEW_LINE_BYTES);
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
}
