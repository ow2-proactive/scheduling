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
import org.ow2.proactive.scheduler.common.SchedulerConstants;


/**
 * An object representing a job submission
 *
 * it contains a job id if the submission was successful or
 * the description of an error whenever an error occurred
 */
@PublicAPI
public class JobIdDataAndError implements Serializable {

    /**
     * id of the job if submission was successful or -1 if not
     */
    private long id;

    /**
     * Name of the job if submission was successful or "NOT SET" if not
     */
    private String readableName;

    /**
     * Error message, if the submission did not succeed
     */
    private String errorMessage;

    /**
     * complete error stack trace if the submission did not succeed
     */
    private String stackTrace;

    /**
     * true if the submission did not succeed
     */
    private boolean isError;

    /**
     * true if the submission did not succeed because the workflow content could not be retrieved
     */
    private boolean isFetchError;

    public JobIdDataAndError() {

    }

    public JobIdDataAndError(String errorMessage, String stackTrace) {
        this(-1L, SchedulerConstants.JOB_DEFAULT_NAME, errorMessage, stackTrace, false);
    }

    public JobIdDataAndError(String errorMessage, String stackTrace, boolean isFetchError) {
        this(-1L, SchedulerConstants.JOB_DEFAULT_NAME, errorMessage, stackTrace, isFetchError);
    }

    public JobIdDataAndError(long id, String readableName) {
        this(id, readableName, null, null, false);
    }

    public JobIdDataAndError(long id, String readableName, String errorMessage, String stackTrace) {
        this(id, readableName, errorMessage, stackTrace, false);
    }

    public JobIdDataAndError(long id, String readableName, String errorMessage, String stackTrace,
            boolean isFetchError) {
        this.id = id;
        this.readableName = readableName;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
        this.isFetchError = isFetchError;
        if (errorMessage != null || stackTrace != null) {
            this.isError = true;
        } else {
            this.isError = false;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReadableName() {
        return readableName;
    }

    public void setReadableName(String readableName) {
        this.readableName = readableName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public boolean isFetchError() {
        return isFetchError;
    }

    public void setFetchError(boolean fetchError) {
        isFetchError = fetchError;
    }
}
