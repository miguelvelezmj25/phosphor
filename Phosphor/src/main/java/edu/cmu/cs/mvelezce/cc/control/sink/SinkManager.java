package edu.cmu.cs.mvelezce.cc.control.sink;

import edu.cmu.cs.mvelezce.cc.instrumenter.SinkInstrumenter;
import edu.columbia.cs.psl.phosphor.PreMain;
import edu.columbia.cs.psl.phosphor.control.standard.StandardControlFlowStack;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

import java.lang.reflect.Field;

public final class SinkManager {

  private SinkManager() {
    System.out.println();
  }

  public static void preProcessSinks(String programName) {
    System.out.println(programName);
  }

  public static <T> void postProcessSinks() {
    try {
      long start = System.nanoTime();
      Class[] classes = PreMain.getInstrumentation().getAllLoadedClasses();

      for (Class clazz : classes) {
        // TODO might be able to not look at some classes here
        Field[] fields = clazz.getFields();

        for (Field field : fields) {
          if (!field.getName().startsWith(SinkInstrumenter.CC_STATIC_FIELD_PREFIX)) {
            continue;
          }

          edu.columbia.cs.psl.phosphor.struct.harmony.util.Set<SinkData<T>> data =
              (edu.columbia.cs.psl.phosphor.struct.harmony.util.Set<SinkData<T>>) field.get(null);
        }
      }

      System.out.println("Sink processing took " + (System.nanoTime() - start) / 1E9 + " s");
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
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
