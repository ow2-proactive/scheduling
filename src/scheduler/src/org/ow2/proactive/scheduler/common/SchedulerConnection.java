/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;


/**
 * <b>Start here</b>, it provides a method to join an existing scheduler.<br>
 * The method {@link #join(String)} returns a {@link SchedulerAuthenticationInterface} in order to give the scheduler
 * the possibility to authenticate user that wants to connect a scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class SchedulerConnection extends Connection<SchedulerAuthenticationInterface> {

    /**  */
    private static final long serialVersionUID = 20;
    /** Instance of the connection */
    private static SchedulerConnection instance;

    /**
     * Create a new instance of SchedulerConnection.
     *
     */
    private SchedulerConnection() {
        super(SchedulerAuthenticationInterface.class);
    }

    /**
     * @see org.ow2.proactive.authentication.Loggable#getLogger()
     */
    public Logger getLogger() {
        return ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);
    }

    /**
     * Get the instance of the Scheduler connection
     *
     * @return the instance of the Scheduler connection
     */
    public static synchronized SchedulerConnection getInstance() {
        if (instance == null) {
            instance = new SchedulerConnection();
        }
        return instance;
    }

    /**
     * Returns the {@link SchedulerAuthenticationInterface} from the specified
     * URL. If scheduler is not available or initializing throws an exception.<br >
     * Note that you can use the provided {@link SchedulerAuthenticationGUIHelper} class to display a
     * graphical interface that will ask the URL, login and password.
     *
     * @param url the URL of the scheduler to join.
     * @return the scheduler authentication at the specified URL.
     * @throws SchedulerException
     *             thrown if the connection to the scheduler cannot be
     *             established.
     */
    public static SchedulerAuthenticationInterface join(String url) throws SchedulerException {
        try {
            return getInstance().connect(normalizeScheduler(url));
        } catch (Exception e) {
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * Connects to the scheduler using given URL. The current thread will be block until
     * connection established or an error occurs.<br>
     * Note that you can use the provided {@link SchedulerAuthenticationGUIHelper} class to display a
     * graphical interface that will ask the URL, login and password.
     *
     * @return the interface to be authenticate by the Scheduler
     */
    public static SchedulerAuthenticationInterface waitAndJoin(String url) throws SchedulerException {
        return waitAndJoin(url, 0);
    }

    /**
     * Connects to the scheduler with a specified timeout value. A timeout of
     * zero is interpreted as an infinite timeout. The connection will then
     * block until established or an error occurs.<br>
     * Note that you can use the provided {@link SchedulerAuthenticationGUIHelper} class to display a
     * graphical interface that will ask the URL, login and password.
     *
     * @return the interface to be authenticate by the Scheduler
     */
    public static SchedulerAuthenticationInterface waitAndJoin(String url, long timeout)
            throws SchedulerException {
        try {
            return getInstance().waitAndConnect(normalizeScheduler(url), timeout);
        } catch (Exception e) {
            throw new SchedulerException(e);
        }
    }

    /**
     * Normalize the URL of the SCHEDULER.<br>
     *
     * @param url, the URL to normalize.
     * @return  //localhost/SCHEDULER_NAME if the given url is null.<br>
     *          the given URL if it terminates by the SCHEDULER_NAME<br>
     *          the given URL with /SCHEDULER_NAME appended if URL does not end with /<br>
     *          the given URL with SCHEDULER_NAME appended if URL does end with /<br>
     *          the given URL with SCHEDULER_NAME appended if URL does not end with SCHEDULER_NAME
     */
    private static String normalizeScheduler(String url) {
        url = Connection.normalize(url);
        String SCHEDULER_DEFAULT_NAME = SchedulerConstants.SCHEDULER_DEFAULT_NAME;
        if (!url.endsWith(SCHEDULER_DEFAULT_NAME)) {
            url += SCHEDULER_DEFAULT_NAME;
        }
        return url;
    }

}
