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
package org.ow2.proactive.scheduler.common;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;


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
        return Logger.getLogger(SchedulerConnection.class);
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
     * @throws ConnectionException if connection is not possible on the given URL.
     */
    public static SchedulerAuthenticationInterface join(String url) throws ConnectionException {
        try {
            return getInstance().connect(normalizeScheduler(url));
        } catch (Exception e) {
            throw new ConnectionException("Failed to connect to " + url, e);
        }
    }

    /**
     * Connects to the scheduler using given URL. The current thread will be block until
     * connection established or an error occurs.<br>
     * Note that you can use the provided {@link SchedulerAuthenticationGUIHelper} class to display a
     * graphical interface that will ask the URL, login and password.
     *
     * @param url the URL of the scheduler to join.
     * @return the interface to be authenticate by the Scheduler
     * @throws ConnectionException if connection is not possible on the given URL.
     */
    public static SchedulerAuthenticationInterface waitAndJoin(String url) throws ConnectionException {
        return waitAndJoin(url, 0);
    }

    /**
     * Connects to the scheduler with a specified timeout value. A timeout of
     * zero is interpreted as an infinite timeout. The connection will then
     * block until established or an error occurs.<br>
     * Note that you can use the provided {@link SchedulerAuthenticationGUIHelper} class to display a
     * graphical interface that will ask the URL, login and password.
     *
     * @param url the URL of the scheduler to join.
     * @param timeout the amount of time during which it will try to join the Scheduler
     * @return the interface to be authenticate by the Scheduler
     */
    public static SchedulerAuthenticationInterface waitAndJoin(String url, long timeout) throws ConnectionException {
        try {
            return getInstance().waitAndConnect(normalizeScheduler(url), timeout);
        } catch (Exception e) {
            throw new ConnectionException(e);
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
