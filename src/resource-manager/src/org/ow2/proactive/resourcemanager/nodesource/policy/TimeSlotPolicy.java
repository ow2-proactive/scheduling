/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;


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
public class TimeSlotPolicy extends NodeSourcePolicy implements InitActive {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;

	/**
     * Timer task acquired all node from infrastructure
     */
    private class AcquireTask extends TimerTask {
        @Override
        public void run() {
            synchronized (timer) {
                acquireAllNodes();
            }
        }
    }

    /**
     * Timer task released all node from infrastructure
     */
    private class ReleaseTask extends TimerTask {
        @Override
        public void run() {
            synchronized (timer) {
                // security trick
                thisStub.removeAllNodes(preemptive);
            }
        }
    }

    /** Date formatter */
    public static transient DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.SHORT, SimpleDateFormat.FULL, new Locale("en"));
    /** Timer used for tasks scheduling */
    private transient Timer timer = new Timer(true);

    /**
     * Initial time for nodes acquisition
     */
    @Configurable
    private String acquireTime = dateFormat.format(getDate(System.currentTimeMillis()));

    /**
     * Initial time for nodes releasing
     */
    @Configurable
    private String releaseTime = dateFormat
            .format(getDate(System.currentTimeMillis() + 1 * 60 * 60 * 1000 /*1 hour*/));
    /**
     * Period in milliseconds between nodes acquisition/releasing iterations
     */
    @Configurable(description = "ms (1 day by default)")
    private Long period = new Long(24 * 60 * 60 * 1000);

    /**
     * The way of nodes removing
     */
    @Configurable
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
        try {
            int index = 2;

            acquireTime = policyParameters[index++].toString();
            releaseTime = policyParameters[index++].toString();

            // validation of date parameters
            dateFormat.parse(acquireTime);
            dateFormat.parse(releaseTime);

            if (policyParameters[index++].toString().length() > 0) {
                period = Long.parseLong(policyParameters[index - 1].toString());

                if (period < 0) {
                    throw new RMException("Period cannot be less than zero");
                }

            } else {
                period = new Long(0);
            }

            preemptive = Boolean.parseBoolean(policyParameters[index++].toString());
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
        return new BooleanWrapper(true);
    }

    /**
     * Initializes stub to this active object
     */
    public void initActivity(Body body) {
        thisStub = (TimeSlotPolicy) PAActiveObject.getStubOnThis();
    }

    /**
     * Activates the policy. Schedules acquire/release tasks with specified period.
     */
    @Override
    public BooleanWrapper activate() {

        try {
            if (period > 0) {
                info("Scheduling periodic nodes acquision");
                timer.scheduleAtFixedRate(new AcquireTask(), dateFormat.parse(acquireTime), period);
                timer.scheduleAtFixedRate(new ReleaseTask(), dateFormat.parse(releaseTime), period);
            } else {
                info("Scheduling non periodic nodes acquision");
                timer.schedule(new AcquireTask(), dateFormat.parse(acquireTime));
                timer.schedule(new ReleaseTask(), dateFormat.parse(releaseTime));
            }
        } catch (ParseException e) {
            return new BooleanWrapper(false);
        }

        return new BooleanWrapper(true);
    }

    /**
     * Shutdown the policy and clears the timer.
     */
    @Override
    public void shutdown(Client initiator) {
        synchronized (timer) {
            timer.cancel();
        }
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
        return NamesConvertor.beautifyName(this.getClass().getSimpleName()) + " [Acquire Time: " +
            acquireTime + " Release Time: " + releaseTime + " Period: " + period + " ms]";
    }
}
