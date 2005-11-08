/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.core.util.IbisProperties;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJVM;

import ibis.rmi.registry.LocateRegistry;
import ibis.rmi.registry.Registry;


/**
 * This class talks to ProActive nodes
 */
public class IbisHostRTFinder implements HostRTFinder {
    static Logger log4jlogger = ProActiveLogger.getLogger(Loggers.IC2D);

    static {
        IbisProperties.load();
    }

    private static final int DEFAULT_RMI_PORT = 1099;
    private IC2DMessageLogger logger;
	private DefaultListModel skippedObjects;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public IbisHostRTFinder(IC2DMessageLogger logger, DefaultListModel skippedObjects) {
        this.logger = logger;
        this.skippedObjects= skippedObjects;
    }

    public IbisHostRTFinder() {
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements HostNodeFinder -----------------------------------------------
    //
    public ArrayList findPARuntimes(String host, int port)
        throws java.io.IOException {
        // Try to determine the hostname
        log("Exploring " + host + " with Ibis on port " + port);
        // Hook the registry
        Registry registry = LocateRegistry.getRegistry(host, port);
        return findPARuntimes(registry);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private ArrayList findPARuntimes(Registry registry)
        throws java.io.IOException {
        // enumarate through the rmi binding on the registry
        log("Listing bindings for " + registry);
        String[] list = registry.list();
        ArrayList runtimeArray = new ArrayList();
        for (int idx = 0; idx < list.length; ++idx) {
            String id = list[idx];
            if (id.indexOf("PA_JVM") != -1) {
                ProActiveRuntime part;

                try {
                    RemoteProActiveRuntime r = (RemoteProActiveRuntime) registry.lookup(id);
                    part = new ProActiveRuntimeAdapterImpl(r);
                    runtimeArray.add(part);
                } catch (Exception e) {
//                	we build a jvmObject with depth of 0 since this jvm won't be monitored
                	MonitoredJVM jvmObject = new MonitoredJVM(id, 0);
                	if(! skippedObjects.contains(jvmObject)){
                        log(e.getMessage(), e);
                        skippedObjects.addElement(jvmObject);
                	}
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
            logger.log(s, e);
        } else {
            log4jlogger.info(s);
            e.printStackTrace();
        }
    }
}
