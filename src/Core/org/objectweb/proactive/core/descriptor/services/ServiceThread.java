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
package org.objectweb.proactive.core.descriptor.services;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.VirtualMachine;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RuntimeNotificationData;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class ServiceThread extends Thread {
    private VirtualNodeInternal vn;
    private UniversalService service;
    private VirtualMachine vm;
    private ProActiveRuntime localRuntime;
    int nodeCount = 0;
    long timeout = 0;
    int nodeRequested;
    public static Logger loggerDeployment = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);
    private long expirationTime;

    public ServiceThread(VirtualNodeInternal vn, VirtualMachine vm) {
        this.vn = vn;
        this.service = vm.getService();
        this.vm = vm;
        this.localRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
    }

    @Override
    public void run() {
        ProActiveRuntime[] part = null;

        try {
            part = service.startService();
            nodeCount = nodeCount + ((part != null) ? part.length : 0);
            if (part != null) {
                notifyVirtualNode(part);
            }
        } catch (ProActiveException e) {
            loggerDeployment.error("An exception occured while starting the service " +
                service.getServiceName() + " for the VirtualNode " + vn.getName() + " \n" + e.getMessage());
        }
    }

    public void notifyVirtualNode(ProActiveRuntime[] part) {
        for (int i = 0; i < part.length; i++) {
            String url = part[i].getURL();
            String protocol = URIBuilder.getProtocol(url);

            // JMX Notification
            ProActiveRuntimeWrapperMBean mbean = ProActiveRuntimeImpl.getProActiveRuntime().getMBean();
            if (mbean != null) {
                RuntimeNotificationData notificationData = new RuntimeNotificationData(vn.getName(), url,
                    protocol, vm.getName());
                mbean.sendNotification(NotificationType.runtimeAcquired, notificationData);
            }

            // END JMX Notification
        }
    }
}
