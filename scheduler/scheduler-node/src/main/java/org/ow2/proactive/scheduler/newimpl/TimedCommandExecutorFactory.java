/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  * Tobias Wiens
 */
package org.ow2.proactive.scheduler.newimpl;

import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;

import java.io.File;


public class TimedCommandExecutorFactory {

    public TimedCommandExecutor createTimedCommandExecutor(TaskContext context, File workingDir)
            throws Exception {
        return new PBCommandExecutor(this.getOsProcessBuilder(context, workingDir, context.getDecrypter()));
    }

    private boolean isRunAsUser(TaskContext context) {
        return context.getExecutableContainer().isRunAsUser();
    }

    private OSProcessBuilder getOsProcessBuilder(TaskContext context, File workingDir, Decrypter decrypter)
            throws Exception {
        OSProcessBuilder pb;
        String nativeScriptPath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString(); // TODO Maybe use PA_HOME ?
        if (isRunAsUser(context)) {
            boolean workingDirCanBeWrittenByForked = workingDir.setWritable(true);
            if (!workingDirCanBeWrittenByForked) {
                throw new Exception("Working directory will not be writable by runAsMe user");
            }
            pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder(
                    ForkerUtils.checkConfigAndGetUser(decrypter));
        } else {
            pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder();
        }
        return pb;
    }
}
