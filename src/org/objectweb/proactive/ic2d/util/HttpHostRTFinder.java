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

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl;
import org.objectweb.proactive.core.runtime.http.HttpProActiveRuntime;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJVM;


public class HttpHostRTFinder implements HostRTFinder {
    private IC2DMessageLogger logger;
    private DefaultListModel skippedObjects;

    public HttpHostRTFinder(IC2DMessageLogger logger,
        DefaultListModel skippedObjects) {
        this.logger = logger;
        this.skippedObjects = skippedObjects;
    }

    /**
     * @see org.objectweb.proactive.ic2d.util.HostRTFinder#findPARuntimes(java.lang.String, int)
     */
    public ArrayList findPARuntimes(String host, int port)
        throws IOException {
        logger.log("Exploring " + host + " with HTTP on port " + port);
        ArrayList runtimeArray = new ArrayList();
        ProActiveRuntimeAdapterImpl adapter;
        try {
            adapter = new ProActiveRuntimeAdapterImpl(new HttpProActiveRuntime(
                        UrlBuilder.buildUrl(host, "", "http:", port)));
            runtimeArray.add(adapter);
        } catch (ProActiveException e) {
            //        	we build a jvmObject with depth of 0 since this jvm won't be monitored
            MonitoredJVM jvmObject = new MonitoredJVM(UrlBuilder.buildUrl(
                        host, "", "http:", port), 0);
            if (!skippedObjects.contains(jvmObject)) {
                logger.log(e.getMessage(), e);
                skippedObjects.addElement(jvmObject);
            }
        }

        return runtimeArray;
    }
}
