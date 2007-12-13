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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.jmx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.client.ClientConnector;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.URIBuilder;


/**
 *  This example connects remotely a MBean Server and shows all registered MBeans on this server.
 * @author ProActive Team
 *
 */
public class TestClient implements NotificationListener, Serializable {
    private transient ClientConnector cc;
    private transient ProActiveConnection connection;
    private transient JMXConnector connector;
    private String url;
    private ConnectionListener listener;

    public static void main(String[] args) {
        new TestClient();
    }

    /**
     * Default Constructor : read the url and connect to the MBean Server via The ProActive Connector.
     * When connected, gets and show  the JMX domains one can explore
     */
    public TestClient() {
        System.out.println("Enter the url of the JMX MBean Server :");
        this.url = read();
        connect();
        getMBeanInformations();
    }

    private String read() {
        String what = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            what = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return what;
    }

    private void connect() {
        System.out.println("Connecting to : " + this.url);
        String serverName = URIBuilder.getNameFromURI(url);

        if ((serverName == null) || serverName.equals("")) {
            serverName = "serverName";
        }
        this.cc = new ClientConnector(this.url, serverName);
        this.cc.connect();
        this.connection = cc.getConnection();
        this.connector = cc.getConnector();
        /* adds a connector listener */
        this.connector.addConnectionNotificationListener(this, null, null);
        try {
            this.listener = (ConnectionListener) PAActiveObject.newActive(ConnectionListener.class.getName(),
                    new Object[] { this.connection });
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    private void domains() {
        System.out.println();
        System.out.println("Registered Domains :");
        String[] domains = (String[]) connection.getDomainsAsynchronous().getObject();
        for (int i = 0; i < domains.length; i++) {
            System.out.println("\t [ " + i + " ] " + domains[i]);
        }
        System.out.println();
        System.out.println("Type the domain number to see all registered MBeans in this domain :");
        String read = read();
        try {
            int index = Integer.parseInt(read);
            mbeans(domains[index]);
        } catch (Exception e) {
            System.out.println();
            System.out.println("Type the domain number to see all registered MBeans in this domain :");
            domains();
        }
    }

    private void mbeans(String domain) {
        try {
            ObjectName on = new ObjectName(domain + ":*");
            Set<ObjectInstance> queryMBeans = connection.queryMBeans(on, null);
            ObjectInstance[] beans = new ObjectInstance[queryMBeans.size()];
            queryMBeans.toArray(beans);
            //			Iterator<ObjectInstance> iterator = queryMBeans.iterator();
            for (int i = 0; i < beans.length; i++) {
                ObjectName beanName = beans[i].getObjectName();

                System.out.println(" [ " + i + " ] " + beanName);
            }
            System.out.println("[ D ]  Domains list");
            System.out.println();
            System.out.println("Type the mbean number to see its properties :");
            String read = read();
            assert (read != null);
            if (read.toLowerCase().trim().equals("d")) {
                domains();
                return;
            }

            try {
                int index = Integer.parseInt(read);
                beanProperties(beans[index].getObjectName());
            } catch (Exception e) {
                System.out.println();
                System.out.println("Type the mbean number to see its properties :");
                mbeans(domain);
            }
            mbeans(domain);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void beanProperties(ObjectName on) {
        System.out.println();
        System.out.println("-------------------- " + on + " ---------------------------- ");

        MBeanInfo beanInfo = (MBeanInfo) connection.getMBeanInfoAsynchronous(on).getObject();
        System.out.println("\tConstructors : ");
        System.out.println();
        MBeanConstructorInfo[] constructorInfos = beanInfo.getConstructors();
        for (int i = 0; i < constructorInfos.length; i++) {
            System.out.print(constructorInfos[i].getName() + " ( ");
            MBeanParameterInfo[] params = constructorInfos[i].getSignature();
            if (params.length == 0) {
                System.out.println(" );");
            } else {
                for (int j = 0; j < params.length; j++) {
                    if (j == (params.length - 1)) {
                        System.out.println(params[j].getType() + "  " + params[j].getName() + "  ); ");
                    } else {
                        System.out.println(params[j].getType() + "  " + params[j].getName() + "  , ");
                    }
                }
            }
        }

        String description = beanInfo.getDescription();
        System.out.println("\tDescription :\t" + description);
        System.out.println();
        System.out.println("\tAttributes :");
        MBeanAttributeInfo[] attribs = beanInfo.getAttributes();
        for (int i = 0; i < attribs.length; i++) {
            Object obj = connection.getAttributeAsynchronous(on, attribs[i].getName()).getObject();
            System.out.println("\t\t" + attribs[i].getDescription() + " =\t " + obj);
        }

        System.out.println();

        MBeanOperationInfo[] infos = beanInfo.getOperations();
        if (infos.length != 0) {
            System.out.println("\tOperations :");

            for (int i = 0; i < infos.length; i++) {
                System.out.println("Description = " + infos[i].getDescription());
                System.out.print("\t" + infos[i].getReturnType() + "  ");
                System.out.print(infos[i].getName() + "  ( ");

                MBeanParameterInfo[] params = infos[i].getSignature();
                if (params.length == 0) {
                    System.out.println(" );");
                } else {
                    for (int j = 0; j < params.length; j++) {
                        if (j == (params.length - 1)) {
                            System.out.print(params[j].getType() + "  " + params[j].getName() + " ) ");
                        } else {
                            System.out.print(params[j].getType() + "  " + params[j].getName() + " , ");
                        }
                    }
                    System.out.println();
                }
            }
        }
        System.out.println();

        MBeanNotificationInfo[] notifs = beanInfo.getNotifications();
        if (notifs.length != 0) {
            System.out.println("\tNotifications :");
            try {
                this.connection.addNotificationListener(on, listener, null, null);
            } catch (InstanceNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < notifs.length; i++) {
                System.out.println("\t\t" + notifs[i].getDescription() + " : \t\t" + notifs[i].getName());
            }
        }
    }

    private void getMBeanInformations() {
        domains();
    }

    /* *
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public void handleNotification(Notification notification, Object handback) {
        System.out.println("---> Notification = " + notification);
    }
}
