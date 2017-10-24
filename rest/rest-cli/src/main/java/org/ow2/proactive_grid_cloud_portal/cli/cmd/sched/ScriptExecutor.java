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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;


/**
 * @author ActiveEon Team
 * @since 24/10/2017
 */
public class ScriptExecutor {

    private static Logger logger = Logger.getLogger(ScriptExecutor.class);

    /**
     * Executes the script
     * @param scriptHandler is an object able to perform execution of a script
     * @param scriptFile is the script file
     * @param param are the script execution parameters
     * @throws InvalidScriptException if the creation fails
     */
    public ScriptResult execute(ScriptHandler scriptHandler, File scriptFile, String[] param)
            throws InvalidScriptException {
        ByteArrayOutputStream outputStream = null;
        PrintStream printStream = null;
        ScriptResult scriptResult = null;
        if (scriptFile.exists()) {
            outputStream = new ByteArrayOutputStream();
            printStream = new PrintStream(outputStream, true);
            logger.info("Executing " + scriptFile.getPath());
            scriptResult = scriptHandler.handle(new SimpleScript(scriptFile, param), printStream, printStream);
            logger.info(outputStream.toString());
            outputStream.reset();
        } else {
            logger.warn("Install package script " + scriptFile.getPath() + " not found");
        }

        return scriptResult;

    }

}
