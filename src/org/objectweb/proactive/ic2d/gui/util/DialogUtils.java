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
package org.objectweb.proactive.ic2d.gui.util;

import org.objectweb.proactive.ic2d.data.WorldObject;
import org.objectweb.proactive.ic2d.data.HostObject;
import org.objectweb.proactive.ic2d.util.IC2DMessageLogger;

public class DialogUtils {

  private DialogUtils() {
  }
  
  
  public static void openNewRMIHostDialog(java.awt.Component parentComponent, WorldObject worldObject, IC2DMessageLogger logger) {
    String initialHostValue = "localhost";
    try {
      initialHostValue = java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException e) {}
    Object result = javax.swing.JOptionPane.showInputDialog(
          parentComponent,                                             // Component parentComponent,
          "Please enter the name or the IP of the host to monitor :",  // Object message,
          "Adding a host to monitor",                                  // String title,
          javax.swing.JOptionPane.PLAIN_MESSAGE,                       // int messageType,
          null,                                                        // Icon icon,
          null,                                                        // Object[] selectionValues,
          initialHostValue                                             // Object initialSelectionValue)
        );
    if (result == null || (! (result instanceof String))) return;
    String host = (String) result;
    try {
      worldObject.addHostObject(host);
    } catch (java.rmi.RemoteException e) {
      logger.log("Cannot create the RMI Host "+host, e);
    }
  }


/*
  public static void openNewGlobusHostDialog(java.awt.Frame parent, WorldObject worldObject, IC2DMessageLogger logger) {
    org.objectweb.proactive.ic2d.gui.dialog.NewGlobusHostDialog diag = new org.objectweb.proactive.ic2d.gui.dialog.NewGlobusHostDialog(parent, true);
    diag.setVisible(true);
    if (diag.success)
      worldObject.createNewRemoteHostGlobus(diag.host, diag.port);
    diag = null;
  }
*/


  public static void openNewJINIHostDialog(java.awt.Component parentComponent, WorldObject worldObject, IC2DMessageLogger logger) {
    String initialHostValue = "localhost";
    try {
      initialHostValue = java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException e) {}
    Object result = javax.swing.JOptionPane.showInputDialog(
          parentComponent,                                             // Component parentComponent,
          "Please enter the name or the IP of the host to monitor :",  // Object message,
          "Adding a host to monitor",                                  // String title,
          javax.swing.JOptionPane.PLAIN_MESSAGE,                       // int messageType,
          null,                                                        // Icon icon,
          null,                                                        // Object[] selectionValues,
          initialHostValue                                             // Object initialSelectionValue)
        );
    if (result == null || (! (result instanceof String))) return;
    String host = (String) result;
   
      worldObject.addHosts(host);
    
  }


  public static void openNewNodeDialog(java.awt.Component parentComponent, WorldObject worldObject, IC2DMessageLogger logger) {
    Object result = javax.swing.JOptionPane.showInputDialog(
          parentComponent,                                                       // Component parentComponent,
          "Please enter the URL of the node in the form //hostname/nodename :",  // Object message,
          "Adding a JVM to monitor",                                             // String title,
          javax.swing.JOptionPane.PLAIN_MESSAGE,                                 // int messageType,
          null,                                                                  // Icon icon,
          null,                                                                  // Object[] selectionValues,
          "//hostname/nodename"                                                  // Object initialSelectionValue)
        );
    if (result == null || (! (result instanceof String))) return;
    String url = (String) result;
    int n1 = url.indexOf("//");
    int n2 = url.lastIndexOf("/");
    if (n1 == -1 || n2 == -1 || n2 <= n1+1) {
      logger.warn(url+" isn't an proper node url !");
      return;
    }
    String host = url.substring(n1 + 2, n2);
    String nodeName = url.substring(n2+1);
    try {
      worldObject.addHostObject(host, nodeName);
    } catch (java.rmi.RemoteException e) {
      logger.log("Cannot create the RMI Host "+host, e);
    }
  }


  public static void displayMessageDialog(java.awt.Component parentComponent, Object message) {
    javax.swing.JOptionPane.showMessageDialog(
          parentComponent,                             // Component parentComponent,
          message,                                     // Object message,
          "IC2D Message",                              // String title,
          javax.swing.JOptionPane.INFORMATION_MESSAGE  // int messageType,
        );
  }
  
  
  public static void displayWarningDialog(java.awt.Component parentComponent, Object message) {
    javax.swing.JOptionPane.showMessageDialog(
          parentComponent,                         // Component parentComponent,
          message,                                 // Object message,
          "IC2D Message",                          // String title,
          javax.swing.JOptionPane.WARNING_MESSAGE  // int messageType,
        );
  }
}
