/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.TaskScript;
import org.apache.log4j.Logger;


@XmlAccessorType(XmlAccessType.FIELD)
public class ScriptExecutableContainer extends ExecutableContainer {

    public static final Logger logger = Logger.getLogger(ScriptExecutableContainer.class);

    private TaskScript script;

    /** Hibernate default constructor */
    public ScriptExecutableContainer() {
    }

    /**
     * Create a new container for a script executable.
     *
     * @param command the command to be executed.
     * @param generated the script that generates the command (can be null).
     */
    public ScriptExecutableContainer(TaskScript script) {
        this.script = script;
    }

    /**
     * @see ExecutableContainer#getExecutable()
     */
    @Override
    public Executable getExecutable() throws ExecutableCreationException {
        return new ScriptExecutable();
    }

    /**
     * Copy constructor
     *
     * @param cont original object to copy
     */
    public ScriptExecutableContainer(ScriptExecutableContainer cont) throws ExecutableCreationException {
        try {
            this.script = new TaskScript(cont.getScript());
        } catch (InvalidScriptException e) {
            throw new ExecutableCreationException("Could not copy script", e);
        }
    }

    /**
     * @see ExecutableContainer#init(ExecutableContainerInitializer)
     */
    @Override
    public void init(ExecutableContainerInitializer initializer) {
        // Nothing to do for now...
    }

    /**
     * @see ExecutableContainer#createExecutableInitializer()
     */
    @Override
    public ScriptExecutableInitializer createExecutableInitializer() {
        ScriptExecutableInitializer nei = new ScriptExecutableInitializer();
        nei.setScript(script);
        return nei;
    }

    public Script getScript() {
        return script;
    }
}
