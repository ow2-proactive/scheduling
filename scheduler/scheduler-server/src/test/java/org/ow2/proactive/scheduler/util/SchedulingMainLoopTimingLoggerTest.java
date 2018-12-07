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
import org.mockito.MockitoAnnotations;;


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
        verify(logger, Mockito.times(0)).info("");

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
        assertThat(loggingString, containsString(method + "["));
        assertThat(loggingString, containsString("Max:"));
        assertThat(loggingString, containsString("Total:"));
        assertThat(loggingString, containsString("Average:"));
        assertThat(loggingString, containsString("Times:"));
    }

}
