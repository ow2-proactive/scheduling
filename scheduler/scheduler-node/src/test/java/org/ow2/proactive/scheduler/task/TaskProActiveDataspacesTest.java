package org.ow2.proactive.scheduler.task;

import java.util.Collections;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.data.TaskProActiveDataspaces;
import org.junit.Test;


public class TaskProActiveDataspacesTest {

    @Test(expected = Exception.class)
    public void exception_is_thrown() throws Exception {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);

        TaskProActiveDataspaces dataspaces = new TaskProActiveDataspaces(
          TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false), null);

        dataspaces.copyInputDataToScratch(Collections.<InputSelector>emptyList());
    }
}