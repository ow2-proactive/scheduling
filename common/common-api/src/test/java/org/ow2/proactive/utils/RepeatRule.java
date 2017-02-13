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
package org.ow2.proactive.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


public class RepeatRule implements TestRule {

    private static class RepeatStatement extends Statement {

        private final int times;

        private final Statement statement;

        private final boolean parallel;

        private final long timeout;

        private RepeatStatement(int times, boolean parallel, long timeout, Statement statement) {
            this.times = times;
            this.statement = statement;
            this.parallel = parallel;
            this.timeout = timeout;
        }

        @Override
        public void evaluate() throws Throwable {
            if (parallel) {
                ExecutorService service = Executors.newFixedThreadPool(times);
                try {
                    List<Callable<Void>> callables = new ArrayList<>(times);
                    for (int i = 0; i < times; i++) {
                        callables.add(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                try {
                                    statement.evaluate();
                                } catch (Throwable throwable) {
                                    throw new RuntimeException(throwable);
                                }
                                return null;
                            }
                        });
                    }
                    if (timeout > 0) {
                        service.invokeAll(callables, timeout, TimeUnit.MILLISECONDS);
                    } else {
                        service.invokeAll(callables);
                    }
                } finally {
                    service.shutdownNow();
                }

            } else {
                for (int i = 0; i < times; i++) {
                    System.out.println("Repeat test iteration " + i);
                    statement.evaluate();
                }
            }
        }
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        Statement result = statement;
        Repeat repeat = description.getAnnotation(Repeat.class);
        if (repeat != null) {
            int times = repeat.value();
            boolean parallel = repeat.parallel();
            long timeout = repeat.timeout();
            result = new RepeatStatement(times, parallel, timeout, statement);
        }
        return result;
    }
}
