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
package org.objectweb.proactive.extensions.calcium.environment;

import java.net.URI;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.calcium.environment.multithreaded.MultiThreadedEnvironment;
import org.objectweb.proactive.extensions.calcium.environment.proactive.ProActiveEnvironment;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.calcium.environment.proactivescheduler.ProActiveSchedulerEnvironment;


/**
 * This class provides a Factory for the instantiation of the execution environments.
 * 
 * These methods should be used by programmers to instantiate an environment instead of
 * the object constructors.
 * 
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class EnvironmentFactory {

    /**
     * Creates a new Environment using a thread pool
     *  
     * @param maxThreads Maximum number of threads to be used.
     */
    static public Environment newMultiThreadedEnvironment(int maxThreads) {

        return MultiThreadedEnvironment.factory(maxThreads);
    }

    /**
     * Creates an Environment using the specified deployment descriptor.
     * The descriptor must satisfy a contract with the following variables:
     *
     *  <pre>
     *         &lt;variables&gt;
              &lt;descriptorVariable name="SKELETON_FRAMEWORK_VN" value="framework" /&gt
                 &lt;descriptorVariable name="INTERPRETERS_VN" value="interpreters" /&gt
            &lt;/variables&gt
            </pre>
     *
     *
     * The variable <code>SKELETON_FRAMEWORK_VN</code> specifies the virtual node that will be used to store the
     * main active objects, such as taskpool, file server, etc.
     *
     * The <code>INTERPRETERS_VN</code> variable specifies the virtual node that will be used to execute the computation.
     *
     * And optionally with:<![CDATA[ <programDefaultVariable name="MAX_CINTERPRETERS" value="3"/> ]]>.
     *
     * @param descriptor The local descriptor path.
     * @throws ProActiveException If an error is detected.
     */
    static public Environment newProActiveEnvironment(String descriptor) throws ProActiveException {

        return ProActiveEnvironment.factory(descriptor, false);
    }

    /**
     * Creates a new ProActive Environment using GCM Deployment mechanism.
     * 
     * @param descriptor The path to the deployment file
     * @return A new ProActive Environment
     */
    static public Environment newProActiveEnviromentWithGCMDeployment(String descriptor)
            throws ProActiveException {

        return ProActiveEnvironment.factory(descriptor, true);
    }
}