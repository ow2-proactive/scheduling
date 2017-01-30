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
package org.ow2.proactive.scheduler.common.job.factories.spi;

import java.io.File;

import org.ow2.proactive.scheduler.common.exception.JobValidationException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;


/**
 * Interface used by Job Validators
 */
public interface JobValidatorService {

    /**
     * Validate the provided xml job file, before the job is parsed by the scheduler
     *
     * If this validator does not validate against the xml file, leave the implementation
     * empty.
     *
     * Example of use: xml schema validation.
     *
     * @param jobFile xml job file to validate
     * @throws JobValidationException if the job is not valid
     */
    void validateJob(File jobFile) throws JobValidationException;

    /**
     * Validate a job object after the job has been parsed by the scheduler.
     *
     * Example of use: variable model validation.
     *
     * @param job job object to validate
     * @throws JobValidationException if the job is not valid
     */
    void validateJob(TaskFlowJob job) throws JobValidationException;

}
