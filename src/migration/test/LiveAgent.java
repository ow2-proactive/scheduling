package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class LiveAgent implements Serializable {

  int etape = 0;


  public LiveAgent() {

  }


  public void live(org.objectweb.proactive.Body b) {
    System.out.println("LiveAgent: I have my own live method");
    if (etape == 0) {
      etape++;
      try {
        ProActive.migrateTo("oasis/Node1");
      } catch (Exception e) {
      }
    }
    System.out.println("LiveAgent: now I won't migrate anymore");
  }


  public void echo() {
    System.out.println("LiveAgent: echo() I'am here :-)");

  }


  public static void main(String args[]) {
    try {
      LiveAgent l = (LiveAgent)ProActive.newActive("migration.test.LiveAgent", null);
    } catch (Exception e) {
    }

  }
}
