package org.objectweb.proactive.ext.scilab;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public class SciEngineWorker implements Serializable  {
	
	private JVMProcessImpl process;
	private SciEngineTask sciEngineTask;
	private String idEngine; 
	private String idTask; 
	
	private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_WORKER);
	/**
	 * default constructor
	 *
	 */
	public SciEngineWorker() {
	
	}
	
	/**
	 * 
	 * @param idEngine
	 */
	public SciEngineWorker(String idEngine) {
		this.idEngine = idEngine;
	}
	
	/**
	 * kill the task
	 * @param idTask id of the task to kill
	 */
	public void killTask(String idTask){
		logger.debug("->SciEngineWorker In:killTask" + idTask);
		if(idTask.equals(this.idTask)){
			this.killEngineTask();
		}
		
	}
	
	public void exit(){
		logger.debug("->SciEngineWorker In:exit");
		this.killEngineTask();
	}
	
	public SciResult execute(SciTask sciTask){
		logger.debug("->SciEngineWorker In:execute:" + sciTask.getId());
		this.idTask = sciTask.getId();
		return this.sciEngineTask.execute(sciTask);
	}
	
	public BooleanWrapper activate(){
		logger.debug("->SciEngineWorker In:activate");
		String url = "//localhost/" + idEngine + (long)(Math.random() * 1000000) + (new Date()).getTime(); 
		try{
			process = new JVMProcessImpl();
			process.setClassname("org.objectweb.proactive.core.node.StartNode");
			process.setParameters(url);
			process.startProcess();
		}catch(IOException e){
			return new BooleanWrapper(false);
		}
		
		for(int i=0; i<60; i++){
			try{	
				Thread.sleep(3000);
				sciEngineTask  =  (SciEngineTask) ProActive.newActive(SciEngineTask.class.getName(), null, url);	
				return new BooleanWrapper(true);
			}catch(InterruptedException e){
				e.printStackTrace();
			}catch(ProActiveException e){
			}
		}  
		return new BooleanWrapper(false);
	}
	
	private void killEngineTask(){
			process.stopProcess();
			this.sciEngineTask = null;
	}
	
}
