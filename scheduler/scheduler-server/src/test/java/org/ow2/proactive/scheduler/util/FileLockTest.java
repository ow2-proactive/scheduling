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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;


/**
 * FileLockTest aims to check that {@link FileLock} implementation
 * can be used from several threads at the same time to perform a
 * kind of synchronization barrier.
 */
public class FileLockTest {

    @Test(timeout = 5000)
    public void testWaitUntilUnlocked() throws Exception {
        int nbTasks = 3;
        final FileLock fileLock = new FileLock();
        final Path lockFilePath = fileLock.lock();

        List<Future> tasks = new ArrayList<>(nbTasks);

        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < nbTasks; i++) {
            Future<Object> task = threadPool.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    fileLock.waitUntilUnlocked(lockFilePath);
                    return null;
                }
            });
            tasks.add(task);
        }

        TimeUnit.MILLISECONDS.sleep(300);

        fileLock.unlock();

        for (int i = 0; i < nbTasks; i++) {
            tasks.get(i).get();
        }

        threadPool.shutdown();
    }

}
