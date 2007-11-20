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
package org.objectweb.proactive.ic2d.timit.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.chart.model.Chart;
import org.objectweb.proactive.benchmarks.timit.util.CoreTimersContainer;
import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeNodeObject;
import org.objectweb.proactive.ic2d.timit.editparts.BasicChartEditPart;


/**
 * This class represents the model part of a chart.
 *
 * @author vbodnart
 *
 */
public class BasicChartObject {
    public static final boolean DEBUG = false;

    /**
     * The basic timer level
     */
    public static final String[] BASIC_LEVEL = new String[] {
            CoreTimersContainer.TOTAL_TIMER_NAME,
            CoreTimersContainer.SERVE_TIMER_NAME,
            CoreTimersContainer.SEND_REQUEST_TIMER_NAME,
            CoreTimersContainer.SEND_REPLY_TIMER_NAME,
            CoreTimersContainer.WAIT_BY_NECESSITY_TIMER_NAME,
            CoreTimersContainer.WAIT_FOR_REQUEST_TIMER_NAME
        };

    /**
     * The detailed timer level
     */
    public static final String[] DETAILED_LEVEL = new String[] {
            CoreTimersContainer.TOTAL_TIMER_NAME,
            CoreTimersContainer.SERVE_TIMER_NAME,
            CoreTimersContainer.SEND_REQUEST_TIMER_NAME,
            CoreTimersContainer.SEND_REPLY_TIMER_NAME,
            CoreTimersContainer.WAIT_BY_NECESSITY_TIMER_NAME,
            CoreTimersContainer.WAIT_FOR_REQUEST_TIMER_NAME,
            CoreTimersContainer.LOCAL_COPY_TIMER_NAME,
            CoreTimersContainer.BEFORE_SERIALIZATION_TIMER_NAME,
            CoreTimersContainer.SERIALIZATION_TIMER_NAME,
            CoreTimersContainer.AFTER_SERIALIZATION_TIMER_NAME,
            CoreTimersContainer.GROUP_ONE_WAY_CALL_TIMER_NAME,
            CoreTimersContainer.GROUP_ASYNC_CALL_TIMER_NAME
        };

    /**
     * The associated chart builder
     */
    protected BarChartBuilder barChartBuilder;

    /**
     * The reference on the parent container
     */
    protected BasicChartContainerObject parent;

    /**
     * A map view on the timer tree node objects
     */
    protected Map<Integer, TimerTreeNodeObject> timersMap;

    /**
     * A list view on the timer tree node objects
     */
    protected List<TimerTreeNodeObject> timersList;

    /**
     * A reference on the root timer
     */
    protected TimerTreeNodeObject rootTimer;

    /**
     * The reference on the active object representation of the JMXMonitoring plugin
     */
    protected ActiveObject aoObject;

    /**
     * The reference on the edit part associated to this model
     */
    protected BasicChartEditPart ep;

    /**
     * A boolean used to know if he model has changed
     */
    protected boolean hasChanged;

    /**
     * The current timer level default is basic
     */
    protected String[] currentTimerLevel = BASIC_LEVEL;

    /**
     * The constructor of the BasicChartObject.
     *
     * @param parent The parent container reference
     * @param basicTimersArray The array of incoming timers
     * @param aoObject The reference on the active object representation
     */
    public BasicChartObject(final BasicChartContainerObject parent,
        final BasicTimer[] basicTimersArray, final ActiveObject aoObject) {
        this.parent = parent;
        this.timersMap = new java.util.HashMap<Integer, TimerTreeNodeObject>();
        this.timersList = new ArrayList<TimerTreeNodeObject>();

        // Populate the map and update root
        this.updateCurrentTimersList(basicTimersArray);
        this.hasChanged = true;
        this.aoObject = aoObject;
        this.barChartBuilder = new BarChartBuilder((this.aoObject == null)
                ? "Unknown name" : this.aoObject.getName());

        this.parent.addChild(this);
    }

    /**
     * Provides the cached or created chart.<p>
     * The cached chart is referenced by the chart builder.
     *
     * @return The created or cached chart.
     */
    public final Chart provideChart() {
        if (this.hasChanged) {
            this.hasChanged = false;
            return this.barChartBuilder.createChart(this.timersList,
                this.currentTimerLevel);
        }
        return this.barChartBuilder.chart;
    }

    /**
     * Returns the inversed current timer level.
     *
     * @return The inversed current timer level
     */
    public final String getInversedTimerLevel() {
        return (this.currentTimerLevel.equals(BASIC_LEVEL) ? "Detailed"
                                                           : "Basic   ");
    }

    /**
     * Switches the current timer level and performs a snapshot
     *
     * @return The switched timer level
     */
    public final String switchTimerLevel() {
        String res;
        if (this.currentTimerLevel == BASIC_LEVEL) {
            this.currentTimerLevel = DETAILED_LEVEL;
            res = "Basic   ";
        } else {
            this.currentTimerLevel = BASIC_LEVEL;
            res = "Detailed";
        }
        this.performSnapshot();
        return res;
    }

