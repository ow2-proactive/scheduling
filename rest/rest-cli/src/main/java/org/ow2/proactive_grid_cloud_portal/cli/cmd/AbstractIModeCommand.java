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
package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;


public abstract class AbstractIModeCommand extends AbstractCommand implements Command {

    public static final String IMODE = "org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractIModeCommand.imode";

    public static final String TERMINATE = "org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractIModeCommand.terminate";

    public AbstractIModeCommand() {
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        currentContext.setProperty(IMODE, true);
        ScriptEngine engine = currentContext.getEngine();
        try {
            // load supported functions
            engine.eval(new InputStreamReader(script()));
        } catch (ScriptException error) {
            throw new CLIException(CLIException.REASON_OTHER, error);
        }

        while (!currentContext.getProperty(TERMINATE, Boolean.TYPE, false)) {
            try {
                String command = readLine(currentContext, "> ");
                if (command == null) {
                    break; // EOF, exit interactive shell
                }
                engine.eval(command);
            } catch (ScriptException se) {
                handleError(String.format("An error occurred while executing the script:"), se, currentContext);
            }
        }
    }

    protected abstract InputStream script();
}
