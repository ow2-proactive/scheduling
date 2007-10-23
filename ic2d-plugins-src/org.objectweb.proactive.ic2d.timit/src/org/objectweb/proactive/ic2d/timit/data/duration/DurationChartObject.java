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
package org.objectweb.proactive.ic2d.timit.data.duration;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.timit.data.BasicChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.BasicChartObject;
import org.objectweb.proactive.ic2d.timit.editparts.duration.DurationChartEditPart;


public class DurationChartObject {
    private final ArrayList<SequenceObject> childrenList = new ArrayList<SequenceObject>();
    private DurationChartEditPart ep;

    public void provideSourceContainer(
        BasicChartContainerObject sourceContainer) {
        try {
            for (BasicChartObject c : sourceContainer.getChildrenList()) {
                ActiveObject a = c.getAoObject();
                if (a != null) {
                    SequenceObject sequenceObject = this.createSequence(a);
                    sequenceObject.startRecord();
                }
            }
            if (this.childrenList.size() < 2) {
                System.out.println(
                    "DurationChartContainerObject.provideSourceContainer() -----> does not refresh");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        long longuestDurationTime = 0;
        for (SequenceObject s : this.childrenList) {
            if ((s.lastTimeStampValue != -1) &&
                    (s.lastTimeStampValue > longuestDurationTime)) {
                longuestDurationTime = s.lastTimeStampValue;
            }
        }
        if (longuestDurationTime != 0) {
            this.ep.asyncRefresh(longuestDurationTime);
        }
    }

    public List<SequenceObject> getChildrenList() {
        return childrenList;
    }

    public void setEp(DurationChartEditPart ep) {
        this.ep = ep;
    }

    public SequenceObject createSequence(ActiveObject a) {
        SequenceObject sequenceObject = getSequence(a.getName());
        if (sequenceObject == null) {
            sequenceObject = new SequenceObject(a.getName(), this);
            // Subscribe to notif manager
            JMXNotificationManager.getInstance()
                                  .subscribe(a.getObjectName(), sequenceObject,
                a.getHostUrlServer(), a.getServerName());
        }
        return sequenceObject;
    }

    private SequenceObject getSequence(String name) {
        for (SequenceObject seq : childrenList) {
            if (seq.name.equals(name)) {
                return seq;
            }
        }
        return null;
    }
}
