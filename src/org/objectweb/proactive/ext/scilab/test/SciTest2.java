package org.objectweb.proactive.ext.scilab.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.ext.scilab.SciEvent;
import org.objectweb.proactive.ext.scilab.SciEventListener;
import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.SciTask;
import org.objectweb.proactive.ext.scilab.SciTaskInfo;
import org.objectweb.proactive.ext.scilab.ScilabService;

public class SciTest2 {

	SciTask task;
	ScilabService scilab;
	
	public void  displayResult(SciTaskInfo scitaskInfo){
		SciResult sciResult = scitaskInfo.getSciResult();
		ArrayList listResult;
		listResult = sciResult.getList();

		for (int i = 0; i < listResult.size(); i++) {
			SciData result = (SciData) listResult.get(i);
			
			if (result instanceof SciDoubleMatrix) {
				SciDoubleMatrix rsMatrix = (SciDoubleMatrix) result;
				System.out.println(rsMatrix.toString());
			}
		}
		scilab.exit();
	}
	
	
	public SciTest2() throws Exception{
		SciTask task = new SciTask("id");
		task.setJobInit("n = 10;");
		task.addDataOut(new SciData("n"));
		task.setJob("n = n+1;");
		System.out.println("Job : " + task.getJob());
		
		ScilabService scilab = new ScilabService();
		
		scilab.getTaskObservable().addSciEventListener( new SciEventListener(){
			public void actionPerformed(SciEvent evt){
				SciTaskInfo sciTaskInfo = (SciTaskInfo) evt.getSource();
				
				if(sciTaskInfo.getState() == SciTaskInfo.SUCCESS){
					 displayResult(sciTaskInfo);
					return;
				}
			}
		});
		
		scilab.deployEngine( "ScilabVN", "ProActiveScilab.xml", new String[]{"Scilab1", "Scilab2"});
		scilab.sendTask(task);
		
	}
	
	
	public static void main(String[] args) throws Exception {
		new SciTest2();
	}

}
