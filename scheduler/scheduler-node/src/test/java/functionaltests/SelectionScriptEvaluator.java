/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
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
            System.err.println(
                    "Usage: java " + SelectionScriptEvaluator.class.getName() + " SELECTION_SCRIPT_FILE");
            System.exit(1);
        }

        evaluateScript(args[0]);
    }

    private static void evaluateScript(String selectionScript) {
        try {
            final ScriptHandler localHandler = ScriptLoader.createLocalHandler();

            final ScriptResult<Boolean> result = localHandler.handle(
                    new SelectionScript(new File(selectionScript), null));

            if (result.errorOccured()) {
                result.getException().printStackTrace(System.err);
                System.exit(3);
            }
        } catch (InvalidScriptException e) {
            System.exit(2);
        }
    }

}