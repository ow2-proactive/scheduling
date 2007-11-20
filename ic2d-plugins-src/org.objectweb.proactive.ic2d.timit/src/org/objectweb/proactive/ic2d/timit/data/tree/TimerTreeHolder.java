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
package org.objectweb.proactive.ic2d.timit.data.tree;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ic2d.timit.data.AbstractObject;
import org.objectweb.proactive.ic2d.timit.data.BasicChartObject;


public class TimerTreeHolder extends AbstractObject {
    private static TimerTreeHolder instance;
    public static final String P_ADD_SOURCE = "_add_source";
    public static final String P_REMOVE_SELECTED = "_remove_selected";

    /** List of hold tree's sources */
    protected List<BasicChartObject> chartObjectSources;
    protected List<TimerTreeNodeObject> dummyRoots;

    public static final TimerTreeHolder getInstance() {
        return instance;
    }

    public TimerTreeHolder() {
        this.chartObjectSources = new ArrayList<BasicChartObject>();
        this.dummyRoots = new ArrayList<TimerTreeNodeObject>();
        instance = this;
    }

    public final void provideChartObject(final BasicChartObject source,
        final boolean retrieveOnly) {
        int sourceIndex = this.chartObjectSources.indexOf(source);
        if (sourceIndex != -1) {
            if (sourceIndex < this.dummyRoots.size()) {
                this.dummyRoots.get(sourceIndex)
                               .firePropertyChange(TimerTreeNodeObject.P_SELECTION,
                    null, null);
            }
        } else if (!retrieveOnly) {
            this.chartObjectSources.add(source);
            // Add dummyRoot to current dummyRoots and attach the total
            TimerTreeNodeObject dummyRoot = new TimerTreeNodeObject(source.getAoObject()
                                                                          .getName());
            dummyRoot.children.add(source.getRootTimer());
            dummyRoots.add(dummyRoot);
            firePropertyChange(P_ADD_SOURCE, null, null);
            dummyRoot.firePropertyChange(TimerTreeNodeObject.P_SELECTION, null,
                null);
            dummyRoot.firePropertyChange(TimerTreeNodeObject.P_EXPAND_STATE,
                null, true);
        }
    }

    /**
     * TimerObject t MUST be a dummy root
     * @param t
     */
    public final void removeDummyRoot(final TimerTreeNodeObject t) {
        this.dummyRoots.remove(t);
        TimerTreeNodeObject rootOfSource = t.getChildren().get(0);
        BasicChartObject sourceToRemove = null;
        for (BasicChartObject c : this.chartObjectSources) {
            if (c.getRootTimer().equals(rootOfSource)) {
                sourceToRemove = c;
                break; // unary association therefore equality can happen only once
            }
        }
        if (sourceToRemove != null) {
            this.chartObjectSources.remove(sourceToRemove);
        }
    }

    public final List<TimerTreeNodeObject> getChildren() {
        return this.dummyRoots;
    }

    public final List<BasicChartObject> getChartObjectSources() {
        return chartObjectSources;
    }

    public List<TimerTreeNodeObject> getDummyRoots() {
        return dummyRoots;
    }
}
