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
package functionaltests;

import java.io.File;

import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * Evaluate a selection script.
 * <p/>
 * A nonzero exit code indicates an abnormal termination. In particular, {@code 2} means that an error
 * occurred with the creation of the script object representation while {@code 1} indicates that an error
 * occurred during the evaluation of the script. If an exception is raised, the stacktrace is printed on the
 * standard error output.
 */
public class SelectionScriptEvaluator {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java " + SelectionScriptEvaluator.class.getName() + " SELECTION_SCRIPT_FILE");
            System.exit(1);
        }

        evaluateScript(args[0]);
    }

    private static void evaluateScript(String selectionScript) {
        try {
            final ScriptHandler localHandler = ScriptLoader.createLocalHandler();

            final ScriptResult<Boolean> result = localHandler.handle(new SelectionScript(new File(selectionScript),
                                                                                         null));

            if (result.errorOccured()) {
                result.getException().printStackTrace(System.err);
                System.exit(3);
            }
        } catch (InvalidScriptException e) {
            System.exit(2);
        }
    }

}
