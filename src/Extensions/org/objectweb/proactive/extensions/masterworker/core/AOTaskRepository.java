/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.masterworker.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskRepository;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * This active object acts as a repository of tasks that are currently processed by the master<br>
 * The master asks this repository for the real task by giving the task id.<br>
 * The purpose of this class is to save having duplicated task objects within the framework <br>
 * @author The ProActive Team
 *
 */
public class AOTaskRepository implements TaskRepository, Serializable {

    /**
     *
     */

    /**
     * logger of the task repository
     */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER_REPOSITORY);

    /**
     * associations of ids to actual tasks
     */
    protected HashMap<Long, TaskIntern<Serializable>> idToTaskIntern = new HashMap<Long, TaskIntern<Serializable>>();

    /**
     * associations of ids to zipped versions of the tasks
     */
    protected HashMap<Long, byte[]> idToZippedTask = new HashMap<Long, byte[]>();

    /**
     * counter of the last task id created
     */
    protected long taskCounter = 0;

    /**
     * Size of compression buffers
     */
    private static final int COMPRESSION_BUFFER_SIZE = 1024;

    /**
     * ProActive empty constructor
     */
    public AOTaskRepository() {
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public long addTask(final Task<? extends Serializable> task) {

        TaskIntern<Serializable> ti = new TaskWrapperImpl(taskCounter, (Task<Serializable>) task);
        idToTaskIntern.put(taskCounter, ti);
        if (logger.isDebugEnabled()) {
            logger.debug("Adding task id " + taskCounter);
        }
        taskCounter = (taskCounter + 1) % (Long.MAX_VALUE - 1);
        return ti.getId();
    }

    /**
     * {@inheritDoc}
     */
    public TaskIntern<Serializable> getTask(final long id) {
        if (!idToTaskIntern.containsKey(id) && !idToZippedTask.containsKey(id)) {
            throw new NoSuchElementException("task unknown : " + id);
        }

        if (idToTaskIntern.containsKey(id)) {
            return idToTaskIntern.get(id);
        } else {
            return loadTask(id);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeTask(final long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing task id " + id);
        }
        if (!idToTaskIntern.containsKey(id) && !(idToZippedTask.containsKey(id))) {
            throw new NoSuchElementException("task unknown : " + id);
        }

        if (idToTaskIntern.containsKey(id)) {
            idToTaskIntern.remove(id);
        } else {
            idToZippedTask.remove(id);
        }
    }

    /**
     * loads a task from a compressed version
     * @param id the task id
     * @return the loaded task
     */
    @SuppressWarnings("unchecked")
    protected TaskIntern<Serializable> loadTask(final long id) {
        TaskIntern<Serializable> task = null;
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
        byte[] buf = new byte[COMPRESSION_BUFFER_SIZE];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
                logger.error("Error during task decompression", e);
            }
        }

        try {
            bos.close();
        } catch (IOException e) {
            logger.error("Error during task decompression", e);
        }

        // Get the decompressed data
        byte[] decompressedData = bos.toByteArray();
        ByteArrayInputStream objectInput = new ByteArrayInputStream(decompressedData);
        try {
            ObjectInputStream ois = new ObjectInputStream(objectInput);
            task = (TaskIntern<Serializable>) ois.readObject();
            idToTaskIntern.put(id, task);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return task;
    }

    /**
     * {@inheritDoc}
     */
    public void saveTask(final long id) {
        if (!idToTaskIntern.containsKey(id)) {
            throw new NoSuchElementException("task unknown");
        }

        TaskIntern<Serializable> ti = idToTaskIntern.remove(id);

        // Serialize the task
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(ti);
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
            byte[] buf = new byte[COMPRESSION_BUFFER_SIZE];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }

            try {
                bos.close();
            } catch (IOException e) {
                logger.error("Error during task compression", e);
            }

            // Get the compressed data
            byte[] compressedData = bos.toByteArray();
            idToZippedTask.put(id, compressedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean terminate() {
        PAActiveObject.terminateActiveObject(true);
        return true;
    }

    public void clear() {
        idToTaskIntern.clear();
        idToZippedTask.clear();
    }
}
