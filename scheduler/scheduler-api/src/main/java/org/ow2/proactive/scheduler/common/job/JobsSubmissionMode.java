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
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public class JobsSubmissionMode implements Serializable {

    private final int submittedFromJobPlanner;

    private final int submittedFromStudio;

    private final int submittedFromCatalog;

    private final int submittedFromWorkflowApi;

    private final int submittedFromSchedulerPortal;

    private final int submittedFromWorkflowExecution;

    private final int submittedFromCli;

    private final int submittedFromServiceAutomation;

    private final int submittedFromRestApi;

    private final int submittedFromEventOrchestration;

    public JobsSubmissionMode(int submittedFromJobPlanner, int submittedFromStudio, int submittedFromCatalog,
            int submittedFromWorkflowApi, int submittedFromSchedulerPortal, int submittedFromWorkflowExecution,
            int submittedFromCli, int submittedFromServiceAutomation, int submittedFromRestApi,
            int submittedFromEventOrchestration) {
        this.submittedFromJobPlanner = submittedFromJobPlanner;
        this.submittedFromStudio = submittedFromStudio;
        this.submittedFromCatalog = submittedFromCatalog;
        this.submittedFromWorkflowApi = submittedFromWorkflowApi;
        this.submittedFromSchedulerPortal = submittedFromSchedulerPortal;
        this.submittedFromWorkflowExecution = submittedFromWorkflowExecution;
        this.submittedFromCli = submittedFromCli;
        this.submittedFromServiceAutomation = submittedFromServiceAutomation;
        this.submittedFromRestApi = submittedFromRestApi;
        this.submittedFromEventOrchestration = submittedFromEventOrchestration;
    }

    public int getSubmittedFromJobPlanner() {
        return submittedFromJobPlanner;
    }

    public int getSubmittedFromStudio() {
        return submittedFromStudio;
    }

    public int getSubmittedFromCatalog() {
        return submittedFromCatalog;
    }

    public int getSubmittedFromWorkflowApi() {
        return submittedFromWorkflowApi;
    }

    public int getSubmittedFromSchedulerPortal() {
        return submittedFromSchedulerPortal;
    }

    public int getSubmittedFromWorkflowExecution() {
        return submittedFromWorkflowExecution;
    }

    public int getSubmittedFromCli() {
        return submittedFromCli;
    }

    public int getSubmittedFromServiceAutomation() {
        return submittedFromServiceAutomation;
    }

    public int getSubmittedFromRestApi() {
        return submittedFromRestApi;
    }

    public int getSubmittedFromEventOrchestration() {
        return submittedFromEventOrchestration;
    }
}
