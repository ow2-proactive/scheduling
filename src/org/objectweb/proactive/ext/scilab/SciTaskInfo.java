package org.objectweb.proactive.ext.scilab;

import java.io.File;
import java.util.Date;

public class SciTaskInfo {

	public static final int LOW=0, NORMAL=1, HIGH=2;
	public static final int SUCCESS = 0, 
							ABORT = 1, 
							WAIT = 2, 
							RUN = 3, 
							KILL = 4, 
							CANCEL = 5,
							REMOVE = 6;
	
	private int priority = NORMAL;
	
	private int state;
	private String idEngine;
	private String nameScript;
	private String pathScript;
	private File fileScript;
	private SciTask sciTask;
	private SciResult sciResult;
	private long dateStart;
	private long dateEnd;
	
	public SciTaskInfo(SciTask sciTask){
		this.sciTask = sciTask;
		this.dateStart = (new Date()).getTime();
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public SciTask getSciTask() {
		return sciTask;
	}
	public SciResult getSciResult() {
		return sciResult;
	}
	public void setSciResult(SciResult sciResult) {
		this.sciResult = sciResult;
	}
	public String getIdTask() {
		return sciTask.getId();
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getIdEngine() {
		return idEngine;
	}
	public void setIdEngine(String idEngine) {
		this.idEngine = idEngine;
	}
	
	public void setDateEnd(){
		this.dateEnd = (new Date()).getTime();
	}
	
	public long getTimeGlobal(){
		return this.dateEnd - this.dateStart;
	}
	
	public long getTimeExecution(){
		return this.sciResult.getTimeExecution();
	}
	public long getDateStart() {
		return dateStart;
	}
	public String getPathScript() {
		return fileScript.getAbsolutePath();
	}
	
	public String getNameScript() {
		return fileScript.getName();
	}

	public void setFileScript(File fileScript) {
		this.fileScript = fileScript;
	}
}
