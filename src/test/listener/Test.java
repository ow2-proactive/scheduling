package test.listener;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.event.MessageEvent;
import org.objectweb.proactive.core.event.MessageEventListener;

public class Test implements org.objectweb.proactive.RunActive {

  private DummyObject other;


  public Test() {

  }


  public void setOther(DummyObject o) {
    this.other = o;
  }


  public void toto() {
    System.out.println("toto");

  }


  public void callOther() {
    this.other.getObject();
  }


  public Object getObject() {
    return null;
  }


  public void runActivity(Body body) {
    System.out.println("Test: starting custom live()");
    body.addMessageEventListener(new MyListener());
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    service.fifoServing();
  }


  public static void main(String[] args) {
    Test t = null;
    DummyObject t2 = null;
    try {
      t = (Test)ProActive.newActive("test.listener.Test", null);
      t2 = (DummyObject)ProActive.newActive("test.listener.DummyObject", null);
      t.setOther(t2);
      //t.toto();
      //t.getObject();

      t.callOther();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  

  private class MyListener implements MessageEventListener {
    
    private void print(String name, MessageEvent e) {
      System.out.println(" --------------- "+name+(e.wasSent() ? " SENT" : " RECEIVED"));
      System.out.println("RequestReceived :");
      System.out.println(" From : " + e.getSourceBodyID());
      System.out.println(" MethodName: " + e.getMethodName());
      //System.out.println(" SequenceNumber: " + e.getSequenceNumber());
      System.out.println("-----------------------------");
    }

    public void replyReceived(MessageEvent event) {
      print("Reply", event);
    }
    
    public void replySent(MessageEvent event) {
      print("Reply", event);
    }
    
    public void requestReceived(MessageEvent event) {
      print("Request", event);
    }
    
    public void requestSent(MessageEvent event) {
      print("Request", event);
    }

    public void voidRequestServed(MessageEvent event) {
      print("Served", event);
    }

    public void servingStarted(MessageEvent event) {
      print("Serving", event);
    }

  }
  
}
