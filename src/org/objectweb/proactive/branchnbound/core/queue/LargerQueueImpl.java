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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.branchnbound.core.Result;
import org.objectweb.proactive.branchnbound.core.Task;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class LargerQueueImpl implements TaskQueue {
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

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#size()
     */
    public IntWrapper size() {
        return new IntWrapper(this.size);
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#hasNext()
     */
    public BooleanWrapper hasNext() {
        return new BooleanWrapper(this.size > 0);
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
        } else {
            this.size--;
            return (Task) subTasks.remove(0);
        }
    }

    public void flushAll() {
        this.queue.removeAllElements();
        this.current = 0;
        this.size = 0;
    }

    public BooleanWrapper isHungry() {
        if (logger.isDebugEnabled()) {
            logger.debug("Queue size is " + this.size + " - Hungry level is " +
                hungryLevel);
        }
        return new BooleanWrapper(this.size < hungryLevel);
    }

    public void setHungryLevel(int level) {
        this.hungryLevel = level;
    }

    public void backupTasks(Task rootTask, Vector pendingTasks) {
        File currentBck = new File(backupTaskFile);
        File oldBck = new File(backupTaskFile + "~");
        if (currentBck.exists()) {
            oldBck.delete();
            currentBck.renameTo(oldBck);
        }
        currentBck = new File(backupTaskFile);
        try {
            FileOutputStream fos = new FileOutputStream(currentBck);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(rootTask);
            for (int i = 0; i < pendingTasks.size(); i++) {
                oos.writeObject(pendingTasks.get(i));
            }
            oos.writeObject(BCK_SEPARTOR);
            oos.writeObject(this.queue);
            fos.close();
            oos.close();
        } catch (FileNotFoundException e) {
            logger.warn("Backup tasks failed", e);
        } catch (IOException e) {
            logger.warn("Backup tasks failed", e);
        }
    }

    public void loadTasks(String taskFile) {
        try {
            FileInputStream fis = new FileInputStream(taskFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.rootTaskFromBackup = (Task) ois.readObject();
            boolean separationReached = false;
            while (!separationReached) {
                Object read = ois.readObject();
                if (!separationReached && read instanceof String &&
                        (((String) read).compareTo(BCK_SEPARTOR) == 0)) {
                    separationReached = true;
                    continue;
                }
                this.pendingTasksFromBackup.add((Task) read);
            }
            this.queue = (Vector) ois.readObject();

            ois.close();
            fis.close();
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

    public IntWrapper howManyResults() {
        return new IntWrapper(this.allResults.size());
    }

    public Collection getAllResults() {
        return this.allResults;
    }

    public void backupResults(String backupResultFile) {
        try {
            File currentBck = new File(backupResultFile);
            File oldBck = new File(backupResultFile + "~");
            if (currentBck.exists()) {
                oldBck.delete();
                currentBck.renameTo(oldBck);
            }
            currentBck = new File(backupResultFile);
            FileOutputStream fos = new FileOutputStream(currentBck);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            for (int i = 0; i < this.allResults.size(); i++) {
                oos.writeObject(this.allResults.get(i));
            }
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            logger.fatal("The file is not found", e);
        } catch (IOException e) {
            logger.warn("Problem I/O with the reulst backup", e);
        }
    }

    public void loadResults(String backupResultFile) {
        try {
            FileInputStream fis = new FileInputStream(new File(backupResultFile));
            ObjectInputStream ois = new ObjectInputStream(fis);
            while (ois.available() > 0) {
                this.allResults.add((Result) ois.readObject());
            }
            ois.close();
            fis.close();
        } catch (Exception e) {
            logger.fatal("Problem to read result file.");
            throw new ProActiveRuntimeException(e);
        }
    }
}
