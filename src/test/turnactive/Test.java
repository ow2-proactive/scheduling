package test.turnactive;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

public class Test implements org.objectweb.proactive.RunActive, java.io.Serializable {

  private Test myPeer;
  private java.util.Random random;
  private String dogName;
  
  public Test() {
  }

  public Test(String dogName) {
    this.dogName = dogName;
    random = new java.util.Random();
  }

  public Test(String dogName, Test myPeer) {
    this.myPeer = myPeer;
    this.dogName = dogName;
    random = new java.util.Random();
  }

  
  public void receiveDog(Dog dog) {
    System.out.println("I got a new dog of name :"+dog.getName());
  }
  
  public void echo() {
    System.out.println("I'm on the node "+getNodeURL());
    if (myPeer != null)
      System.out.println("My peer is on node "+myPeer.getNodeURL());
    else System.out.println("I don't have a peer yet ");
  }
  
  public String getNodeURL() {
    return ProActive.getBodyOnThis().getNodeURL();
  }
  
  public void runActivity(Body body) {
    int counter = 0;
    Service service = new Service(body);
    while (body.isActive()) {
      if (myPeer != null) {
        myPeer.receiveDog(new Dog(dogName+random.nextInt(100)));
        counter++;
        if (counter > 10) {
          System.out.println("I sent my 10 dogs, now terminating...");
          body.terminate();
        }
      }
      service.blockingServeOldest();
    }
    System.out.println("I'm done");
  }
  
  private void setPeer(Test myPeer) {
    this.myPeer = myPeer;
  }


  public static void main(String args[]) {
    if (args.length < 1) {
      System.out.println("usage: java test.lookupactive.Test <nodename>");
      System.exit(1);
    }
    Test test1 = new Test("Malik");
    Test activeTest1 = null;
    try {
      System.out.println("Test: creating object and turning it active localy");
      //Test t = new Test();
      activeTest1 = (Test)ProActive.turnActive(test1,args[0]);
      activeTest1.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Test: creating object and turning it active on a remote node");
    try {
      Test test2 = new Test("Igor", activeTest1);
      Test activeTest2 = (Test)ProActive.turnActive(test2, args[0]);
      test1.setPeer(activeTest2);
      activeTest2.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }
    /*
    System.out.println("Test: creating new active object on a remote node");
    try {
      Test activeTest3 = (Test)ProActive.newActive(Test.class.getName(),new Object[] {"Juca", activeTest1}, args[0]);
      test1.setPeer(activeTest3);
      activeTest3.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }
    */
  }
}
