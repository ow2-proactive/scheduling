package test.migration;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.migration.MigrationException;

public class Test implements Serializable, EndActive { //  , RunActive {

  public Test() {
    System.out.println("Test constructor");
  }


  public Test(Integer i) {
    System.out.println("Test constructor with parameter");
  }


  public void echo() {
    System.out.println("Hello, I am here");
  }

  public void migrateTo(String url) throws MigrationException  {
  	System.out.println("Test migrating to " + url);
   // try {
      ProActive.migrateTo(url);
 //   } catch (Exception e) {
 //     e.printStackTrace();
 //   }
  }
  
//  public void migrateTo(String url)   {
//	  System.out.println("Test migrating to " + url);
//	  try {
//		ProActive.migrateTo(url);
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//  // return 0;
//	}

  public void runtimeException() {
    Integer s = null;
    s.toString();
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java test.migration.Test <nodeName>");
      System.exit(1);
    }

    Test test = null;

    try {
      System.out.println("Creating object");
      test = (Test)ProActive.newActive(Test.class.getName(), null);
      System.out.println("Requesting migration");
      test.migrateTo(args[0]);
      Thread.sleep(5000);
      System.out.println("Calling echo");
      test.echo();
      Thread.sleep(5000);
      System.out.println("Calling runtimeException");
      test.runtimeException();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }


public void endActivity(Body body) {
	// to do : Auto-generated method stub
	
}

//
//public void runActivity(Body body) {
//	System.out.println("Activity Started");
//	Service s = new Service(body);
//	s.fifoServing();
//	
//}
}
