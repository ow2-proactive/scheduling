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

    //Source package which can be a local directory path or an URL
    private final String SOURCE_PACKAGE;

    private static final String REGEX_URL = "((https?://|ftp://|www\\.|[^\\s:=]+@www\\.).*?[a-zA-Z_\\/0-9\\-\\#=&])(?=(\\.|,|;|\\?|\\!)?(\"|'|«|»|\\[|\\s|\\r|\\n|$))";

    private static final String SCRIPT_PATH = "tools/LoadPackageClient.groovy";

    private static Logger logger = Logger.getLogger(InstallPackageCommand.class);

    private PackageDownloader packageDownloader;

    public InstallPackageCommand(String sourcePackagePath) throws CLIException {

        this.SOURCE_PACKAGE = sourcePackagePath;
        packageDownloader = new PackageDownloader();

    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        try {
            SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
            String packageDirPath = retrievePackagePath();
            Map<String, Object> schedulerProperties = retrieveSchedulerProperties(currentContext, scheduler);
            addSessionIdToSchedulerProperties(currentContext, schedulerProperties);
            ScriptResult scriptResult = executeScript(schedulerProperties, packageDirPath);

            if (scriptResult.errorOccured()) {
                logger.error("Failed to execute script: " + SCRIPT_PATH);
                throw new InvalidScriptException("Failed to execute script: " +
                                                 scriptResult.getException().getMessage(), scriptResult.getException());
            } else {
                writeLine(currentContext, "Package('%s') successfully installed in the catalog", SOURCE_PACKAGE);
            }

        } catch (Exception e) {
            handleError(String.format("An error occurred while attempting to install package('%s') in the catalog",
                                      SOURCE_PACKAGE),
                        e,
                        currentContext);

        }

    }

    /**
     * This method returns the package source path depending on the nature of the source location which can be a local path or an url.
     * If the source location is an url, the object packageDownloader locally downloads the package and returns its path.
     * Otherwise, it returns the given local path.
     * It throws an exception in three cases. When the given source location is not
     * <ul>
     *         <li>a directory or a zip file </li>
     *         <li>a valid Url</li>
     *         <li>a valid directory path</li>
     *     </ul>
     * @return
     */
    private String retrievePackagePath() {
        File file = new File(SOURCE_PACKAGE);
        if (!SOURCE_PACKAGE.matches(REGEX_URL)) {
            //the source package is not an Url and can be a valid local path
            if (file.exists()) {
                if (!(file.isDirectory() || file.getPath().endsWith(".zip"))) {
                    //the source package is not a directory or a zip file
                    logger.warn(SOURCE_PACKAGE + " must be a directory or a zip file.");
                    throw new CLIException(REASON_INVALID_ARGUMENTS,
                                           String.format("'%s' must be a directory or a zip file.", SOURCE_PACKAGE));
                } else {
                    //the source package is a local path
                    return SOURCE_PACKAGE;
                }
            } else {
                //the source package is neither a valid Url nor a local path
                logger.warn(SOURCE_PACKAGE + " does not exist.");
                throw new CLIException(REASON_INVALID_ARGUMENTS, String.format("'%s' does not exist.", SOURCE_PACKAGE));
            }
        } else {
            //the source package is a valid Url
            return packageDownloader.downloadPackage(SOURCE_PACKAGE);
        }

    }

    private ScriptResult executeScript(Map<String, Object> schedulerProperties, String packageDirPath)
            throws InvalidScriptException {
        ByteArrayOutputStream outputStream = null;
        PrintStream printStream = null;
        File scriptFile = new File(PASchedulerProperties.getAbsolutePath(SCRIPT_PATH));
        String[] param = { packageDirPath };
        ScriptResult scriptResult = null;
        if (scriptFile.exists()) {
            outputStream = new ByteArrayOutputStream();
            printStream = new PrintStream(outputStream, true);
            scriptResult = new SimpleScript(scriptFile, param).execute(schedulerProperties, printStream, printStream);
            logger.info(outputStream.toString());
            outputStream.reset();

        } else {
            logger.warn("Load package script " + scriptFile.getPath() + " not found");
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

    private void addSessionIdToSchedulerProperties(ApplicationContext currentContext,
            Map<String, Object> schedulerProperties) {
        schedulerProperties.put("pa.scheduler.session.id", currentContext.getSessionId());

    }

}
