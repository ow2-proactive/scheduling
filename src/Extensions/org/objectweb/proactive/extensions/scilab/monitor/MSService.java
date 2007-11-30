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
package org.objectweb.proactive.extensions.scilab.monitor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.scilab.AbstractGeneralTask;
import org.objectweb.proactive.extensions.scilab.GeneralResult;
import org.objectweb.proactive.extensions.scilab.GeneralTask;
import org.objectweb.proactive.extensions.scilab.MSDeployEngine;
import org.objectweb.proactive.extensions.scilab.MSEngine;
import org.objectweb.proactive.extensions.scilab.MatlabTask;
import org.objectweb.proactive.extensions.scilab.SciTask;


/**
 * This class is used to offer a set of services:
 * 1. to deploy, activate, and manage Scilab Engines over the Grid,
 * 2. to execute and kill tasks
 * 3. to retrieve results,
 * 4. to notify the user application of each event.
 * @author ProActive Team (amangin)
 */
public class MSService implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -2572074681533287826L;
    private HashMap<String, MSEngineInfo> mapEngine;
    private ArrayList<String> listIdEngineFree;
    private ArrayList<GenTaskInfo> listTaskWait;
    private HashMap<String, GenTaskInfo> mapTaskRun;
    private HashMap<String, GenTaskInfo> mapTaskEnd;
    private long countIdTask;
    private long countIdEngine;
    private MSEventSource taskObservable;
    private MSEventSource engineObservable;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_SERVICE);

    /**
     * constructor
     */
    public MSService() {
        this.mapEngine = new HashMap<String, MSEngineInfo>();
        this.listIdEngineFree = new ArrayList<String>();

        this.listTaskWait = new ArrayList<GenTaskInfo>();
        this.mapTaskRun = new HashMap<String, GenTaskInfo>();
        this.mapTaskEnd = new HashMap<String, GenTaskInfo>();

        this.taskObservable = new MSEventSource();
        this.engineObservable = new MSEventSource();

        (new Thread() {
                @Override
                public void run() {
                    executeTasks();
                }
            }).start();

        (new Thread() {
                @Override
                public void run() {
                    retrieveResults();
                }
            }).start();
    }

    /**
     * Deploy engines over each node defined in the the file descriptor
     * @param nameVirtualNode
     * @param pathDescriptor
     * @param arrayIdEngine
     * @return the number of deployed engine
     */
    public synchronized int deployEngine(String nameVirtualNode,
        String pathDescriptor, String[] arrayIdEngine) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:deployEngine:" + nameVirtualNode);
        }

        HashMap<String, MSEngine> mapNewEngine = MSDeployEngine.deploy(nameVirtualNode,
                pathDescriptor, arrayIdEngine);
        MSEngine mSEngine;
        String idEngine;
        BooleanWrapper isActivate;

        for (int i = 0; i < arrayIdEngine.length; i++) {
            idEngine = arrayIdEngine[i];
            mSEngine = mapNewEngine.get(idEngine);

            if (mSEngine == null) {
                continue;
            }

            isActivate = mSEngine.activate();
            mapEngine.put(idEngine,
                new MSEngineInfo(idEngine, mSEngine, isActivate));
        }

        this.listIdEngineFree.addAll(mapNewEngine.keySet());
        this.engineObservable.fireMSEvent(null);
        notifyAll();
        return mapNewEngine.size();
    }

    public synchronized int deployEngine(String nameVirtualNode,
        String pathDescriptor) {
        long countTmp = this.countIdEngine;
        int nbEngine = MSDeployEngine.getNbMappedNodes(nameVirtualNode,
                pathDescriptor);
        String[] arrayIdEngine = new String[nbEngine];
        for (int i = 0; i < arrayIdEngine.length; i++) {
            arrayIdEngine[i] = "Engine" + countTmp++;
        }

        nbEngine = this.deployEngine(nameVirtualNode, pathDescriptor,
                arrayIdEngine);
        this.countIdEngine += nbEngine;
        return nbEngine;
    }

    public synchronized int deployEngine(String nameVirtualNode,
        String pathDescriptor, int nbEngine) {
        long countTmp = this.countIdEngine;
        String[] arrayIdEngine = new String[nbEngine];
        for (int i = 0; i < arrayIdEngine.length; i++) {
            arrayIdEngine[i] = "Engine" + countTmp++;
        }

        nbEngine = this.deployEngine(nameVirtualNode, pathDescriptor,
                arrayIdEngine);
        this.countIdEngine += nbEngine;
        return nbEngine;
    }

    /**
     * Put the task in a queue of pending tasks. The task will be sent when a Scilab Engine will be free.
     * An event notify the user application of the effective sending .
     * @param sciTask
     */
    public synchronized void sendTask(AbstractGeneralTask sciTask) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:sendTask:" + sciTask.getId());
        }

        GenTaskInfo sciTaskInfo = new GenTaskInfo(sciTask);
        sciTaskInfo.setState(GenTaskInfo.PENDING);
        this.listTaskWait.add(sciTaskInfo);
        this.taskObservable.fireMSEvent(new MSEvent(sciTaskInfo));
        notifyAll();
    }

    public synchronized void sendTask(File scriptFile, String jobInit,
        String[] dataOut) throws IOException {
        this.sendTask(scriptFile, jobInit, dataOut, GenTaskInfo.NORMAL);
    }

    public synchronized void sendTask(File scriptFile, String jobInit,
        String[] dataOut, int Priority) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:sendTask");
        }

        String name = scriptFile.getName();
        String ext = name.substring(name.lastIndexOf('.'));
        GeneralTask task = null;

        if (ext.equals(".m")) {
            task = new MatlabTask("Task" + this.countIdTask++);
            if (logger.isDebugEnabled()) {
                logger.debug("->MSService :sendTask MatlabTask");
            }
        } else {
            task = new SciTask("Task" + this.countIdTask++);
            if (logger.isDebugEnabled()) {
                logger.debug("->MSService :sendTask SciTask");
            }
        }

        for (int i = 0; i < dataOut.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("->MSService :sendTask DataOut:" + dataOut[i]);
            }

            if (dataOut[i].trim().equals("")) {
                continue;
            }

            task.addDataOut(dataOut[i]);
        }

        task.setJob(scriptFile);
        task.setJobInit(jobInit);

        GenTaskInfo sciTaskInfo = new GenTaskInfo(task);
        sciTaskInfo.setFileScript(scriptFile);

        sciTaskInfo.setState(GenTaskInfo.PENDING);
        sciTaskInfo.setPriority(Priority);

        this.listTaskWait.add(sciTaskInfo);
        this.taskObservable.fireMSEvent(new MSEvent(sciTaskInfo));
        notifyAll();
    }

    /**
     * Kill a running task
     * @param idTask
     */
    public synchronized void killTask(String idTask) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:killTask:" + idTask);
        }

        GenTaskInfo sciTaskInfo = this.mapTaskRun.remove(idTask);

        if (sciTaskInfo == null) {
            return;
        }

        String idEngine = sciTaskInfo.getIdEngine();
        MSEngineInfo mSEngineInfo = mapEngine.get(idEngine);

        MSEngine mSEngine = mSEngineInfo.getMSEngine();
        mSEngine.killWorker();

        BooleanWrapper isActivate = mSEngine.activate();
        mSEngineInfo.setIsActivate(isActivate);
        mSEngineInfo.setIdCurrentTask(null);

        sciTaskInfo.setState(GenTaskInfo.KILLED);

        this.listIdEngineFree.add(idEngine);
        this.taskObservable.fireMSEvent(new MSEvent(sciTaskInfo));
    }

    /**
     * Restart a Scilab Engine
     * @param idEngine
     */
    public synchronized void restartEngine(String idEngine) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:restartEngine:" + idEngine);
        }

        MSEngineInfo mSEngineInfo = this.mapEngine.get(idEngine);

        if (mSEngineInfo == null) {
            return;
        }

        String idTask = mSEngineInfo.getIdCurrentTask();
        GenTaskInfo sciTaskInfo;

        if (idTask != null) {
            sciTaskInfo = this.mapTaskRun.remove(idTask);
            sciTaskInfo.setState(GenTaskInfo.KILLED);
            mSEngineInfo.setIdCurrentTask(null);
            this.listIdEngineFree.add(idEngine);
            this.taskObservable.fireMSEvent(new MSEvent(sciTaskInfo));
        }

        MSEngine mSEngine = mSEngineInfo.getMSEngine();
        mSEngine.killWorker();
        BooleanWrapper isActivate = mSEngine.activate();
        mSEngineInfo.setIsActivate(isActivate);
    }

    /**
     * Cancel a pending task
     * @param idTask
     */
    public synchronized void cancelTask(String idTask) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:cancelTask:" + idTask);
        }

        GenTaskInfo sciTaskInfo;
        for (int i = 0; i < this.listTaskWait.size(); i++) {
            sciTaskInfo = this.listTaskWait.get(i);

            if (idTask.equals(sciTaskInfo.getIdTask())) {
                this.listTaskWait.remove(i);
                sciTaskInfo.setState(GenTaskInfo.CANCELLED);
                this.taskObservable.fireMSEvent(new MSEvent(sciTaskInfo));
                break;
            }
        }
    }

    /**
     * Remove a terminated task
     * @param idTask
     */
    public synchronized void removeTask(String idTask) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:removeTask:" + idTask);
        }

        GenTaskInfo sciTaskInfo = this.mapTaskEnd.remove(idTask);
        sciTaskInfo.setState(GenTaskInfo.REMOVED);
        this.taskObservable.fireMSEvent(new MSEvent(sciTaskInfo));
    }

    private synchronized void retrieveResults() {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:retrieveResult");
        }

        GeneralResult sciResult;
        GenTaskInfo sciTaskInfo;
        String idEngine;
        MSEngineInfo mSEngineInfo;
        Object[] keys;

        while (true) {
            keys = this.mapTaskRun.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                sciTaskInfo = this.mapTaskRun.get(keys[i]);
                sciResult = sciTaskInfo.getResult();
                if (!ProFuture.isAwaited(sciResult)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("->MSService loop:retrieveResult:" +
                            keys[i]);
                    }

                    this.mapTaskRun.remove(keys[i]);
                    sciTaskInfo.setState(sciResult.getState());

                    idEngine = sciTaskInfo.getIdEngine();
                    mSEngineInfo = mapEngine.get(idEngine);

                    mSEngineInfo.setIdCurrentTask(null);
                    this.listIdEngineFree.add(idEngine);
                    sciTaskInfo.setDateEnd();
                    this.mapTaskEnd.put(sciTaskInfo.getIdTask(), sciTaskInfo);
                    this.taskObservable.fireMSEvent(new MSEvent(sciTaskInfo));
                    notifyAll();
                }
            }

            try {
                wait(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private GenTaskInfo getNextTask() {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService loop:getNextTask");
        }

        GenTaskInfo sciTaskInfo;

        if (this.listTaskWait.size() == 0) {
            return null;
        }

        for (int i = 0; i < this.listTaskWait.size(); i++) {
            sciTaskInfo = this.listTaskWait.get(i);

            if (sciTaskInfo.getPriority() == GenTaskInfo.HIGH) {
                return this.listTaskWait.remove(i);
            }
        }

        for (int i = 0; i < this.listTaskWait.size(); i++) {
            sciTaskInfo = this.listTaskWait.get(i);

            if (sciTaskInfo.getPriority() == GenTaskInfo.NORMAL) {
                return this.listTaskWait.remove(i);
            }
        }

        return this.listTaskWait.remove(0);
    }

    private MSEngineInfo getNextEngine() {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService loop:getNextEngine");
        }

        String idEngine;
        MSEngineInfo mSEngineInfo;
        BooleanWrapper isActivate;
        int i = 0;
        int count = this.listIdEngineFree.size();

        while (i < count) {
            idEngine = this.listIdEngineFree.remove(0);
            mSEngineInfo = mapEngine.get(idEngine);
            isActivate = mSEngineInfo.getIsActivate();
            if (logger.isDebugEnabled()) {
                logger.debug("->MSService test0:getNextEngine:" + idEngine);
            }

            if (ProFuture.isAwaited(isActivate)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("->MSService test1:getNextEngine:" + idEngine);
                }

                this.listIdEngineFree.add(idEngine);
            } else if (isActivate.booleanValue()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("->MSService test2:getNextEngine:" + idEngine);
                }

                return mSEngineInfo;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("->MSService test3:getNextEngine:" + idEngine);
                }

                this.listIdEngineFree.add(idEngine);
                MSEngine mSEngine = mSEngineInfo.getMSEngine();
                mSEngineInfo.setIsActivate(mSEngine.activate());
            }

            i++;
        }

        return null;
    }

    private synchronized void executeTasks() {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:executeTasks");
        }

        GenTaskInfo sciTaskInfo;
        MSEngineInfo mSEngineInfo;

        while (true) {
            if (this.listTaskWait.size() == 0) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("->MSService test0:executeTask");
                    }

                    wait();
                } catch (InterruptedException e) {
                }

                continue;
            }

            mSEngineInfo = this.getNextEngine();
            if (mSEngineInfo == null) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("->MSService test1:executeTask");
                    }

                    wait(1000);
                } catch (InterruptedException e) {
                }

                continue;
            }

            sciTaskInfo = this.getNextTask();
            if (sciTaskInfo == null) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("->MSService test2:executeTask");
                    }

                    wait(1000);
                } catch (InterruptedException e) {
                }

                continue;
            }

            this.executeTask(mSEngineInfo, sciTaskInfo);
        }
    }

    private synchronized void executeTask(MSEngineInfo mSEngineInfo,
        GenTaskInfo sciTaskInfo) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:executeTask");
        }

        GeneralResult genResult;
        MSEngine mSEngine;

        mSEngineInfo.setIdCurrentTask(sciTaskInfo.getIdTask());

        mSEngine = mSEngineInfo.getMSEngine();
        sciTaskInfo.setIdEngine(mSEngineInfo.getIdEngine());
        sciTaskInfo.setState(GenTaskInfo.RUNNING);
        genResult = mSEngine.execute(sciTaskInfo.getTask());

        sciTaskInfo.setResult(genResult);
        this.mapTaskRun.put(sciTaskInfo.getIdTask(), sciTaskInfo);
        this.taskObservable.fireMSEvent(new MSEvent(sciTaskInfo));
        notifyAll();
    }

    public synchronized void addEventListenerTask(MSEventListener evtListener) {
        taskObservable.addMSEventListener(evtListener);
    }

    public synchronized void addEventListenerEngine(MSEventListener evtListener) {
        engineObservable.addMSEventListener(evtListener);
    }

    public synchronized void removeEventListenerTask(
        MSEventListener evtListener) {
        taskObservable.removeMSEventListener(evtListener);
    }

    public synchronized void removeAllEventListenerTask() {
        taskObservable = null;
        taskObservable = new MSEventSource();
    }

    public synchronized void removeAllEventListenerEngine() {
        engineObservable = null;
        engineObservable = new MSEventSource();
    }

    public synchronized void removeEventListenerEngine(
        MSEventListener evtListener) {
        engineObservable.removeMSEventListener(evtListener);
    }

    /**
     * @param idTask of the terminated task
     * @return return the task
     */
    public synchronized GenTaskInfo getTaskEnd(String idTask) {
        return mapTaskEnd.get(idTask);
    }

    /**
     * @return the number of deployed engine
     */
    public synchronized int getNbEngine() {
        return mapEngine.size();
    }

    /**
     *
     * @return a Map of terminated task
     */
    @SuppressWarnings("unchecked")
    public synchronized HashMap<String, GenTaskInfo> getMapTaskEnd() {
        return (HashMap<String, GenTaskInfo>) mapTaskEnd.clone();
    }

    /**
     * @return a Map of running task
     */
    @SuppressWarnings("unchecked")
    public synchronized HashMap<String, GenTaskInfo> getMapTaskRun() {
        return (HashMap<String, GenTaskInfo>) mapTaskRun.clone();
    }

    /**
     * @return a Map of Deployed Engine
     */
    @SuppressWarnings("unchecked")
    public synchronized HashMap<String, MSEngineInfo> getMapEngine() {
        return (HashMap<String, MSEngineInfo>) mapEngine.clone();
    }

    /**
     * @return a List of pending tasks
     */
    @SuppressWarnings("unchecked")
    public synchronized ArrayList<GenTaskInfo> getListTaskWait() {
        return (ArrayList<GenTaskInfo>) listTaskWait.clone();
    }

    /**
     * exit the monitor and free each deployed engine
     *
     */
    public synchronized void exit() {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSService In:exit");
        }

        MSEngineInfo mSEngineInfo;
        MSEngine mSEngine;

        Object[] keys = mapEngine.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            mSEngineInfo = mapEngine.get(keys[i]);
            mSEngine = mSEngineInfo.getMSEngine();
            try {
                mSEngine.exit();
            } catch (Exception e) {
            }
        }
    }
}
