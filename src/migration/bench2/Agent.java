package migration.bench2;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

public class Agent implements org.objectweb.proactive.RunActive, Serializable {

  public static int MAXINTVALUE = 100;
  private int id;
  private boolean running = false;
  private int messageSequenceNumber = 0;

  //the initial value is 100 to indicate we didn't find any min yet
  private int min = 100;

  //The liste of destinations and the number of elements inside.
  private Vector destinationList;
  private int maxDestination;
  private String nextDestination;
  private Vector otherList = new Vector();
  private Vector receivedMessages = new Vector();
  //  private Vector sentMessages = new Vector();

  private Random randomGenerator = new Random();


  public Agent() {
  }


  public Agent(Integer i) {
    id = i.intValue();
  }


  public void start() {
    running = true;
  }


  public void setDestinationList(Vector v) {
    destinationList = v;
    maxDestination = v.size();
  }


  public void addAgent(Agent a) {
    otherList.addElement(a);
  }


  public void setOtherList(Vector v) {
    otherList = v;
  }


  private String getNextDestination() {
    String newString;
    do {
      newString = (String)destinationList.elementAt(randomGenerator.nextInt(maxDestination));
    } while (newString.equals(nextDestination));
    return newString;
  }


  public void echo() {
    System.out.println("I am now on host " + nextDestination);
  }


  /**
   * This is the method called by others when a new minimum value is discovered
   */
  public void newMinimum(int m, String sourceID) {
    if (m < min)
      min = m;
    receivedMessages.addElement(new ReceivedMessage(m, sourceID));
  }


  /**
   * Choose randomly a new value to match against the minimum
   */
  private void chooseValue() {
    //first we sleep a bit to loose some time
    try {
      Thread.currentThread().sleep(500 + randomGenerator.nextInt(1000));
    } catch (Exception e) {
      e.printStackTrace();
    }
	
    //then we choose a value
    int value = randomGenerator.nextInt(MAXINTVALUE) + 1;

    System.out.println("Agent " + id + ": chooseValue() value = " + value + " min = " + min);

    //if it is a new minimum we notifiy the others
    if (value < min) {
      min = value;
      notifyOthers();
    }

    this.checkForHalt();
  }


  private void checkForHalt() {
    if (min <= 5) {
      System.out.println("We stop");
      System.out.println("=====Received messages for agent " + id);
      for (int i = 0; i < receivedMessages.size(); i++) {
        System.out.println(receivedMessages.elementAt(i));
      }
      System.out.println("============================");
      this.running = false;
    }
  }


  /** 
   * Notify other agents that we have found a value
   * lower than the min
   */
  public void notifyOthers() {
    int max = otherList.size();
    for (int j = 0; j < max; j++) {
      ((Agent)otherList.elementAt(j)).newMinimum(min, new String("" + id + "-" + messageSequenceNumber));
      //sentMessages.addElement(new SentMessage(min, j));
    }
    messageSequenceNumber++;
  }


  public void runActivity(Body body) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    while (body.isActive()) {
      //		System.out.println("Live started for agent " +id);
      //first we serve all the requests	
      while (service.hasRequestToServe()) {
        service.serveOldest();
      }
      if (running) {
        this.chooseValue();
        nextDestination = getNextDestination();
        try {
          // ProActive.onArrival("echo");
          ProActive.migrateTo(nextDestination);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        service.waitForRequest();
      }
    }
  }
}
