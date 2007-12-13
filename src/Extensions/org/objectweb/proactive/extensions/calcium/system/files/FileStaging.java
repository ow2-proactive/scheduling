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
package org.objectweb.proactive.extensions.calcium.system.files;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.stateness.Handler;
import org.objectweb.proactive.extensions.calcium.stateness.ObjectGraph;
import org.objectweb.proactive.extensions.calcium.system.ProxyFile;
import org.objectweb.proactive.extensions.calcium.system.WSpaceImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


/**
 * This class is used to keep track of the file reference modifications between
 * two different states of a task. (ie before the execution and after)
 *
 * @author The ProActive Team (mleyton)
 */
public class FileStaging implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_SYSTEM);
    ArrayList<ProxyFile> beforeProxyFiles;

    public <T> FileStaging(Task<T> task, FileServerClient fserver, WSpaceImpl wspace) throws Exception {
        String policy = System.getProperty("proactive.skeletons.filetransfer.policy");
        if (logger.isDebugEnabled()) {
            logger.debug("Using File Transfer policy: " + policy);
        }

        IdentityHashMap<ProxyFile, ProxyFile> allFiles = new IdentityHashMap<ProxyFile, ProxyFile>();

        Handler<ProxyFile> before = null;

        if ((policy == null) || policy.equalsIgnoreCase("hybrid")) {
            before = new HandlerPreHybridProxyFile(task, fserver, wspace, allFiles);
        } else if (policy.equalsIgnoreCase("eager")) {
            before = new HandlerPreEagerProxyFile(fserver, wspace, allFiles);
        } else if (policy.equalsIgnoreCase("lazy")) {
            before = new HandlerPreLazyProxyFile(fserver, wspace, allFiles);
        }

        navigateObjectGraph(task.family.hasFinishedChild(), task.family.childrenFinished, task, before);

        beforeProxyFiles = new ArrayList<ProxyFile>(allFiles.size());
        beforeProxyFiles.addAll(allFiles.values());
    }

    public void stageOut(FileServerClient fserver, Task<?> task) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Staging out for data parallelism");
        }

        IdentityHashMap<ProxyFile, ProxyFile> allFiles = new IdentityHashMap<ProxyFile, ProxyFile>();
        for (ProxyFile pfile : beforeProxyFiles) {
            allFiles.put(pfile, pfile);
        }

        Handler<ProxyFile> handler = new HandlerPostProxyFile(fserver, allFiles);

        //mark the after reference count
        navigateObjectGraph(task.family.hasReadyChildTask(), task.family.childrenReady, task, handler);

        //now process the files: new, modified, updated, etc..
        Collection<ProxyFile> list = allFiles.values();
        for (ProxyFile pfile : list) {
            pfile.handleStageOut(fserver);
        }

        //keep track of stats
        updateProxyFileStats(task, allFiles.values());
    }

    private static void updateProxyFileStats(Task task, Collection<ProxyFile> list) {
        for (ProxyFile pfile : list) {
            task.getStats().addComputationBlockedFetchingData(pfile.blockedFetchingTime);
            task.getStats().addUploadedBytes(pfile.uploadedBytes);
            task.getStats().addDownloadedBytes(pfile.downloadedBytes);

            pfile.setStageOutState();
        }
    }

    private static <T, U> void navigateObjectGraph(boolean condition, Vector<Task<T>> list, Task task,
            Handler<U> handler) throws Exception {
        if (condition) {
            for (Task t : list) {
                t.setObject(ObjectGraph.searchForClass(t.getObject(), handler));
            }
        } else {
            task.setObject(ObjectGraph.searchForClass(task.getObject(), handler));
        }
    }

    public static <T> Task<T> stageInput(FileServerClient fserver, Task<T> task) throws PanicException {
        // TODO change the exception type
        IdentityHashMap<ProxyFile, ProxyFile> files = new IdentityHashMap<ProxyFile, ProxyFile>();

        try {
            T o = (T) ObjectGraph.searchForClass(task.getObject(), new HandlerFile2ProxyFile(fserver, files));
            task.setObject(o);

            updateProxyFileStats(task, files.values());
        } catch (Exception e) {
            throw new PanicException(e);
        }

        return task;
    }

    public static <R> Task<R> stageOutput(FileServerClient fserver, Task<R> task, File outDir)
            throws Exception {
        IdentityHashMap<ProxyFile, ProxyFile> files = new IdentityHashMap<ProxyFile, ProxyFile>();

        R o = (R) ObjectGraph.searchForClass(task.getObject(), new HandlerProxy2File(fserver, files, outDir));
        task.setObject(o);

        updateProxyFileStats(task, files.values());

        return task;
    }
}
