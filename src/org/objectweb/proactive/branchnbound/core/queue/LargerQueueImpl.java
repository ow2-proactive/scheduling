/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
import java.util.Collection;
import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.branchnbound.core.Result;
import org.objectweb.proactive.branchnbound.core.Task;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;


public class LargerQueueImpl extends TaskQueue {
    private static final String BCK_SEPARTOR = "End pending tasks backup -- Starting not started tasks backup";
    private Vector queue = new Vector();
    private int size = 0;
    private int hungryLevel;
    private int current = 0;
    private Vector pendingTasksFromBackup = new Vector();
    private Task rootTaskFromBackup = null;
    private Vector allResults = new Vector();

    public LargerQueueImpl() {
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#addAll(java.util.Collection)
     */
    public void addAll(Collection tasks) {
        tasks = (Collection) ProActive.getFutureValue(tasks);
        if (tasks.size() > 0) {
            this.queue.add(tasks);
            this.size += tasks.size();
            if (logger.isDebugEnabled()) {
                logger.debug("Task provider just received and added " +
                    tasks.size());
            }
        }
    }

    public void reset() {
        queue = new Vector();
        size = 0;
        hungryLevel = 0;
        current = 0;
        pendingTasksFromBackup = new Vector();
        rootTaskFromBackup = null;
        allResults = new Vector();
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#size()
     */
    public IntMutableWrapper size() {
        return new IntMutableWrapper(this.size);
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#hasNext()
     */
    public BooleanMutableWrapper hasNext() {
        return new BooleanMutableWrapper(this.size > 0);
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#next()
     */
    public Task next() {
        if (this.size == 0) {
            throw new RuntimeException("No more elements");
        }
        if (current >= this.queue.size()) {
            current = 0;
        }
        Vector subTasks = (Vector) this.queue.get(current);
        if (subTasks.size() == 0) {
            this.queue.remove(current);
            current++;
            return this.next();
        }
        this.size--;
        return (Task) subTasks.remove(0);
    }

    public void flushAll() {
        this.queue.removeAllElements();
        this.current = 0;
        this.size = 0;
    }

    public BooleanMutableWrapper isHungry() {
        if (logger.isDebugEnabled()) {
            logger.debug("Queue size is " + this.size + " - Hungry level is " +
                hungryLevel);
        }
        return new BooleanMutableWrapper(this.size <= hungryLevel);
    }

    public void setHungryLevel(int level) {
        this.hungryLevel = level;
    }

    public void backupTasks(Task rootTask, Vector pendingTasks,
        java.io.OutputStream backupOutputStream) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(backupOutputStream);
            oos.writeObject(rootTask);
            for (int i = 0; i < pendingTasks.size(); i++) {
                oos.writeObject(pendingTasks.get(i));
            }
            oos.writeObject(BCK_SEPARTOR);
            oos.writeObject(this.queue);
            oos.close();
            backupOutputStream.close();
        } catch (FileNotFoundException e) {
            logger.warn("Backup tasks failed", e);
        } catch (IOException e) {
            logger.warn("Backup tasks failed", e);
        }
    }

    public void loadTasks(InputStream taskInputStream) {
        try {
            ObjectInputStream ois = new ObjectInputStream(taskInputStream);
            this.rootTaskFromBackup = (Task) ois.readObject();
            boolean separationReached = false;
            while (!separationReached) {
                Object read = ois.readObject();
                if (!separationReached && read instanceof String &&
                        (((String) read).compareTo(BCK_SEPARTOR) == 0)) {
                    separationReached = true;
                    continue;
                }
                this.pendingTasksFromBackup.add(read);
            }
            this.queue = (Vector) ois.readObject();

            ois.close();
            taskInputStream.close();
        } catch (Exception e) {
            logger.fatal("Failed to read tasks", e);
            throw new ProActiveRuntimeException(e);
        }
    }

    public Task getRootTaskFromBackup() {
        return this.rootTaskFromBackup;
    }

    public Collection getPendingTasksFromBackup() {
        return this.pendingTasksFromBackup;
    }

    // --------------------------------------------------------------
    // Mananging results
    // --------------------------------------------------------------
    public void addResult(Result result) {
        this.allResults.add(result);
    }

    public IntMutableWrapper howManyResults() {
        return new IntMutableWrapper(this.allResults.size());
    }

    public Collection getAllResults() {
        return this.allResults;
    }

    public void backupResults(java.io.OutputStream backupOutputStream) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(backupOutputStream);
            for (int i = 0; i < this.allResults.size(); i++) {
                oos.writeObject(this.allResults.get(i));
            }
            oos.close();
            backupOutputStream.close();
        } catch (FileNotFoundException e) {
            logger.fatal("The file is not found", e);
        } catch (IOException e) {
            logger.warn("Problem I/O with the reulst backup", e);
        }
    }

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

    public void addTask(Task t) {
        Vector v = new Vector();
        v.add(t);
        this.queue.add(v);
        size++;
    }
}
