package org.ow2.proactive.scheduler.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.python.google.common.collect.Maps;


public class SchedulingMainLoopTimingLogger {

    private final Map<String, TimingModel> allTimings;

    private final Logger logger;

    public SchedulingMainLoopTimingLogger(Logger logger) {
        this.logger = logger;
        this.allTimings = Maps.newHashMap();
    }

    public void start(String nameOfTiming) {
        allTimings.putIfAbsent(nameOfTiming, new TimingModel());
        allTimings.get(nameOfTiming).start();
    }

    public void end(String nameOfTiming) {
        allTimings.getOrDefault(nameOfTiming, new TimingModel()).end();
    }

    public void printTimingsINFOLevel() {
        List<String> loggingStrings = allTimings.entrySet().stream()
                .map(timing -> timing.getValue().getLoggingString(timing.getKey()))
                .collect(Collectors.toList());
        if (!loggingStrings.isEmpty()) {
            logger.info("SchedulingMainLoopTiming::" + System.getProperty("line.separator") +
                String.join(System.getProperty("line.separator"), loggingStrings));
        }
    }

}

class TimingModel {

    private long start;
    private long max;
    private long total;
    private long counter;

    public void start() {
        counter++;
        this.start = System.currentTimeMillis();
    }

    public void end() {
        add(System.currentTimeMillis() - start);
    }

    public String getLoggingString(String methodName) {
        return "Max:" + max + "ms;Total:" + total + "ms;Average:" + getAverage() + "ms;Times:" + counter +
            ";" + methodName;
    }

    private void add(long time) {
        max = time > max ? time : max;
        total += time;
    }

    private long getAverage() {
        return counter > 0 ? total / counter : 0;
    }

}
