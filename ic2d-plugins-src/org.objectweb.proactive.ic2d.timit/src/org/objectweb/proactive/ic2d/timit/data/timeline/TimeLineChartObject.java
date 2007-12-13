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
package org.objectweb.proactive.ic2d.timit.data.timeline;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.timit.data.BasicChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.BasicChartObject;
import org.objectweb.proactive.ic2d.timit.editparts.timeline.TimeLineChartEditPart;


public class TimeLineChartObject {
    private final ArrayList<SequenceObject> childrenList = new ArrayList<SequenceObject>();
    private TimeLineChartEditPart ep;
    private TimeIntervalManager timeIntervalManager;

    public TimeLineChartObject() {
        // Create the time interval manager
        this.timeIntervalManager = new TimeIntervalManager();
    }

    public void provideSourceContainer(BasicChartContainerObject sourceContainer) {
        for (BasicChartObject c : sourceContainer.getChildrenList()) {
            ActiveObject a = c.getAoObject();
            if (a != null) {
                SequenceObject sequenceObject = this.createSequence(a);
                sequenceObject.startRecord();
            }
        }
    }

    public void addChild(SequenceObject child) {
        this.childrenList.add(child);
    }

    public void stopRecordAndBuildChart() {
        // Stop all
        for (SequenceObject s : this.childrenList) {
            s.stopRecord();
        }

        // Get the longuest time
        long longuestTime = 0;
        for (SequenceObject s : this.childrenList) {
            if ((s.lastTimeStampValue != 0) && (s.lastTimeStampValue > longuestTime)) {
                longuestTime = s.lastTimeStampValue;
            }
        }
        if (longuestTime != 0) {
            // Init the time interval manager
            this.timeIntervalManager.init(0, longuestTime);
            this.ep.asyncRefresh(true);
        }
    }

    public List<SequenceObject> getChildrenList() {
        return childrenList;
    }

    public void clearChildrenList() {
        // Unsubsrcribe children
        for (SequenceObject s : this.childrenList) {
            s.clear();
            // Unsubscribe the listener
            JMXNotificationManager.getInstance().unsubscribe(s.objectName, s);
        }
        this.childrenList.clear();
    }

    public void setEp(TimeLineChartEditPart ep) {
        this.ep = ep;
    }

    public SequenceObject createSequence(ActiveObject a) {
        SequenceObject sequenceObject = getSequence(a.getName());
        if (sequenceObject == null) {
            sequenceObject = new SequenceObject(a.getName(), a.getObjectName(), this);
            // Subscribe to notif manager
            JMXNotificationManager.getInstance().subscribe(a.getObjectName(), sequenceObject,
                    a.getParent().getParent().getUrl());
        }
        return sequenceObject;
    }

    private final SequenceObject getSequence(final String name) {
        for (final SequenceObject seq : childrenList) {
            if (seq.name.equals(name)) {
                return seq;
            }
        }
        return null;
    }

    public TimeIntervalManager getTimeIntervalManager() {
        return timeIntervalManager;
    }
}
