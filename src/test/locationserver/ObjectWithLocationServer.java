package test.locationserver;

import org.objectweb.proactive.ProActive;

public class ObjectWithLocationServer implements java.io.Serializable {

  // private Server s;

  public ObjectWithLocationServer() {

  }


  public void migrateTo(String url) {
    try {
      ProActive.migrateTo(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void echo() {
    System.out.println("------------------ I am here");
  }
}
