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
package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;


/**
 * @author ActiveEon Team
 * @since 23/06/2017
 */
public class CheckEligibleTaskDescriptorScript {

    /**
     * Check weather the task is trying to bind using the scheduler api
     * @param etd an eligible task descriptor
     * @return true if one of the etd scripts is using the api
     */

    public boolean containsAPIBinding(EligibleTaskDescriptor etd) {
        if (!etd.getInternal().getPreScript().getScript().contains(SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME) &&
            !etd.getInternal().getPreScript().getScript().contains(SchedulerConstants.DS_GLOBAL_API_BINDING_NAME) &&
            !etd.getInternal().getPreScript().getScript().contains(SchedulerConstants.DS_USER_API_BINDING_NAME) &&
            !etd.getInternal().getPostScript().getScript().contains(SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME) &&
            !etd.getInternal().getPostScript().getScript().contains(SchedulerConstants.DS_GLOBAL_API_BINDING_NAME) &&
            !etd.getInternal().getPostScript().getScript().contains(SchedulerConstants.DS_USER_API_BINDING_NAME) &&
            !etd.getInternal().getFlowScript().getScript().contains(SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME) &&
            !etd.getInternal().getFlowScript().getScript().contains(SchedulerConstants.DS_GLOBAL_API_BINDING_NAME) &&
            !etd.getInternal().getFlowScript().getScript().contains(SchedulerConstants.DS_USER_API_BINDING_NAME)) {
            if (etd.getInternal() instanceof InternalScriptTask) {
                if (!((ScriptExecutableContainer) etd.getInternal().getExecutableContainer()).getScript()
                                                                                             .getScript()
                                                                                             .contains(SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME) &&
                    !((ScriptExecutableContainer) etd.getInternal().getExecutableContainer()).getScript()
                                                                                             .getScript()
                                                                                             .contains(SchedulerConstants.DS_GLOBAL_API_BINDING_NAME) &&
                    !((ScriptExecutableContainer) etd.getInternal()
                                                     .getExecutableContainer()).getScript()
                                                                               .getScript()
                                                                               .contains(SchedulerConstants.DS_USER_API_BINDING_NAME))
                    return false;
                else
                    return true;
            }
            return false;
        } else {
            return true;
        }
    }
}
