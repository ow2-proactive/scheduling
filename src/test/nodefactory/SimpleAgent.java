package test.nodefactory;

import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class SimpleAgent implements Serializable {

  public SimpleAgent() {
  }


  public void echo() {
    System.out.println("I'm here");
  }


  public void migrateTo(String s) {
    try {
      ProActive.migrateTo(s);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
