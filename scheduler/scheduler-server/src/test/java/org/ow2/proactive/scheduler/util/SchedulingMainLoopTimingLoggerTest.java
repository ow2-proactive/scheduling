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
package org.ow2.proactive.scheduler.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


public class SchedulingMainLoopTimingLoggerTest {

    SchedulingMainLoopTimingLogger schedulingMainLoopTimingLogger;

    ArgumentCaptor<String> loggingCaptor = ArgumentCaptor.forClass(String.class);

    @Mock
    private Logger logger;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.schedulingMainLoopTimingLogger = new SchedulingMainLoopTimingLogger(logger);

    }

    @Test
    public void testNoStart() {
        schedulingMainLoopTimingLogger.end("method1");
        schedulingMainLoopTimingLogger.printTimingsINFOLevel();
        verify(logger, Mockito.times(0)).debug("");

    }

    @Test
    public void testOneMethod() throws InterruptedException {
        schedulingMainLoopTimingLogger.start("method1");
        Thread.sleep(100);
        schedulingMainLoopTimingLogger.end("method1");
        schedulingMainLoopTimingLogger.printTimingsINFOLevel();
        verify(logger).info(loggingCaptor.capture());
        String loggingString = loggingCaptor.getAllValues().get(0);
        assertTimingPerMethod(loggingString, "method1");

    }

    @Test
    public void testOneMethodManyCalls() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            schedulingMainLoopTimingLogger.start("method1");
            Thread.sleep(10 * i);
            schedulingMainLoopTimingLogger.end("method1");
        }

        schedulingMainLoopTimingLogger.printTimingsINFOLevel();
        verify(logger).info(loggingCaptor.capture());
        String loggingString = loggingCaptor.getAllValues().get(0);
        assertTimingPerMethod(loggingString, "method1");

    }

    @Test
    public void test3MethodsManyCalls() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            schedulingMainLoopTimingLogger.start("method_long_long_long_one1");
            Thread.sleep(10 * i);
            schedulingMainLoopTimingLogger.end("method_long_long_long_one1");
        }
        for (int i = 0; i < 3; i++) {
            schedulingMainLoopTimingLogger.start("method2");
            Thread.sleep(10 * i);
            schedulingMainLoopTimingLogger.end("method2");
        }
        for (int i = 0; i < 9; i++) {
            schedulingMainLoopTimingLogger.start("method_semi_long3");
            Thread.sleep(10 * i);
            schedulingMainLoopTimingLogger.end("method_semi_long3");
        }

        schedulingMainLoopTimingLogger.printTimingsINFOLevel();
        verify(logger).info(loggingCaptor.capture());
        String loggingString = loggingCaptor.getAllValues().get(0);
        assertTimingPerMethod(loggingString, "method_long_long_long_one1");

    }

    private void assertTimingPerMethod(String loggingString, String method) {
        assertThat(loggingString, containsString(method));
        assertThat(loggingString, containsString("Max:"));
        assertThat(loggingString, containsString("Total:"));
        assertThat(loggingString, containsString("Average:"));
        assertThat(loggingString, containsString("Times:"));
    }

}
