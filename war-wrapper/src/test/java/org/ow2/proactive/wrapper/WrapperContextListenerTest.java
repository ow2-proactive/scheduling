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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.ow2.proactive.scheduler.util.WarWrapper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * Created by root on 05/04/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ WrapperSingleton.class, WarWrapper.class, WrapperContextListener.class })

public class WrapperContextListenerTest extends Mockito {

    public WrapperContextListenerTest() {
    }

    @Mock
    ServletContextEvent mockEvent;

    @Mock
    private WarWrapper mockStarter;

    @Before
    public void setup() throws Exception {
        // preparing the data by making the mock
        PowerMockito.mockStatic(WrapperSingleton.class);
        PowerMockito.when(WrapperSingleton.getInstance()).thenReturn(mockStarter);

    }

    @Test
    public void testContextInitialized() throws Exception {

        new WrapperContextListener().contextInitialized(mockEvent);

        verify(mockStarter, times(1)).launchProactiveServer();
    }
}
