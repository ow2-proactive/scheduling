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
package org.ow2.proactive.scheduler.task.executors;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.security.KeyException;

import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedJvmTaskExecutionCommandCreator;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.ClasspathUtils;

import com.google.common.base.StandardSystemProperty;


public class ForkedProcessBuilderCreator implements Serializable {
    private final ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator = new ForkedJvmTaskExecutionCommandCreator();

    private final TaskContextVariableExtractor taskContextVariableExtractor = new TaskContextVariableExtractor();

    private final ForkEnvironmentScriptExecutor forkEnvironmentScriptExecutor = new ForkEnvironmentScriptExecutor();

    private final transient ForkerUtils forkerUtils;

    public ForkedProcessBuilderCreator() {
        forkerUtils = ForkerUtils.getInstance();
    }

    /**
     * Creates a process builder for a given task context.
     *
     * @param context           The task context to execute.
     * @param serializedContext The task context saved to disk.
     * @param outputSink        Standard output sink.
     * @param errorSink         Error sink.
     * @param workingDir        The working directory to execute the process in.
     * @return Returns a process builder, ready to execute.
     * @throws Exception
     */
    public OSProcessBuilder createForkedProcessBuilder(TaskContext context, File serializedContext,
            PrintStream outputSink, PrintStream errorSink, File workingDir) throws Exception {

        String nativeScriptPath = context.getSchedulerHome();

        OSProcessBuilder processBuilder = getOsProcessBuilder(context, workingDir, nativeScriptPath);

        ScriptResult forkEnvironmentScriptResult = executeForkEnvironmentScriptAndExtractVariables(context,
                                                                                                   outputSink,
                                                                                                   errorSink,
                                                                                                   processBuilder);

        processBuilder.command()
                      .addAll(forkedJvmTaskExecutionCommandCreator.createForkedJvmTaskExecutionCommand(context,
                                                                                                       forkEnvironmentScriptResult,
                                                                                                       serializedContext.getAbsolutePath(),
                                                                                                       processBuilder));

        processBuilder = processBuilder.directory(workingDir);
        return processBuilder;
    }

    private ScriptResult executeForkEnvironmentScriptAndExtractVariables(TaskContext context, PrintStream outputSink,
            PrintStream errorSink, OSProcessBuilder processBuilder) throws Exception {
        ScriptResult forkEnvironmentScriptResult = null;

        ForkEnvironment forkEnvironment = context.getInitializer().getForkEnvironment();
        if (forkEnvironment != null) {

            if (forkEnvironment.getEnvScript() != null) {

                if (!context.getInitializer().isAuthorizedForkEnvironmentScript()) {
                    throw new SecurityException("Unauthorized fork environment script: " +
                                                System.getProperty("line.separator") +
                                                forkEnvironment.getEnvScript().fetchScript());
                }

                forkEnvironmentScriptResult = forkEnvironmentScriptExecutor.executeForkEnvironmentScript(context,
                                                                                                         outputSink,
                                                                                                         errorSink);
            }

            try {
                processBuilder.environment()
                              .putAll(
                                      // replace variables in defined system environment values
                                      // by existing environment variables, variables and credentials
                                      taskContextVariableExtractor.extractVariablesThirdPartyCredentialsAndSystemEnvironmentVariables(context));
            } catch (IllegalArgumentException processEnvironmentReadOnly) {
                throw new IllegalStateException("Cannot use runAsMe mode and set system environment properties",
                                                processEnvironmentReadOnly);
            }
        }
        return forkEnvironmentScriptResult;
    }

    private OSProcessBuilder getOsProcessBuilder(TaskContext context, File workingDir, String nativeScriptPath)
            throws IOException, IllegalAccessException, KeyException {
        OSProcessBuilder processBuilder;
        if (context.isRunAsUser()) {
            addExecutionPermissionToScripts();
            forkerUtils.setSharedExecutablePermissions(workingDir);
            processBuilder = forkerUtils.getOSProcessBuilderFactory(nativeScriptPath)
                                        .getBuilder(forkerUtils.checkConfigAndGetUser(context));
        } else {
            processBuilder = forkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder();
        }
        return processBuilder;
    }

    private void addExecutionPermissionToScripts() throws IOException {
        //Add execution permissions to scripts except for windows OS
        if (!StandardSystemProperty.OS_NAME.value().toLowerCase().contains("windows")) {
            String schedulerHome = ClasspathUtils.findSchedulerHome();
            File scriptsDirectory = new File(schedulerHome, "dist/scripts/processbuilder/linux");
            if (scriptsDirectory != null) {
                for (File script : scriptsDirectory.listFiles()) {
                    script.setExecutable(true, false);
                }
            } else {
                throw new IOException("The directory that contains scripts required for the runAsMe mode does not exist");
            }
        }
    }
}
