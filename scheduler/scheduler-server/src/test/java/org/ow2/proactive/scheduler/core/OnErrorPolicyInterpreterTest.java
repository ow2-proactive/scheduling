package org.ow2.proactive.scheduler.core;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class OnErrorPolicyInterpreterTest {

    @Test
    public void testRequiresPauseTaskOnError() {

        InternalJob job = new InternalJob() {

            @Override
            public JobType getType() {
                // TODO Auto-generated method stub
                return null;
            }
        };

        job.setOnTaskError(OnTaskError.PAUSE_TASK);
        InternalTask task = new InternalTask() {

            @Override
            public boolean handleResultsArguments() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public TaskLauncher createLauncher(InternalJob job, Node node)
                    throws ActiveObjectCreationException, NodeException {
                // TODO Auto-generated method stub
                return null;
            }
        };
        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(job, task);

        assertThat(actual, is(true));

    }

}
