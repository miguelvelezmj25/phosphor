package edu.cmu.cs.mvelezce.taint.debug.data;

public class TaintDebugManager {

  public static void lineNumber(int lineNumber, String className) {
    System.out.println(className + " @ " + lineNumber);
  }
}
