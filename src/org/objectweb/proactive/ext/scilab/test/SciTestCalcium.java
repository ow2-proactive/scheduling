package org.objectweb.proactive.ext.scilab.test;

import java.io.Serializable;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.calcium.Calcium;
import org.objectweb.proactive.calcium.ResourceManager;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.exceptions.ParameterException;
import org.objectweb.proactive.calcium.interfaces.Execute;
import org.objectweb.proactive.calcium.interfaces.Skeleton;
import org.objectweb.proactive.calcium.proactive.ProActiveManager;
import org.objectweb.proactive.calcium.skeletons.Farm;
import org.objectweb.proactive.calcium.skeletons.Seq;
import org.objectweb.proactive.calcium.statistics.StatsGlobal;
import org.objectweb.proactive.ext.scilab.SciEngineWorker;
import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.SciTask;

public class SciTestCalcium implements Serializable{

	private Skeleton<SciTask> root;
	private String descriptorPath;
	private String nameVN;
	
	private class SciExecute implements Execute<SciTask>{
		public SciTask execute(SciTask sciTask) {
			SciResult sciResult = SciEngineWorker.executeTask(sciTask);
			sciTask.setListDataOut(sciResult.getList());
			return sciTask;
		}
	}
	
	public SciTestCalcium(String nameVN, String descriptorPath){
		this.nameVN = nameVN;
		this.descriptorPath = descriptorPath;
	}
	
	public void solve(){
		ResourceManager manager= new ProActiveManager(descriptorPath, nameVN);
		//ResourceManager manager= new MultiThreadedManager(1);
		
		this.root = new Farm<SciTask>(new Seq<SciTask>(new SciExecute()));
		Calcium<SciTask> calcium = new Calcium<SciTask>(manager, root);
		
		SciTask sciTask;
		
		for(int i=0; i<10; i++){
			sciTask = new SciTask("ScilabTask" + i);
			SciDoubleMatrix sciMatrix = new SciDoubleMatrix("M", 1, 1, new double[]{i});
			
			sciTask.addDataIn(sciMatrix);
			sciTask.addDataOut(sciMatrix);
			sciTask.setJob(sciMatrix.getName() + "=" +  sciMatrix.getName() + "* 2;");
			calcium.inputParameter(sciTask);
		}
		
		calcium.eval();

		
		try {
			SciTask res = calcium.getResult();
			while(res != null){
				for(int i=0; i< res.getListDataOut().size(); i++){
					SciData sciData = (SciData) res.getListDataOut().get(i);
					System.out.println(sciData.toString());
				}
				res = calcium.getResult();
			}
			
			
		} catch (ParameterException e) {
			e.printStackTrace();
		} catch (PanicException e) {
			e.printStackTrace();
		}
		
		StatsGlobal stats = calcium.getStatsGlobal();
		System.out.println(stats);
	}
	
	public static void main(String[] args) {
		if(args.length !=2){
			System.out.println("Invalid number of parameter : " + args.length);
			return;
		}
		
		SciTestCalcium st = new SciTestCalcium(args[0], args[1]);
		st.solve();
	}
	

}
