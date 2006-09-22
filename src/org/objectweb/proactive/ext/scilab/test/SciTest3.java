package org.objectweb.proactive.ext.scilab.test;

import java.util.HashMap;
import java.util.Vector;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.ext.scilab.SciDeployEngine;
import org.objectweb.proactive.ext.scilab.SciEngine;
import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.SciTask;

public class SciTest3 {
	private String arrayEngine[];
	private HashMap mapEngine;
	private int countEngine = 0;
	
	public SciTest3(String nameVN, String pathDescriptor, String[] arrayEngine){
		
		Vector listStateEngine = new Vector();
		this.arrayEngine = arrayEngine;
		SciData m1 = new SciDoubleMatrix("a",1, 1, new double[]{10});
	    SciData m2 = new SciDoubleMatrix("b",1, 1, new double[]{20});
	    SciData m3 = new SciData("x");
	    SciData m4 = new SciData("y");
			
	    //Deployment
	    mapEngine = SciDeployEngine.deploy(nameVN, pathDescriptor, arrayEngine);
		
	    //Activation
		for(int i=0; i<mapEngine.size(); i++){
			listStateEngine.add(((SciEngine) mapEngine.get(arrayEngine[i])).activate());
		}
		
		ProActive.waitForAll(listStateEngine);
		
		for(int i=0; i<mapEngine.size(); i++){
			if(!((BooleanWrapper)listStateEngine.get(i)).booleanValue()){
				System.out.println("->Activation Error");
				return;
			}
		}
		
		//Computation
		SciEngine sciEngine;
		
		SciTask task1 = new SciTask("id1");
		task1.addDataIn(m1);
		task1.addDataIn(m2);
		task1.addDataOut(m3);
		task1.setJob("x = a+b;");
		
		sciEngine = (SciEngine) mapEngine.get(this.getNextEngine());
		
		//asynchronous call
		SciResult result1 = sciEngine.execute(task1);
		
		SciTask task3 = new SciTask("id3");
		task3.addDataIn(m1);
		task3.addDataOut(m1);
		task3.setJob("a = a*2;");
		
		sciEngine = (SciEngine) mapEngine.get(this.getNextEngine());
        //asynchronous call
		SciResult result3 = sciEngine.execute(task3);
		
		
		SciTask task2 = new SciTask("id2");
		
		//wait value
		task2.addDataIn(result1.get("x"));
		task2.addDataIn(m2);
		task2.addDataOut(m4);
		task2.setJob("y = x+b;");
		
		sciEngine = (SciEngine) mapEngine.get(this.getNextEngine());
        //asynchronous call
		SciResult result2 = sciEngine.execute(task2);
		
		System.out.println(result1.get("x").toString());
		System.out.println(result3.get("a").toString());
		System.out.println(result2.get("y").toString());
		
		//SciDeployEngine.killAll();
		System.exit(0);
	}
	
	
	//get id of the nex engine 
	public String getNextEngine(){
		countEngine++;
		if(countEngine == mapEngine.size()){
			countEngine = 0;
		}
		return arrayEngine[countEngine];
	}
	
	public static void main(String[] args) {
		if(args.length != 3){
			System.out.println("Invalid number of parameter : " + args.length);
			return;
		}
		
		int nbEngine = Integer.parseInt(args[2]);
		String arrayId[] = new String[nbEngine];
		
		for(int i=0; i< nbEngine; i++){
			arrayId[i] = "Scilab" + i;
		}
		
		new SciTest3(args[0], args[1], arrayId);
	}
}
