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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.executables;

import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class PropagateVariablesExec extends JavaExecutable {

    private Map<String, String> check;
    private Map<String, String> set;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        check();
        set();
        return "task_result";
    }

    private void check() throws Exception {
        if (check != null && !check.isEmpty()) {
            for (String key : check.keySet()) {
                String expected = check.get(key);
                if (expected != null) {
                    String actual = (String) getVariable(key);
                    if (actual == null || !actual.equals(expected)) {
                        throw new RuntimeException(String.format(
                                "Propagated variable error: %s, expected=%s, actual=%s", key, expected,
                                actual));
                    }
                }

            }
        }
    }

    private void set() {
        if (set != null && !set.isEmpty()) {
            for (String key : set.keySet()) {
                setVariable(key, set.get(key));
            }
        }
    }

    private Serializable getVariable(String key) {
        return getVariables().get(key);
    }

    private void setVariable(String key, Serializable value) {
        getVariables().put(key, value);
    }

}
