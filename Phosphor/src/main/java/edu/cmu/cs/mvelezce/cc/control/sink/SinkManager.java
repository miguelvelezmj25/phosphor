package edu.cmu.cs.mvelezce.cc.control.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmu.cs.mvelezce.cc.instrumenter.ControlStmt;
import edu.cmu.cs.mvelezce.cc.instrumenter.Method;
import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.PreMain;
import edu.columbia.cs.psl.phosphor.control.standard.StandardControlFlowStack;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class SinkManager {

  public static final boolean USE_PHOSPHOR_UTILS = true;
  public static final String CC_STATIC_FIELD_PREFIX = "CC_SINK_";
  public static final Map<ControlStmt, String> CONTROL_STMTS_TO_FIELDS = new HashMap<>();
  public static final String SET_CLASS_SIGNATURE_FOR_FIELD = getSetClassSignatureForField();
  public static final String SET_CLASS_DESC_FOR_FIELD = getSetClassDescForField();
  public static final String SET_CLASS_NAME = getSetClassName();
  public static final String HASHSET_CLASS_NAME = getHashSetClassName();

  private static final String CC_SINKS_CLASS = SinkManager.class.getName().replaceAll("\\.", "\\/");
  private static final String PHOSPHOR_SET_ADD_METHOD_NAME = "add";
  private static final String PHOSPHOR_SET_ADD_METHOD_DESC = "(Ljava/lang/Object;)Z";
  private static final String GET_SINK_DATA_METHOD_NAME = "getSinkData";
  private static final String GET_SINK_DATA_METHOD_DESC =
      "(Ledu/columbia/cs/psl/phosphor/control/standard/StandardControlFlowStack;Ledu/columbia/cs/psl/phosphor/runtime/Taint;)Ledu/cmu/cs/mvelezce/cc/control/sink/SinkData;";
  private static final Map<Method, Integer> METHODS_TO_CONTROL_COUNTS = new HashMap<>();

  private static Method currentMethod;
  private static int index = -1;

  private SinkManager() {
    System.out.println();
  }

  private static String getHashSetClassName() {
    if (USE_PHOSPHOR_UTILS) {
      return "edu/columbia/cs/psl/phosphor/struct/harmony/util/HashSet";
    }

    return "java/util/HashSet";
  }

  private static String getSetClassName() {
    if (USE_PHOSPHOR_UTILS) {
      return "edu/columbia/cs/psl/phosphor/struct/harmony/util/Set";
    }

    return "java/util/Set";
  }

  private static String getSetClassDescForField() {
    if (USE_PHOSPHOR_UTILS) {
      return "Ledu/columbia/cs/psl/phosphor/struct/harmony/util/Set;";
    }

    return "Ljava/util/Set;";
  }

  private static String getSetClassSignatureForField() {
    if (USE_PHOSPHOR_UTILS) {
      return "Ledu/columbia/cs/psl/phosphor/struct/harmony/util/Set<Ledu/cmu/cs/mvelezce/cc/control/sink/SinkData<Ljava/lang/Integer;>;>;";
    }

    return "Ljava/util/Set<Ledu/cmu/cs/mvelezce/cc/control/sink/SinkData<Ljava/lang/Integer;>;>;";
  }

  public static void preProcessSinks(String programName) {
    System.out.println(programName);
  }

  /** Execution */
  public static <T> void postProcessSinks() {
    try {
      long start = System.nanoTime();
      Class[] classes = PreMain.getInstrumentation().getAllLoadedClasses();

      for (Class clazz : classes) {
        // TODO might be able to not look at some classes here
        Field[] fields = clazz.getFields();

        for (Field field : fields) {
          if (!field.getName().startsWith(CC_STATIC_FIELD_PREFIX)) {
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

  public static void setCurrentMethod(String className, String methodName, String desc) {
    SinkManager.currentMethod = new Method(className, methodName, desc);
    METHODS_TO_CONTROL_COUNTS.putIfAbsent(SinkManager.currentMethod, 0);
    SinkManager.index = METHODS_TO_CONTROL_COUNTS.get(SinkManager.currentMethod);
  }

  /**
   * Instrument
   *
   * @param methodVisitor
   */
  public static void addCCSink(MethodVisitor methodVisitor) {
    if (SinkManager.currentMethod == null || SinkManager.index < 0) {
      throw new RuntimeException("We do not know the method that we are instrumenting");
    }

    methodVisitor.visitInsn(Opcodes.DUP2);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        CC_SINKS_CLASS,
        GET_SINK_DATA_METHOD_NAME,
        GET_SINK_DATA_METHOD_DESC,
        false);
    methodVisitor.visitFieldInsn(
        Opcodes.GETSTATIC,
        SinkManager.currentMethod.getClassName(),
        getFieldName(),
        SET_CLASS_DESC_FOR_FIELD);
    methodVisitor.visitInsn(Opcodes.SWAP);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        SET_CLASS_NAME,
        PHOSPHOR_SET_ADD_METHOD_NAME,
        PHOSPHOR_SET_ADD_METHOD_DESC,
        true);
    methodVisitor.visitInsn(Opcodes.POP);

    SinkManager.currentMethod = null;
    SinkManager.index = -1;
  }

  private static String getFieldName() {
    int currentIndex = METHODS_TO_CONTROL_COUNTS.get(SinkManager.currentMethod);
    currentIndex++;
    METHODS_TO_CONTROL_COUNTS.put(SinkManager.currentMethod, currentIndex);

    ControlStmt controlStmt =
        new ControlStmt(
            SinkManager.currentMethod.getClassName(),
            SinkManager.currentMethod.getMethodName(),
            SinkManager.currentMethod.getDesc(),
            SinkManager.index);
    CONTROL_STMTS_TO_FIELDS.put(controlStmt, UUID.randomUUID().toString().replaceAll("-", "_"));

    return getFieldName(CONTROL_STMTS_TO_FIELDS.get(controlStmt));
  }

  public static String getFieldName(String id) {
    return CC_STATIC_FIELD_PREFIX + id;
  }

  public static <T> SinkData<T> getSinkData(
      StandardControlFlowStack<T> stack, Taint<T> dataTaints) {
    // TODO Should we cache these taints?
    return new SinkData<T>(stack.copyTag(), dataTaints);
  }

  public static void saveControlStmts() {
    long start = System.currentTimeMillis();
    Set<ControlStmtField> controlStmtFields = new HashSet<>();

    for (Map.Entry<ControlStmt, String> entry : CONTROL_STMTS_TO_FIELDS.entrySet()) {
      ControlStmtField controlStmtField = new ControlStmtField(entry.getKey(), entry.getValue());
      controlStmtFields.add(controlStmtField);
    }

    try {
      File outputFile =
          new File(
              "/Users/mvelezce/Documents/programming/java/projects/phosphor/Phosphor/scripts/instrument/control/"
                  + Configuration.PROGRAM_NAME
                  + ".json");
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(outputFile, controlStmtFields);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    long end = System.currentTimeMillis();
    System.out.println("Done saving stmts after " + (end - start) + " ms");
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
