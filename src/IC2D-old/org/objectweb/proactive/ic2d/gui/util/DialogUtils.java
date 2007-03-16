/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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

import java.net.InetAddress;

import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.data.WorldObject;
import org.objectweb.proactive.ic2d.gui.data.IC2DPanel;
import org.objectweb.proactive.ic2d.gui.data.WorldPanel;
import org.objectweb.proactive.ic2d.gui.dialog.FilteredClassesPanel;
import org.objectweb.proactive.ic2d.util.ActiveObjectFilter;
import org.objectweb.proactive.ic2d.util.IC2DMessageLogger;


public class DialogUtils {
    private DialogUtils() {
    }

    public static void openNewRMIHostDialog(
        java.awt.Component parentComponent, WorldPanel worldPanel,
        IC2DMessageLogger logger) {
        WorldObject worldObject = worldPanel.getWorldObject();
        String initialHostValue = "localhost";
        try {
            initialHostValue = UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost()) +
                ":" + System.getProperty("proactive.rmi.port");
        } catch (java.net.UnknownHostException e) {
            logger.log(e.getMessage());
            return;
        }

        //	calling dlg for host or ip and depth control
        HostDialog rmihostdialog = HostDialog.showHostDialog((javax.swing.JFrame) parentComponent,
                initialHostValue);

        if (!rmihostdialog.isButtonOK()) {
            return;
        }
        rmihostdialog.setButtonOK(false);
        String host = rmihostdialog.getJTextFieldHostIp();
        try {
            int port = UrlBuilder.getPortFromUrl(host);
            String host1 = UrlBuilder.removePortFromHost(host);
            host = UrlBuilder.getHostNameorIP(InetAddress.getByName(host1));
            host = host + ":" + port;
        } catch (java.net.UnknownHostException e) {
            logger.log(e, false);
            return;
        }

