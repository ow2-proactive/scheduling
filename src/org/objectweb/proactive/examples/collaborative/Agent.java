/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.examples.collaborative;

public class Agent implements java.io.Serializable {

  public static int MAXINTVALUE = 100;
  private int id; // Id of the agent
  private String home; // Url of the Home Node
  
  private boolean finished = false;
  private int peersEnded = 0;
  private boolean running = false;
  private int messageSequenceNumber = 0;

  //the initial value is 100 to indicate we didn't find any min yet
  private int min = 100;

  //The liste of destinations and the number of elements inside.
  private java.util.Vector destinationList;
  private int maxDestination;
  private String nextDestination;
  private java.util.Vector otherList = new java.util.Vector();
  private java.util.Vector receivedMessages = new java.util.Vector();
  private java.util.Random randomGenerator = new java.util.Random();


  public Agent() {
  }


  public Agent(Integer i, String home) {
    id = i.intValue();
    this.home = home;
  }


  public void start() {
    running = true;
  }


  public void setDestinationList(java.util.Vector v) {
    destinationList = v;
    maxDestination = v.size();
  }


  public void addAgent(Agent a) {
    otherList.addElement(a);
  }


  public void setOtherList(java.util.Vector v) {
    otherList = v;
  }


  /**
   * Choose the next destination for the agent
   */
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
    this.checkForHalt();
  }


  /**
   * Choose randomly a new value to match against the minimum
   */
  private void chooseValue() {

    //then we choose a value
    int value = randomGenerator.nextInt(MAXINTVALUE) + 1;
      
    //if it is a new minimum we notifiy the others
    if (value < min) {
      min = value;
      notifyOthers();
    }	//first we sleep a bit to loose some time
	
    try {
      Thread.currentThread().sleep(500000 + randomGenerator.nextInt(500));
    } catch (Exception e) {
      e.printStackTrace();
    }
	
    //finally we check wether we should stop
    this.checkForHalt();
  }


  public boolean hasFinished() {
    return finished;
  }


  /**
   * Checks if the agent has completed ist task
   */
  private void checkForHalt() {
    if (min <= 5) {
      int max = otherList.size();
      //inform other agents that we want to stop
      for (int j = 0; j < max; j++) {
        ((Agent)otherList.elementAt(j)).informPeerEnded(this.id);
      }
      finished = true;
      try {
        org.objectweb.proactive.ProActive.migrateTo(home);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  /**
   * Prints the messages received by the agents,
   */
  public void printInMessages() {
    System.out.println("=====Received messages for Agent " + id);
    for (int i = 0; i < receivedMessages.size(); i++) {
      System.out.println(receivedMessages.elementAt(i));
    }
    System.out.println("============================");
  }


  /** 
   * Notify other agents that we have found a value
   * lower than the min
   */
  public void notifyOthers() {
    int max = otherList.size();
    for (int j = 0; j < max; j++) {
      ((Agent)otherList.elementAt(j)).newMinimum(min, new String("" + id + "-" + messageSequenceNumber));
    }
    messageSequenceNumber++;
  }


  public void informPeerEnded(int id) {
    peersEnded++;
  }


  public void waitForOthersToStop(org.objectweb.proactive.Service service) {
    while (peersEnded < otherList.size()) {
      service.blockingServeOldest();
    }
  }


  public void live(org.objectweb.proactive.Body body) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    while (body.isActive()) {
      if (finished == true) {
        waitForOthersToStop(service);
        printInMessages();
        body.terminate();
      } else {
        while (service.hasRequestToServe()) {
          service.serveOldest();
        }
        if (running) {
          this.chooseValue();
          nextDestination = getNextDestination();
          try {
            org.objectweb.proactive.ProActive.migrateTo(nextDestination);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }


  public static void main(String args[]) {
    if (args.length < 2) {
      System.out.println("Usage: java org.objectweb.proactive.examples.collaborative.Agent <number of agents (n)> <hostname 1> ... <hostname m>");
      System.exit(-1);
    }
    try {
      String strHome = "///home";
      // get destination list
      java.util.Vector destinationList = new java.util.Vector(args.length - 1);
      for (int i = 1; i < args.length; i++) {
        destinationList.add(args[i]);
      }
      org.objectweb.proactive.core.node.Node home = org.objectweb.proactive.core.node.NodeFactory.getNode(strHome);
      int n = Integer.parseInt(args[0]);
      System.out.println("Creating agents");
      Agent agents[] = createAgents(n, strHome, home, destinationList);
      System.out.println("Linking agents");
      linkAgents(agents);
      System.out.println("Linking agents successful");
      System.out.println("Start agents");
      for (int i = 0; i < n; i++) {
        System.out.println("Starting agent " + i);
        agents[i].start();
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }


  private static Agent[] createAgents(int size, String homeString, org.objectweb.proactive.core.node.Node home, java.util.Vector destinationList) throws Exception {
    Agent agents[] = new Agent[size];
    Object param[] = new Object[2];
    param[1] = homeString;
    // Create the agents
    for (int j = 0; j < size; j++) {
      param[0] = new Integer(j);
      //we create the agents
      agents[j] = (Agent)org.objectweb.proactive.ProActive.newActive(Agent.class.getName(), param, home);
      agents[j].setDestinationList(destinationList);
    }
    return agents;
  }


  private static void linkAgents(Agent[] agents) {
    int n = agents.length;
    for (int j = 0; j < n; j++) {
      for (int i = 0; i < n; i++) {
        if (i != j) {
          agents[j].addAgent(agents[i]);
        }
      }
      try {
        Thread.sleep(300);
      } catch (Exception ex) {
      }
    }
  }
}

