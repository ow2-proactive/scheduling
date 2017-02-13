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
package org.ow2.proactive.scheduler.task.utils.task.termination;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;


public class CleanupTimeoutGetterTest {

    private static final long CLEANUP_TIME_DEFAULT_SECONDS = 10;

    private static final String CLEANUP_TIME_PROPERTY_NAME = RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME;

    @Test
    public void testThatDefaultTimeoutIsReturnedIfNoPropertyIsSet() {
        testThatReturnedTimeoutIs(CLEANUP_TIME_DEFAULT_SECONDS);
    }

    @Test
    public void testThatDefaultTimeoutIsReturnedIfPropertyIsSetToGarbage() {
        System.setProperty(CLEANUP_TIME_PROPERTY_NAME, "bahasd3342");
        testThatReturnedTimeoutIs(CLEANUP_TIME_DEFAULT_SECONDS);
        System.clearProperty(CLEANUP_TIME_PROPERTY_NAME);
    }

    @Test
    public void testThatCorrectValueIsReturnedIfProperyIsSet() {
        System.setProperty(CLEANUP_TIME_PROPERTY_NAME, "15");
        testThatReturnedTimeoutIs(15L);
        System.clearProperty(CLEANUP_TIME_PROPERTY_NAME);
    }

    private void testThatReturnedTimeoutIs(long expectedTimeout) {
        CleanupTimeoutGetter cleanupTimeoutGetter = new CleanupTimeoutGetter();
        assertThat(cleanupTimeoutGetter.getCleanupTimeSeconds(), is(expectedTimeout));
    }

}
