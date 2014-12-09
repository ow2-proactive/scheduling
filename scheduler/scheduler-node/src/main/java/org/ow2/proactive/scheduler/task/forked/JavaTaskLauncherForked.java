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
package org.ow2.proactive.scheduler.task.forked;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.java.JavaTaskLauncher;
import org.ow2.proactive.utils.Formatter;


/**
 * JavaTaskLauncherForked
 *
 * @author The ProActive Team
 **/
public class JavaTaskLauncherForked extends JavaTaskLauncher {

    private static final long DATASPACE_CLOSE_TIMEOUT = 21 * 1000; // seconds (Christian's last wish)

    private String sharedForkerForkedDataspaceUri;

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
    public JavaTaskLauncherForked(TaskLauncherInitializer initializer) throws NodeException,
            ActiveObjectCreationException {
        super(initializer);
    }

    /**
     * Configure node to use dataspace !
     * MUST ONLY BE USED BY FORKED EXECUTABLE
     *
     */
    public void configureNode(String sharedForkerForkedDataspaceUri) {
        try {
            String scratchDir = System.getProperty("java.io.tmpdir");
            BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration((String) null,
                scratchDir);
            DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()),
                    scratchConf);
            this.nodeConfigured = true;
            this.sharedForkerForkedDataspaceUri = sharedForkerForkedDataspaceUri;
        } catch (Throwable t) {
            logger.error("Cannot configure dataSpace", t);
        }
    }

    @Override
    protected boolean isForkedTask() {
        return true;
    }

    @Override
    protected void initDataSpaces() {
        super.initDataSpaces();

        try {
            SCRATCH = PADataSpaces.resolveFile(sharedForkerForkedDataspaceUri);
            logger.debug("Override forked scratch space with forker scratch space: " + SCRATCH.getRealURI());
        } catch (Throwable t) {
            logger.error("There was a problem while initializing scratch space with uri " +
                sharedForkerForkedDataspaceUri, t);
            this.logDataspacesStatus("There was a problem while initializing scratch space with uri " +
                sharedForkerForkedDataspaceUri, DataspacesStatusLevel.ERROR);
            this.logDataspacesStatus(Formatter.stackTraceToString(t), DataspacesStatusLevel.ERROR);
        }
    }

    @ImmediateService
    public void killForkedJavaTaskLauncher() {

        try {
            executableGuard.kill(false);

        } catch (Throwable e) {
            logger.warn("Exception occurred while executing kill on task " + taskId.value(), e);
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
                future.get(DATASPACE_CLOSE_TIMEOUT, TimeUnit.MILLISECONDS);
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
    protected void finalizeTask(TaskTerminateNotification terminateNotificationStub, TaskResult res) {
        // if the task was killed, cleaning was performed by the killForkedJavaTaskLauncher call
        if (!executableGuard.wasKilled()) {
            super.finalizeTask(terminateNotificationStub, res);
        }
    }
}
