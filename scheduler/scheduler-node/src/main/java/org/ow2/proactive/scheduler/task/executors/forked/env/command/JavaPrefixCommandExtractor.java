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
package org.ow2.proactive.scheduler.task.executors.forked.env.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scripting.ForkEnvironmentScriptResult;
import org.ow2.proactive.scripting.ScriptResult;


public class JavaPrefixCommandExtractor implements Serializable {
    /**
     * Extracts a java fork prefix command from a script result.
     *
     * @param scriptResult ScriptResult object from which the fork environment command is extracted.
     * @return Java prefix command, extracted out of the fork environment script variables.
     */
    public List<String> extractJavaPrefixCommandToCommandListFromScriptResult(ScriptResult scriptResult) {
        List<String> javaPrefixCommand = new ArrayList<>();

        if (scriptResult != null && scriptResult.getResult() instanceof ForkEnvironmentScriptResult) {

            ForkEnvironmentScriptResult forkEnvResult = (ForkEnvironmentScriptResult) scriptResult.getResult();

            javaPrefixCommand.addAll(forkEnvResult.getJavaPrefixCommand());
        }
        return javaPrefixCommand;
    }
}
