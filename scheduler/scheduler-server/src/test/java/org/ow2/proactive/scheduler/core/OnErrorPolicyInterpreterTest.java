package org.ow2.proactive.scheduler.core;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class OnErrorPolicyInterpreterTest {

    @Test
    public void testRequiresPauseTaskOnError() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_TASK);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(true));

    }

    @Test
    public void testRequiresPauseTaskOnErrorNull() {

        InternalTask task = createTask();

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseTaskOnErrorNotset() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.NONE);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseTaskOnErrorCancelJob() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.CANCEL_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseTaskOnErrorPauseJob() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseTaskOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseJobOnError() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(true));

    }

    @Test
    public void testRequiresPauseJobOnErrorNull() {

        InternalTask task = createTask();

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseJobOnErrorNotset() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.NONE);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseJobOnErrorCancelJob() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.CANCEL_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresPauseJobOnErrorPauseTask() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_TASK);

        boolean actual = new OnErrorPolicyInterpreter().requiresPauseJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresCancelJobOnError() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.CANCEL_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(true));

    }

    @Test
    public void testRequiresCancelJobOnErrorNull() {

        InternalTask task = createTask();

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresCancelJobOnErrorNotset() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.NONE);

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresCancelJobOnErrorPauseJob() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_JOB);

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(false));

    }

    @Test
    public void testRequiresCancelJobOnErrorPauseTask() {

        InternalTask task = createTask();

        task.setOnTaskError(OnTaskError.PAUSE_TASK);

        boolean actual = new OnErrorPolicyInterpreter().requiresCancelJobOnError(task);

        assertThat(actual, is(false));

    }

    private InternalTask createTask() {
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
        return task;
    }

}
