/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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

import java.util.HashMap;
import java.util.Map;


public class MonitoredHost extends BasicMonitoredObject {
    static protected int lastID = 0;
    static protected Map prettyNames = new HashMap();
    private int port;
    private String monitorProtocol;

    protected int incLastID() {
        return ++lastID;
    }

    protected Map getPrettyNames() {
        return prettyNames;
    }

    public void copyInto(BasicMonitoredObject o) {
        super.copyInto(o);
        MonitoredHost hostObject = (MonitoredHost) o;
        hostObject.port = port;
    }

    public MonitoredHost(String hostname, int port, String monitorProtocol) {
        super(HOST, hostname, hostname + ":" + port);
        this.port = port;
        this.monitorProtocol = monitorProtocol;
    }

    public int getPort() {
        return port;
    }

    /**
     * @return Returns the monitorProtocol.
     */
    public String getMonitorProtocol() {
        return monitorProtocol;
    }
}
