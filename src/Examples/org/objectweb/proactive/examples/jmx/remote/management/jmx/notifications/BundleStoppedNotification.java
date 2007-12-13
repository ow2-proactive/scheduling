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
package org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications;

import javax.management.ObjectName;

import org.objectweb.proactive.examples.jmx.remote.management.mbean.BundleInfo;


public class BundleStoppedNotification extends BundleNotification {

    /**
     *
     */
    private String url;
    private ObjectName on;
    private String message;
    private BundleInfo bundleInfo;

    public BundleStoppedNotification(String type, Object source, long sequenceNumber, String message,
            String url, ObjectName on) {
        super(type, source, sequenceNumber);
        this.url = url;
        this.on = on;
        this.message = message;
        this.bundleInfo = (BundleInfo) source;
    }

    @Override
    public BundleInfo getBundleInfo() {
        return this.bundleInfo;
    }

    @Override
    public int getEventType() {
        return BundleNotification.BUNDLE_STOPPED;
    }

    @Override
    public ObjectName getObjectName() {
        return this.on;
    }

    @Override
    public String getUrl() {
        return this.url;
    }
}
