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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadPoolRouter {

    private List<ExecutorService> pool;

    private int poolSize;

    public ThreadPoolRouter(int numberOfThreads) {
        poolSize = numberOfThreads;
        initPool();
    }

    public synchronized void route(String key, Runnable runnable) {
        int indexOfThread = key.hashCode() % pool.size();
        pool.get(indexOfThread).execute(runnable);
    }

    public synchronized void shutdown() {
        pool.forEach(ExecutorService::shutdown);
    }

    public synchronized void resizePoolTo(int newSize) {
        poolSize = newSize;
        shutdown();
        initPool();
    }

    private synchronized void initPool() {
        pool = new ArrayList<>();
        for (int i = 0; i < poolSize; ++i) {
            pool.add(Executors.newSingleThreadExecutor());
        }
    }

    public synchronized int getPoolSize() {
        return poolSize;
    }
}
