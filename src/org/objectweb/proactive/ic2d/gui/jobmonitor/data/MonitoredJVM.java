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
package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.core.util.UrlBuilder;


public class MonitoredJVM extends BasicMonitoredObject {
    static protected int lastID = 0;
    static protected Map prettyNames = new HashMap();
    private int depth;
    private int port;

    protected int incLastID() {
        return ++lastID;
    }

    protected Map getPrettyNames() {
        return prettyNames;
    }

    private void extractPort() {
        Pattern p = Pattern.compile("(.*//)?([^:]+):?([0-9]*)/(.+)");
        Matcher m = p.matcher(fullname);
        if (!m.matches()) {
            port = Registry.REGISTRY_PORT;
        }

        String portStr = m.group(3);
        try {
            port = Integer.parseInt(portStr);
        } catch (Exception e) {
            port = Registry.REGISTRY_PORT;
        }
    }

    public MonitoredJVM(String url, int depth) {
        super(JVM, "JVM", url);
        this.depth = depth;
        //extractPort();
        this.port = UrlBuilder.getPortFromUrl(url);
    }

    public void copyInto(BasicMonitoredObject o) {
        super.copyInto(o);
        MonitoredJVM jvmObject = (MonitoredJVM) o;
        jvmObject.depth = depth;
        jvmObject.port = port;
    }

    public int getDepth() {
        return depth;
    }

    public int getPort() {
        return port;
    }
}
