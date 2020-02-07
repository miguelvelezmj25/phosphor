package edu.cmu.cs.mvelezce.cc.instrumenter;

import edu.cmu.cs.mvelezce.cc.control.sink.SinkManager;
import edu.columbia.cs.psl.phosphor.Configuration;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class CCFieldAdderClassVisitor extends ClassVisitor {

  private final String className;

  public CCFieldAdderClassVisitor(ClassVisitor classVisitor, String className) {
    super(Configuration.ASM_VERSION, classVisitor);

    this.className = className;
  }

  @Override
  public MethodVisitor visitMethod(
      int access, String name, String descriptor, String signature, String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

    if (name.equals(CCClinitMethodVisitor.CLINIT_METHOD_NAME)) {
      mv = new CCClinitMethodVisitor(mv, this.className);
    }

    return mv;
  }

  @Override
  public void visitEnd() {
    for (Map.Entry<ControlStmt, String> entry : SinkManager.CONTROL_STMTS_TO_FIELDS.entrySet()) {
      ControlStmt sink = entry.getKey();

      if (!sink.getClassName().equals(this.className)) {
        continue;
      }

      super.visitField(
          Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
          SinkManager.getFieldName(entry.getValue()),
          SinkManager.SET_CLASS_DESC_FOR_FIELD,
          SinkManager.SET_CLASS_SIGNATURE_FOR_FIELD,
          null);
    }

    super.visitEnd();
  }
}
