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
package org.ow2.proactive.scheduler.common.job.factories;

import static org.ow2.proactive.scheduler.common.util.VariableSubstitutor.filterAndUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;


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
        ArrayList<Task> tasks = job.getTasks();
        for (Task task : tasks) {
            Map<String, String> variables = task.getVariablesOverriden(job);
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

    private static void resolveScript(Script<?> script, Map<String, String> variables) {
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
