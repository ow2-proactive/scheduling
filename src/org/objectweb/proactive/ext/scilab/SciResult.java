package org.objectweb.proactive.ext.scilab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javasci.SciData;

public class SciResult implements Serializable{
	public static final int SUCCESS = 0, ABORT = 1;
	private String id;
	private int state;
	private HashMap mapResult; //map of SciResult
	private long timeExecution;
	
	/**
	 * default constructor
	 *
	 */
	public SciResult(){
	}
	
	public SciResult(String id){
		this.id = id;
		mapResult = new HashMap();
	}
	
	/**
	 * add an Out data
	 * @param data
	 */
	public void add(SciData data){
		this.mapResult.put(data.getName(), data);
	}
	
	/**
	 * 
	 * @param name data id
	 * @return the data
	 */
	public SciData get(String name){
		return (SciData) this.mapResult.get(name);
	}
	
	/**
	 * 
	 * @return list of all out data 
	 */
	public ArrayList getList(){
		ArrayList listResult = new ArrayList();
		Iterator it = this.mapResult.values().iterator();
		
		while(it.hasNext()){
			listResult.add(it.next());
		}
		
		return listResult;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getTimeExecution() {
		return timeExecution;
	}

	public void setTimeExecution(long timeExecution) {
		this.timeExecution = timeExecution;
	}

	public String getId() {
		return id;
	}
}
