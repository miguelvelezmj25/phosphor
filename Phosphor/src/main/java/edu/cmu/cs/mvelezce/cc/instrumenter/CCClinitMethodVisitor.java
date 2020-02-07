package edu.cmu.cs.mvelezce.cc.instrumenter;

import edu.cmu.cs.mvelezce.cc.control.sink.SinkManager;
import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class CCClinitMethodVisitor extends MethodVisitor {

  public static final String CLINIT_METHOD_NAME = "<clinit>";

  private final String className;

  CCClinitMethodVisitor(MethodVisitor methodVisitor, String className) {
    super(Configuration.ASM_VERSION, methodVisitor);

    this.className = className;
  }

  @Override
  public void visitInsn(int opcode) {
    if (TaintUtils.isReturnOpcode(opcode)) {
      for (Map.Entry<ControlStmt, String> entry : SinkManager.CONTROL_STMTS_TO_FIELDS.entrySet()) {
        ControlStmt sink = entry.getKey();

        if (!sink.getClassName().equals(this.className)) {
          continue;
        }

        super.visitTypeInsn(Opcodes.NEW, SinkManager.HASHSET_CLASS_NAME);
        super.visitInsn(Opcodes.DUP);
        super.visitMethodInsn(
            Opcodes.INVOKESPECIAL, SinkManager.HASHSET_CLASS_NAME, "<init>", "()V", false);
        super.visitFieldInsn(
            Opcodes.PUTSTATIC,
            this.className,
            SinkManager.getFieldName(entry.getValue()),
            SinkManager.SET_CLASS_DESC_FOR_FIELD);
      }
    }
    super.visitInsn(opcode);
  }
}
