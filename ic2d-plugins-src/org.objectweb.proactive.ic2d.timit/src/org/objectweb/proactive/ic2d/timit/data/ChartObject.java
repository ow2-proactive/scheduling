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
import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.editparts.ChartEditPart;


/**
 * This class represents the model of a chart.
 *
 * @author vbodnart
 *
 */
public class ChartObject {
    public static final boolean DEBUG = false;
    public static final String[] BASIC_LEVEL = new String[] {
            "Total", "Serve", "SendRequest", "SendReply", "WaitByNecessity",
            "WaitForRequest"
        };
    public static final String[] DETAILED_LEVEL = new String[] {
            "Total", "Serve", "SendRequest", "SendReply", "WaitByNecessity",
            "WaitForRequest", "LocalCopy", "BeforeSerialization",
            "Serialization", "AfterSerialization", "GroupOneWayCall",
            "GroupAsyncCall"
        };
    protected BarChartBuilder barChartBuilder;
    protected ChartContainerObject parent;
    protected Map<String, TimerObject> timersMap;
    protected List<TimerObject> timersList;
    protected TimerObject rootTimer;
    protected ActiveObject aoObject;
    protected ChartEditPart ep;
    protected boolean hasChanged;
    protected String[] currentTimerLevel = BASIC_LEVEL;

    public TimerObject getRootTimer() {
        return rootTimer;
    }

    public void setRootTimer(TimerObject rootTimer) {
        this.rootTimer = rootTimer;
    }

    public ChartObject(final ChartContainerObject parent,
        final List<BasicTimer> basicTimersList, final ActiveObject aoObject) {
        this.parent = parent;
        this.timersMap = new java.util.HashMap<String, TimerObject>();
        this.timersList = new ArrayList<TimerObject>();

        // Populate the map and update root
        this.updateCurrentTimersList(basicTimersList);
        this.hasChanged = true;
        this.aoObject = aoObject;
        this.barChartBuilder = new BarChartBuilder((this.aoObject == null)
                ? "Unknown name" : this.aoObject.getName());

        this.parent.addChild(this);
    }

    /**
     * Provides the cached or created chart.
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

    public final String getInversedTimerLevel() {
        return (this.currentTimerLevel.equals(BASIC_LEVEL) ? "Detailed"
                                                           : "Basic   ");
    }

    public final String switchTimerLevel() {
        String res;
        if (this.currentTimerLevel == BASIC_LEVEL) {
            this.currentTimerLevel = DETAILED_LEVEL;
            res = "Basic   ";
        } else {
            this.currentTimerLevel = BASIC_LEVEL;
            res = "Detailed";
        }
        this.performSnapshot(true);
        return res;
    }

    /**
     * Performs a snapshot on the associated active object and refreshes the
     * edit part.
     */
    public final void performSnapshot() {
        this.performSnapshot(false);
    }

    /**
     * Performs a snapshot on the associated active object and refreshes the
     * edit part.
     */
    public final void performSnapshot(final boolean updateLevel) {
        List<BasicTimer> availableTimersList = ChartObject.performSnapshotInternal(this.aoObject,
                this.currentTimerLevel);

        // If the received collection is not null
        if (availableTimersList != null) {
            // Update the current timers object collection
            updateCurrentTimersList(availableTimersList);
            // Iterate through all timers to fire changes
            for (final TimerObject t : this.timersList) {
                // If update level is asked then check if the timers name is
                // filtered
                if (updateLevel && !t.currentTimer.isUserLevel()) {
                    t.setViewed(contains(this.currentTimerLevel,
                            t.currentTimer.getName()));
                }
            }
            this.rootTimer.firePropertyChange(TimerObject.P_CHILDREN, null, null);
            this.hasChanged = true;
            this.ep.asyncRefresh();
        }
    }

    public final void updateCurrentTimersList(final List<BasicTimer> list) {
        for (final BasicTimer basicTimer : list) {
            TimerObject timerObject = this.timersMap.get(basicTimer.getName());

            if (timerObject != null) {
                // Update the timer object
                timerObject.updateCurrentTimerAndCompute(basicTimer);
            } else {
                // If is not root
                if (basicTimer.getParent() != null) {
                    // Retreive parent object
                    TimerObject parent = this.timersMap.get(basicTimer.getParent()
                                                                      .getName());
                    if (parent != null) {
                        // Create the timer object
                        timerObject = new TimerObject(basicTimer, parent);
                        // Set the total timer reference
                        timerObject.setTotalTimerAndCompute(this.rootTimer);
                        // Add to map
                        this.timersMap.put(basicTimer.getName(), timerObject);
                        // Add to list
                        this.timersList.add(timerObject);
                    }
                } else { // If root then add to map and to list
                    if (this.rootTimer == null) {
                        this.rootTimer = new TimerObject(basicTimer, null);
                        this.rootTimer.setTotalTimerAndCompute(this.rootTimer);
                        this.timersMap.put(basicTimer.getName(), this.rootTimer);
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
    public final List<TimerObject> getTimersList() {
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
    public final ChartContainerObject getParent() {
        return parent;
    }

    /**
     * A setter for the parent object
     *
     * @param parent
     */
    public final void setParent(final ChartContainerObject parent) {
        this.parent = parent;
    }

    /**
     * A getter for hashChanged
     *
     * @return hashChanged value
     */
    public final boolean getHasChanged() {
        return hasChanged;
    }

    /**
     * A setter for hasChanged
     *
     * @param hasChanged
     */
    public final void setHasChanged(final boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    /**
     * A setter for the current editPart
     *
     * @param ep
     */
    public final void setEp(final ChartEditPart ep) {
        this.ep = ep;
    }

    /**
     * A getter for the current editPart
     *
     * @return ep
     */
    public final ChartEditPart getEp() {
        return this.ep;
    }

    /**
     * Performs a snapshot on timers of a remote active object
     *
     * @param aoObject
     *            The reference on the remote active object
     * @return A list of BasicTimer
     */
    protected static final List<BasicTimer> performSnapshotInternal(
        final ActiveObject aoObject, final String[] timerLevel) {
        try {
            //            Spy spy = ((NodeObject) aoObject.getParent()).getSpy();
            Object[] result = (Object[]) aoObject.invoke("getTimersSnapshotFromBody",
                    new Object[] { timerLevel },
                    new String[] { String[].class.getName() });

            //                    timerLevel);
            List<BasicTimer> availableTimersList = (List<BasicTimer>) result[0];
            long remoteTimeStamp = (Long) result[1];

            // Here we need to stop all timers
            for (BasicTimer t : availableTimersList) {
                if (t.isStarted()) {
                    t.stop(remoteTimeStamp);
                }
            }
            if ((availableTimersList == null) ||
                    (availableTimersList.size() == 0)) {
                Console.getInstance(Activator.CONSOLE_NAME)
                       .log("There is no available timers for " +
                    aoObject.getName());
                return null;
            }
            return availableTimersList;
        } catch (Exception e) {
            Console console = Console.getInstance(Activator.CONSOLE_NAME);
            console.log("Cannot perform timers snapshot on " +
                aoObject.getName() + ". Reason : " + e.getMessage());
            if (e instanceof NullPointerException) {
                console.log(
                    "Be sure to attach a TimIt technical service to the virtual node : " +
                    aoObject.getParent().getParent().getName());
            }

            // e.printStackTrace();
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
    private final static boolean contains(String[] arr, String val) {
        for (String x : arr) {
            if (val.equals(x)) {
                return true;
            }
        }
        return false;
    }
}
