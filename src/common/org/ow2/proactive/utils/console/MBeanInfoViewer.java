/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils.console;

import java.util.HashMap;
import java.util.ListIterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.ow2.proactive.authentication.Authentication;
import org.ow2.proactive.authentication.crypto.Credentials;


/**
 * MBeanInfoViewer is used to handle MBean information display
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public final class MBeanInfoViewer {
    /** The authentication interface */
    private final Authentication auth;
    /** The name of MBean */
    private final String name;
    /** The connector environment */
    private final HashMap<String, Object> env;
    /** The name of the MBean to view */
    private ObjectName mbeanName;
    /** The connection to the MBeanServer */
    private MBeanServerConnection connection;
    /** The names of the attributes of the MBean */
    private String[] names;
    /** The padding applied to the output string */
    private int padding;

    /**
     * Creates a new instance of MBeanInfoViewer.
     * 
     * @param auth the authentication interface
     * @param name a string representation of the object name of the MBean
     * @param user the user that wants to connect to the JMX infrastructure 
     * @param creds the credentials of the user
     */
    public MBeanInfoViewer(final Authentication auth, final String name, final String user,
            final Credentials creds) {
        this.auth = auth;
        this.name = name;
        this.env = new HashMap<String, Object>(2);
        // Fill the env with credentials 
        this.env.put(JMXConnector.CREDENTIALS, new Object[] { creds, user });
    }

    /**
     * Return the informations about the Scheduler MBean as a formatted string.
     * The first time this method is called it connects to the JMX connector server.
     * The default behavior will try to establish a connection using RMI protocol
     *
     * @return the informations about the MBean as a formatted string
     */
    public String getInfo() {
        // Lazy initial connection
        if (this.connection == null) {
            try {
                String rawUrl = this.auth.getJMXConnectorURL();
                if (name.endsWith("admin")) {
                    rawUrl += "_admin";
                }
                final JMXServiceURL jmxUrl = new JMXServiceURL(rawUrl);
                final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxUrl, env);
                this.mbeanName = new ObjectName(this.name);
                this.connection = jmxConnector.getMBeanServerConnection();
            } catch (Exception e) {
                throw new RuntimeException("Unable create the MBeanInfoViewer about " + name, e);
            }
        }

        try {
            // Attribute names can't change so it is asked only once from the MBean
            if (this.names == null) {
                final MBeanAttributeInfo[] attrs = this.connection.getMBeanInfo(this.mbeanName)
                        .getAttributes();
                this.names = new String[attrs.length];
                for (int i = 0; i < attrs.length; i++) {
                    String name = attrs[i].getName();
                    if (name.length() > this.padding) {
                        this.padding = name.length();
                    }
                    this.names[i] = name;
                }
                this.padding += 2;
            }
            // Get the list of attributes in a single JMX call  
            final AttributeList list = this.connection.getAttributes(this.mbeanName, names);
            final ListIterator<?> it = list.listIterator();
            final StringBuilder out = new StringBuilder();
            while (it.hasNext()) {
                Attribute att = (Attribute) it.next();
                out.append(String
                        .format("  %1$-" + this.padding + "s" + att.getValue() + "\n", att.getName()));
            }
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve JMX informations from Selected Bean", e);
        }
    }
}