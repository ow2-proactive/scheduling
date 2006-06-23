package org.objectweb.proactive.ext.scilab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javasci.SciData;

public class SciTask implements Serializable {
	
	private String id;
	private ArrayList listDataIn;
	private ArrayList listDataOut;
	private String job;
	private String jobInit;

	public SciTask(String id) {
		this.id = id;
		this.listDataIn = new ArrayList();
		this.listDataOut = new ArrayList();
	}
	
	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}
	
	public void setJobInit(String jobInit){
		this.jobInit = jobInit;
	}
	
	public void setJob(File fileJob) throws FileNotFoundException, IOException{
		StringBuffer strBuffer = new StringBuffer();
	
		FileReader reader = new FileReader(fileJob);
		int c;
		
		while((c = reader.read()) != -1){
			strBuffer.append((char)c);
		}
		this.job = strBuffer.toString();
		
		reader.close();
	}

	public ArrayList getListDataIn() {
		return listDataIn;
	}

	public void setListDataIn(ArrayList listDataIn) {
		this.listDataIn = listDataIn;
	}

	public void addDataIn(SciData data) {
		this.listDataIn.add(data);
	}
	
	public ArrayList getListDataOut() {
		return listDataOut;
	}

	public void setListDataOut(ArrayList listDataOut) {
		this.listDataOut = listDataOut;
	}
	
	public void addDataOut(SciData data) {
		this.listDataOut.add(data);
	}

	public String getId() {
		return id;
	}

	public String getJobInit() {
		return jobInit;
	}
	
}
