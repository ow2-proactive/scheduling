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
import org.objectweb.proactive.extensions.scilab.MatlabTask;
import org.objectweb.proactive.extensions.scilab.SciDeployEngine;
import org.objectweb.proactive.extensions.scilab.SciEngine;
import org.objectweb.proactive.extensions.scilab.SciTask;


/**
 * This class is used to offer a set of services:
 * 1. to deploy, activate, and manage Scilab Engines over the Grid,
 * 2. to execute and kill tasks
 * 3. to retrieve results,
 * 4. to notify the user application of each event.
 * @author ProActive Team (amangin)
 */
public class ScilabService implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -2572074681533287826L;
    private HashMap<String, SciEngineInfo> mapEngine;
    private ArrayList<String> listIdEngineFree;
    private ArrayList<GenTaskInfo> listTaskWait;
    private HashMap<String, GenTaskInfo> mapTaskRun;
    private HashMap<String, GenTaskInfo> mapTaskEnd;
    private long countIdTask;
    private long countIdEngine;
    private SciEventSource taskObservable;
    private SciEventSource engineObservable;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_SERVICE);

    /**
     * constructor
     */
    public ScilabService() {
        this.mapEngine = new HashMap<String, SciEngineInfo>();
        this.listIdEngineFree = new ArrayList<String>();

        this.listTaskWait = new ArrayList<GenTaskInfo>();
        this.mapTaskRun = new HashMap<String, GenTaskInfo>();
        this.mapTaskEnd = new HashMap<String, GenTaskInfo>();

        this.taskObservable = new SciEventSource();
        this.engineObservable = new SciEventSource();

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
            logger.debug("->ScilabService In:deployEngine:" + nameVirtualNode);
        }

        HashMap<String, SciEngine> mapNewEngine = SciDeployEngine.deploy(nameVirtualNode,
                pathDescriptor, arrayIdEngine);
        SciEngine sciEngine;
        String idEngine;
        BooleanWrapper isActivate;

        for (int i = 0; i < arrayIdEngine.length; i++) {
            idEngine = arrayIdEngine[i];
            sciEngine = mapNewEngine.get(idEngine);

            if (sciEngine == null) {
                continue;
            }

            isActivate = sciEngine.activate();
            mapEngine.put(idEngine,
                new SciEngineInfo(idEngine, sciEngine, isActivate));
        }

        this.listIdEngineFree.addAll(mapNewEngine.keySet());
        this.engineObservable.fireSciEvent(null);
        notifyAll();
        return mapNewEngine.size();
    }

    public synchronized int deployEngine(String nameVirtualNode,
        String pathDescriptor) {
        long countTmp = this.countIdEngine;
        int nbEngine = SciDeployEngine.getNbMappedNodes(nameVirtualNode,
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
            logger.debug("->ScilabService In:sendTask:" + sciTask.getId());
        }

        GenTaskInfo sciTaskInfo = new GenTaskInfo(sciTask);
        sciTaskInfo.setState(GenTaskInfo.PENDING);
        this.listTaskWait.add(sciTaskInfo);
        this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
        notifyAll();
    }

    public synchronized void sendTask(File scriptFile, String jobInit,
        String[] dataOut) throws IOException {
        this.sendTask(scriptFile, jobInit, dataOut, GenTaskInfo.NORMAL);
    }

    public synchronized void sendTask(File scriptFile, String jobInit,
        String[] dataOut, int Priority) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("->ScilabService In:sendTask");
        }

        String name = scriptFile.getName();
        String ext = name.substring(name.lastIndexOf('.'));
        GeneralTask task = null;

        if (ext.equals(".m")) {
            task = new MatlabTask("Task" + this.countIdTask++);
            if (logger.isDebugEnabled()) {
                logger.debug("->ScilabService :sendTask MatlabTask");
            }
        } else {
            task = new SciTask("Task" + this.countIdTask++);
            if (logger.isDebugEnabled()) {
                logger.debug("->ScilabService :sendTask SciTask");
            }
        }

        for (int i = 0; i < dataOut.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("->ScilabService :sendTask DataOut:" + dataOut[i]);
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
        this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
        notifyAll();
    }

    /**
     * Kill a running task
     * @param idTask
     */
    public synchronized void killTask(String idTask) {
        if (logger.isDebugEnabled()) {
            logger.debug("->ScilabService In:killTask:" + idTask);
        }

        GenTaskInfo sciTaskInfo = this.mapTaskRun.remove(idTask);

        if (sciTaskInfo == null) {
            return;
        }

        String idEngine = sciTaskInfo.getIdEngine();
        SciEngineInfo sciEngineInfo = mapEngine.get(idEngine);

        SciEngine sciEngine = sciEngineInfo.getSciEngine();
        sciEngine.killWorker();

        BooleanWrapper isActivate = sciEngine.activate();
        sciEngineInfo.setIsActivate(isActivate);
        sciEngineInfo.setIdCurrentTask(null);

        sciTaskInfo.setState(GenTaskInfo.KILLED);

        this.listIdEngineFree.add(idEngine);
        this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
    }

    /**
     * Restart a Scilab Engine
     * @param idEngine
     */
    public synchronized void restartEngine(String idEngine) {
        if (logger.isDebugEnabled()) {
            logger.debug("->ScilabService In:restartEngine:" + idEngine);
        }

        SciEngineInfo sciEngineInfo = this.mapEngine.get(idEngine);

        if (sciEngineInfo == null) {
            return;
        }

        String idTask = sciEngineInfo.getIdCurrentTask();
        GenTaskInfo sciTaskInfo;

        if (idTask != null) {
            sciTaskInfo = this.mapTaskRun.remove(idTask);
            sciTaskInfo.setState(GenTaskInfo.KILLED);
            sciEngineInfo.setIdCurrentTask(null);
            this.listIdEngineFree.add(idEngine);
            this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
        }

        SciEngine sciEngine = sciEngineInfo.getSciEngine();
        sciEngine.killWorker();
        BooleanWrapper isActivate = sciEngine.activate();
        sciEngineInfo.setIsActivate(isActivate);
    }

    /**
     * Cancel a pending task
     * @param idTask
     */
    public synchronized void cancelTask(String idTask) {
        if (logger.isDebugEnabled()) {
            logger.debug("->ScilabService In:cancelTask:" + idTask);
        }

        GenTaskInfo sciTaskInfo;
        for (int i = 0; i < this.listTaskWait.size(); i++) {
            sciTaskInfo = this.listTaskWait.get(i);

            if (idTask.equals(sciTaskInfo.getIdTask())) {
                this.listTaskWait.remove(i);
                sciTaskInfo.setState(GenTaskInfo.CANCELLED);
                this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
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
            logger.debug("->ScilabService In:removeTask:" + idTask);
        }

        GenTaskInfo sciTaskInfo = this.mapTaskEnd.remove(idTask);
        sciTaskInfo.setState(GenTaskInfo.REMOVED);
        this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
    }

    private synchronized void retrieveResults() {
        if (logger.isDebugEnabled()) {
            logger.debug("->ScilabService In:retrieveResult");
        }

        GeneralResult sciResult;
        GenTaskInfo sciTaskInfo;
        String idEngine;
        SciEngineInfo sciEngineInfo;
        Object[] keys;

        while (true) {
            keys = this.mapTaskRun.keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                sciTaskInfo = this.mapTaskRun.get(keys[i]);
                sciResult = sciTaskInfo.getResult();
                if (!ProFuture.isAwaited(sciResult)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("->ScilabService loop:retrieveResult:" +
                            keys[i]);
                    }

                    this.mapTaskRun.remove(keys[i]);
                    sciTaskInfo.setState(sciResult.getState());

                    idEngine = sciTaskInfo.getIdEngine();
                    sciEngineInfo = mapEngine.get(idEngine);

                    sciEngineInfo.setIdCurrentTask(null);
                    this.listIdEngineFree.add(idEngine);
                    sciTaskInfo.setDateEnd();
                    this.mapTaskEnd.put(sciTaskInfo.getIdTask(), sciTaskInfo);
                    this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
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
            logger.debug("->ScilabService loop:getNextTask");
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

    private SciEngineInfo getNextEngine() {
        if (logger.isDebugEnabled()) {
            logger.debug("->ScilabService loop:getNextEngine");
        }

        String idEngine;
        SciEngineInfo sciEngineInfo;
        BooleanWrapper isActivate;
        int i = 0;
        int count = this.listIdEngineFree.size();

        while (i < count) {
            idEngine = this.listIdEngineFree.remove(0);
            sciEngineInfo = mapEngine.get(idEngine);
            isActivate = sciEngineInfo.getIsActivate();
            if (logger.isDebugEnabled()) {
                logger.debug("->ScilabService test0:getNextEngine:" + idEngine);
            }

            if (ProFuture.isAwaited(isActivate)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("->ScilabService test1:getNextEngine:" +
                        idEngine);
                }

                this.listIdEngineFree.add(idEngine);
            } else if (isActivate.booleanValue()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("->ScilabService test2:getNextEngine:" +
                        idEngine);
                }

                return sciEngineInfo;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("->ScilabService test3:getNextEngine:" +
                        idEngine);
                }

                this.listIdEngineFree.add(idEngine);
                SciEngine sciEngine = sciEngineInfo.getSciEngine();
                sciEngineInfo.setIsActivate(sciEngine.activate());
            }

            i++;
        }

        return null;
    }

    private synchronized void executeTasks() {
        if (logger.isDebugEnabled()) {
            logger.debug("->ScilabService In:executeTasks");
        }

        GenTaskInfo sciTaskInfo;
        SciEngineInfo sciEngineInfo;

        while (true) {
            if (this.listTaskWait.size() == 0) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("->ScilabService test0:executeTask");
                    }

                    wait();
                } catch (InterruptedException e) {
                }

                continue;
            }

            sciEngineInfo = this.getNextEngine();
            if (sciEngineInfo == null) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("->ScilabService test1:executeTask");
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
                        logger.debug("->ScilabService test2:executeTask");
                    }

                    wait(1000);
                } catch (InterruptedException e) {
                }

                continue;
            }

            this.executeTask(sciEngineInfo, sciTaskInfo);
        }
    }

    private synchronized void executeTask(SciEngineInfo sciEngineInfo,
        GenTaskInfo sciTaskInfo) {
        if (logger.isDebugEnabled()) {
            logger.debug("->ScilabService In:executeTask");
        }

        GeneralResult genResult;
        SciEngine sciEngine;

        sciEngineInfo.setIdCurrentTask(sciTaskInfo.getIdTask());

        sciEngine = sciEngineInfo.getSciEngine();
        sciTaskInfo.setIdEngine(sciEngineInfo.getIdEngine());
        sciTaskInfo.setState(GenTaskInfo.RUNNING);
        genResult = sciEngine.execute(sciTaskInfo.getTask());

        sciTaskInfo.setResult(genResult);
        this.mapTaskRun.put(sciTaskInfo.getIdTask(), sciTaskInfo);
        this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
        notifyAll();
    }

    public synchronized void addEventListenerTask(SciEventListener evtListener) {
        taskObservable.addSciEventListener(evtListener);
    }

    public synchronized void addEventListenerEngine(
        SciEventListener evtListener) {
        engineObservable.addSciEventListener(evtListener);
    }

    public synchronized void removeEventListenerTask(
        SciEventListener evtListener) {
        taskObservable.removeSciEventListener(evtListener);
    }

    public synchronized void removeAllEventListenerTask() {
        taskObservable = null;
        taskObservable = new SciEventSource();
    }

    public synchronized void removeAllEventListenerEngine() {
        engineObservable = null;
        engineObservable = new SciEventSource();
    }

    public synchronized void removeEventListenerEngine(
        SciEventListener evtListener) {
        engineObservable.removeSciEventListener(evtListener);
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
    public synchronized HashMap<String, SciEngineInfo> getMapEngine() {
        return (HashMap<String, SciEngineInfo>) mapEngine.clone();
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
            logger.debug("->ScilabService In:exit");
        }

        SciEngineInfo sciEngineInfo;
        SciEngine sciEngine;

        Object[] keys = mapEngine.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            sciEngineInfo = mapEngine.get(keys[i]);
            sciEngine = sciEngineInfo.getSciEngine();
            try {
                sciEngine.exit();
            } catch (RuntimeException e) {
            }
        }
    }
}
