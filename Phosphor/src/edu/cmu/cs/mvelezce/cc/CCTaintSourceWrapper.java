package edu.cmu.cs.mvelezce.cc;

import edu.columbia.cs.psl.phosphor.runtime.AutoTaintLabel;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.runtime.TaintSourceWrapper;
import edu.columbia.cs.psl.phosphor.struct.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CCTaintSourceWrapper<T extends AutoTaintLabel> extends TaintSourceWrapper<T> {
  private static byte[] NEW_LINE_BYTES;
  private static byte[] COMMA_BYTES;
  private static byte[] EMPTY;

  private FileOutputStream fos;

  void preProcessSinks(String program) {
    try {
      System.out.println("If there is no folder for the serialized file, there will be an error");
      // For some reason, phosphor does not like to define this static fields as final; there is a
      // crash at when the program is executed
      NEW_LINE_BYTES = "\n".getBytes();
      COMMA_BYTES = ",".getBytes();
      EMPTY = new byte[0];

      File outputFile = new File("../../../examples/implicit-optimized/" + program + "/data.ser");
      this.fos = new FileOutputStream(outputFile);
    } catch (IOException ioe) {
      throw new RuntimeException("Could not initialize the file output stream", ioe);
    }
  }

  void postProcessSinks() {
    this.processSinkData();
  }

  private void processSinkData() {
    System.out.println("Process sink data");
    this.serializeData();
  }

  private void serializeData() {
    try {
      fos.flush();
      fos.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  synchronized void checkTaint(
      ControlTaintTagStack controlTaintTagStack, Object taintedObject, String id) {
    try {
      this.fos.write(id.getBytes());
      this.fos.write(NEW_LINE_BYTES);
      this.writeTaints(controlTaintTagStack.getTaintHistory().peek());
      this.fos.write(NEW_LINE_BYTES);
      this.writeTaints(taintedObject);
      this.fos.write(NEW_LINE_BYTES);
    } catch (IOException e) {
      // TODO throw error that the taint cannot be written.
    }
  }

  private void writeTaints(Object taintedObject) throws IOException {
    if (taintedObject == null) {
      this.fos.write(EMPTY);
    } else if (taintedObject instanceof Taint) {
      this.writeTaintLabels((Taint) taintedObject);
    } else if (taintedObject instanceof TaintedWithObjTag) {
      Taint taint = (Taint) ((TaintedWithObjTag) taintedObject).getPHOSPHOR_TAG();
      this.writeTaintLabels(taint);
    } else {
      throw new RuntimeException("What is this thing now? " + taintedObject.getClass());
      //      this.fos.write(EMPTY);
    }
  }

  private void writeTaintLabels(Taint taint) throws IOException {
    if (taint == null) {
      this.fos.write(EMPTY);
      return;
    }

    Object[] labels = taint.getLabels();

    for (int i = 0; i < (labels.length - 1); i++) {
      this.fos.write(String.valueOf(labels[i]).intern().getBytes());
      this.fos.write(COMMA_BYTES);
    }

    this.fos.write(String.valueOf(labels[labels.length - 1]).intern().getBytes());
  }

  public Taint<AutoTaintLabel> generateTaint(String source) {
    throw new UnsupportedOperationException("I believe this method should not be called");
    //    TaintLabel taintLabel = new TaintLabel(source, null);
    //    return new Taint<>(taintLabel);
  }

  public void checkTaint(Object obj, String sink) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public void checkTaint(Taint<T> tag, String sink) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public TaintedIntWithObjTag autoTaint(TaintedIntWithObjTag ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public TaintedShortWithObjTag autoTaint(TaintedShortWithObjTag ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public TaintedLongWithObjTag autoTaint(TaintedLongWithObjTag ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public TaintedFloatWithObjTag autoTaint(TaintedFloatWithObjTag ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public TaintedDoubleWithObjTag autoTaint(TaintedDoubleWithObjTag ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public TaintedByteWithObjTag autoTaint(TaintedByteWithObjTag ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public TaintedCharWithObjTag autoTaint(TaintedCharWithObjTag ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public LazyByteArrayObjTags autoTaint(LazyByteArrayObjTags ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public LazyBooleanArrayObjTags autoTaint(LazyBooleanArrayObjTags ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public LazyCharArrayObjTags autoTaint(LazyCharArrayObjTags ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public LazyDoubleArrayObjTags autoTaint(LazyDoubleArrayObjTags ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public LazyFloatArrayObjTags autoTaint(LazyFloatArrayObjTags ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public LazyIntArrayObjTags autoTaint(LazyIntArrayObjTags ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public LazyShortArrayObjTags autoTaint(LazyShortArrayObjTags ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public LazyLongArrayObjTags autoTaint(LazyLongArrayObjTags ret, String source, int argIdx) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public void checkTaint(int tag, String sink) {
    throw new UnsupportedOperationException("Method should not be called");
  }

  public boolean hasTaints(int[] tags) {
    throw new UnsupportedOperationException("Method should not be called");
  }
}
