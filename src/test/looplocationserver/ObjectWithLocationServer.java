package test.looplocationserver;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

public class ObjectWithLocationServer implements java.io.Serializable {

  private String[] destinations;
  int index;

  public ObjectWithLocationServer() {}

  public ObjectWithLocationServer(String[] s) {
    this.destinations = s;
    this.index = 0;
  }

  public void migrateTo(String url) {
    try {
      ProActive.migrateTo(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void live(Body body) {
    try {
      while (body.isActive()) {
        if (index < destinations.length) {
          index++;
          ProActive.migrateTo(destinations[index - 1]);
        } else {
          System.out.println("---- Done");
          try {
            Thread.sleep(500);
          } catch (Exception e) {
            e.printStackTrace();
          }
          index = 0;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void echo() {
    System.out.println("------------------ I am here");
  }
}