/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;


/**
 *
 * Allocated nodes for specified time slot: from "acquire time" to "release time" with specified period.
 * Remove nodes preemptively if "preemptive" parameter is set to true. <br>
 * If period is zero then acquisition will be performed once. <br>
 *
 * NOTE: difference between acquire and release time have to be bigger than time required
 * to nodes all acquisition. Period have to be enough to release all nodes.
 *
 */
@ActiveObject
public class TimeSlotPolicy extends NodeSourcePolicy implements InitActive {

    private static final String TIMER_NAME = "TimeSlotPolicy Timer";

    public static final transient DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT,
                                                                                                SimpleDateFormat.FULL,
                                                                                                new Locale("en"));

    /** Timer used for tasks scheduling */
    private transient Timer timer;

    /**
     * Initial time for nodes acquisition
     */
    @Configurable(description = "Time of nodes acquisition", dynamic = true, sectionSelector = 2)
    private String acquireTime = DATE_FORMAT.format(getDate(System.currentTimeMillis()));

    /**
     * Initial time for nodes releasing
     */
    @Configurable(description = "Time of nodes removal", dynamic = true, sectionSelector = 2)
    private String releaseTime = DATE_FORMAT.format(getDate(System.currentTimeMillis() + 60 * 60 * 1000)); // 1 hour

    /**
     * Period in milliseconds between nodes acquisition/releasing iterations
     */
    @Configurable(description = "ms (1 day by default)", dynamic = true, sectionSelector = 2)
    private Long period = 24 * 60 * 60 * 1000L;

    /**
     * The way of nodes removing
     */
    @Configurable(description = "How nodes are removed", dynamic = true, checkbox = true, sectionSelector = 2)
    private boolean preemptive = true;

    /**
     * Active object stub
     */
    private TimeSlotPolicy thisStub;

    /**
     * Proactive default constructor
     */
    public TimeSlotPolicy() {
    }

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    @Override
    public BooleanWrapper configure(Object... policyParameters) {
        super.configure(policyParameters);
        configureTimeSlotParametersStartingFromIndex(2, policyParameters);
        return new BooleanWrapper(true);
    }

    @Override
    public void reconfigure(Object... updatedPolicyParameters) throws Exception {
        super.reconfigure(updatedPolicyParameters);
        this.timer.cancel();
        configureTimeSlotParametersStartingFromIndex(2, updatedPolicyParameters);
        try {
            scheduleAcquireAndReleaseTasks();
        } catch (ParseException e) {
            this.timer.cancel();
            throw new IllegalArgumentException("Time cannot be parsed", e);
        }
    }

    private void configureTimeSlotParametersStartingFromIndex(int index, Object[] policyParameters) {
        try {
            this.acquireTime = policyParameters[index++].toString();
            this.releaseTime = policyParameters[index++].toString();

            // validation of date parameters
            DATE_FORMAT.parse(this.acquireTime);
            DATE_FORMAT.parse(this.releaseTime);

            if (!policyParameters[index++].toString().isEmpty()) {
                this.period = Long.parseLong(policyParameters[index - 1].toString());

                if (this.period < 0) {
                    throw new RMException("Period cannot be less than zero");
                }

            } else {
                this.period = 0L;
            }

            this.preemptive = Boolean.parseBoolean(policyParameters[index].toString());
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    /**
     * Initializes stub to this active object
     */
    public void initActivity(Body body) {
        this.thisStub = (TimeSlotPolicy) PAActiveObject.getStubOnThis();
    }

    /**
     * Activates the policy. Schedules acquire/release tasks with specified period.
     */
    @Override
    public BooleanWrapper activate() {
        try {
            scheduleAcquireAndReleaseTasks();
        } catch (ParseException e) {
            this.timer.cancel();
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    private void scheduleAcquireAndReleaseTasks() throws ParseException {

        this.timer = new Timer(TIMER_NAME, true);

        Date dateOfAcquireTimerStart = getDateClosestToNow(this.acquireTime);
        Date dateOfReleaseTimerStart = getDateClosestToNow(this.releaseTime);

        if (this.period > 0) {
            info("Scheduling periodic nodes acquisition");
            this.timer.scheduleAtFixedRate(new AcquireTask(), dateOfAcquireTimerStart, this.period);
            this.timer.scheduleAtFixedRate(new ReleaseTask(), dateOfReleaseTimerStart, this.period);
        } else {
            info("Scheduling non periodic nodes acquisition");
            this.timer.schedule(new AcquireTask(), dateOfAcquireTimerStart);
            this.timer.schedule(new ReleaseTask(), dateOfReleaseTimerStart);
        }
    }

    private Date getDateClosestToNow(String unparsedDate) throws ParseException {

        long timeNow = System.currentTimeMillis();

        Date dateOfFirstAcquire = DATE_FORMAT.parse(unparsedDate);
        long timeOfAcquireTimerStart = dateOfFirstAcquire.getTime();

        while (timeOfAcquireTimerStart < timeNow) {
            timeOfAcquireTimerStart += this.period;
        }

        return new Date(timeOfAcquireTimerStart);
    }

    /**
     * Shutdown the policy and clears the timer.
     */
    @Override
    public void shutdown(Client initiator) {
        this.timer.cancel();
        super.shutdown(initiator);
    }

    /**
     * Converts ms to Date object
     */
    private Date getDate(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.getTime();
    }

    /**
     * Policy description for UI
     * @return policy description
     */
    @Override
    public String getDescription() {
        return "Acquires and releases nodes at specified time.";
    }

    /**
     * Policy string representation.
     */
    @Override
    public String toString() {
        return super.toString() + " [Acquire Time: " + this.acquireTime + " Release Time: " + this.releaseTime +
               " Period: " + this.period + " ms]";
    }

    /**
     * Timer task acquired all node from infrastructure
     */
    private class AcquireTask extends TimerTask {
        @Override
        public void run() {
            TimeSlotPolicy.this.thisStub.acquireAllNodes();
        }
    }

    /**
     * Timer task released all node from infrastructure
     */
    private class ReleaseTask extends TimerTask {
        @Override
        public void run() {
            TimeSlotPolicy.this.thisStub.removeAllNodes(TimeSlotPolicy.this.preemptive);
        }
    }

    @Override
    public Map<Integer, String> getSectionDescriptions() {
        Map<Integer, String> sectionDescriptions = super.getSectionDescriptions();
        sectionDescriptions.put(2, "Time Slot Configuration");
        return sectionDescriptions;
    }
}
