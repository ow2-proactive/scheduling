public class Launcher implements Startable {
    
    /** TimIt needs an noarg constructor (can be implicit) */
    public Launcher() {}
    
    /** The main method, not used by TimIt */
    public static void main( String[] args ) {
        new Launcher().start(args);
    }
    
    /** TimIt will invoke this method to start your application */
    public void start( String[] args ) {
        try {
            // Common stuff about ProActive deployement
            ...
            
        	// Creation of a group of Timed objects
            Worker workers = ProSPMD.newSPMDGroup(...);
            
            // You must create a TimItManager instance and give it
            // a typed group of Timed workers.
            TimItManager tManager = new TimItManager(workers);
            
            // Workers starts their job
            workers.start();
            
            // ... and finalize the TimIt.
            // Note: you don't have to wait for the end of your workers
            tManager.finalizeStats();
            
            // Print the final result
            System.out.println(tManager.getBenchmarkStatistics());
            
        } catch (Exeption e) { e.printStackTrace(); }
    }
    
    /** Invoked by TimIt between each run */
    public void kill() {
    	// kill all active objects
        workers.terminate();
    }
    
    /** Invoked by TimIt between each benchmark */
	public void masterKill() {
		// kill all jvms
		pad.killall(false);
	}
}