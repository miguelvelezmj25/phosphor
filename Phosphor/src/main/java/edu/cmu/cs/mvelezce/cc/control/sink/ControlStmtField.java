package edu.cmu.cs.mvelezce.cc.control.sink;

import edu.cmu.cs.mvelezce.cc.instrumenter.ControlStmt;

public class ControlStmtField {

  private final ControlStmt controlStmt;
  private final String field;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ControlStmtField that = (ControlStmtField) o;

    if (!controlStmt.equals(that.controlStmt)) {
      return false;
    }
    return field.equals(that.field);
  }

  @Override
  public int hashCode() {
    int result = controlStmt.hashCode();
    result = 31 * result + field.hashCode();
    return result;
  }
}
