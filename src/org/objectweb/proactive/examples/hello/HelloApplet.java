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
package org.objectweb.proactive.examples.hello;

public class HelloApplet extends org.objectweb.proactive.examples.AppletWrapper {

  /**
   * The active Hello object
   */
  private Hello activeHello;
  private boolean shouldRun = true;
  /**
   * The remote node locator if runnning over the network
   */
  private org.objectweb.proactive.core.node.Node node;
  /**
   * The label 
   */
  private javax.swing.JLabel lMessage;


  public HelloApplet(String name, int width, int height) {
    super(name, width, height);
  }


  public static void main(String arg[]) {
    HelloApplet applet = new HelloApplet("Hello applet", 300, 200);
  }


  public void init() {
    String nodeUrl = (isApplet) ? getParameter("node") : null;
    try {
      if (nodeUrl == null)
        node = null; // We'll create the objects locally
      else
        node = org.objectweb.proactive.core.node.NodeFactory.getNode(nodeUrl); // We'll create the objects on the specified node
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void start() {
    displayMessage("Applet creating active objects");
    try {
      activeHello = (Hello)org.objectweb.proactive.ProActive.newActive(Hello.class.getName(), null);
    } catch (Exception e) {
      // There has been a problem...
      displayMessage("Error while initializing");
      lMessage.setText("Error... Did you set the security attributes?");
      e.printStackTrace(); // Print the exception to stderr
      return;
    }
    displayMessage("Ok");
    Thread t = new Thread(new Dummy(), "Dummy Hello clock");
    t.start();
  }


  public void stop() {
    shouldRun = false;
    activeHello = null;
    lMessage.setText("Applet stopped");
  }


  protected javax.swing.JPanel createRootPanel() {
    javax.swing.JPanel rootPanel = new javax.swing.JPanel();
    // Layout 
    rootPanel.setBackground(java.awt.Color.white);
    rootPanel.setForeground(java.awt.Color.blue);
    lMessage = new javax.swing.JLabel("Please wait...........");
    rootPanel.add(lMessage);
    return rootPanel;
  }


  private class Dummy implements Runnable {

    public void run() {
      while (shouldRun) {
        lMessage.setText(activeHello.sayHello());
        try {
          Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
        }
      }
    }
  }
}
