package edu.cmu.cs.mvelezce.cc.instrumenter;

import edu.cmu.cs.mvelezce.cc.control.sink.SinkManager;
import edu.columbia.cs.psl.phosphor.Configuration;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class CCFieldAdderClassVisitor extends ClassVisitor {

  private final String className;

  public CCFieldAdderClassVisitor(ClassVisitor classVisitor, String className) {
    super(Configuration.ASM_VERSION, classVisitor);

    this.className = className;
  }

  @Override
  public void visitEnd() {
    for (Map.Entry<String, String> entry : SinkManager.CONTROL_STMTS_TO_FIELDS.entrySet()) {
      String sink = entry.getKey();

      if (!sink.startsWith(this.className)) {
        continue;
      }

      super.visitField(
          Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
          SinkManager.CC_STATIC_FIELD_PREFIX + entry.getValue(),
          "Ljava/util/Set;",
          "Ljava/util/Set<Ljava/lang/String;>;",
          null);
    }

    super.visitEnd();
  }
}
