package org.objectweb.proactive.ext.scilab.util;

import javasci.SciDoubleMatrix;

import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.monitor.SciEvent;
import org.objectweb.proactive.ext.scilab.monitor.SciEventListener;
import org.objectweb.proactive.ext.scilab.monitor.SciTaskInfo;
import org.objectweb.proactive.ext.scilab.monitor.ScilabService;


/**
 * This class is a listener for Pi calculation events in order to retrieve the result of the parallel computation
 */
public class PiEventListener implements SciEventListener{
	private FutureDoubleMatrix res;
	private int nbBloc;
	private int count;
	private double pi;
	private ScilabService service;
	
	public PiEventListener(ScilabService service, int nbBloc, FutureDoubleMatrix res){
		this.service = service;
		this.res = res;
		this.nbBloc = nbBloc;
	}
	
	public void actionPerformed(SciEvent evt){
		SciTaskInfo sciTaskInfo = (SciTaskInfo) evt.getSource();
		
		if(sciTaskInfo.getState() != SciTaskInfo.SUCCEEDED){
			/*if(sciTaskInfo.getState() == SciTaskInfo.ABORT){
				System.out.println("***************** Task:" + sciTaskInfo.getIdTask() + " ABORT ********************");
			}*/
			return;
		}
		
		System.out.println("IDTASK: " + sciTaskInfo.getIdTask() + " IDRES: " + res.getName());
		if(!sciTaskInfo.getIdTask().startsWith(res.getName())){
			return;
		}
		
		service.removeTask(sciTaskInfo.getIdTask());
		
		System.out.println("---------------- Task:" + sciTaskInfo.getIdTask() + " " + sciTaskInfo.getIdEngine() + " " + service.getMapTaskRun().size() +" SUCCESS -----------------");
		
		SciResult sciResult = sciTaskInfo.getSciResult();
		//System.out.println(sciTaskInfo.getTimeGlobal() +" " + sciResult.getTimeExecution());
		
		if(!sciResult.getId().startsWith(res.getName())){
			return;
		}
		
		SciDoubleMatrix sciData = (SciDoubleMatrix) sciResult.getList().get(0);
		pi += sciData.getData()[0];
		
		count++;
		
		//System.out.println("COUNT = " + count + "  NBBLOC = " + nbBloc);
		if(count == nbBloc){
			res.set(new double[]{pi});
			service.removeEventListenerEngine(this);
		}
	}
}