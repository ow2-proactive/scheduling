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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;


/**
 * @author ActiveEon Team
 * @since 13/10/2017
 */
public class InstallPackageCommand extends AbstractCommand implements Command {

    private final String PACKAGE_PATH_NAME;

    private static final String SCRIPT_PATH = "tools/InstallPackage.groovy";

    private static Logger logger = Logger.getLogger(InstallPackageCommand.class);

    // A script executor is an object able to perform execution of a script.
    private ScriptExecutor scriptExecutor;

    public InstallPackageCommand(String packagePathName) throws CLIException {

        this.PACKAGE_PATH_NAME = packagePathName;
        this.scriptExecutor = new ScriptExecutor();

    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
        ByteArrayOutputStream outputStream = null;
        PrintStream printStream = null;
        ScriptResult scriptResult;
        try {
            validatePackagePath();

            Map<String, Object> schedulerProperties = retrieveSchedulerProperties(currentContext, scheduler);

            Map<String, Object> propertiesWithSessionIdAndPackage = addSessionIdAndPackageNameToProperties(currentContext,
                                                                                                           schedulerProperties);
            ScriptHandler scriptHandler = createScriptHandlerAndAddBindings(propertiesWithSessionIdAndPackage);

            // Execute the script
            File scriptFile = new File(PASchedulerProperties.getAbsolutePath(SCRIPT_PATH));
            String[] param = { PACKAGE_PATH_NAME };
            scriptResult = scriptExecutor.execute(scriptHandler, scriptFile, param);

            if (scriptResult.errorOccured()) {
                logger.warn("Failed to execute script: " + SCRIPT_PATH);
                throw new InvalidScriptException("Failed to execute script: " +
                                                 scriptResult.getException().getMessage(), scriptResult.getException());
            } else {
                writeLine(currentContext, "Package('%s') successfully installed in the catalog", PACKAGE_PATH_NAME);
            }
        } catch (Exception e) {
            handleError(String.format("An error occurred while attempting to install package('%s') in the catalog",
                                      PACKAGE_PATH_NAME),
                        e,
                        currentContext);

        } finally {
            // Close streams
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (printStream != null) {
                printStream.close();
            }
        }

    }

    private void validatePackagePath() {
        File file = new File(PACKAGE_PATH_NAME);
        if (file.exists()) {
            if (!(file.isDirectory() || file.getPath().endsWith(".zip"))) {
                logger.warn(PACKAGE_PATH_NAME + " must be a directory or a zip file.");
                throw new CLIException(REASON_INVALID_ARGUMENTS,
                                       String.format("'%s' must be a directory or a zip file.", PACKAGE_PATH_NAME));
            }
        } else {
            logger.warn(PACKAGE_PATH_NAME + " is not a valid package.");
            throw new CLIException(REASON_INVALID_ARGUMENTS,
                                   String.format("'%s' is not a valid package.", PACKAGE_PATH_NAME));

        }

    }

    private ScriptHandler createScriptHandlerAndAddBindings(Map<String, Object> schedulerProperties) {
        ScriptHandler scriptHandler = new ScriptHandler();
        scriptHandler.addBindings(schedulerProperties);
        return scriptHandler;
    }

    private Map<String, Object> retrieveSchedulerProperties(ApplicationContext currentContext,
            SchedulerRestInterface scheduler) throws PermissionRestException, NotConnectedRestException {
        return scheduler.getSchedulerPropertiesFromSessionId(currentContext.getSessionId());
    }

    private Map<String, Object> addSessionIdAndPackageNameToProperties(ApplicationContext currentContext,
            Map<String, Object> schedulerProperties) {
        Map<String, Object> propertiesWithSessionIdAndPackage = new HashMap<>(schedulerProperties);
        propertiesWithSessionIdAndPackage.put("package.path.name", PACKAGE_PATH_NAME);
        propertiesWithSessionIdAndPackage.put("session.id", currentContext.getSessionId());
        return propertiesWithSessionIdAndPackage;
    }
}
