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
package org.objectweb.proactive.ic2d.util;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJVM;


public class RMIHostRTFinder implements HostRTFinder {
    static Logger log4jlogger = ProActiveLogger.getLogger(Loggers.IC2D);
    private static final int DEFAULT_RMI_PORT = 1099;
    private IC2DMessageLogger logger;
    private DefaultListModel skippedObjects;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RMIHostRTFinder(IC2DMessageLogger logger,
        DefaultListModel skippedObjects) {
        this.logger = logger;
        this.skippedObjects = skippedObjects;
    }

    public RMIHostRTFinder() {
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements HostNodeFinder -----------------------------------------------
    //
    public ArrayList<ProActiveRuntime> findPARuntimes(String host, int port)
        throws java.io.IOException {
        // Try to determine the hostname
        log("Exploring " + host + " with RMI on port " + port);
        // Hook the registry
        Registry registry = LocateRegistry.getRegistry(host,
                new Integer(port).intValue());
        return findPARuntimes(registry);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private ArrayList<ProActiveRuntime> findPARuntimes(Registry registry)
        throws java.io.IOException {
        // enumarate through the rmi binding on the registry
        log("Listing bindings for " + registry);
        String[] list = registry.list();
        ArrayList<ProActiveRuntime> runtimeArray = new ArrayList<ProActiveRuntime>();
        for (int idx = 0; idx < list.length; ++idx) {
            String id = list[idx];
            if (id.indexOf("PA_JVM") != -1) {
                ProActiveRuntime part = null;

                try {
                    RemoteProActiveRuntime r = (RemoteProActiveRuntime) registry.lookup(id);
                    part = new ProActiveRuntimeAdapterImpl(r);
                    runtimeArray.add(part);
                } catch (Exception e) {
                    //we build a jvmObject with depth of 0 since this jvm won't be monitored
                    MonitoredJVM jvmObject = new MonitoredJVM(id, 0);
                    if (!skippedObjects.contains(jvmObject)) {
                        log(e.getMessage(), e);
                        skippedObjects.addElement(jvmObject);
                    }
                    continue;
                }
            }
        }
        return runtimeArray;
    }

    private void log(String s) {
        if (logger != null) {
            logger.log(s);
        } else {
            log4jlogger.info(s);
        }
    }

    private void log(String s, Exception e) {
        if (logger != null) {
            logger.log(s, e, false); // false to not have the dialog box
        } else {
            log4jlogger.info(s);
            e.printStackTrace();
        }
    }
}
