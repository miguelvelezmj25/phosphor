package edu.cmu.cs.mvelezce.cc.control.sink;

import edu.columbia.cs.psl.phosphor.runtime.Taint;

import java.util.Objects;

public class SinkData<T> {

  private final Taint<T> control;
  private final Taint<T> data;

  public SinkData(Taint<T> control, Taint<T> data) {
    this.control = control;
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SinkData<?> sinkData = (SinkData<?>) o;

    if (!Objects.equals(control, sinkData.control)) {
      return false;
    }
    return Objects.equals(data, sinkData.data);
  }

  @Override
  public int hashCode() {
    int result = control != null ? control.hashCode() : 0;
    result = 31 * result + (data != null ? data.hashCode() : 0);
    return result;
  }
}
