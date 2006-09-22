package org.objectweb.proactive.ext.scilab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javasci.SciData;
import javasci.Scilab;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

public class SciEngineWorker implements Serializable{
	private SciTask sciTask; 
	private SciResult sciResult;
	private long dateStart;
	private long dateEnd;
	private boolean isStateFull = false;
	private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_TASK);
	private static SciEngineWorker sciEngineWorker;
	
	
	public SciEngineWorker(){
		Scilab.init();
	}
	
	/**
	 * execute a task 
	 * @param sciTask Scilab task
	 * @return result of the computation 
	 */
	public SciResult execute(SciTask sciTask){
		logger.debug("->SciEngineTask In:execute:" + sciTask.getId());
		this.sciTask = sciTask;
		this.sciResult = new  SciResult(sciTask.getId());
		
		this.setListDataToScilab();
		
		this.dateStart = System.currentTimeMillis();
		try{	
			if(executeJob()){
				getListDataToScilab();
				this.sciResult.setState(SciResult.SUCCESS);
			}else{
				this.sciResult.setState(SciResult.ABORT);
				System.out.println("->SciEngine test:execute\n");
			}
		}catch(Exception e){
			e.printStackTrace();
			this.sciResult.setState(SciResult.ABORT);
		}
		
		this.dateEnd = System.currentTimeMillis();
		this.sciResult.setTimeExecution(this.dateEnd - this.dateStart);
		return sciResult;
	}

	/**
	 * set In data in Scilab environment
	 *
	 */
	private void setListDataToScilab(){
		ArrayList listData = this.sciTask.getListDataIn();
	
		SciData data;
		for (int i = 0; i < listData.size(); i++) {
			data = (SciData) listData.get(i);
			Scilab.sendData(data);
		}
	}
	
	/**
	 * clear Scilab environment
	 *
	 */
	private void clearScilab(){
		ArrayList listData = this.sciTask.getListDataOut();
		SciData data;
		Scilab.exec("clearglobal();");
		for (int i = 0; i < listData.size(); i++) {
			data = (SciData) listData.get(i);
			Scilab.exec("clear " + data.getName() + ";");
		}
	}
		
	/**
	 * retrieve results of the computation
	 * 
	 */
	private void getListDataToScilab(){
		ArrayList listData = this.sciTask.getListDataOut();
		SciData data;
		for (int i = 0; i < listData.size(); i++) {
			data = (SciData) listData.get(i);
			data = Scilab.receiveDataByName(data.getName());
			this.sciResult.add(data);	
		}
		
		if(!isStateFull){
			clearScilab();
		}
	}
	
	/**
	 * @return true if is a valid execution, otherwise false
	 */
	private boolean executeJob(){
		String job = sciTask.getJobInit() + "\n" + sciTask.getJob();
		BufferedWriter out;
		File temp;
		boolean isValid;
		try {
			temp = File.createTempFile("scilab", ".sce");
			temp.deleteOnExit();
			out = new BufferedWriter(new FileWriter(temp));
			out.write(job);
			out.close();
			logger.debug("->SciEngineTask:executeJob:test1:" + temp.getAbsolutePath());
			isValid = Scilab.exec("exec(''"+temp.getAbsolutePath()+"'');");
		} catch (IOException e) {
			isValid = false;
		}
		
		return isValid;
	}
	
	public synchronized static SciResult executeTask(SciTask sciTask){
		if(sciEngineWorker == null){
			sciEngineWorker = new SciEngineWorker();
		}
		return sciEngineWorker.execute(sciTask);
	}
	
	public void exit(){
		System.exit(0);
	}

}
