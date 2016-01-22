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

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * FileLock is in charge to provide a mean for two or more processes
 * to synchronize based on the existence of a file.
 */
public class FileLock {

    private static final String FILE_PREFIX = "pa-file-lock-";

    private final String nameSuffix;

    private Path tmpFile;

    private boolean isLocked;

    public FileLock() throws IOException {
        this("");
    }

    public FileLock(String nameSuffix) throws IOException {
        this.isLocked = false;
        this.nameSuffix = nameSuffix;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (tmpFile != null && Files.exists(tmpFile)) {
                    try {
                        Files.delete(tmpFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public Path lock() throws IOException {
        if (isLocked) {
            throw new IllegalStateException("Lock already acquired: " + tmpFile);
        }

        isLocked = true;
        tmpFile = Files.createTempFile(FILE_PREFIX, nameSuffix);

        return tmpFile;
    }

    public void unlock() throws IOException {
        if (isLocked) {
            unlock(tmpFile);
            isLocked = false;
        }
    }

    public static void unlock(String file) throws IOException {
        unlock(Paths.get(file));
    }

    public static void unlock(Path file) throws IOException {
        if (Files.exists(file)) {
            Files.delete(file);
        }
    }

    public boolean isLocked() {
        return isLocked;
    }

    public static void waitUntilUnlocked(String lockFilePath) throws InterruptedException, ExecutionException, IOException {
        waitUntilUnlocked(Paths.get(lockFilePath));
    }

    public static void waitUntilUnlocked(
            Path lockFile) throws InterruptedException, IOException, ExecutionException {
        if (!lockFile.getFileName().toString().startsWith(FILE_PREFIX)) {
            throw new IllegalArgumentException(
                    "The specified path to file is probably incorrect: " + lockFile);
        }

        while (Files.exists(lockFile)) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        // TODO: investigate why FileWatcher is not working as expected

        // FileWatcher fileWatcher = new FileWatcher(lockFile);
        // Thread thread = new Thread(fileWatcher);
        // thread.start();
        // thread.join();
    }

    public static class FileWatcher implements Runnable {

        private volatile boolean running;

        private final WatchService watcher;

        private final Path lockFile;

        public FileWatcher(Path lockFile) throws IOException {
            this.lockFile = lockFile;
            this.running = true;
            this.watcher = FileSystems.getDefault().newWatchService();
            lockFile.getParent().register(watcher, StandardWatchEventKinds.ENTRY_DELETE);
        }

        @Override
        public void run() {
            while (running) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        if (fileName.equals(this.lockFile.getFileName())) {
                            running = false;
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }

            try {
                watcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}