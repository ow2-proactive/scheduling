package org.objectweb.proactive.core.util.profiling;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * The Engine used for profiling code
 * It creates profilers on request and keep them on a list
 * It also registers itself to run when the JVM shutdowns and dump the results of the profilers
 */
public class PAProfilerEngine implements Runnable {
    ArrayList profilerList = new ArrayList();
    private static PAProfilerEngine engine;

    static {
        engine = new PAProfilerEngine();
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(engine));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a profiler of default type
     * @return an AverageTimeProfiler
     */
    public static Timer createTimer() {
    	Timer tmp = new AverageMicroTimer();
    	registerTimer(tmp);
        return tmp;
    }

    /**
     * Add profilers to be managed by this profiler engine
     * @param papr
     */
    public static void registerTimer(Timer papr) {
        synchronized (engine.profilerList) {
            engine.profilerList.add(papr);
        }
    }
    
    /**
     * Remove a profiler from this engine
     * It's dump() method will thus never be called
     * @param papr
     * @return
     */
    public static boolean removeTimer(Timer papr) {
        synchronized (engine.profilerList) {
            return engine.profilerList.remove(papr);
        }
    }

    private PAProfilerEngine() {
    }

    /**
     * This method starts when a shutdown of the VM is initiated
     */
    public void run() {
    	dump();
    }
    
    /**
	 * Call dump on all profilers registered in this engine
	 */
	public void dump() {
		Iterator it = profilerList.iterator();
    	while (it.hasNext()) {
			( (Timer) it.next()).dump();
		}
	}

	public static void main(String[] args) {
		System.out.println("Creating a profiler and registering it");
		PAProfilerEngine.createTimer();
		System.out.println("Creating an AverageTimeProfiler and registering it");
		Timer avg = new AverageMicroTimer();
		PAProfilerEngine.registerTimer(avg);
	
		for (int i=0; i< 10;i++) {
			avg.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		avg.stop();
		}
		System.out.println("Now dying");	
	}
    
}
