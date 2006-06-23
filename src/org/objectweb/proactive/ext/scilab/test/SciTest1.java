package org.objectweb.proactive.ext.scilab.test;

import java.io.File;
import java.util.ArrayList;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.ext.scilab.SciDeployEngine;
import org.objectweb.proactive.ext.scilab.SciEngineWorker;
import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.SciTask;

public class SciTest1 {

	public static void main(String[] args) throws Exception {
		//initialized dispatcher engine
				
        SciData m1 = new SciDoubleMatrix("a",1, 1, new double[]{15});
        SciData m2 = new SciDoubleMatrix("b",1, 1, new double[]{23});
        SciData m3 = new SciDoubleMatrix("x",1, 1);
		
		SciTask task = new SciTask("id");
		task.addDataIn(m1);
		task.addDataIn(m2);
		task.addDataIn(m3);
		task.addDataOut(m3);
		task.setJob("x = a+b;");
		
		SciEngineWorker engine = SciDeployEngine.deploy("Scilab1");
		BooleanWrapper isActivate = engine.activate();
		
		
		if(isActivate.booleanValue()){
			System.out.println("->Scilab engine is not activate");
		}
		SciResult sciResult = engine.execute(task);
		System.out.println("->test1");
	
		
		
		ArrayList listResult = sciResult.getList();
		
		System.out.println("->test2");
		
		SciData result;
		for(int i=0; i<listResult.size(); i++){
			result = (SciData)listResult.get(i);
			System.out.println(result);
		}
	
		
		System.out.println("->test3");
		engine.exit();
	}
}
