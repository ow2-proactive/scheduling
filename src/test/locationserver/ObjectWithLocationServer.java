package test.locationserver;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ext.locationserver.ActiveWithLocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.BodyWithLocationServer;

import java.io.Serializable;

public class ObjectWithLocationServer implements ActiveWithLocationServer, Serializable {

  // private Server s;

  public ObjectWithLocationServer() {

  }


//   public void initialize(LocationServer s) {
//     System.out.println("ObjectWithLocationServer: Initialize called with server " + s);
//     try {
//       ((BodyWithLocationServer)ProActive.getBodyOnThis()).setServer(s);
//     } catch (Exception e) {
//       e.printStackTrace();
//     }
//   }


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
