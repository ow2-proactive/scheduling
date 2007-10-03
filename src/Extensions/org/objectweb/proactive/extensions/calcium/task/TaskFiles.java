/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.task;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.exceptions.TaskException;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.system.ProxyFile;
import org.objectweb.proactive.extensions.calcium.system.WSpaceImpl;


/**
 * This class is used to keep track of the file reference modifications
 * between two different states of a task. (ie before the execution and after)
 *
 * Between one execution and the next, files can be:
 *
 * 1. Dereferenced (Referenced in before state, but not referenced in after state)
 * 2. Unreferenced (Not referenced in before state, but referenced in after state)
 * 3. Referenced (Referenced in before state, and referenced in after state)
 *
 * When a file is Referenced two sub cases must be considered:
 *
 * 3a. File was unmodified.
 * 3b. File was modified.
 *
 * @author The ProActive Team (mleyton)
 */
public class TaskFiles implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_SYSTEM);
    IdentityHashMap<ProxyFile, ProxyFile> before; //files referenced before execution
    String taskId;

    public <T>TaskFiles(Task<T> task)
        throws IllegalArgumentException, IllegalAccessException, TaskException {
        if (task.family.hasFinishedChild()) {
            this.before = new IdentityHashMap<ProxyFile, ProxyFile>();
            for (Task t : task.family.childrenFinished) {
                Stateness.getAllFieldObjects(t.getObject(), this.before,
                    ProxyFile.class);
            }
        } else {
            this.before = Stateness.getAllFieldObjects(task.getObject(),
                    ProxyFile.class);
        }

        this.taskId = task.toString();

        Collection<ProxyFile> list = this.before.values();
        for (ProxyFile pf : list) {
            if (!pf.isRemotelyStored()) {
                throw new TaskException(
                    "ProxyFile does not have a reference on a remote file! " +
                    pf);
            }
        }
    }

    /**
     * This method performs the register and unregister of files on the file server.
     *
     * The files inside the parameter task are compared by reference with the files specified at
     * the instantiation of this object. Where the instantiation files are the before files, and
     * the parameter tasks are the after files.
     *
     * -All modified files are dereferenced (remote counter decreased), and treated as new files.
     * -All new files (files that are not stored in the remote file server) are stored.
      * -For each task referencing a file the remote reference counter is increased.
     *
     * @param fserver The file server.
     * @param task The "after" task.
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IOException
     */
    public void stageOut(FileServer fserver, Task<?> task)
        throws IllegalArgumentException, IllegalAccessException, IOException {
        unreferenceModifiedFiles(fserver);

        if (task.family.hasReadyChildTask()) {
            for (Task t : task.family.childrenReady) {
                storeNewFiles(fserver, t);
                mark(t, fserver, true);
            }
        } else {
            storeNewFiles(fserver, task);
            mark(task, fserver, false);
        }

        sweep(fserver);
    }

    /**
     * This method decreaeses the remote reference counter
     * when finding a file who's marked field is false.
     */
    private void sweep(FileServer fserver) {
        Collection<ProxyFile> list = before.values();

        for (ProxyFile b : list) {
            if (!b.marked) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sweeping file:" + b);
                }
                b.dereference(fserver);
            }
        }
    }

    /**
     * When finding a file, this method sets true to its marked state.
     *
     * @param task The task who's files will be searched.
     * @param fserver The file server to count file references
     * @param count Weather to count the reference or not.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IOException
     */
    private void mark(Task<?> task, FileServer fserver, Boolean count)
        throws IllegalArgumentException, IllegalAccessException, IOException {
        IdentityHashMap<ProxyFile, ProxyFile> files = Stateness.getAllFieldObjects(task.getObject(),
                ProxyFile.class);
        Collection<ProxyFile> list = files.values();

        for (ProxyFile f : list) {
            f.marked = true;
            if (count) {
                f.countReference(fserver);
            }
        }
    }

    /**
     * This method looks for files that are not remotely stored, and stores them.
     *
     * @param fserver The server to store the files.
     * @param task The task who's files will be searched.
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IOException
     */
    private void storeNewFiles(FileServer fserver, Task task)
        throws IllegalArgumentException, IllegalAccessException, IOException {
        IdentityHashMap<ProxyFile, ProxyFile> files = Stateness.getAllFieldObjects(task.getObject(),
                ProxyFile.class);

        //Store new files
        Collection<ProxyFile> after = files.values();
        for (ProxyFile a : after) {
            if (!a.isRemotelyStored()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Task " + taskId +
                        ": stage-out from wspace: " + a);
                }
                a.store(fserver);
            }
        }
    }

    /**
     * This methods searches for files that have been modified.
     * When a file is found to be modified, the dereference method is called on the file,
     * and the file is removed from the before hash map.
     *
     * In other words, a modified file is transformed into a new file.
     *
     * @param before The hash to search for modified files.
     */
    private void unreferenceModifiedFiles(FileServer fserver) {
        //Find unreferenced files
        Collection<ProxyFile> list = before.values();

        for (Iterator<ProxyFile> i = list.iterator(); i.hasNext();) {
            ProxyFile b = i.next();

            if (b.hasBeenModified()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Task " + taskId +
                        ": detected modified file: " + b);
                }
                b.dereference(fserver);
                i.remove(); //also remove from before hash map
            }
        }
    }

    public void stageIn(WSpaceImpl wspace) throws IOException {
        Collection<ProxyFile> list = this.before.values();
        for (ProxyFile b : list) {
            b.marked = false; //all files are candidate for deletion in the stageOut
            b.setWSpace(wspace.getWSpaceDir());
            if (logger.isDebugEnabled()) {
                logger.debug("Task " + taskId + ": stage-in to wspace: " + b);
            }
            b.saveRemoteFileInWSpace();
        }
    }

    /**
     * This method looks for File References inside an Object. When a reference
     * to a File is found, the reference is replaced with a ProxyFile object, and
     * the file is stored remotely on the File Server.
     *
     * @param fserver The file server to store files.
     * @param o The Object that will be searched.
     * @return The Object with the correct references.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static Object stageInput(FileServer fserver, Object o)
        throws IllegalArgumentException, IllegalAccessException, IOException {
        //null object, nothing to do
        if (o == null) {
            return o;
        }

        Class<?> cls = o.getClass();
        if (cls == File.class) {
            File f = (File) o;
            ProxyFile pf = new ProxyFile(f.getParentFile(), f.getName());
            pf.store(fserver);

            if (logger.isDebugEnabled()) {
                logger.debug("Stage Input, stored file: " + f);
            }

            return pf;
        }

        //Maybe one of the sub-objects contains a File object
        ArrayList<Field> field = Stateness.getAllInheritedFields(cls);

        for (Field f : field) {
            Class<?> c = f.getType();

            //primitives are not Files || static vars are not considered
            if (c.isPrimitive() || Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            f.setAccessible(true);

            Object fieldObject = f.get(o);

            if (fieldObject == null) {
                continue;
            }

            //If its an array of T[], where T is not primitive
            //(ex: int[] is not assignable to Object[])
            if (new Object[0].getClass().isAssignableFrom(fieldObject.getClass())) {
                Object[] array = (Object[]) fieldObject;
                for (Object a : array) {
                    if (a != null) {
                        f.set(o, stageInput(fserver, fieldObject));
                    }
                }
            } else { // regular object, search recursively
                f.set(o, stageInput(fserver, fieldObject));
            }
        }

        return o;
    }

    /**
     * This method looks for ProxyFile References inside an Object. When a reference
     * to a ProxyFile is found, the remote file is retrieved and stored locally.
     * Then, the reference to ProxyFile is replaced with a File object.
     *
     * @param fserver The file server from where files will be retrieved.
     * @param o The object to look inside.
     * @param outDir The output directory to store the retrieved files.
     * @return  The object with the ProxyFile references replaced with File references.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IOException
     */
    public static Object stageOutput(FileServer fserver, Object o, File outDir)
        throws IllegalArgumentException, IllegalAccessException, IOException {
        //null object, nothing to do
        if (o == null) {
            return o;
        }

        Class<?> cls = o.getClass();
        if (cls == ProxyFile.class) {
            ProxyFile pf = (ProxyFile) o;
            pf.setWSpace(outDir);
            pf.saveRemoteFileInWSpace();
            pf.dereference(fserver);

            File f = pf.getCurrent();

            if (logger.isDebugEnabled()) {
                logger.debug("Stage Output, stored file: " + f);
            }

            return f;
        }

        //Maybe one of the sub-objects contains a File object
        ArrayList<Field> field = Stateness.getAllInheritedFields(cls);

        for (Field f : field) {
            Class<?> c = f.getType();

            //primitives are not Files || static vars are not considered
            if (c.isPrimitive() || Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            f.setAccessible(true);

            Object fieldObject = f.get(o);

            if (fieldObject == null) {
                continue;
            }

            //If its an array of T[], where T is not primitive
            //(ex: int[] is not assignable to Object[])
            if (new Object[0].getClass().isAssignableFrom(fieldObject.getClass())) {
                Object[] array = (Object[]) fieldObject;
                for (Object a : array) {
                    if (a != null) {
                        f.set(o, stageOutput(fserver, fieldObject, outDir));
                    }
                }
            } else { // regular object, search recursively
                f.set(o, stageOutput(fserver, fieldObject, outDir));
            }
        }

        return o;
    }
}
