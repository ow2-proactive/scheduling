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
package org.ow2.proactive.scheduler.task.containers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.util.ByteArrayWrapper;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.TaskScript;


/**
 * Reuse the Java executable container as part of a forked task, specialized for a script execution.
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
public class ScriptExecutableContainer extends ExecutableContainer {

    /** Arguments of the task as a map */
    protected final Map<String, ByteArrayWrapper> serializedArguments = new HashMap<>();

    private final TaskScript script;

    public ScriptExecutableContainer(TaskScript script) {
        this.script = script;
    }

    public Script<Serializable> getScript() {
        return script;
    }

    public Map<String, ByteArrayWrapper> getSerializedArguments() {
        return serializedArguments;
    }

}
