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
package org.ow2.proactive.scheduler.common.task;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


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
        assertThat("Equals method returns not equals but instances are equal.",
                   OnTaskError.CANCEL_JOB,
                   is(OnTaskError.CANCEL_JOB));
        assertThat("Equals method returns not equals but instances are equal.",
                   OnTaskError.PAUSE_JOB,
                   is(OnTaskError.PAUSE_JOB));
        assertThat("Equals method returns not equals but instances are equal.",
                   OnTaskError.CONTINUE_JOB_EXECUTION,
                   is(OnTaskError.CONTINUE_JOB_EXECUTION));
        assertThat("Equals method returns not equals but instances are equal.",
                   OnTaskError.PAUSE_TASK,
                   is(OnTaskError.PAUSE_TASK));
        assertThat("Equals method returns not equals but instances are equal.", OnTaskError.NONE, is(OnTaskError.NONE));
        assertThat("Equals method returns equals but instances are not equal.",
                   OnTaskError.NONE.equals(null),
                   is(false));
        assertThat("Equals method returns equals but instances are not equal.",
                   OnTaskError.NONE,
                   not(OnTaskError.PAUSE_TASK));
        assertThat("Equals method returns equals but instances are not equal.", OnTaskError.NONE, not(new Object()));
    }

    @Test
    public void testHashCode() {
        assertThat("Hashcode returns not equal hashcode but instances are equal.",
                   OnTaskError.CANCEL_JOB.hashCode(),
                   is(OnTaskError.CANCEL_JOB.hashCode()));
        assertThat("Hashcode returns not equal hashcode but instances are equal.",
                   OnTaskError.PAUSE_JOB.hashCode(),
                   is(OnTaskError.PAUSE_JOB.hashCode()));
        assertThat("Hashcode returns not equal hashcode but instances are equal.",
                   OnTaskError.CONTINUE_JOB_EXECUTION.hashCode(),
                   is(OnTaskError.CONTINUE_JOB_EXECUTION.hashCode()));
        assertThat("Hashcode returns not equal hashcode but instances are equal.",
                   OnTaskError.PAUSE_TASK.hashCode(),
                   is(OnTaskError.PAUSE_TASK.hashCode()));
        assertThat("Hashcode returns not equal hashcode but instances are equal.",
                   OnTaskError.NONE.hashCode(),
                   is(OnTaskError.NONE.hashCode()));
    }

    private void testThatGetInstanceReturnsExpectedInstance(String descriptor, OnTaskError expectedInstance) {
        assertThat("getInstance method returned unexpected result.",
                   OnTaskError.getInstance(descriptor),
                   is(expectedInstance));
    }
}
