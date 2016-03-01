/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
