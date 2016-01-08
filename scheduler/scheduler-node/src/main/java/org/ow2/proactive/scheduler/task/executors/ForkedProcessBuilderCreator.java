/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.scheduler.task.executors;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedJvmTaskExecutionCommandCreator;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.ow2.proactive.scripting.ScriptResult;

public class ForkedProcessBuilderCreator extends ForkEnvironmentScriptExecutor implements Serializable {
    private static final Set<PosixFilePermission> SHARED_FOLDER_PERMISSIONS = PosixFilePermissions.fromString(
            "rwxrwxrwx");
    private final ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator = new ForkedJvmTaskExecutionCommandCreator();
    private final TaskContextVariableExtractor taskContextVariableExtractor = new TaskContextVariableExtractor();
    private final ForkEnvironmentScriptExecutor forkEnvironmentScriptExecutor = new ForkEnvironmentScriptExecutor();

    public OSProcessBuilder createForkedProcessBuilder(TaskContext context, File serializedContext,
            PrintStream outputSink, PrintStream errorSink, File workingDir) throws Exception {
        OSProcessBuilder processBuilder;

        String nativeScriptPath = context.getSchedulerHome();
        ScriptResult forkEnvironmentScriptResult = null;

        if (context.isRunAsUser()) {
            shareWorkingDirWithRunAsMeUser(workingDir);
            processBuilder = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder(
                    ForkerUtils.checkConfigAndGetUser(context.getDecrypter()));
        } else {
            processBuilder = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder();
        }

        ForkEnvironment forkEnvironment = context.getInitializer().getForkEnvironment();

        if (forkEnvironment != null) {

            if (forkEnvironment.getEnvScript() != null) {
                forkEnvironmentScriptResult = forkEnvironmentScriptExecutor
                        .executeForkEnvironmentScript(context, outputSink, errorSink);
            }

            try {
                processBuilder.environment().putAll(
                        // replace variables in defined system environment values
                        // by existing environment variables, variables and credentials
                        taskContextVariableExtractor
                                .extractVariablesThirdPartyCredentialsAndSystemEnvironmentVariables(context));
            } catch (IllegalArgumentException processEnvironmentReadOnly) {
                throw new IllegalStateException(
                        "Cannot use runAsMe mode and set system environment properties",
                        processEnvironmentReadOnly);
            }
        }

        processBuilder
                .command()
                .addAll(forkedJvmTaskExecutionCommandCreator
                        .createForkedJvmTaskExecutionCommand(context,
                                forkEnvironmentScriptResult, serializedContext.getAbsolutePath()
                        ));

        processBuilder = processBuilder.directory(workingDir);
        return processBuilder;
    }


    private void shareWorkingDirWithRunAsMeUser(File workingDir) throws IOException {
        try {
            Files.setPosixFilePermissions(workingDir.toPath(), SHARED_FOLDER_PERMISSIONS);
        } catch (IOException e) {
            throw new IOException("Working directory will not be writable by runAsMe user", e);
        } catch (UnsupportedOperationException ignored) {
            // ignored, could be running on Windows
        }
    }
}
