/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.scheduler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


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

    public static final String SCHEDULER_DEFAULT_NAME = PASchedulerProperties.SCHEDULER_DEFAULT_NAME
            .getValueAsString();
    private static SchedulerConnection instance;

    private SchedulerConnection() {
        super(SchedulerAuthenticationInterface.class);
    }

    public Logger getLogger() {
        return ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);
    }

    public static synchronized SchedulerConnection getInstance() {
        if (instance == null) {
            instance = new SchedulerConnection();
        }
        return instance;
    }

    /**
     * Returns the {@link SchedulerAuthenticationInterface} from the specified
     * URL. If scheduler is not available or initializing throws an exception.
     * 
     * @param url the URL of the scheduler to join.
     * @return the scheduler authentication at the specified URL.
     * @throws SchedulerException
     *             thrown if the connection to the scheduler cannot be
     *             established.
     */
    public static SchedulerAuthenticationInterface join(String url) throws SchedulerException {
        try {
            return getInstance().connect(url);
        } catch (Exception e) {
            throw new SchedulerException(e);
        }
    }

    /**
     * Connects to the scheduler using given URL. The current thread will be block until
     * connection established or an error occurs.
     */
    public static SchedulerAuthenticationInterface waitAndJoin(String url) throws SchedulerException {
        return waitAndJoin(url, 0);
    }

    /**
     * Connects to the scheduler with a specified timeout value. A timeout of
     * zero is interpreted as an infinite timeout. The connection will then
     * block until established or an error occurs.
     */
    public static SchedulerAuthenticationInterface waitAndJoin(String url, int timeout)
            throws SchedulerException {
        try {
            return getInstance().waitAndConnect(url, timeout);
        } catch (Exception e) {
            throw new SchedulerException(e);
        }
    }

    /**
     * Returns default url of scheduler
     */
    public String getDefaultUrl() {
        return "//localhost/" + SCHEDULER_DEFAULT_NAME;
    }

}
