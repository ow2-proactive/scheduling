package migration.test;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.request.Request;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;

public class AgentForThread implements org.objectweb.proactive.RunActive, Serializable {

  int etape = 0; // this is to count the jumps we have made so far

  public AgentForThread() {
  }


  public void runActivity(Body body) {
    org.objectweb.proactive.core.body.request.BlockingRequestQueue requestQueue = body.getRequestQueue();
    try {

      if (etape == 0) //Not migrated yet
      {
        //b.migrateTo(new URL("http://oasis/Node1"));
        //  System.out.println("TOTOTOTOTOTOT");
        Request request = requestQueue.blockingRemoveOldest("moveTo");
        System.out.println("AgentForThread: live() The request moveTo has just arrived");
        System.out.println("AgentForThread: live() The requestQueue is ");
        System.out.println(requestQueue.toString());
        etape++;
        body.serve(request);
      } else if (etape == 1) {
        //Now we can serve the others request
        System.out.println("AgentForThread: I am now back after migration");
        System.out.println(requestQueue.toString());
        body.fifoPolicy();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public String synchroneRequest() {
    System.out.println("AgentForThread: Now executing synchroneRequest()");
    return ("TOTO");
  }


  public void moveTo(String t) {
    try {
      ProActive.migrateTo(t);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ;
  }
}
