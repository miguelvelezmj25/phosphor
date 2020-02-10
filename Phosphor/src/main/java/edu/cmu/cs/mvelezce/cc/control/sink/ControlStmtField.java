package edu.cmu.cs.mvelezce.cc.control.sink;

import edu.cmu.cs.mvelezce.cc.instrumenter.ControlStmt;

public class ControlStmtField {

  private final ControlStmt controlStmt;
  private final String field;

  private ControlStmtField() {
    this.controlStmt = null;
    this.field = "";
  }

  public ControlStmtField(ControlStmt controlStmt, String field) {
    this.controlStmt = controlStmt;
    this.field = field;
  }

  public ControlStmt getControlStmt() {
    return controlStmt;
  }

  public String getField() {
    return field;
  }
}
