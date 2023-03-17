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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

public class JobsSubmissionModeData implements java.io.Serializable {

    private int submittedFromJP;

    private int submittedFromStudio;

    private int submittedFromCatalog;

    private int submittedFromWorkflowApi;

    private int submittedFromSchedulerPortal;

    private int submittedFromWorkflowExecution;

    private int submittedFromCli;

    private int submittedFromServiceAutomation;

    private int submittedFromRestApi;

    private int submittedFromEventOrchestration;

    public int getSubmittedFromJP() {
        return submittedFromJP;
    }

    public void setSubmittedFromJP(int submittedFromJP) {
        this.submittedFromJP = submittedFromJP;
    }

    public int getSubmittedFromStudio() {
        return submittedFromStudio;
    }

    public void setSubmittedFromStudio(int submittedFromStudio) {
        this.submittedFromStudio = submittedFromStudio;
    }

    public int getSubmittedFromCatalog() {
        return submittedFromCatalog;
    }

    public void setSubmittedFromCatalog(int submittedFromCatalog) {
        this.submittedFromCatalog = submittedFromCatalog;
    }

    public int getSubmittedFromWorkflowApi() {
        return submittedFromWorkflowApi;
    }

    public void setSubmittedFromWorkflowApi(int submittedFromWorkflowApi) {
        this.submittedFromWorkflowApi = submittedFromWorkflowApi;
    }

    public int getSubmittedFromSchedulerPortal() {
        return submittedFromSchedulerPortal;
    }

    public void setSubmittedFromSchedulerPortal(int submittedFromSchedulerPortal) {
        this.submittedFromSchedulerPortal = submittedFromSchedulerPortal;
    }

    public int getSubmittedFromWorkflowExecution() {
        return submittedFromWorkflowExecution;
    }

    public void setSubmittedFromWorkflowExecution(int submittedFromWorkflowExecution) {
        this.submittedFromWorkflowExecution = submittedFromWorkflowExecution;
    }

    public int getSubmittedFromCli() {
        return submittedFromCli;
    }

    public void setSubmittedFromCli(int submittedFromCli) {
        this.submittedFromCli = submittedFromCli;
    }

    public int getSubmittedFromServiceAutomation() {
        return submittedFromServiceAutomation;
    }

    public void setSubmittedFromServiceAutomation(int submittedFromServiceAutomation) {
        this.submittedFromServiceAutomation = submittedFromServiceAutomation;
    }

    public int getSubmittedFromRestApi() {
        return submittedFromRestApi;
    }

    public void setSubmittedFromRestApi(int submittedFromRestApi) {
        this.submittedFromRestApi = submittedFromRestApi;
    }

    public int getSubmittedFromEventOrchestration() {
        return submittedFromEventOrchestration;
    }

    public void setSubmittedFromEventOrchestration(int submittedFromEventOrchestration) {
        this.submittedFromEventOrchestration = submittedFromEventOrchestration;
    }
}
