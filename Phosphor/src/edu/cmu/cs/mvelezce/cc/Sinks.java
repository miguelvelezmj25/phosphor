package edu.cmu.cs.mvelezce.cc;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.runtime.TaintInstrumented;
import edu.columbia.cs.psl.phosphor.runtime.TaintSentinel;
import edu.columbia.cs.psl.phosphor.struct.*;
import edu.columbia.cs.psl.phosphor.struct.multid.MultiDTaintedArrayWithObjTag;

@TaintInstrumented
public class Sinks implements TaintedWithObjTag {
  static final long serialVersionUID = -4448519827989177134L;
  private static final CCTaintSourceWrapper CC_TAINT_SOURCE_WRAPPER =
      (CCTaintSourceWrapper) Configuration.autoTainter;
  public static Taint serialVersionUIDPHOSPHOR_TAG;
  public Taint PHOSPHOR_TAG;

  public Sinks(ControlTaintTagStack phosphorJumpControlTag, TaintSentinel var2) {}

  public static void preProcessSinks$$PHOSPHORTAGGED(String program) {
    Sinks.preProcessSinks(program);
  }

  public static void preProcessSinks(String program) {
    CC_TAINT_SOURCE_WRAPPER.preProcessSinks(program);
  }

  public static void postProcessSinks() {
    System.err.println(
        "Post processing sinks is a bit slow. It might be better to write to a file and call a regular JVM to read and process");
    long start = System.nanoTime();
    CC_TAINT_SOURCE_WRAPPER.postProcessSinks();
    long end = System.nanoTime();

    System.out.println("Time to serialize sinks: " + (end - start) / 1_000_000_000.0);
  }

  public static void sink(
      ControlTaintTagStack controlTaintTagStack, Object taintedObject, String id) {
    if (taintedObject == null && controlTaintTagStack.isEmpty()) {
      return;
    }

    CC_TAINT_SOURCE_WRAPPER.checkTaint(controlTaintTagStack, taintedObject, id);
  }

  public boolean equals(Object other) {
    return super.equals(other);
  }

  public int hashCode() {
    return super.hashCode();
  }

  public Object getPHOSPHOR_TAG() {
    return this.PHOSPHOR_TAG;
  }

  public void setPHOSPHOR_TAG(Object var1) {
    this.PHOSPHOR_TAG = (Taint) var1;
  }

  public TaintedBooleanWithObjTag equals$$PHOSPHORTAGGED(
      Object phosphorNativeWrapArg1,
      ControlTaintTagStack var2,
      TaintedBooleanWithObjTag phosphorReturnHolder) {
    Object var10001 = phosphorNativeWrapArg1;
    if (phosphorNativeWrapArg1 instanceof LazyArrayObjTags[]
        | phosphorNativeWrapArg1 instanceof LazyArrayObjTags) {
      var10001 = MultiDTaintedArrayWithObjTag.unboxRaw(phosphorNativeWrapArg1);
    }

    phosphorReturnHolder.val = this.equals(var10001);
    phosphorReturnHolder.taint = null;
    return phosphorReturnHolder;
  }

  public TaintedIntWithObjTag hashCode$$PHOSPHORTAGGED(
      ControlTaintTagStack var1, TaintedIntWithObjTag phosphorReturnHolder) {
    phosphorReturnHolder.val = this.hashCode();
    phosphorReturnHolder.taint = null;
    return phosphorReturnHolder;
  }

  public Class getClass$$PHOSPHORTAGGED(ControlTaintTagStack var1) {
    return this.getClass();
  }

  public String toString$$PHOSPHORTAGGED(ControlTaintTagStack var1) {
    return this.toString();
  }
}