    /**
     * Performs a snapshot on the associated active object and refreshes the
     * edit part.
     */
    public final void performSnapshot() {
        final BasicTimer[] availableTimers = BasicChartObject.performSnapshotInternal(this.aoObject,
                this.currentTimerLevel);

        // If the received collection is not null
        if (availableTimers != null) {
            // Update the current timers object collection
            this.updateCurrentTimersList(availableTimers);
            this.rootTimer.firePropertyChange(TimerTreeNodeObject.P_CHILDREN,
                null, null);
            this.hasChanged = true;
            this.ep.asyncRefresh();
        }
    }

    /**
     * Updates the current representation of timers.
     *
     * @param incomingTimersArray The array of incoming timers
     */
    public final void updateCurrentTimersList(
        final BasicTimer[] incomingTimersArray) {
        for (final BasicTimer basicTimer : incomingTimersArray) {
            TimerTreeNodeObject timerTreeNodeObject = this.timersMap.get(basicTimer.getId());

            if (timerTreeNodeObject != null) {
                // Update the timer object
                timerTreeNodeObject.updateCurrentTimerAndCompute(basicTimer);
            } else {
                // If is not root
                if (basicTimer.getParent() != null) {
                    // Retreive parent object
                    TimerTreeNodeObject parent = this.timersMap.get(basicTimer.getParent()
                                                                              .getId());
                    if (parent != null) {
                        // Create the timer object
                        timerTreeNodeObject = new TimerTreeNodeObject(basicTimer,
                                parent);
                        // Set the total timer reference
                        timerTreeNodeObject.setTotalTimerAndCompute(this.rootTimer);
                        // Add to map
                        this.timersMap.put(basicTimer.getId(),
                            timerTreeNodeObject);
                        // Add to list
                        this.timersList.add(timerTreeNodeObject);
                    }
                } else { // If root then add to map and to list
                    if (this.rootTimer == null) {
                        this.rootTimer = new TimerTreeNodeObject(basicTimer,
                                null);
                        this.rootTimer.setTotalTimerAndCompute(this.rootTimer);
                        this.timersMap.put(basicTimer.getId(), this.rootTimer);
                        this.timersList.add(this.rootTimer);
                    }
                }
            }
        }
    }

    /**
     * Returns this uniqueId of the associated active object
     *
     * @return The uniqueId of the active object
     */
    public final UniqueID getAoObjectID() {
        return this.aoObject.getUniqueID();
    }

    /**
     * Return the list of timer objects.
     *
     * @return The list of timer objects
     */
    public final List<TimerTreeNodeObject> getTimersList() {
        return timersList;
    }

    /**
     * Returns the associated active object
     *
     * @return The active object representation
     */
    public final ActiveObject getAoObject() {
        return aoObject;
    }

    /**
     * Returns the parent of this
     *
     * @return
     */
    public final BasicChartContainerObject getParent() {
        return parent;
    }

    /**
     * A setter for the parent object
     *
     * @param parent
     */
    public final void setParent(final BasicChartContainerObject parent) {
        this.parent = parent;
    }

    /**
     * A setter for the current editPart.
     *
     * @param ep The edit part
     */
    public final void setEp(final BasicChartEditPart ep) {
        this.ep = ep;
    }

    /**
     * A getter for the current editPart.
     *
     * @return ep Te current edit part
     */
    public final BasicChartEditPart getEp() {
        return this.ep;
    }

    /**
     * A getter for the root timer.
     *
     * @return The root timer
     */
    public TimerTreeNodeObject getRootTimer() {
        return rootTimer;
    }

    /**
     * Performs a snapshot on timers of a remote active object
     *
     * @param aoObject
     *            The reference on the remote active object
     * @return A list of BasicTimer
     */
    protected static final BasicTimer[] performSnapshotInternal(
        final ActiveObject aoObject, final String[] timerLevel) {
        try {
            final Object[] result = (Object[]) aoObject.getAttribute(
                    "TimersSnapshotFromBody");
            final BasicTimer[] availableTimers = (BasicTimer[]) result[0];
            final long remoteTimeStamp = (Long) result[1];

            // Here we need to stop all timers
            for (final BasicTimer t : availableTimers) {
                if (t.isStarted()) {
                    t.stop(remoteTimeStamp);
                }
            }

            // Once all timers are stopped
            // Update all user computation timers			
            CoreTimersContainer.updateAllUserComputationTimers(availableTimers);

            if ((availableTimers == null) || (availableTimers.length == 0)) {
                Console.getInstance(Activator.CONSOLE_NAME)
                       .log("There is no available timers for " +
                    aoObject.getName());
                return null;
            }
            return availableTimers;
        } catch (Exception e) {
            Console console = Console.getInstance(Activator.CONSOLE_NAME);
            String message = "Cannot perform timers snapshot on " +
                aoObject.getName() + ". ";
            if (e.getCause() instanceof javax.management.RuntimeMBeanException) {
                message += ("No available TimItTechnicalService for the virtual node : " +
                aoObject.getParent().getVirtualNodeName());
            }
            console.log(message);
        }
        return null;
    }

    /**
     * A predicate that returns true if the string val is contained in the
     * array.
     *
     * @param arr
     *            An array of strings
     * @param val
     *            A String
     * @return True if val is contained in arr
     */
    public final static boolean contains(String[] arr, String val) {
        for (String x : arr) {
            if (val.equals(x)) {
                return true;
            }
        }
        return false;
    }
}
