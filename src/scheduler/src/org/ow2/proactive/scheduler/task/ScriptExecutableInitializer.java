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

import java.util.Map;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.ExecutableInitializer;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher.OneShotDecrypter;
import org.ow2.proactive.scripting.TaskScript;


/**
 * ScriptExecutableInitializer is the class used to store context of script executable initialization
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
public class ScriptExecutableInitializer implements ExecutableInitializer {

    /** Decrypter from launcher */
    private OneShotDecrypter decrypter = null;

    private TaskScript script;
    private Map<String, DataSpacesFileObject> dataspaceBindings;

    public void setScript(TaskScript script) {
        this.script = script;
    }

    public TaskScript getScript() {
        return script;
    }

    @Override
    public void setDecrypter(OneShotDecrypter decrypter) {
        this.decrypter = decrypter;
    }

    @Override
    public OneShotDecrypter getDecrypter() {
        return decrypter;
    }

    public void setDataspaceBindings(Map<String, DataSpacesFileObject> dataspaceBindings) {
        this.dataspaceBindings = dataspaceBindings;
    }

    public Map<String, DataSpacesFileObject> getDataspaceBindings() {
        return dataspaceBindings;
    }
}
