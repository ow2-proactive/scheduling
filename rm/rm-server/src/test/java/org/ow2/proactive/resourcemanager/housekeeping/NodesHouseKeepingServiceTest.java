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
package org.ow2.proactive.resourcemanager.housekeeping;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import it.sauronsoftware.cron4j.Scheduler;


@RunWith(MockitoJUnitRunner.class)
public class NodesHouseKeepingServiceTest {

    @InjectMocks
    @Spy
    private NodesHouseKeepingService nodesHouseKeepingService;

    @Mock
    private Scheduler nodesHouseKeepingScheduler;

    @Before
    public void setup() {
        when(this.nodesHouseKeepingService.createOrGetCronScheduler()).thenReturn(this.nodesHouseKeepingScheduler);
    }

    @Test
    public void testNodesHouseKeepingServiceStartsCronSchedule() {
        this.nodesHouseKeepingService.start();

        verify(this.nodesHouseKeepingScheduler).schedule(anyString(), any(Runnable.class));
        verify(this.nodesHouseKeepingScheduler).start();
    }

    @Test
    public void testNodesHouseKeepingServiceStopsCronSchedule() {
        this.nodesHouseKeepingService.start();
        this.nodesHouseKeepingService.stop();

        verify(this.nodesHouseKeepingScheduler).deschedule(anyString());
        verify(this.nodesHouseKeepingScheduler).stop();
    }

    @Test
    public void testNodesAreRemoved() {

    }

}
