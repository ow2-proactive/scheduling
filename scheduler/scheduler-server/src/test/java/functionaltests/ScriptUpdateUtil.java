/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package functionaltests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;

import static org.ow2.proactive.scheduler.common.util.VariableSubstitutor.filterAndUpdate;


/**
 * Utility class which facilitates the filtering of scripts in a Job object.
 */
public class ScriptUpdateUtil {

    private ScriptUpdateUtil() {
    }

    /**
     * Filters the scripts in the specified job.
     */
    public static TaskFlowJob resolveScripts(TaskFlowJob job) {
        Map<String, String> variables = job.getVariables();
        ArrayList<Task> tasks = job.getTasks();
        for (Task task : tasks) {
            List<SelectionScript> selectionScripts = task.getSelectionScripts();
            if (selectionScripts != null) {
                for (SelectionScript sscript : selectionScripts) {
                    resolveScript(sscript, variables);
                }
            }
            resolveScript(task.getFlowScript(), variables);
            resolveScript(task.getPreScript(), variables);
            resolveScript(task.getPostScript(), variables);
            resolveScript(task.getCleaningScript(), variables);
        }
        return job;
    }

    private static void resolveScript(Script script, Map variables) {
        if (script != null) {
            script.setScript(filterAndUpdate(script.getScript(), variables));
            Serializable[] parameters = script.getParameters();
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = filterAndUpdate(parameters[i].toString(), variables);
                }
            }
        }
    }

}
