package org.ow2.proactive.scheduler.task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.utils.FileUtils;
import org.apache.log4j.Logger;

/**
 * ProgressFileReader is in charge of:
 * - creating a progress file in the specified working directory
 * - detecting changes on a progress file efficiently
 * - reading new value and saving it in memory
 * - exposing the last read value
 * <p/>
 * Instances of this class are NOT thread-safe.
 *
 * @author The ProActive Team
 */
public class ProgressFileReader {

    private static final Logger logger = Logger.getLogger(ProgressFileReader.class);

    private static final String PROGRESS_FILE_DIR = ".tasks-progress";

    private Path progressFileDir;

    private Path progressFile;

    private volatile int progress;

    private WatchService watchService;

    private Thread watchServiceThread;

    private Set<Listener> observers;

    public ProgressFileReader() {
        // mainly for test purposes
        observers = new HashSet<>(0);
    }

    public boolean start(File workingDir, TaskId taskId) {
        String progressFileName =
                "job-" + taskId.getJobId().value() + "-task-" + taskId.value()
                        + "-" + UUID.randomUUID() + ".progress";

        return start(workingDir, progressFileName);
    }

    boolean start(File workingDir, String filename) {
        try {
            createProgressFile(workingDir, filename);

            watchService = FileSystems.getDefault().newWatchService();

            watchServiceThread = new Thread(new ProgressFileReaderThread(filename));
            watchServiceThread.setName(ProgressFileReaderThread.class.getName());
            watchServiceThread.start();

            progress = 0;

            progressFileDir.register(
                    watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            return true;
        } catch (IOException e) {
            logger.warn("Error while creating progress file. Progress will not be reported.", e);
            return false;
        }
    }

    private void createProgressFile(File workingDir, String progressFileName) throws IOException {
        progressFileDir = workingDir.toPath().resolve(PROGRESS_FILE_DIR);

        try {
            Files.createDirectories(progressFileDir);

            progressFile = progressFileDir.resolve(progressFileName);

            Files.createFile(progressFile);
        } catch (FileAlreadyExistsException e) {
            // ignore file already exists exception
        }

        logger.debug("Progress file '" + progressFile + "' created");
    }

    public int getProgress() {
        return progress;
    }

    public Path getProgressFile() {
        return progressFile;
    }

    public void register(Listener listener) {
        this.observers.add(listener);
    }

    public void unregister(Listener listener) {
        this.observers.remove(listener);
    }

    public void stop() {
        if(watchService != null) {
            try {
                watchService.close();
                watchServiceThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info(e);
            } catch (IOException e) {
                logger.error(e);
            } finally {
                removeProgressFileDir();
            }
        }
    }

    private void removeProgressFileDir() {
        if (progressFileDir == null
                || !Files.exists(progressFileDir)) {
            return;
        }

        try {
            FileUtils.deleteRecursively(progressFileDir);
        } catch (IOException e) {
            logger.warn("Cannot delete progress file dir: " + progressFileDir, e);
        }
    }

    public interface Listener {

        void onProgressUpdate(int newValue);

    }

    public final class ProgressFileReaderThread implements Runnable {

        private final String progressFileName;

        public ProgressFileReaderThread(String progressFileName) {
            this.progressFileName = progressFileName;
        }

        @Override
        public void run() {
            FileTime lastModificationTime = FileTime.fromMillis(0);

            try {
                WatchKey watchKey;

                while ((watchKey = watchService.take()) != null) {
                    // we have a polled event, now we traverse it and
                    // receive all the states from it
                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                        WatchEvent<Path> ev = cast(event);
                        WatchEvent.Kind kind = event.kind();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        Path path = ev.context();

                        // compare file modification time to prevent duplicate events since
                        // updating content and metadata may be detected as two independent update operations
                        FileTime newLastModificationTime = Files.getLastModifiedTime(progressFile);

                        if (newLastModificationTime.compareTo(lastModificationTime) > 0
                                && path.toString().equals(progressFileName)) {
                            readNewValue();
                            lastModificationTime = newLastModificationTime;
                        }
                    }

                    watchKey.reset();
                }
            } catch (InterruptedException e) {
                logger.warn(e);
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                logger.warn(e);
            } catch (ClosedWatchServiceException e) {
                logger.debug("Watch service closed");
            }
        }

        @SuppressWarnings("unchecked")
        private WatchEvent<Path> cast(WatchEvent<?> event) {
            return (WatchEvent<Path>) event;
        }

        private void readNewValue() {
            try {
                String line =
                        com.google.common.io.Files.readFirstLine(
                                progressFile.toFile(), Charset.defaultCharset());

                if (line != null) {
                    try {
                        // try to parse double to allow int + double
                        int value = (int) Double.parseDouble(line);

                        if (value >= 0 && value <= 100) {
                            progress = value;

                            for (Listener observer : observers) {
                                observer.onProgressUpdate(progress);
                            }

                            if (logger.isDebugEnabled()) {
                                logger.debug("New progress value read: " + value);
                            }
                        } else {
                            logger.warn("Invalid progress value: " + value);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Progress value is a not a numeric value: " + line);
                    }
                }
            } catch (IOException e) {
                logger.warn("Error while reading the first line of " + progressFile);
            }
        }

    }

}
