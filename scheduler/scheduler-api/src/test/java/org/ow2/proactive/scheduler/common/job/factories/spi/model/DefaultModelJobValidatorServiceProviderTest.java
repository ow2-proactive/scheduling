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
package org.ow2.proactive.scheduler.common.job.factories.spi.model;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.JobValidationException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.factory.BooleanParserValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ModelValidator;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;


public class DefaultModelJobValidatorServiceProviderTest {

    DefaultModelJobValidatorServiceProvider factory;

    @Before
    public void before() {
        factory = new DefaultModelJobValidatorServiceProvider();
    }

    @Test
    public void testValidateJobWithJobModelVariableOK() throws UserException, JobValidationException {
        factory.validateJob(createJobWithJobModelVariable("true",
                                                          ModelValidator.PREFIX + BooleanParserValidator.BOOLEAN_TYPE));
    }

    @Test(expected = JobValidationException.class)
    public void testValidateJobWithJobModelVariableKO() throws UserException, JobValidationException {
        factory.validateJob(createJobWithJobModelVariable("blabla",
                                                          ModelValidator.PREFIX + BooleanParserValidator.BOOLEAN_TYPE));
    }

    @Test
    public void testValidateJobWithJobModelVariableEmptyModel() throws UserException, JobValidationException {
        factory.validateJob(createJobWithJobModelVariable("blabla", "  "));
    }

    @Test
    public void testValidateJobWithJobModelVariableUnknownModel() throws UserException, JobValidationException {
        factory.validateJob(createJobWithJobModelVariable("blabla", "UNKNOWN"));
    }

    @Test(expected = JobValidationException.class)
    public void testValidateJobWithJobModelVariableValidPrefixButUnknownModel()
            throws UserException, JobValidationException {
        factory.validateJob(createJobWithJobModelVariable("blabla", ModelValidator.PREFIX + "UNKNOWN"));
    }

    @Test
    public void testValidateJobWithTaskModelVariableOK() throws UserException, JobValidationException {
        factory.validateJob(createJobWithTaskModelVariable("true",
                                                           ModelValidator.PREFIX +
                                                                   BooleanParserValidator.BOOLEAN_TYPE));
    }

    @Test(expected = JobValidationException.class)
    public void testValidateJobWithTaskModelVariableKO() throws UserException, JobValidationException {
        factory.validateJob(createJobWithTaskModelVariable("blabla",
                                                           ModelValidator.PREFIX +
                                                                     BooleanParserValidator.BOOLEAN_TYPE));
    }

    @Test
    public void testValidateJobWithTaskModelVariableEmptyModel() throws UserException, JobValidationException {
        factory.validateJob(createJobWithTaskModelVariable("blabla", "  "));
    }

    @Test
    public void testValidateJobWithTaskModelVariableUnknownModel() throws UserException, JobValidationException {
        factory.validateJob(createJobWithTaskModelVariable("blabla", "UNKNOWN"));
    }

    @Test(expected = JobValidationException.class)
    public void testValidateJobWithTaskModelVariableValidPrefixButUnknownModel()
            throws UserException, JobValidationException {
        factory.validateJob(createJobWithTaskModelVariable("blabla", ModelValidator.PREFIX + "UNKNOWN"));
    }

    private TaskFlowJob createJobWithJobModelVariable(String value, String model) throws UserException {
        TaskFlowJob job = new TaskFlowJob();
        JobVariable jobVariable = new JobVariable("VAR", value, model);
        job.setVariables(Collections.singletonMap(jobVariable.getName(), jobVariable));
        return job;
    }

    private TaskFlowJob createJobWithTaskModelVariable(String value, String model) throws UserException {
        TaskFlowJob job = new TaskFlowJob();
        TaskVariable variable = new TaskVariable("VAR", value, model, false);
        Task task = new ScriptTask();
        task.setName("ModelTask");
        task.setVariables(Collections.singletonMap(variable.getName(), variable));
        job.addTask(task);
        return job;
    }

}
