package org.objectweb.proactive.p2p.peerconfiguration;

import java.beans.*;
import java.io.*;
import java.util.*;

/**
 * @author jbustos
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Daemon implements Runnable{

private ScheduleBean[] schedule;
private String loadBalancer;
private String p2pRegistry;
private String protocol;
private boolean isAlive;
private Process process;

public Daemon(String file, String lb) {
			// Read the XML descriptor and create the schedule

isAlive = false;
process = null;
		
XMLDecoder d;
			try {
				d =
					new XMLDecoder(
						new BufferedInputStream(new FileInputStream(file)));
			schedule = (ScheduleBean[]) d.readObject();
			p2pRegistry = (String) d.readObject();
			protocol = (String) d.readObject();
			d.close();
			} catch (FileNotFoundException e) {
				System.err.println("[Daemon ERROR] Cannot open "+file);
				System.exit(-1);
			}
		if (lb != null) {
			loadBalancer = lb;
			} else {
			loadBalancer = "./loadbalancer.sh"; 
			}
		if (schedule != null) this.run();
		}

public static void main(String[] args) {
	
	if (args.length < 2) {
		System.err.println("[Daemon ERROR] use: daemon    file_schedule.xml   load_balancer.exe");
		System.exit(-1);
	}
	Daemon daemon = new Daemon(args[0],args[1]);
	}
	
public synchronized void run() {
	while (true) {
		// First: check if hte load balancer was to work today:
		
		int i = 0;
		boolean hasToWork = true;
		for (i=0; i < schedule.length; i++) {
			hasToWork = hasToWork & schedule[i].WorkToday();
			} 
		
		if (hasToWork) work();
		else if (isAlive) killIt(); 
		
		// Then: Sleep until tomorrow
		Calendar today= Calendar.getInstance();

		long hour = today.get(Calendar.HOUR_OF_DAY);
		long minute = today.get(Calendar.MINUTE);
		
		long timeToSleep = (2400 - (hour*100 + minute))* 60 * 1000;
		
		try {
			wait(timeToSleep);
		} catch (InterruptedException e) {
		} 
	}
}

private synchronized void work() {
	int i = 0;
	long wakeUp = 0;
	List schedToday = new LinkedList(); 
	Calendar today = Calendar.getInstance();

	// First: get the schedule for today
	for (i=0; i< schedule.length; i++) {
		if (schedule[i].WorkToday()) schedToday.add(schedule[i]);
	}
	
	// Second: order the schedule by start time
	Object[] schedOrdered = schedToday.toArray();
	sortSchedule(schedOrdered);
	
	// Third: start & stop the application
	for (i=0; i< schedOrdered.length; i++) {
		int now = today.get(Calendar.HOUR_OF_DAY) *100 + today.get(Calendar.MINUTE);
		ScheduleBean schedNow = (ScheduleBean) schedOrdered[i];
		// wait for the next start time?
		if (schedNow.getStartTime() > now) {
				wakeUp = (schedNow.getStartTime() - now) * 60 * 1000 + 1;
				try {
					wait(wakeUp);
				} catch (InterruptedException e) {
					today = Calendar.getInstance();
					now = today.get(Calendar.HOUR_OF_DAY) *100 + today.get(Calendar.MINUTE);
				}
		}				

		// the right schedule?
		if (schedNow.getStartTime() + schedNow.getWorkTime() < now) continue;
		
		// start it!
		if (!isAlive) startIt(schedNow.getMaxLoad());

		// If the schedule < 24 hours, stop it
		
		if (schedNow.getWorkTime() < 2400  && isAlive) {
				wakeUp = (schedNow.getStartTime() + schedNow.getWorkTime() - now) * 60 * 1000;
				try {
					wait(wakeUp);
				} catch (InterruptedException e1) {
					if (isAlive) killIt();
				} 
			}
		}
}


/************
 * Sort the schedule array, using the start time
 */

private synchronized void sortSchedule(Object[] schedArray) {
	int i,j;
	
	if (schedArray.length < 2) return;

	Object aux = null;
	
	for (i=0; i< schedArray.length; i++) {
		
		for (j=i+1; j< schedArray.length;j++) {
			
			ScheduleBean sched_i = (ScheduleBean) schedArray[i];
			ScheduleBean sched_j = (ScheduleBean) schedArray[j];
			
			if (sched_j.getStartTime() < sched_i.getStartTime()) {
				aux = schedArray[i];
				schedArray[i] = schedArray[j];
				schedArray[j] = aux;
			}
		}
		
	}
}

/**********
 * Methods for start and stop the load balancer
 */

private synchronized void startIt(double maxload) {
	isAlive = true;	
	System.out.println("[Daemon] Started application");
	try {
		process = Runtime.getRuntime().exec(new String[] {loadBalancer,maxload+"",p2pRegistry,protocol});
	} catch (IOException e) {
		System.err.println("[Daemon ERROR] cannot start the scheduled application");
	}
}
private synchronized void killIt() {
	System.out.println("[Daemon] Application Stoped");
	isAlive = false;
	process.destroy();
}

/* (non-Javadoc)
 * @see java.lang.Object#finalize()
 */
protected void finalize() throws Throwable {
	killIt();
	super.finalize();
}

}
