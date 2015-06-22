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
package org.ow2.proactive.scheduler.task.script;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scripting.TaskScript;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


/**
 * This class is a container for forked Script executable. The actual executable is instantiated on the worker node
 * .<br>
 * In this case an other JVM is started from the current one, and the task itself is deployed on the new JVM.<br>
 * As a consequence, we keep control on the forked JVM, can kill the process or give a new brand environment to the user
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ForkedScriptExecutableContainer extends ScriptExecutableContainer {

    public static final Logger logger = Logger.getLogger(ForkedScriptExecutableContainer.class);

    /** Environment of a new dedicated JVM */
    protected ForkEnvironment forkEnvironment = new ForkEnvironment();

    public ForkedScriptExecutableContainer(TaskScript script) {
        super(script);
    }

    public ForkedScriptExecutableContainer(TaskScript script, String workingDir) {
        this(script);
        this.forkEnvironment = new ForkEnvironment();
        this.forkEnvironment.setWorkingDir(workingDir);
    }

    public ForkedScriptExecutableContainer(TaskScript script, ForkEnvironment forkEnvironment) {
        super(script);
        this.forkEnvironment = forkEnvironment;
    }

    public String getWorkingDir() {
        return forkEnvironment.getWorkingDir();
    }

    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
    }

}
