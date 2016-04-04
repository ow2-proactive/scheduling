package org.ow2.proactive.scheduler.common.task;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OnTaskErrorTest {

    @Test
    public void testGetInstanceAndToString() throws Exception {
        testThatGetInstanceReturnsExpectedInstance(OnTaskError.CANCEL_JOB.toString(), OnTaskError.CANCEL_JOB);
        testThatGetInstanceReturnsExpectedInstance(OnTaskError.PAUSE_JOB.toString(), OnTaskError.PAUSE_JOB);
        testThatGetInstanceReturnsExpectedInstance(OnTaskError.CONTINUE_JOB_EXECUTION.toString(),
                OnTaskError.CONTINUE_JOB_EXECUTION);
        testThatGetInstanceReturnsExpectedInstance(OnTaskError.PAUSE_TASK.toString(), OnTaskError.PAUSE_TASK);
        testThatGetInstanceReturnsExpectedInstance(OnTaskError.NONE.toString(), OnTaskError.NONE);
        testThatGetInstanceReturnsExpectedInstance("arbitrary", OnTaskError.NONE);
    }

    @Test
    public void testEquals() {
        assertThat("Equals method returns not equals but instances are equal.", OnTaskError.CANCEL_JOB,
                is(OnTaskError.CANCEL_JOB));
        assertThat("Equals method returns not equals but instances are equal.", OnTaskError.PAUSE_JOB,
                is(OnTaskError.PAUSE_JOB));
        assertThat("Equals method returns not equals but instances are equal.",
                OnTaskError.CONTINUE_JOB_EXECUTION, is(OnTaskError.CONTINUE_JOB_EXECUTION));
        assertThat("Equals method returns not equals but instances are equal.", OnTaskError.PAUSE_TASK,
                is(OnTaskError.PAUSE_TASK));
        assertThat("Equals method returns not equals but instances are equal.", OnTaskError.NONE,
                is(OnTaskError.NONE));
        assertThat("Equals method returns equals but instances are not equal.", OnTaskError.NONE.equals(null),
                is(false));
        assertThat("Equals method returns equals but instances are not equal.", OnTaskError.NONE,
                not(OnTaskError.PAUSE_TASK));
        assertThat("Equals method returns equals but instances are not equal.", OnTaskError.NONE,
                not(new Object()));
    }

    @Test
    public void testHashCode() {
        assertThat("Hashcode returns not equal hashcode but instances are equal.", OnTaskError.CANCEL_JOB.hashCode(),
                is(OnTaskError.CANCEL_JOB.hashCode()));
        assertThat("Hashcode returns not equal hashcode but instances are equal.", OnTaskError.PAUSE_JOB.hashCode(),
                is(OnTaskError.PAUSE_JOB.hashCode()));
        assertThat("Hashcode returns not equal hashcode but instances are equal.", OnTaskError.CONTINUE_JOB_EXECUTION.hashCode(),
                is(OnTaskError.CONTINUE_JOB_EXECUTION.hashCode()));
        assertThat("Hashcode returns not equal hashcode but instances are equal.", OnTaskError.PAUSE_TASK.hashCode(),
                is(OnTaskError.PAUSE_TASK.hashCode()));
        assertThat("Hashcode returns not equal hashcode but instances are equal.", OnTaskError.NONE.hashCode(),
                is(OnTaskError.NONE.hashCode()));
    }

    private void testThatGetInstanceReturnsExpectedInstance(String descriptor, OnTaskError expectedInstance) {
        assertThat("getInstance method returned unexpected result.", OnTaskError.getInstance(descriptor),
                is(expectedInstance));
    }
}