package edu.cmu.cs.mvelezce.taint.debug.instrument;

import edu.cmu.cs.mvelezce.taint.debug.data.TaintDebugManager;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TaintDebugInstrumenter {

  private static final String TAINT_DEBUG_MANAGER_CLASS =
      TaintDebugManager.class.getName().replaceAll("\\.", "\\/");
  private static final String TAINT_DEBUG_MANAGER_METHOD = "lineNumber";
  private static final String TAINT_DEBUG_MANAGER_DES =
      "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";

  public static void instrumentCombineTags(
      MethodVisitor mv, int currentLineNumber, String className, String methodName, String desc) {
    className = className.replace("/", ".");
    methodName = methodName.replace("$$PHOSPHORTAGGED", "");

    mv.visitIntInsn(Opcodes.BIPUSH, currentLineNumber);
    mv.visitLdcInsn(className);
    mv.visitLdcInsn(methodName);
    mv.visitLdcInsn(desc);
    mv.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        TAINT_DEBUG_MANAGER_CLASS,
        TAINT_DEBUG_MANAGER_METHOD,
        TAINT_DEBUG_MANAGER_DES,
        false);
  }
}
