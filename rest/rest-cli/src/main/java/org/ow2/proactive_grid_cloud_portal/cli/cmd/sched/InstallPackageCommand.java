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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;


/**
 * @author ActiveEon Team
 * @since 13/10/2017
 */
public class InstallPackageCommand extends AbstractCommand implements Command {

    private final String PACKAGE_PATH_NAME;

    private static final String SCRIPT_PATH = "tools/InstallPackage.groovy";

    private static Logger logger = Logger.getLogger(InstallPackageCommand.class);

    public InstallPackageCommand(String packagePathName) {

        this.PACKAGE_PATH_NAME = packagePathName;

    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
        try {
            //retrieve the scheduler properties
            Map<String, Object> schedulerProperties = scheduler.getSchedulerPropertiesFromSessionId(currentContext.getSessionId());
            resultStack(currentContext).push(schedulerProperties);
            logger.info("The scheduler properties are retrieved");

            // Retrieve the script path
            if (!validatePackagePath()) {
                throw new CLIException(REASON_INVALID_ARGUMENTS,
                                       String.format("'%s'does not exist or is not a valid Package path.",
                                                     PACKAGE_PATH_NAME));
            }
            // Scripts binding
            schedulerProperties.put("package.path.name", PACKAGE_PATH_NAME);
            schedulerProperties.put("session.id", currentContext.getSessionId());
            ScriptHandler scriptHandler = new ScriptHandler();
            scriptHandler.addBindings(schedulerProperties);

            // Execute the script
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os, true);
            ScriptResult scriptResult;
            File scriptFile;
            scriptFile = new File(PASchedulerProperties.getAbsolutePath(SCRIPT_PATH));
            if (scriptFile.exists()) {
                logger.info("Executing " + SCRIPT_PATH);
                String[] param = { PACKAGE_PATH_NAME };
                scriptResult = scriptHandler.handle(new SimpleScript(scriptFile, param), ps, ps);
                if (scriptResult.errorOccured()) {

                    // Close streams before throwing
                    os.close();
                    ps.close();
                    throw new InvalidScriptException("Failed to execute script: " +
                                                     scriptResult.getException().getMessage(),
                                                     scriptResult.getException());
                }
                logger.info(os.toString());
                os.reset();
            } else {
                logger.warn("Start script " + SCRIPT_PATH + " not found");
            }

            // Close streams
            os.close();
            ps.close();
            writeLine(currentContext, "Package('%s') successfully installed in the catalog", PACKAGE_PATH_NAME);
            resultStack(currentContext).push(PACKAGE_PATH_NAME);

        } catch (Exception e) {
            handleError(String.format("An error occurred while attempting to install package('%s') in the catalog",
                                      PACKAGE_PATH_NAME),
                        e,
                        currentContext);
            e.printStackTrace();
        }

    }

    private boolean validatePackagePath() {
        File file = new File(PACKAGE_PATH_NAME);
        try {
            return ((file.exists() && file.isDirectory()) || (file.exists() && file.getPath().endsWith(".zip")));
        } catch (Exception e) {
            return false;
        }

    }

}
