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
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
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

    private static final String SCRIPT_PATH = "tools/LoadPackage.groovy";

    private static Logger logger = Logger.getLogger(InstallPackageCommand.class);

    public InstallPackageCommand(String packagePathName) throws CLIException {

        this.PACKAGE_PATH_NAME = packagePathName;

    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
        ScriptResult scriptResult;
        Map<String, Object> schedulerProperties;
        try {
            validatePackagePath();

            schedulerProperties = retrieveSchedulerProperties(currentContext, scheduler);

            scriptResult = executeScript(schedulerProperties);

            if (scriptResult.errorOccured()) {
                logger.error("Failed to execute script: " + SCRIPT_PATH);
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

    private ScriptResult executeScript(Map<String, Object> schedulerProperties) throws InvalidScriptException {
        ByteArrayOutputStream outputStream = null;
        PrintStream printStream = null;
        File scriptFile = new File(PASchedulerProperties.getAbsolutePath(SCRIPT_PATH));
        String[] param = { PACKAGE_PATH_NAME };
        ScriptResult scriptResult = null;
        if (scriptFile.exists()) {
            outputStream = new ByteArrayOutputStream();
            printStream = new PrintStream(outputStream, true);
            scriptResult = new SimpleScript(scriptFile, param).execute(schedulerProperties, printStream, printStream);
            logger.info(outputStream.toString());
            outputStream.reset();

        } else {
            logger.warn("Install package script " + scriptFile.getPath() + " not found");
        }

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

        return scriptResult;

    }

    private Map<String, Object> retrieveSchedulerProperties(ApplicationContext currentContext,
            SchedulerRestInterface scheduler) throws PermissionRestException, NotConnectedRestException {
        return scheduler.getSchedulerPropertiesFromSessionId(currentContext.getSessionId());
    }
}
