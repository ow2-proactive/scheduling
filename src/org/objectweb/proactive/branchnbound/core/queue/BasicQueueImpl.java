/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.branchnbound.core.queue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Vector;

import org.objectweb.proactive.branchnbound.core.Result;
import org.objectweb.proactive.branchnbound.core.Task;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;


/**
 * A FIFO queue for providing tasks. The tasks are provided as the same order of
 * their arrival in the queue.
 *
 * @author Alexandre di Costanzo
 *
 * Created on Nov 3, 2005
 */
public class BasicQueueImpl extends TaskQueue {
    private static final String BCK_SEPARTOR = "End pending tasks backup -- Starting not started tasks backup";
    private Vector queue = new Vector();
    private int hungryLevel;
    private Task rootTaskFromBackup = null;
    private Vector pendingTasksFromBackup = new Vector();
    private Vector allResults = new Vector();

    /**
     * The no args constructor for ProActive activate.
     */
    public BasicQueueImpl() {
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#addAll(java.util.Collection)
     */
    public void addAll(Collection tasks) {
        if (tasks.size() > 0) {
            queue.addAll(tasks);
            if (logger.isDebugEnabled()) {
                logger.debug("Task provider just received and added " +
                    tasks.size());
            }
        }
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#size()
     */
    public IntMutableWrapper size() {
        return new IntMutableWrapper(this.queue.size());
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#hasNext()
     */
    public BooleanMutableWrapper hasNext() {
        return new BooleanMutableWrapper(this.queue.size() > 0);
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#next()
     */
    public Task next() {
        return (Task) this.queue.remove(0);
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#flushAll()
     */
    public void flushAll() {
        queue = new Vector();
        hungryLevel = 0;
        rootTaskFromBackup = null;
        pendingTasksFromBackup = new Vector();
        allResults = new Vector();
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#isHungry()
     */
    public BooleanWrapper isHungry() {
        if (logger.isDebugEnabled()) {
            logger.debug("Queue size is " + this.queue.size() +
                " - Hungry level is " + this.hungryLevel);
        }
        return new BooleanWrapper(this.queue.size() <= this.hungryLevel);
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#setHungryLevel(int)
     */
    public void setHungryLevel(int level) {
        this.hungryLevel = level;
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#backupTasks(org.objectweb.proactive.branchnbound.core.Task, java.util.Vector, java.io.OutputStream)
     */
    public void backupTasks(Task rootTask, Vector pendingTasks,
        OutputStream backupOutputStream) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(backupOutputStream);
            oos.writeObject(rootTask);
            for (int j = 0; j < pendingTasks.size(); j++) {
                oos.writeObject(pendingTasks.get(j));
            }
            oos.writeObject(BCK_SEPARTOR);
            for (int i = 0; i < this.queue.size(); i++) {
                oos.writeObject(this.queue.get(i));
            }
            oos.close();
            backupOutputStream.close();
        } catch (FileNotFoundException e) {
            logger.warn("Backup tasks failed", e);
        } catch (IOException e) {
            logger.warn("Backup tasks failed", e);
        }
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#loadTasks(java.io.InputStream)
     */
    public void loadTasks(InputStream taskInputStream) {
        try {
            ObjectInputStream ois = new ObjectInputStream(taskInputStream);
            this.rootTaskFromBackup = (Task) ois.readObject();
            boolean separationReached = false;
            while (ois.available() > 0) {
                Object read = ois.readObject();
                if (!separationReached && read instanceof String &&
                        (((String) read).compareTo(BCK_SEPARTOR) == 0)) {
                    separationReached = true;
                }
                if (!separationReached) {
                    this.pendingTasksFromBackup.add(read);
                } else {
                    this.queue.add(read);
                }
            }
            ois.close();
            taskInputStream.close();
        } catch (Exception e) {
            logger.fatal("Failed to read tasks", e);
            throw new ProActiveRuntimeException(e);
        }
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#getRootTaskFromBackup()
     */
    public Task getRootTaskFromBackup() {
        return this.rootTaskFromBackup;
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#addResult(org.objectweb.proactive.branchnbound.core.Result)
     */
    public void addResult(Result result) {
        this.allResults.add(result);
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#howManyResults()
     */
    public IntMutableWrapper howManyResults() {
        return new IntMutableWrapper(this.allResults.size());
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#getAllResults()
     */
    public Collection getAllResults() {
        return this.allResults;
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#backupResults(java.io.OutputStream)
     */
    public void backupResults(OutputStream backupResultOutputStream) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(backupResultOutputStream);
            for (int i = 0; i < this.allResults.size(); i++) {
                oos.writeObject(this.allResults.get(i));
            }
            oos.close();
            backupResultOutputStream.close();
        } catch (FileNotFoundException e) {
            logger.fatal("The file is not found", e);
        } catch (IOException e) {
            logger.warn("Problem I/O with the reulst backup", e);
        }
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#loadResults(java.io.InputStream)
     */
    public void loadResults(InputStream backupResultInputStream) {
        try {
            ObjectInputStream ois = new ObjectInputStream(backupResultInputStream);
            while (ois.available() > 0) {
                this.allResults.add(ois.readObject());
            }
            ois.close();
            backupResultInputStream.close();
        } catch (Exception e) {
            logger.fatal("Problem to read result file.");
            throw new ProActiveRuntimeException(e);
        }
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#addTask(org.objectweb.proactive.branchnbound.core.Task)
     */
    public void addTask(Task t) {
        queue.add(t);
    }
}
