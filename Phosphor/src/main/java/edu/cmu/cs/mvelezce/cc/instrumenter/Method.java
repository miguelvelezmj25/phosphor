package edu.cmu.cs.mvelezce.cc.instrumenter;

public class Method {

  private final String className;
  private final String methodName;
  private final String desc;

  public Method(String className, String methodName, String desc) {
    this.className = className;
    this.methodName = methodName;
    this.desc = desc;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Method method = (Method) o;

    if (!className.equals(method.className)) {
      return false;
    }
    if (!methodName.equals(method.methodName)) {
      return false;
    }
    return desc.equals(method.desc);
  }

  @Override
  public int hashCode() {
    int result = className.hashCode();
    result = 31 * result + methodName.hashCode();
    result = 31 * result + desc.hashCode();
    return result;
  }
}
