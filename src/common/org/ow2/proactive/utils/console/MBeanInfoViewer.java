/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.utils.console;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.ProActiveConnection;


/**
 * MBeanInfoViewer is used to handle MBean information display
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class MBeanInfoViewer {
    private ProActiveConnection connection;
    private ObjectName mbeanName;
    private MBeanInfo info;

    /**
     * Create a new instance of MBeanInfoViewer
     *
     * @param connection
     * @param mbeanName
     * @param info
     */
    public MBeanInfoViewer(ProActiveConnection connection, ObjectName mbeanName, MBeanInfo info) {
        super();
        this.connection = connection;
        this.mbeanName = mbeanName;
        this.info = info;
    }

    /**
     * Return the informations about the Scheduler MBean as a formatted string
     *
     * @return the informations about the Scheduler MBean as a formatted string
     */
    public String getInfo() {
        try {
            int len = 0;
            for (MBeanAttributeInfo attr : info.getAttributes()) {
                if (attr.getName().length() > len) {
                    len = attr.getName().length();
                }
            }
            len += 2;
            StringBuilder out = new StringBuilder();
            for (MBeanAttributeInfo attr : info.getAttributes()) {
                out.append(String.format("  %1$-" + len + "s" +
                    connection.getAttribute(mbeanName, attr.getName()) + "\n", attr.getName()));
            }
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve JMX informations from SchedulerMBean");
        }
    }
}
