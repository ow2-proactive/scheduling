package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.NotBoundException;

public class TalkativeAgent implements org.objectweb.proactive.RunActive, Serializable {

  int etape = 0; // this is to count the jumps we have made so far

  public TalkativeAgent() {
    System.out.println("TalkativeAgent constructor");
  }


  public TalkativeAgent(Integer i) {
    System.out.println("TalkativeAgent constructor with parameter");
  }


  public void titi() {
    System.out.println("TalkativeAgent: migrate()");
    try {
      ProActive.migrateTo("//oasis/Node1");
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("TalkativeAgent: migrate() Over");
  }


  public void titi2() {
    System.out.println("TalkativeAgent: migrate() deuxieme etape");
    try {
      ProActive.migrateTo("oasis/Node2");
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("TalkativeAgent: migrate() deuxieme etape Over");
  }


  public void moveTo(String s) {
    try {
      ProActive.migrateTo(s);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void runActivity(org.objectweb.proactive.Body b) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(b);
    service.fifoServing();
  }

  // public void live (ExplicitBody b)
  //     {
  // 	System.out.println("Now calling custom live");
  // 	try {
  // 	    if (etape ==0) //Not migrated yet
  // 	       {
  // 		   b.getRequestQueue().blockingRemoveOldest("titi");
  // 		   System.out.println("TalkativeAgent: live() The request titi has just arrived");
  // 		   etape++;
  // 		   b.blockingServeOldest("titi");
  // 	       }		
  // 	    else
  // 		if (etape == 1) //Now we can serve the others request
  // 		    {
  // 			b.fifoPolicy();
			
  // 		    }
  // 	} catch (Exception e) {e.printStackTrace();}	
  //     }

  /**
   * Display on standard output the local hostname
   */
  public void echo() {
    System.out.println("TalkativeAgent:Echo()");
    //	String location;
    //	location = this.getLocation();
    //System.out.println("TalkativeAgent: I'm now on host " + location);
  }


  /**
   * Get some informations about the load of the current host
   * Warning: this only work on Solaris since it calls some unix command
   */
  public void getLoad() {
    try {
      Process p = Runtime.getRuntime().exec("ps");
      p.waitFor();
      String s;
      BufferedReader d = new BufferedReader(new InputStreamReader(p.getInputStream()));
      while ((s = d.readLine()) != null) {
        System.out.println(s);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }


  public int echoInt() {
    return (6);
  }


  /**
   * Send an echoInt() to the Agent t
   */
  public void echoIntOn(TalkativeAgent t) {
    System.out.println("TalkativeAgent: echoIntOn() Should be synchrone");
    echoInt();
  }


  public MyString withFuture() {
    return new MyString();

  }
}