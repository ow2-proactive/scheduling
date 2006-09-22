/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ext.scilab.monitor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javasci.SciData;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.ext.scilab.SciDeployEngine;
import org.objectweb.proactive.ext.scilab.SciEngine;
import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.SciTask;

/**
 * This class is used to offer a set of services:
 * 1. to deploy, activate, and manage Scilab Engines over the Grid,
 * 2. to execute and kill tasks
 * 3. to retrieve results,
 * 4. to notify the user application of each event.
 * @author ProActive Team (amangin)
 */

public class ScilabService implements Serializable{

	private HashMap mapEngine;
	private ArrayList listIdEngineFree;
	
	private ArrayList listTaskWait;
	private HashMap mapTaskRun;
	private HashMap mapTaskEnd;
	
	private long countIdTask;
	private long countIdEngine;

	private SciEventSource taskObservable;
	private SciEventSource engineObservable;
	
	private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_SERVICE);
	
	/**
	 * constructor
	 */
	public ScilabService() {	
		this.mapEngine = new HashMap();
		this.listIdEngineFree = new ArrayList();
		
		this.listTaskWait = new ArrayList();
		this.mapTaskRun = new HashMap();
		this.mapTaskEnd = new HashMap();
		
		this.taskObservable = new SciEventSource();
		this.engineObservable = new SciEventSource();
		
		(new Thread(){
			public void run(){
				executeTasks();
			}
		}).start();
		
		
		(new Thread(){
			public void run(){
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
	
	public synchronized int deployEngine(String nameVirtualNode, String pathDescriptor, String arrayIdEngine[]){
		logger.debug("->ScilabService In:deployEngine:" + nameVirtualNode);
		HashMap mapNewEngine = SciDeployEngine.deploy(nameVirtualNode, pathDescriptor, arrayIdEngine); 
		SciEngine sciEngine;
		String idEngine;
		BooleanWrapper isActivate;
		
		for(int i=0; i<arrayIdEngine.length; i++){
			idEngine = arrayIdEngine[i];
			sciEngine = (SciEngine)mapNewEngine.get(idEngine);
			
			if(sciEngine == null)
				continue;
			
			isActivate = sciEngine.activate();
			mapEngine.put(idEngine, new SciEngineInfo(idEngine, sciEngine, isActivate));
		}
		
		this.listIdEngineFree.addAll(mapNewEngine.keySet());
		this.engineObservable.fireSciEvent(null);
		notifyAll();
		return mapNewEngine.size();
	}
	
	public synchronized int deployEngine(String nameVirtualNode, String pathDescriptor){
		long countTmp = this.countIdEngine;
		int nbEngine = SciDeployEngine.getNbMappedNodes(nameVirtualNode, pathDescriptor);
		String arrayIdEngine[] = new String[nbEngine];
		for(int i=0; i<arrayIdEngine.length; i++){
			arrayIdEngine[i] = "Engine" + countTmp++;
		}
		
		nbEngine = this.deployEngine(nameVirtualNode, pathDescriptor, arrayIdEngine);
		this.countIdEngine += nbEngine;
		return nbEngine;
	}
	
	public synchronized int deployEngine(String nameVirtualNode, String pathDescriptor, int nbEngine){
		long countTmp = this.countIdEngine;
		String arrayIdEngine[] = new String[nbEngine];
		for(int i=0; i<arrayIdEngine.length; i++){
			arrayIdEngine[i] = "Engine" + countTmp++;
		}
		
		nbEngine = this.deployEngine(nameVirtualNode, pathDescriptor, arrayIdEngine);
		this.countIdEngine += nbEngine;
		return nbEngine;
	}
	
	/**
	 * Put the task in a queue of pending tasks. The task will be sent when a Scilab Engine will be free.
	 * An event notify the user application of the effective sending . 
	 * @param sciTask
	 */
	public synchronized void sendTask(SciTask sciTask){
		logger.debug("->ScilabService In:sendTask:" + sciTask.getId());
		SciTaskInfo sciTaskInfo = new SciTaskInfo(sciTask);
		sciTaskInfo.setState(SciTaskInfo.PENDING);
		this.listTaskWait.add(sciTaskInfo);
		this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
		notifyAll();
	}
	
	public synchronized void sendTask(String pathScript, String jobInit, String dataOut[]) throws IOException{
		this.sendTask(pathScript, jobInit, dataOut, SciTaskInfo.NORMAL);
	}
	
	public synchronized void sendTask(String pathScript, String jobInit, String dataOut[], int Priority) throws IOException{
		logger.debug("->ScilabService In:sendTask");
		
		
		SciTask sciTask = new SciTask("Task" + this.countIdTask++);
		
		File f = new File(pathScript);
		
		sciTask.setJob(f);
		sciTask.setJobInit(jobInit);
		
		for(int i=0; i< dataOut.length; i++){
			logger.debug("->ScilabService DataOut:sendTask:" + dataOut[i]);
			if(dataOut[i].trim().equals("")){
				continue;
			}
			sciTask.addDataOut(new SciData(dataOut[i]));
		}
	
		SciTaskInfo sciTaskInfo = new SciTaskInfo(sciTask);
		sciTaskInfo.setFileScript(f);
		
		sciTaskInfo.setState(SciTaskInfo.PENDING);
		sciTaskInfo.setPriority(Priority);
		
		this.listTaskWait.add(sciTaskInfo);
		this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
		notifyAll();
		
	}
	
	/**
	 * Kill a running task
	 * @param idTask
	 */
	public synchronized void killTask(String idTask){
		logger.debug("->ScilabService In:killTask:" + idTask);
		
		SciTaskInfo sciTaskInfo = (SciTaskInfo) this.mapTaskRun.remove(idTask);
		
		if(sciTaskInfo == null){
			return;
		}
		
		String idEngine = sciTaskInfo.getIdEngine();
		SciEngineInfo sciEngineInfo = (SciEngineInfo) mapEngine.get(idEngine);
		
		
		SciEngine sciEngine = sciEngineInfo.getSciEngine();
		sciEngine.killWorker();
		
		BooleanWrapper isActivate = sciEngine.activate();
		sciEngineInfo.setIsActivate(isActivate);
		sciEngineInfo.setIdCurrentTask(null);
		
		sciTaskInfo.setState(SciTaskInfo.KILLED);
		
		this.listIdEngineFree.add(idEngine);
		this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
	}
	
	/**
	 * Restart a Scilab Engine
	 * @param idEngine
	 */
	public synchronized void restartEngine(String idEngine){
		logger.debug("->ScilabService In:restartEngine:" + idEngine);

		SciEngineInfo sciEngineInfo = (SciEngineInfo) this.mapEngine.get(idEngine);
		
		if(sciEngineInfo == null){
			return;
		}
		
		String idTask = sciEngineInfo.getIdCurrentTask();
		SciTaskInfo sciTaskInfo;
		
		if(idTask != null){
			sciTaskInfo = (SciTaskInfo) this.mapTaskRun.remove(idTask);
			sciTaskInfo.setState(SciTaskInfo.KILLED);
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
	public synchronized void cancelTask(String idTask){
		logger.debug("->ScilabService In:cancelTask:" + idTask);
		SciTaskInfo sciTaskInfo;
		for(int i=0; i<this.listTaskWait.size(); i++){
			sciTaskInfo = (SciTaskInfo) this.listTaskWait.get(i);
			
			if(idTask.equals(sciTaskInfo.getIdTask())){
				this.listTaskWait.remove(i);
				sciTaskInfo.setState(SciTaskInfo.CANCELLED);
				this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
				break;
			}
		}
	}
	
	/**
	 * Remove a terminated task
	 * @param idTask
	 */
	public synchronized void removeTask(String idTask){
		logger.debug("->ScilabService In:removeTask:" + idTask);
		SciTaskInfo sciTaskInfo = (SciTaskInfo) this.mapTaskEnd.remove(idTask);
		sciTaskInfo.setState(SciTaskInfo.REMOVED);
		this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
	}
		
	
	private synchronized void retrieveResults(){
		logger.debug("->ScilabService In:retrieveResult");
		SciResult sciResult;
		SciTaskInfo sciTaskInfo;
		String idEngine;
		SciEngineInfo sciEngineInfo;
		Object keys[]; 

		while (true) {
			keys= this.mapTaskRun.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				sciTaskInfo = (SciTaskInfo) this.mapTaskRun.get(keys[i]);
				sciResult = sciTaskInfo.getSciResult();
				if (!ProActive.isAwaited(sciResult)) {
					logger.debug("->ScilabService loop:retrieveResult:" + keys[i]);
					
					this.mapTaskRun.remove(keys[i]);
					sciTaskInfo.setState(sciResult.getState());
					
					idEngine = sciTaskInfo.getIdEngine();
					sciEngineInfo = (SciEngineInfo) mapEngine.get(idEngine);
					
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
	
	private SciTaskInfo getNextTask(){
		logger.debug("->ScilabService loop:getNextTask");
		SciTaskInfo sciTaskInfo;
			
		if (this.listTaskWait.size() == 0) return null;
		
		for(int i=0; i<this.listTaskWait.size(); i++){
			sciTaskInfo = (SciTaskInfo)this.listTaskWait.get(i);
			
			if(sciTaskInfo.getPriority() == SciTaskInfo.HIGH){
				return (SciTaskInfo) this.listTaskWait.remove(i);
			}	
		}
		
		for(int i=0; i<this.listTaskWait.size(); i++){
			sciTaskInfo = (SciTaskInfo)this.listTaskWait.get(i);
			
			if(sciTaskInfo.getPriority() == SciTaskInfo.NORMAL){
				return (SciTaskInfo) this.listTaskWait.remove(i);
			}	
		}
		
		return (SciTaskInfo) this.listTaskWait.remove(0);
	}
	
	
	private SciEngineInfo getNextEngine(){
		logger.debug("->ScilabService loop:getNextEngine");
		String idEngine;
		SciEngineInfo sciEngineInfo;
		BooleanWrapper isActivate;
		int i = 0;
		int count = this.listIdEngineFree.size();
		
		while (i<count){
			idEngine = (String) this.listIdEngineFree.remove(0);
			sciEngineInfo = (SciEngineInfo) mapEngine.get(idEngine);
			isActivate = sciEngineInfo.getIsActivate();
			logger.debug("->ScilabService test0:getNextEngine:" + idEngine);
			if(ProActive.isAwaited(isActivate)){
				logger.debug("->ScilabService test1:getNextEngine:" + idEngine);
				this.listIdEngineFree.add(idEngine);
			}
			else if(isActivate.booleanValue()){
				logger.debug("->ScilabService test2:getNextEngine:" + idEngine);
				return sciEngineInfo;
			}else{
				logger.debug("->ScilabService test3:getNextEngine:" + idEngine);
				this.listIdEngineFree.add(idEngine);
				SciEngine sciEngine = sciEngineInfo.getSciEngine();
				sciEngineInfo.setIsActivate(sciEngine.activate());
			}
			i++;
		}
		return null;
	}
	
	
	private synchronized void executeTasks(){
		logger.debug("->ScilabService In:executeTasks");
		
		SciTaskInfo sciTaskInfo;
		SciEngineInfo sciEngineInfo;
		
		
		while(true){
			if (this.listTaskWait.size() == 0) {
				try {
					logger.debug("->ScilabService test0:executeTask");
					wait();
				} catch (InterruptedException e) {
				}

				continue;
			}
			
			sciEngineInfo = this.getNextEngine();
			if(sciEngineInfo == null){
				try {
					logger.debug("->ScilabService test1:executeTask");
					wait(1000);
				} catch (InterruptedException e) {
				}
				continue;
			}
			
			sciTaskInfo = this.getNextTask();
			if(sciTaskInfo == null){
				try {
					logger.debug("->ScilabService test2:executeTask");
					wait(1000);
				} catch (InterruptedException e) {
				}
				continue;
			}
			
			this.executeTask(sciEngineInfo, sciTaskInfo);
		}	
	}
	
	private synchronized void executeTask(SciEngineInfo sciEngineInfo, SciTaskInfo sciTaskInfo) {
		logger.debug("->ScilabService In:executeTask");
		SciResult sciResult;
		SciEngine sciEngine;
		
		sciEngineInfo.setIdCurrentTask(sciTaskInfo.getIdTask());
		
		sciEngine = sciEngineInfo.getSciEngine();
		sciTaskInfo.setIdEngine(sciEngineInfo.getIdEngine());
		sciTaskInfo.setState(SciTaskInfo.RUNNING);
		sciResult = sciEngine.execute(sciTaskInfo.getSciTask());
		
		sciTaskInfo.setSciResult(sciResult);
		this.mapTaskRun.put(sciTaskInfo.getIdTask(), sciTaskInfo);
		this.taskObservable.fireSciEvent(new SciEvent(sciTaskInfo));
		notifyAll();
	}
	
	
	public synchronized void addEventListenerTask(SciEventListener evtListener){
		taskObservable.addSciEventListener(evtListener);
	}
	
	public synchronized void addEventListenerEngine(SciEventListener evtListener){
		engineObservable.addSciEventListener(evtListener);
	}
	
	public synchronized void removeEventListenerTask(SciEventListener evtListener){
		taskObservable.removeSciEventListener(evtListener);
	}
	
	public synchronized void removeAllEventListenerTask(){
		taskObservable = null;
		taskObservable = new SciEventSource();
	}
	
	public synchronized void removeAllEventListenerEngine(){
		engineObservable = null;
		engineObservable = new SciEventSource();
	}
	
	public synchronized void removeEventListenerEngine(SciEventListener evtListener){
		engineObservable.removeSciEventListener(evtListener);
	}
	
	/**
	 * @param idTask of the terminated task
	 * @return return the task
	 */
	public synchronized SciTaskInfo getTaskEnd(String idTask){
		return (SciTaskInfo) mapTaskEnd.get(idTask);
	}
	
	/**
	 * @return the number of deployed engine
	 */
	public synchronized int getNbEngine(){
		return mapEngine.size();
	}
	
	/**
	 * 
	 * @return a Map of terminated task
	 */
	public synchronized HashMap getMapTaskEnd() {
		return (HashMap) mapTaskEnd.clone();
	}

	
	/**
	 * @return a Map of running task
	 */
	public synchronized HashMap getMapTaskRun() {
		return (HashMap) mapTaskRun.clone();
	}

	/**
	 * @return a Map of Deployed Engine
	 */
	public synchronized HashMap getMapEngine() {
		return (HashMap) mapEngine.clone();
	}

	/**
	 * @return a List of pending tasks
	 */
	public synchronized ArrayList getListTaskWait() {
		return (ArrayList) listTaskWait.clone();
	}
	
	/**
	 * exit the monitor and free each deployed engine
	 *
	 */
	public synchronized void exit(){
		logger.debug("->ScilabService In:exit");
		SciEngineInfo sciEngineInfo;
		SciEngine sciEngine;
		
		Object keys[] = mapEngine.keySet().toArray();
		for(int i=0; i<keys.length; i++){
			sciEngineInfo = (SciEngineInfo) mapEngine.get(keys[i]);
			sciEngine = sciEngineInfo.getSciEngine();
			try{
			sciEngine.exit();
			}catch(RuntimeException e ){
				
			}
		}
	}
}
