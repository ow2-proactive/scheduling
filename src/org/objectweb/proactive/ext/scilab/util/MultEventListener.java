package org.objectweb.proactive.ext.scilab.util;

import javasci.SciDoubleMatrix;

import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.monitor.SciEvent;
import org.objectweb.proactive.ext.scilab.monitor.SciEventListener;
import org.objectweb.proactive.ext.scilab.monitor.SciTaskInfo;
import org.objectweb.proactive.ext.scilab.monitor.ScilabService;

/**
 * This class is a listener for matrix multiplication events in order to retrieve 
 * the result of the parallel computation
 */

public class MultEventListener implements SciEventListener{
	private FutureDoubleMatrix res;
	private ScilabService service;
	private int count;
	private double matrixResult[];
	private int nbTask;
	private int sizeSubMatrix;
	
	public MultEventListener(ScilabService service, FutureDoubleMatrix res){
		this.service = service;
		this.res = res;
		this.nbTask = service.getNbEngine();
		matrixResult = new double[res.getNbRow() * res.getNbCol()];
		sizeSubMatrix = res.getNbRow() * res.getNbCol()/nbTask;
	}
	
	public void actionPerformed(SciEvent evt){
		SciTaskInfo sciTaskInfo = (SciTaskInfo) evt.getSource();
		if(sciTaskInfo.getState() != SciTaskInfo.SUCCEEDED){
			if(sciTaskInfo.getState() == SciTaskInfo.ABORTED){
				System.out.println("---------------- Task:" + sciTaskInfo.getIdTask() + " ABORT -----------------");
			}
			return;
		}
		
		System.out.println("IDTASK: " + sciTaskInfo.getIdTask() + " IDRES: " + res.getName());
		if(!sciTaskInfo.getIdTask().startsWith(res.getName())){
			return;
		}
		
		service.removeTask(sciTaskInfo.getIdTask());
		
		System.out.println("---------------- Task:" + sciTaskInfo.getIdTask() +" SUCCESS -----------------");
		
		SciResult sciResult = sciTaskInfo.getSciResult();
		//System.out.println(sciTaskInfo.getTimeGlobal() +" " + sciResult.getTimeExecution());
		
		SciDoubleMatrix sciSubMatrix = (SciDoubleMatrix) sciResult.getList().get(0);
		int iTask = Integer.parseInt(sciSubMatrix.getName().substring(1));
		double [] subMatrix = sciSubMatrix.getData();
		
		for(int i=0; i<sizeSubMatrix ; i++){
			matrixResult[i + iTask * sizeSubMatrix] = subMatrix[i];
		}
		
		count++;
        //System.out.println("COUNT = " + count + "  NBTASK = " + nbTask);
		if(count == nbTask){
			res.set(matrixResult);
			service.removeEventListenerEngine(this);
		}
	}
}
