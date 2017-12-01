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
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scripting.Script;


/**
 * @author ActiveEon Team
 * @since 23/06/2017
 */
public class CheckEligibleTaskDescriptorScript {

    /**
     * Check whether or not the task is asking for the api binding
     * @param etd an eligible task descriptor
     * @return true if at least one of the pre script, the post script, the flow script, the environment script or the internal script contains the api binding
     */

    public boolean isTaskContainsAPIBinding(EligibleTaskDescriptor etd) {
        return isPrePostFlowEnvironmentScriptContainsApiBinding(etd) ||
               ((((EligibleTaskDescriptorImpl) etd).getInternal() instanceof InternalScriptTask) &&
                (isInternalScriptContainsApiBinding(etd)));

    }

    private boolean isInternalScriptContainsApiBinding(EligibleTaskDescriptor etd) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getExecutableContainer() != null) &&
               (((EligibleTaskDescriptorImpl) etd).getInternal()
                                                  .getExecutableContainer() instanceof ScriptExecutableContainer) &&
               (((ScriptExecutableContainer) ((EligibleTaskDescriptorImpl) etd).getInternal().getExecutableContainer())
                                                                                                                       .getScript() != null) &&
               isScriptContainsApiBinding(((ScriptExecutableContainer) ((EligibleTaskDescriptorImpl) etd).getInternal()
                                                                                                         .getExecutableContainer()).getScript());

    }

    private boolean isPresScriptContainsApiBinding(EligibleTaskDescriptor etd) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getPreScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal().getPreScript());
    }

    private boolean isPostScriptContainsApiBinding(EligibleTaskDescriptor etd) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getPostScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal().getPostScript());
    }

    private boolean isCleanScriptContainsApiBinding(EligibleTaskDescriptor etd) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getCleaningScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal().getCleaningScript());
    }

    private boolean isFlowScriptContainsApiBinding(EligibleTaskDescriptor etd) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getFlowScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal().getFlowScript());

    }

    private boolean isEnvironmentScriptContainsApiBinding(EligibleTaskDescriptor etd) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getForkEnvironment() != null) &&
               (((EligibleTaskDescriptorImpl) etd).getInternal().getForkEnvironment().getEnvScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal()
                                                                            .getForkEnvironment()
                                                                            .getEnvScript());
    }

    private boolean isScriptContainsApiBinding(Script script) {
        return script.getScript().contains(SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME) ||
               script.getScript().contains(SchedulerConstants.DS_GLOBAL_API_BINDING_NAME) ||
               script.getScript().contains(SchedulerConstants.DS_USER_API_BINDING_NAME);
    }

    private boolean isPrePostFlowEnvironmentScriptContainsApiBinding(EligibleTaskDescriptor etd) {
        return isPresScriptContainsApiBinding(etd) || isPostScriptContainsApiBinding(etd) ||
               isFlowScriptContainsApiBinding(etd) || isEnvironmentScriptContainsApiBinding(etd) ||
               isCleanScriptContainsApiBinding(etd);
    }
}