        worldPanel.monitoredHostAdded(host, "rmi:");
        worldPanel.getMonitorThread().updateHosts();
        //new MonitorThread("rmi:", host, rmihostdialog.getJTextFielddepth(),worldObject, logger);//.start();
    }

    public static void openNewHTTPHostDialog(
        java.awt.Component parentComponent, WorldPanel worldPanel,
        IC2DMessageLogger logger) {
        WorldObject worldObject = worldPanel.getWorldObject();
        String initialHostValue = "localhost";
        try {
            initialHostValue = UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost()) +
                ":" + System.getProperty("proactive.http.port");
            ;
        } catch (java.net.UnknownHostException e) {
            logger.log(e.getMessage());
            return;
        }
        HostDialog httphostdialog = HostDialog.showHostDialog((javax.swing.JFrame) parentComponent,
                initialHostValue);

        if (!httphostdialog.isButtonOK()) {
            return;
        }

        httphostdialog.setButtonOK(false);
        String host = httphostdialog.getJTextFieldHostIp();

        try {
            // ********************* Fix bug : In Http we need the port number !****************************
            int port = UrlBuilder.getPortFromUrl(host);
            String host1 = UrlBuilder.removePortFromHost(host);

            //Get the host IP           
            host = UrlBuilder.getHostNameorIP(InetAddress.getByName(host1));
            //Put the port
            host = host + ':' + port;
        } catch (java.net.UnknownHostException e) {
            logger.log(e, false);
            return;
        }

        //new MonitorThread("http:", host, httphostdialog.getJTextFielddepth(),worldObject, logger);//.start();
        worldPanel.monitoredHostAdded(host, "http:");
        worldPanel.getMonitorThread().updateHosts();

        //        Object result = javax.swing.JOptionPane.showInputDialog(parentComponent, // Component parentComponent,
        //                "Please enter the name or the IP of the host to monitor :", // Object message,
        //                "Adding a host to monitor", // String title,
        //                javax.swing.JOptionPane.PLAIN_MESSAGE, // int messageType,
        //                null, // Icon icon,
        //                null, // Object[] selectionValues,
        //                initialHostValue // Object initialSelectionValue)
        //            );
        //        if ((result == null) || (!(result instanceof String))) {
        //            return;
        //        }
        //        String host = (String) result;
        //        System.out.println("host " + host);
        //        try {
        //            worldObject.addHostObject(host, asso);
        //        } catch (RemoteException e1) {
        //            e1.printStackTrace();
        //        }
    }

    public static void openNewIbisHostDialog(
        java.awt.Component parentComponent, WorldPanel worldPanel,
        IC2DMessageLogger logger) {
        WorldObject worldObject = worldPanel.getWorldObject();
        String initialHostValue = "localhost";
        try {
            initialHostValue = UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost()) +
                ":" + System.getProperty("proactive.rmi.port");
        } catch (java.net.UnknownHostException e) {
            logger.log(e.getMessage());
            return;
        }
        HostDialog ibishostdialog = HostDialog.showHostDialog((javax.swing.JFrame) parentComponent,
                initialHostValue);

        if (!ibishostdialog.isButtonOK()) {
            return;
        }
        ibishostdialog.setButtonOK(false);
        String host = ibishostdialog.getJTextFieldHostIp();
        try {
            int port = UrlBuilder.getPortFromUrl(host);
            String host1 = UrlBuilder.removePortFromHost(host);

            //Get the host IP           
            host = UrlBuilder.getHostNameorIP(InetAddress.getByName(host1));
            //Put the port
            host = host + ':' + port;
        } catch (java.net.UnknownHostException e) {
            logger.log(e, false);
            return;
        }

        worldPanel.monitoredHostAdded(host, "ibis:");
        worldPanel.getMonitorThread().updateHosts();
        //new MonitorThread("ibis:", host, ibishostdialog.getJTextFielddepth(), worldObject, logger);//.start();
    }

    public static void openNewJINIHostDialog(
        java.awt.Component parentComponent, WorldPanel worldPanel,
        IC2DMessageLogger logger) {
        WorldObject worldObject = worldPanel.getWorldObject();
        String initialHostValue = "localhost";
        try {
            initialHostValue = UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost());
        } catch (java.net.UnknownHostException e) {
            logger.log(e.getMessage());
            return;
        }
        HostDialog jinihostdialog = HostDialog.showHostDialog((javax.swing.JFrame) parentComponent,
                initialHostValue);

        if (!jinihostdialog.isButtonOK()) {
            return;
        }
        jinihostdialog.setButtonOK(false);
        String host = jinihostdialog.getJTextFieldHostIp();
        try {
            String host1 = UrlBuilder.removePortFromHost(host);
            host = UrlBuilder.getHostNameorIP(InetAddress.getByName(host1));
        } catch (java.net.UnknownHostException e) {
            logger.log(e, false);
            return;
        }

        worldPanel.monitoredHostAdded(host, "jini:");
        worldPanel.getMonitorThread().updateHosts();
        //new MonitorThread("jini:", host, jinihostdialog.getJTextFielddepth(), worldObject, logger);//.start();
    }

    public static void openNewJINIHostsDialog(
        java.awt.Component parentComponent, WorldPanel worldPanel,
        IC2DMessageLogger logger) {
        WorldObject worldObject = worldPanel.getWorldObject();

        //            String initialHostValue = "localhost";
        //            try {
        //                initialHostValue = UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost());
        //            } catch (java.net.UnknownHostException e) {
        //                logger.log(e.getMessage());
        //            }
        //            HostDialog jinihostdialog = HostDialog.showHostDialog((javax.swing.JFrame) parentComponent,
        //                    initialHostValue);
        //
        //            if (!jinihostdialog.isButtonOK()) {
        //                return;
        //            }
        //            jinihostdialog.setButtonOK(false);
        //            String host = jinihostdialog.getJTextFieldHostIp();
        worldPanel.monitoredHostAdded(null, "jini:");
        worldPanel.getMonitorThread().updateHosts();
        //new MonitorThread("jini:", null, "3", worldObject, logger);//.start();
    }

    //    public static void openNewNodeDialog(java.awt.Component parentComponent,
    //        WorldObject worldObject, IC2DMessageLogger logger) {
    //        Object result = javax.swing.JOptionPane.showInputDialog(parentComponent, // Component parentComponent,
    //                "Please enter the URL of the node in the form //hostname/nodename :", // Object message,
    //                "Adding a JVM to monitor with ", // String title,
    //                javax.swing.JOptionPane.PLAIN_MESSAGE, // int messageType,
    //                null, // Icon icon,
    //                null, // Object[] selectionValues,
    //                "//hostname/nodename" // Object initialSelectionValue)
    //            );
    //        if ((result == null) || (!(result instanceof String))) {
    //            return;
    //        }
    //        String url = (String) result;
    //        
    //        String host = UrlBuilder.getHostNameFromUrl()
    //        String nodeName = url.substring(n2 + 1);
    //
    //        //        try {
    //        //            worldObject.addHostObject(host, null, nodeName);
    //        //        } catch (java.rmi.RemoteException e) {
    //        //            logger.log("Cannot create the RMI Host " + host, e);
    //        //        }
    //    }
    public static void displayMessageDialog(
        java.awt.Component parentComponent, Object message) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent, // Component parentComponent,
            message, // Object message,
            "IC2D Message", // String title,
            javax.swing.JOptionPane.INFORMATION_MESSAGE // int messageType,
        );
    }

    public static void displayWarningDialog(
        java.awt.Component parentComponent, Object message) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent, // Component parentComponent,
            message, // Object message,
            "IC2D Message", // String title,
            javax.swing.JOptionPane.WARNING_MESSAGE // int messageType,
        );
    }

    public static void openFilteredClassesDialog(
        java.awt.Component parentComponent, IC2DPanel ic2dPanel,
        ActiveObjectFilter filter) {
        FilteredClassesPanel panel = new FilteredClassesPanel(filter);
        int result = javax.swing.JOptionPane.showOptionDialog(parentComponent, // Component parentComponent,
                panel, // Object message,
                "Filtered classes dialog", // String title,
                javax.swing.JOptionPane.OK_CANCEL_OPTION, // option buttons
                javax.swing.JOptionPane.PLAIN_MESSAGE, // int messageType,
                null, // Icon icon,
                null, // Object[] selectionValues
                null);
        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        if (panel.updateFilter(filter)) {
            ic2dPanel.updateFilteredClasses();
        }
    }
}
