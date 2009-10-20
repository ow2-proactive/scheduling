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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler;

import java.net.URI;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAProperties;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.UserScheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;


/**
 * Object which performs the Scheduler (RM)creation,
 * and provides Scheduler's front-end active objects. :<BR>
 * -{@link SchedulerAuthentication}.<BR>
 * -{@link UserScheduler}.<BR>
 * -{@Link AdminScheduler}.<BR>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
@PublicAPI
public class SchedulerFactory {

    private static ResourceManagerProxy imp = null;
    private static boolean allowNullInit = false;

    /**
     * Creates and starts a Scheduler on the local host using the given initializer to configure it.
     * Only one Scheduler can be started by JVM.
     *
     * @param rmURL the URL of a started Resource Manager
     * @param initializer Use to configure the Scheduler before starting it.
     * 		This parameter cannot be null, if you want to start the Scheduler using JVM properties
     * 		use the {@link #} to start the Scheduler without configuration
     *
     * @return a RM authentication that allow you to administer the RM or get its connection URL.
     *
     * @throws ActiveObjectCreationException If Scheduler cannot be created
     */
    public static SchedulerAuthenticationInterface startLocal(String rmURL, SchedulerInitializer initializer)
            throws Exception {
        if (imp == null) {
            if (!allowNullInit) {
                if (initializer != null) {
                    //configure application
                    configure(initializer);
                } else {
                    throw new IllegalArgumentException("Initializer cannot be null !");
                }
            }
            if (rmURL == null || rmURL.length() == 0) {
                throw new IllegalArgumentException("RM url is null or empty !");
            }
            ResourceManagerProxy imp = ResourceManagerProxy.getProxy(new URI(rmURL));
            String policy = initializer.getPolicyFullClassName();
            //start scheduler
            AdminScheduler.createScheduler(imp, policy);
            return SchedulerConnection.waitAndJoin(null);
        } else {
            throw new SchedulerException("Scheduler already localy running");
        }
    }

    /**
     * Configure the VM to be ready to start the new Scheduler.
     *
     * @param initializer the initializer used to configured the VM.
     */
    private static void configure(SchedulerInitializer initializer) {
        //security manager
        if (System.getProperty("java.security.manager") == null) {
            System.setProperty("java.security.manager", "");
        }
        //check policy
        String s = initializer.getPolicyFullClassName();
        if (s == null) {
            throw new IllegalArgumentException("Scheduler policy is not set, cannot start Scheduler !");
        }
        //scheduler properties
        s = initializer.getSchedulerPropertiesConfiguration();
        if (s == null) {
            throw new IllegalArgumentException(
                "Scheduler properties file is not set, cannot start Scheduler !");
        }
        System.setProperty(PASchedulerProperties.PA_SCHEDULER_PROPERTIES_FILEPATH, s);
        //pa conf
        s = initializer.getProActiveConfiguration();
        if (s != null) {
            System.setProperty(PAProperties.PA_CONFIGURATION_FILE.getKey(), s);
        }
        //Scheduler home
        s = initializer.getSchedulerHomePath();
        if (s != null) {
            System.setProperty(PASchedulerProperties.SCHEDULER_HOME.getKey(), s);
        }
    }

    /**
     * Creates and starts a Scheduler on the local host.
     * This call considered that the JVM is correctly configured for starting Scheduler.
     * The "pa.scheduler.home" and required JVM properties MUST be set.
     *
     * @param rmURL the URL of a started Resource Manager
     * @param policy the full class name of the Scheduling policy to use.
     *
     * @return a Scheduler authentication that allow you to administer the Scheduler or get its connection URL.
     *
     * @throws ActiveObjectCreationException If Scheduler cannot be created
     */
    public static SchedulerAuthenticationInterface startLocal(String rmURL, String policy) throws Exception {
        SchedulerInitializer init = new SchedulerInitializer();
        init.setPolicyFullClassName(policy);
        allowNullInit = true;
        SchedulerAuthenticationInterface sai = startLocal(rmURL, init);
        allowNullInit = false;
        return sai;
    }

}
