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
package org.ow2.proactive.wrapper;

import javax.servlet.ServletContextEvent;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.util.WarWrapper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ WarWrapper.class, WrapperContextListener.class })
public class WrapperContextListenerTest extends Mockito {

    public WrapperContextListenerTest() {
    }

    @Mock
    ServletContextEvent mockEvent;

    private static WarWrapper mockStarter;

    @BeforeClass
    public static void setup() throws Exception {
        mockStarter = PowerMockito.mock(WarWrapper.class);
        Whitebox.setInternalState(WarWrapper.class, "INSTANCE", mockStarter);
    }

    @Test
    public void testContextInitialized() throws Exception {

        new WrapperContextListener().contextInitialized(mockEvent);

        verify(mockStarter, times(1)).launchProactive();
    }

    @Test
    public void testcontextDestroyed() throws Exception {

        new WrapperContextListener().contextDestroyed(mockEvent);

        verify(mockStarter, times(1)).shutdownProactive();
    }
}
