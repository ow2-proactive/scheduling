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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;


public class MultipleTimingLogger {

    private final LinkedHashMap<String, TimingModel> allTimings;

    private final Logger logger;

    private final String name;

    private final static String nl = System.getProperty("line.separator");

    private final boolean hierarchical;

    public MultipleTimingLogger(String name, Logger logger) {
        this(name, logger, false);
    }

    public MultipleTimingLogger(String name, Logger logger, boolean hierarchical) {
        this.logger = logger;
        this.allTimings = Maps.newLinkedHashMap();
        this.name = name;
        this.hierarchical = hierarchical;
    }

    public String getName() {
        return name;
    }

    public boolean isHierarchical() {
        return hierarchical;
    }

    public void start(String nameOfTiming) {
        TimingModel model = new TimingModel();
        TimingModel storedModel = allTimings.putIfAbsent(nameOfTiming, model);
        Optional.ofNullable(storedModel).orElse(model).start();
    }

    public void end(String nameOfTiming) {
        allTimings.getOrDefault(nameOfTiming, new TimingModel()).end();
    }

    public void printTimings(Level level) {
        if (logger.isEnabledFor(level)) {
            List<String> loggingStrings = allTimings.entrySet()
                                                    .stream()
                                                    .map(timing -> timing.getValue().getLoggingString(timing.getKey()))
                                                    .collect(Collectors.toList());
            if (!loggingStrings.isEmpty()) {
                logger.log(level, name + "::" + nl + String.join(nl, loggingStrings));

            }
        }
    }

    public void clear() {
        allTimings.clear();
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
        return "Max:" + max + "ms;Total:" + total + "ms;Average:" + getAverage() + "ms;Times:" + counter + ";" +
               methodName;
    }

    private void add(long time) {
        max = time > max ? time : max;
        total += time;
    }

    private long getAverage() {
        return counter > 0 ? total / counter : 0;
    }

}
