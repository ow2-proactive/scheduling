/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

public abstract class AbstractIModeCommand extends AbstractCommand implements
        Command {

    public AbstractIModeCommand() {
    }

    @Override
    public void execute() throws CLIException {
        ScriptEngine engine = context().getEngine();
        try {
            // load supported functions
            engine.eval(new InputStreamReader(script()));
        } catch (ScriptException error) {
            throw new CLIException(CLIException.REASON_OTHER, error);
        }

        while (!context().isTermiated()) {
            try {
                String command = readLine("> ");
                if (command == null) {
                    break; // EOF, exit interactive shell
                }
                engine.eval(command);
            } catch (ScriptException se) {
                writeLine("An error occurred while executing the script:");
                se.printStackTrace(new PrintWriter(writer(), true));
            }
        }
    }

    protected abstract InputStream script();
}
