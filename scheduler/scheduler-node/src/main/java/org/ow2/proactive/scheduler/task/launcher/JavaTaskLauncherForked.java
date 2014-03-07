/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.launcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * JavaTaskLauncherForked
 *
 * @author The ProActive Team
 **/
public class JavaTaskLauncherForked extends JavaTaskLauncher {

    /**
     * empty no arg constructor used for ProActive
     */
    public JavaTaskLauncherForked() {

    }

    /**
     * Constructor of the java task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize this task launcher.
     */
    public JavaTaskLauncherForked(TaskLauncherInitializer initializer) throws NodeException, ActiveObjectCreationException {
        super(initializer);
    }


    /**
     * Configure node to use dataspace !
     * MUST ONLY BE USED BY FORKED EXECUTABLE
     *
     * @return true if configuration went right
     */
    public String configureNode() {
        BaseScratchSpaceConfiguration scratchConf = null;
        try {
            // configure node for Data Spaces
            String scratchDir;
            if (System.getProperty(NODE_DATASPACE_SCRATCHDIR) == null) {
                //if scratch dir java property is not set, set to default
                scratchDir = System.getProperty("java.io.tmpdir");
            } else {
                //else use the property
                scratchDir = System.getProperty(NODE_DATASPACE_SCRATCHDIR);
            }
            scratchConf = new BaseScratchSpaceConfiguration((String) null,
                    scratchDir);
            DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()),
                    scratchConf);
            nodeConfigured = true;
        } catch (Throwable t) {
            logger.error("Cannot configure dataSpace", t);
        }
        return scratchConf.getPath();
    }

    @ImmediateService
    public void killForkedJavaTaskLauncher() {

        try {
            executableGuard.kill(false);

        } catch (Throwable e) {
            logger.warn("Exception occurred while executing kill on task " + taskId.value(), e);
        } finally {
            // clean ths guard, this
            try {
                killChildrenProcesses();
            } catch (Throwable t) {
                logger.warn("Exception when killing children processes", t);
            }
        }

        // clean ths guard, this will kill processes, close node configuration etc

        cleanForkedJavaTaskLauncher();
    }

    @ImmediateService
    public void cleanForkedJavaTaskLauncher() {

        executableGuard.clean(TaskLauncher.CLEAN_TIMEOUT);

        try {
            closeNodeConfiguration();
        } catch (Throwable t) {
            logger.warn("Exception when closing node configuration", t);
        }
        PAActiveObject.terminateActiveObject(stubOnThis, true);
    }

    /**
     * Close node dataspace configuration !
     * MUST ONLY BE USED BY FORKED EXECUTABLE
     *
     * @return true if configuration went right
     */
    @ImmediateService
    public boolean closeNodeConfiguration() {
        if (nodeConfigured) {
            // using an executorTransfer service to timeout the data spaces deactivation
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<Boolean> task = new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    DataSpacesNodes.closeNodeConfig(PAActiveObject.getActiveObjectNode(stubOnThis));
                    return true;
                }
            };
            Future<Boolean> future = executor.submit(task);
            try {
                future.get(DataSpaceNodeConfigurationAgent.DATASPACE_CLOSE_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                logger.error("Cannot close properly DataSpaces.", t);
                return false;
            } finally {
                future.cancel(true);
                executor.shutdown();
            }
        }
        return true;
    }

    @Override
    protected void finalizeTask(TaskTerminateNotification core, TaskResult res) {
        // if the task was killed, cleaning was performed by the killForkedJavaTaskLauncher call
         if (!executableGuard.wasKilled()) {
             super.finalizeTask(core, res);
         }
    }
}
