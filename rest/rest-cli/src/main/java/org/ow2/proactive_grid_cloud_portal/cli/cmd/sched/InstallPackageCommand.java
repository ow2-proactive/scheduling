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
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.web.WebProperties;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;


/**
 * @author ActiveEon Team
 * @since 13/10/2017
 */
public class InstallPackageCommand extends AbstractCommand implements Command {

    private final String packagePathName;

    private static final String scriptPath = "tools/LoadPackage.groovy";

    private static Logger logger = Logger.getLogger(InstallPackageCommand.class);

    public InstallPackageCommand(String packagePathName) {

        this.packagePathName = packagePathName;

    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        /*
         * if (!validatePackagePath()) {
         * throw new CLIException(REASON_INVALID_ARGUMENTS,
         * String.format("'%s' is not a valid Package.", packagePathName));
         * } else {
         * System.out.println("Path of the package" + packagePathName + " is OK");
         * }
         * System.out.println("1");
         * ScriptEngine engine = currentContext.getEngine();
         * System.out.println("2");
         * Writer writer = currentContext.getDevice().getWriter();
         * System.out.println("3");
         * engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("packagePathName",
         * packagePathName);
         * System.out.println("binding " +
         * engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).toString());
         * String script = FileUtility.readFileToString(new
         * File(PASchedulerProperties.getAbsolutePath(scriptPath)));
         * System.out.println("4");
         * if (script != null) {
         * System.out.println("script " + scriptPath);
         * }
         * try {
         * logger.info("Executing " + scriptPath);
         * System.out.println("Executing " + scriptPath);
         * engine.eval(script);
         * System.out.println("5");
         * } catch (ScriptException e) {
         * e.printStackTrace(new PrintWriter(writer, true));
         * }
         */

        ScriptEngine engine = currentContext.getEngine();

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os, true);
            ScriptResult scriptResult;
            File scriptFile;
            // Retrieve the script path
            if (!validatePackagePath()) {
                throw new CLIException(REASON_INVALID_ARGUMENTS,
                                       String.format("'%s' is not a valid Package.", packagePathName));
            }
            // Scripts binding
            //ScriptEngine engine = currentContext.getEngine();
            // String x = (String) engine.getContext()
            //                          .getBindings(ScriptContext.ENGINE_SCOPE)
            //                          .get("pa.scheduler.dataspace.defaultglobal.localpath");
            // String y = (String) engine.getContext()
            //        .getBindings(ScriptContext.ENGINE_SCOPE)
            //        .get("pa.scheduler.home");
            //System.out.println("pa.scheduler.dataspace.defaultglobal.localpath= " + x);
            //System.out.println("pa.scheduler.home= " + y);

            ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();
            scriptHandler.addBindings(PASchedulerProperties.getPropertiesAsHashMap());
            scriptHandler.addBindings(PAResourceManagerProperties.getPropertiesAsHashMap());
            scriptHandler.addBindings(WebProperties.getPropertiesAsHashMap());

            // Execute the script

            scriptFile = new File(PASchedulerProperties.getAbsolutePath(scriptPath));
            if (scriptFile.exists()) {
                logger.info("Executing " + scriptPath);
                String[] param = { packagePathName };
                scriptResult = scriptHandler.handle(new SimpleScript(scriptFile, param), ps, ps);
                if (scriptResult.errorOccured()) {

                    // Close streams before throwing
                    os.close();
                    ps.close();
                    throw new InvalidScriptException("Failed to execute script: " +
                                                     scriptResult.getException().getMessage(),
                                                     scriptResult.getException());
                }
                //logger.info(os.toString());
                System.out.println(os.toString());
                System.out.println("cool1");
                logger.fatal("cool3");

                os.reset();
            } else {
                logger.warn("Start script " + scriptPath + " not found");
            }

            // Close streams
            os.close();
            ps.close();
            writeLine(currentContext, "Package('%s') successfully installed in the catalog", packagePathName);
            resultStack(currentContext).push(packagePathName);
        } catch (Exception e) {
            handleError(String.format("An error occurred while attempting to install package('%s') in the catalog",
                                      packagePathName),
                        e,
                        currentContext);
            e.printStackTrace();
        }

    }

    private boolean validatePackagePath() {
        /*
         * if (!isPackagePathValid(packagePathName)) {
         * throw new CLIException(REASON_INVALID_ARGUMENTS,
         * String.format("'%s' is not a valid Package.", packagePathName));
         * }
         * 
         * if (!isPackageExisting(packagePathName)) {
         * throw new CLIException(REASON_INVALID_ARGUMENTS, String.format("'%s' does not exist.",
         * packagePathName));
         * }
         */
        try {
            File file = new File(packagePathName);
            return (file.exists() && file.isDirectory());
        } catch (Exception e) {
            return false;
        }

    }

    /*
     * private Boolean isPackageExisting(String packagePathName) {
     * File file = new File(packagePathName);
     * if (file.exists() && file.isDirectory())
     * return true;
     * return false;
     * }
     * 
     * private Boolean isPackagePathValid(String packagePathName) {
     * String regex = "^(\\/[\\w^ ]+)+\\/?$";
     * final Pattern pattern = Pattern.compile(regex);
     * final Matcher matcher = pattern.matcher(packagePathName);
     * 
     * if (matcher.find()) {
     * return true;
     * }
     * 
     * return false;
     * }
     */

}
