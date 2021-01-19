package edu.cmu.cs.mvelezce.taint.debug.data;

public class TaintDebugManager {

  public static void lineNumber(int lineNumber, String className, String methodName, String desc) {
    System.out.println(className + "." + methodName + desc + " @ " + lineNumber);
  }
}
