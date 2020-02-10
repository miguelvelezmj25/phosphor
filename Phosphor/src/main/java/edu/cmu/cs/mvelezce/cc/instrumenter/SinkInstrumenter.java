package edu.cmu.cs.mvelezce.cc.instrumenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cmu.cs.mvelezce.cc.control.sink.ControlStmtField;
import edu.cmu.cs.mvelezce.cc.control.sink.SinkManager;
import edu.columbia.cs.psl.phosphor.Configuration;
import org.apache.commons.lang3.RandomStringUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SinkInstrumenter {

  public static final boolean USE_PHOSPHOR_UTILS = true;
  public static final String STATIC_FIELD_PREFIX_CC = "CC_";
  public static final Map<ControlStmt, String> CONTROL_STMTS_TO_FIELDS = new HashMap<>();
  public static final String SET_CLASS_SIGNATURE_FOR_FIELD = getSetClassSignatureForField();
  public static final String SET_CLASS_DESC_FOR_FIELD = getSetClassDescForField();
  public static final String SET_CLASS_NAME = getSetClassName();
  public static final String HASHSET_CLASS_NAME = getHashSetClassName();

  private static final String SINKS_CLASS_CC = SinkManager.class.getName().replaceAll("\\.", "\\/");
  private static final String SET_ADD_METHOD_NAME = "add";
  private static final String SET_ADD_METHOD_DESC = "(Ljava/lang/Object;)Z";
  private static final String GET_SINK_DATA_METHOD_NAME = "getSinkData";
  private static final String GET_SINK_DATA_METHOD_DESC =
      "(Ledu/columbia/cs/psl/phosphor/control/standard/StandardControlFlowStack;Ledu/columbia/cs/psl/phosphor/runtime/Taint;)Ledu/cmu/cs/mvelezce/cc/control/sink/SinkData;";
  private static final Map<Method, Integer> METHODS_TO_CONTROL_COUNTS = new HashMap<>();

  private static Method currentMethod;
  private static int index = -1;

  private SinkInstrumenter() {
    System.out.print("");
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

  public static void setCurrentMethod(String className, String methodName, String desc) {
    SinkInstrumenter.currentMethod = new Method(className, methodName, desc);
    METHODS_TO_CONTROL_COUNTS.putIfAbsent(SinkInstrumenter.currentMethod, 1);
    SinkInstrumenter.index = METHODS_TO_CONTROL_COUNTS.get(SinkInstrumenter.currentMethod);
  }

  /**
   * Instrument
   *
   * @param methodVisitor
   */
  public static void addCCSink(MethodVisitor methodVisitor) {
    if (SinkInstrumenter.currentMethod == null || SinkInstrumenter.index < 0) {
      throw new RuntimeException("We do not know the method that we are instrumenting");
    }

    methodVisitor.visitInsn(Opcodes.DUP2);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        SINKS_CLASS_CC,
        GET_SINK_DATA_METHOD_NAME,
        GET_SINK_DATA_METHOD_DESC,
        false);
    methodVisitor.visitFieldInsn(
        Opcodes.GETSTATIC,
        SinkInstrumenter.currentMethod.getClassName(),
        getFieldName(),
        SET_CLASS_DESC_FOR_FIELD);
    methodVisitor.visitInsn(Opcodes.SWAP);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKEINTERFACE, SET_CLASS_NAME, SET_ADD_METHOD_NAME, SET_ADD_METHOD_DESC, true);
    methodVisitor.visitInsn(Opcodes.POP);

    SinkInstrumenter.currentMethod = null;
    SinkInstrumenter.index = -1;
  }

  private static String getFieldName() {
    int currentIndex = METHODS_TO_CONTROL_COUNTS.get(SinkInstrumenter.currentMethod);
    currentIndex++;
    METHODS_TO_CONTROL_COUNTS.put(SinkInstrumenter.currentMethod, currentIndex);

    ControlStmt controlStmt =
        new ControlStmt(
            SinkInstrumenter.currentMethod.getClassName(),
            SinkInstrumenter.currentMethod.getMethodName(),
            SinkInstrumenter.currentMethod.getDesc(),
            SinkInstrumenter.index);
    CONTROL_STMTS_TO_FIELDS.put(controlStmt, RandomStringUtils.random(16, true, true));

    return getFieldName(CONTROL_STMTS_TO_FIELDS.get(controlStmt));
  }

  public static String getFieldName(String id) {
    return STATIC_FIELD_PREFIX_CC + id;
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
}
