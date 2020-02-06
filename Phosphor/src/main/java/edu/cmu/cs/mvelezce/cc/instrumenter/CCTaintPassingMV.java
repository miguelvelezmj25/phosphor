package edu.cmu.cs.mvelezce.cc.instrumenter;

import edu.cmu.cs.mvelezce.cc.control.sink.SinkManager;
import edu.columbia.cs.psl.phosphor.control.ControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintPassingMV;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class CCTaintPassingMV extends TaintPassingMV {

  private final String className;
  private final String name;
  private final String desc;

  public CCTaintPassingMV(
      MethodVisitor mv,
      int access,
      String owner,
      String name,
      String descriptor,
      String signature,
      String[] exceptions,
      String originalDesc,
      NeverNullArgAnalyzerAdapter analyzer,
      MethodVisitor passThroughMV,
      LinkedList<MethodNode> wrapperMethodsToAdd,
      ControlFlowPropagationPolicy controlFlowPolicy,
      String originalName) {
    super(
        mv,
        access,
        owner,
        name,
        descriptor,
        signature,
        exceptions,
        originalDesc,
        analyzer,
        passThroughMV,
        wrapperMethodsToAdd,
        controlFlowPolicy);
    this.className = owner;
    this.name = originalName;
    this.desc = originalDesc;
  }

  @Override
  public void visitJumpInsn(int opcode, Label label) {
    switch (opcode) {
      case Opcodes.IFEQ:
      case Opcodes.IFNE:
      case Opcodes.IFLT:
      case Opcodes.IFGE:
      case Opcodes.IFGT:
      case Opcodes.IFLE:
      case Opcodes.IFNULL:
      case Opcodes.IFNONNULL:
      case Opcodes.IF_ICMPEQ:
      case Opcodes.IF_ICMPNE:
      case Opcodes.IF_ICMPLT:
      case Opcodes.IF_ICMPGE:
      case Opcodes.IF_ICMPGT:
      case Opcodes.IF_ICMPLE:
      case Opcodes.IF_ACMPNE:
      case Opcodes.IF_ACMPEQ:
        SinkManager.setCurrentMethod(this.className, this.name, this.desc);
        break;
    }

    super.visitJumpInsn(opcode, label);
  }

  @Override
  public void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label[] labels) {
    SinkManager.setCurrentMethod(this.className, this.name, this.desc);
    super.visitTableSwitchInsn(min, max, defaultLabel, labels);
  }

  @Override
  public void visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label[] labels) {
    SinkManager.setCurrentMethod(this.className, this.name, this.desc);
    super.visitLookupSwitchInsn(defaultLabel, keys, labels);
  }
}
