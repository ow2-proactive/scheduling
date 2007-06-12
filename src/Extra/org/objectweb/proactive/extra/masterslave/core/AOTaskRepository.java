package org.objectweb.proactive.extra.masterslave.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskRepository;


public class AOTaskRepository implements TaskRepository, Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE_REPOSITORY);
    protected HashSet<Integer> hashCodes = new HashSet<Integer>();
    protected HashMap<Long, Integer> idTohashCode = new HashMap<Long, Integer>();
    protected HashMap<Long, TaskIntern> idToTaskIntern = new HashMap<Long, TaskIntern>();
    protected HashMap<Long, byte[]> idToZippedTask = new HashMap<Long, byte[]>();
    protected long taskCounter = 0;

    // ProActive no-args constructor
    public AOTaskRepository() {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskRepository#addTask(org.objectweb.proactive.extra.masterslave.interfaces.Task, int)
     */
    public long addTask(Task task, int hashCode)
        throws IllegalArgumentException {
        if (hashCodes.contains(hashCode)) {
            throw new IllegalArgumentException("task already submitted");
        }
        hashCodes.add(hashCode);
        idTohashCode.put(taskCounter, hashCode);
        TaskIntern ti = new TaskWrapperImpl(taskCounter, task);
        idToTaskIntern.put(taskCounter, ti);
        taskCounter = (taskCounter + 1) % (Long.MAX_VALUE - 1);
        return ti.getId();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskRepository#getTask(long)
     */
    public TaskIntern getTask(long id) {
        if (!idToTaskIntern.containsKey(id) && !idToZippedTask.containsKey(id)) {
            throw new NoSuchElementException("task unknown");
        }
        if (idToTaskIntern.containsKey(id)) {
            return idToTaskIntern.get(id);
        } else {
            return loadTask(id);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskRepository#removeTask(long)
     */
    public void removeTask(long id) {
        if (!idToTaskIntern.containsKey(id) &&
                !(idToZippedTask.containsKey(id))) {
            throw new NoSuchElementException("task unknown");
        }

        if (idToTaskIntern.containsKey(id)) {
            idToTaskIntern.remove(id);
        } else {
            idToZippedTask.remove(id);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskRepository#removeId(long)
     */
    public void removeId(long id) {
        if (!idTohashCode.containsKey(id)) {
            throw new NoSuchElementException("unknown id");
        }
        int hashCode = idTohashCode.get(id);
        hashCodes.remove(hashCode);
    }

    /**
     * loads a task from a compressed version
     * @param id
     * @return
     */
    private TaskIntern loadTask(long id) {
        TaskIntern task = null;
        if (!idToZippedTask.containsKey(id)) {
            throw new NoSuchElementException("task unknown");
        }
        byte[] compressedData = idToZippedTask.get(id);

        // Create the decompressor and give it the data to compress
        Inflater decompressor = new Inflater();
        decompressor.setInput(compressedData);

        // Create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);

        // Decompress the data
        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
            }
        }
        try {
            bos.close();
        } catch (IOException e) {
        }

        // Get the decompressed data
        byte[] decompressedData = bos.toByteArray();
        ByteArrayInputStream objectInput = new ByteArrayInputStream(decompressedData);
        try {
            ObjectInputStream ois = new ObjectInputStream(objectInput);
            task = (TaskIntern) ois.readObject();
            idToTaskIntern.put(id, task);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return task;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskRepository#saveTask(long)
     */
    public void saveTask(long id) {
        if (!idToTaskIntern.containsKey(id)) {
            throw new NoSuchElementException("task unknown");
        }
        TaskIntern task = idToTaskIntern.remove(id);

        // Serialize the task
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(task);
            oos.flush();
            byte[] input = baos.toByteArray();

            // Compress the data
            Deflater compressor = new Deflater();
            compressor.setStrategy(Deflater.FILTERED);
            compressor.setLevel(Deflater.BEST_COMPRESSION);
            compressor.setInput(input);
            compressor.finish();
            // Create an expandable byte array to hold the compressed data.
            // You cannot use an array that's the same size as the orginal because
            // there is no guarantee that the compressed data will be smaller than
            // the uncompressed data.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

            // Compress the data
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
            try {
                bos.close();
            } catch (IOException e) {
            }

            // Get the compressed data
            byte[] compressedData = bos.toByteArray();
            idToZippedTask.put(id, compressedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskRepository#terminate()
     */
    public boolean terminate() {
        ProActive.terminateActiveObject(true);
        return true;
    }
}
