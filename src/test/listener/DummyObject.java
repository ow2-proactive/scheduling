package test.listener;

import org.objectweb.proactive.Active;

public class DummyObject implements Active {

  public DummyObject() {

  }


  public Object getObject() {
    try {
      Thread.currentThread().sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
