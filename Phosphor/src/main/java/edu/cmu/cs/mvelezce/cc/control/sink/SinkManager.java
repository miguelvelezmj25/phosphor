package edu.cmu.cs.mvelezce.cc.control.sink;

import edu.columbia.cs.psl.phosphor.control.standard.StandardControlFlowStack;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SinkManager {

  public static final String CC_STATIC_FIELD_PREFIX = "CC_SINK_";
  public static final Map<String, String> CONTROL_STMTS_TO_FIELDS = new HashMap<>();
  private static final String CC_SINKS_CLASS = SinkManager.class.getName().replaceAll("\\.", "\\/");
  private static final String CC_SINKS_METHOD = "sink";
  private static final String PHOSPHOR_SET_CLASS_DESC = "Ljava/util/Set;";
  //  private static final String PHOSPHOR_SET_CLASS_DESC =
  //      "Ledu/columbia/cs/psl/phosphor/struct/harmony/util/Set;";
  private static final String PHOSPHOR_SET_CLASS_NAME = "java/util/Set";
  //  private static final String PHOSPHOR_SET_CLASS_NAME =
  //      "edu/columbia/cs/psl/phosphor/struct/harmony/util/Set";
  private static final String PHOSPHOR_SET_ADD_METHOD_NAME = "add";
  private static final String PHOSPHOR_SET_ADD_METHOD_DESC = "(Ljava/lang/Object;)Z";
  private static final String GET_SINK_DATA_METHOD_NAME = "getSinkData";
  private static final String GET_SINK_DATA_METHOD_DESC =
      "(Ledu/columbia/cs/psl/phosphor/control/standard/StandardControlFlowStack;Ledu/columbia/cs/psl/phosphor/runtime/Taint;)Ledu/cmu/cs/mvelezce/cc/control/sink/SinkData;";
  private static final Map<String, Integer> CONTROL_STMTS_TO_COUNT = new HashMap<>();
  private static String className = null;
  private static String methodName = null;
  private static String desc = null;
  private static int index = -1;

  private final boolean delme;

  private SinkManager() {
    this.delme = true;
  }

  public static void preProcessSinks(String programName) {
    System.out.println(programName);
  }

  public static void postProcessSinks() {
    System.out.println("post");
  }

  public static void setCurrentMethod(String className, String methodName, String desc) {
    SinkManager.className = className;
    SinkManager.methodName = methodName;
    SinkManager.desc = desc;

    String controlStmt = getFullyQualifiedMethodName(className, methodName, desc);
    CONTROL_STMTS_TO_COUNT.put(controlStmt, 0);
    SinkManager.index = CONTROL_STMTS_TO_COUNT.get(controlStmt);
  }

  private static String getFullyQualifiedMethodName(
      String className, String methodName, String desc) {
    return className + "." + methodName + desc;
  }

  /**
   * Instrument
   *
   * @param methodVisitor
   */
  public static void addCCSink(MethodVisitor methodVisitor) {
    if (SinkManager.className == null
        || SinkManager.methodName == null
        || SinkManager.desc == null
        || SinkManager.index < 0) {
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
        Opcodes.GETSTATIC, SinkManager.className, getFieldName(), PHOSPHOR_SET_CLASS_DESC);
    methodVisitor.visitInsn(Opcodes.SWAP);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        PHOSPHOR_SET_CLASS_NAME,
        PHOSPHOR_SET_ADD_METHOD_NAME,
        PHOSPHOR_SET_ADD_METHOD_DESC,
        true);
    methodVisitor.visitInsn(Opcodes.POP);

    SinkManager.className = null;
    SinkManager.methodName = null;
    SinkManager.desc = null;
    SinkManager.index = -1;
  }

  private static String getFieldName() {
    String controlStmt =
        getFullyQualifiedMethodName(
            SinkManager.className, SinkManager.methodName, SinkManager.desc);
    int currentIndex = CONTROL_STMTS_TO_COUNT.get(controlStmt);
    currentIndex++;
    CONTROL_STMTS_TO_COUNT.put(controlStmt, currentIndex);

    String sink = controlStmt + SinkManager.index;
    CONTROL_STMTS_TO_FIELDS.put(sink, UUID.randomUUID().toString().replaceAll("-", "_"));

    return CC_STATIC_FIELD_PREFIX + CONTROL_STMTS_TO_FIELDS.get(sink);
  }

  public static <T> SinkData<T> getSinkData(
      StandardControlFlowStack<T> stack, Taint<T> dataTaints) {
    // Should we cache these taints?
    return new SinkData<T>(stack.copyTag(), dataTaints);
  }

  // /** Instrument */
  // public static void saveControlFlowStmts() {
  //    long start = System.currentTimeMillis();
  //    Set<SinkInfo> sinkInfos = new HashSet<>();
  //
  //    for (int i = 0; i < STMTS.size(); i++) {
  //      SinkInfo sinkInfo = new SinkInfo(i, STMTS.get(i));
  //      sinkInfos.add(sinkInfo);
  //    }
  //
  //    try {
  //      File outputFile =
  //          new File(
  //
  // "/Users/mvelezce/Documents/programming/java/projects/phosphor/Phosphor/examples/control/stmts.json");
  //      ObjectMapper mapper = new ObjectMapper();
  //      mapper.writeValue(outputFile, sinkInfos);
  //    } catch (IOException ioe) {
  //      throw new RuntimeException(ioe);
  //    }
  //
  //    long end = System.currentTimeMillis();
  //    System.out.println("Done saving stmts after " + (end - start) + " ms");
  // }

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
