package org.objectweb.proactive.ext.scilab.util;

import javasci.SciDoubleMatrix;

import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.monitor.SciEvent;
import org.objectweb.proactive.ext.scilab.monitor.SciEventListener;
import org.objectweb.proactive.ext.scilab.monitor.SciTaskInfo;
import org.objectweb.proactive.ext.scilab.monitor.ScilabService;

/**
 * This class is a listener for Mandelbrot events in order to retrieve the result of the parallel computation
 */
public class MandelbrotEventListener implements SciEventListener{
	private FutureDoubleMatrix res;
	private ScilabService service;
	private int nbBloc;
	private int sizeBloc;
	private int count;
	private double matrixResult[];
	
	public MandelbrotEventListener(ScilabService service, int nbBloc, FutureDoubleMatrix res){
		this.service = service;
		this.res = res;
		this.nbBloc = nbBloc;
		this.sizeBloc = (res.getNbRow()/nbBloc) * res.getNbCol();
		matrixResult = new double[res.getNbRow() * res.getNbCol()];
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
		
		SciDoubleMatrix sciMatrix = (SciDoubleMatrix) sciResult.getList().get(0);
		int iBloc = Integer.parseInt(sciMatrix.getName().substring(res.getName().length()));
		//System.out.println(iBloc + "\n "+ sciMatrix);
		double array[] = sciMatrix.getData();
		int pos = iBloc * sizeBloc;
		
		
		for(int i= 0; i< sizeBloc; i++){
			matrixResult[pos + i] = array[i];
		}
		
		count++;
		//System.out.println("COUNT = " + count + "  NBBLOC = " + nbBloc);
		if(count == nbBloc){
			res.set(matrixResult);
			service.removeEventListenerEngine(this);
		}
	}

}
