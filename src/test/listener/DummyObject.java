package test.listener;

public class DummyObject {

  public Object getObject() {
    try {
      Thread.currentThread().sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
