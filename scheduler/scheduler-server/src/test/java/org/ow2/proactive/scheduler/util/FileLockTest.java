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
package org.ow2.proactive.scheduler.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class FileLockTest {

    @Test(timeout = 1000)
    public void testWaitUntilUnlocked() throws Exception {
        int nbTasks = 3;
        final FileLock fileLock = new FileLock();
        final Path lockFilePath = fileLock.lock();

        List<Future> tasks = new ArrayList<>(nbTasks);

        ExecutorService threadPool =
                Executors.newFixedThreadPool(
                        Runtime.getRuntime().availableProcessors());

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