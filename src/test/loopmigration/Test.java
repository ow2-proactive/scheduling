package test.loopmigration;

import java.io.Serializable;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;


public class Test implements org.objectweb.proactive.RunActive, Serializable {

  private String[] destinations;
  int index;


  public Test() {
    System.out.println("Test constructor");
  }


  public Test(String[] nodes) {
    System.out.println("Test constructor with " + nodes.length + " destinations");
    index = 0;
    destinations = nodes;
  }


  public void echo() {
      System.out.println("Hello, I am here");
  }


  public void runActivity(Body body) {
    try {
	System.out.println("Test");
// 	while (body.getRequestQueue().size() != 0) {
// 	    body.getRequestQueue().removeOldest().serve(body);
// 	} // end of while (body.getRequestQueue().size() != 0)
	body.getRequestQueue().clear();
	
	while (body.isActive()) {
        if (index < destinations.length) {
          index++;
          ProActive.migrateTo(destinations[index - 1]);
        } else {
	    //  System.out.println("---- Done");
          try {
            Thread.sleep(500);
          } catch (Exception e) {
            e.printStackTrace();
          }
          index = 0;
          //body.fifoPolicy();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java " + Test.class.getName() + "<nodeName> ... <nodeName>");
      System.exit(1);
    }

    Test test = null;
    Object[] arg = new Object[1];
    arg[0] = args;
    try {
      System.out.println("Creating object");
      test = (Test)ProActive.newActive("test.loopmigration.Test", arg);
      while (true) {     
	  Thread.sleep(1000);
	  System.out.println("Calling the object");
	  test.echo();
      } // end of while (true)
      
      //
      //
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
