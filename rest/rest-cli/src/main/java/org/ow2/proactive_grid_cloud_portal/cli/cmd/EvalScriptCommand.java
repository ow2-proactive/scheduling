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

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;


public class EvalScriptCommand extends AbstractCommand implements Command {

    private String scriptPathname;

    private String scriptArgs;

    public EvalScriptCommand(String scriptPathname, String scriptArgs) {
        this.scriptPathname = scriptPathname;
        this.scriptArgs = scriptArgs;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        ScriptEngine engine = currentContext.getEngine();
        Writer writer = currentContext.getDevice().getWriter();
        if (scriptArgs != null) {
            engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).putAll(bindings(scriptArgs));
        }
        String script = FileUtility.readFileToString(new File(scriptPathname));
        try {
            engine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace(new PrintWriter(writer, true));
        }
    }

    private Map<String, String> bindings(String bindingString) {
        Map<String, String> bindings = new HashMap<>();
        String[] pairs = bindingString.split(",");
        for (String pair : pairs) {
            String[] nameValue = pair.split("=");
            bindings.put(nameValue[0], nameValue[1]);
        }
        return bindings;
    }

}
