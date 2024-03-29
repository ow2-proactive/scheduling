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
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scripting.Script;


/**
 * @author ActiveEon Team
 * @since 23/06/2017
 */
public class CheckEligibleTaskDescriptorScript {

    private SchedulingMethodImpl schedulingMethod;

    public CheckEligibleTaskDescriptorScript(SchedulingMethodImpl schedulingMethod) {
        this.schedulingMethod = schedulingMethod;
    }

    /**
     * Check whether or not the task is asking for the api binding
     * @param etd an eligible task descriptor
     * @return true if at least one of the pre script, the post script, the flow script, the environment script or the internal script contains the api binding
     */

    public boolean isTaskContainsAPIBinding(EligibleTaskDescriptor etd, InternalJob job) {
        return isPrePostFlowEnvironmentScriptContainsApiBinding(etd, job) ||
               ((((EligibleTaskDescriptorImpl) etd).getInternal() instanceof InternalScriptTask) &&
                (isInternalScriptContainsApiBinding(etd, job)));

    }

    private boolean isInternalScriptContainsApiBinding(EligibleTaskDescriptor etd, InternalJob job) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getExecutableContainer() != null) &&
               (((EligibleTaskDescriptorImpl) etd).getInternal()
                                                  .getExecutableContainer() instanceof ScriptExecutableContainer) &&
               (((ScriptExecutableContainer) ((EligibleTaskDescriptorImpl) etd).getInternal().getExecutableContainer())
                                                                                                                       .getScript() != null) &&
               isScriptContainsApiBinding(((ScriptExecutableContainer) ((EligibleTaskDescriptorImpl) etd).getInternal()
                                                                                                         .getExecutableContainer()).getScript(),
                                          job);

    }

    private boolean isPresScriptContainsApiBinding(EligibleTaskDescriptor etd, InternalJob job) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getPreScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal().getPreScript(), job);
    }

    private boolean isPostScriptContainsApiBinding(EligibleTaskDescriptor etd, InternalJob job) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getPostScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal().getPostScript(), job);
    }

    private boolean isCleanScriptContainsApiBinding(EligibleTaskDescriptor etd, InternalJob job) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getCleaningScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal().getCleaningScript(), job);
    }

    private boolean isFlowScriptContainsApiBinding(EligibleTaskDescriptor etd, InternalJob job) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getFlowScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal().getFlowScript(), job);

    }

    private boolean isEnvironmentScriptContainsApiBinding(EligibleTaskDescriptor etd, InternalJob job) {
        return (((EligibleTaskDescriptorImpl) etd).getInternal().getForkEnvironment() != null) &&
               (((EligibleTaskDescriptorImpl) etd).getInternal().getForkEnvironment().getEnvScript() != null) &&
               isScriptContainsApiBinding(((EligibleTaskDescriptorImpl) etd).getInternal()
                                                                            .getForkEnvironment()
                                                                            .getEnvScript(),
                                          job);
    }

    private boolean isScriptContainsApiBinding(Script script, InternalJob job) {

        if (schedulingMethod != null) {
            String sessionid = schedulingMethod.getSessionid(job);
            script.setSessionid(sessionid);
            script.setOwner(job.getOwner());
        }

        String scriptContent = script.fetchScript();

        if (scriptContent == null) {
            // script could not be fetched, it may be because the script is stored in the catalog and the catalog is not started yet
            return true;
        }

        return (scriptContent != null) && (scriptContent.contains(SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME) ||
                                           scriptContent.contains(SchedulerConstants.RM_CLIENT_BINDING_NAME) ||
                                           scriptContent.contains(SchedulerConstants.DS_GLOBAL_API_BINDING_NAME) ||
                                           scriptContent.contains(SchedulerConstants.DS_USER_API_BINDING_NAME));
    }

    private boolean isPrePostFlowEnvironmentScriptContainsApiBinding(EligibleTaskDescriptor etd, InternalJob job) {
        return isPresScriptContainsApiBinding(etd, job) || isPostScriptContainsApiBinding(etd, job) ||
               isFlowScriptContainsApiBinding(etd, job) || isEnvironmentScriptContainsApiBinding(etd, job) ||
               isCleanScriptContainsApiBinding(etd, job);
    }
}
