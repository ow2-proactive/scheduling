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

            ForkEnvironmentScriptResult forkEnvResult =
                    (ForkEnvironmentScriptResult) scriptResult.getResult();

            javaPrefixCommand.addAll(forkEnvResult.getJavaPrefixCommand());
        }
        return javaPrefixCommand;
    }
}
