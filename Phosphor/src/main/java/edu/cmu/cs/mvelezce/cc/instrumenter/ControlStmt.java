package edu.cmu.cs.mvelezce.cc.instrumenter;

public class ControlStmt {

  private final String className;
  private final String methodName;
  private final String desc;
  private final int index;

  public ControlStmt(String className, String methodName, String desc, int index) {
    this.className = className;
    this.methodName = methodName;
    this.desc = desc;
    this.index = index;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getDesc() {
    return desc;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ControlStmt that = (ControlStmt) o;

    if (index != that.index) {
      return false;
    }
    if (!className.equals(that.className)) {
      return false;
    }
    if (!methodName.equals(that.methodName)) {
      return false;
    }
    return desc.equals(that.desc);
  }

  @Override
  public int hashCode() {
    int result = className.hashCode();
    result = 31 * result + methodName.hashCode();
    result = 31 * result + desc.hashCode();
    result = 31 * result + index;
    return result;
  }
}
