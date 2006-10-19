import org.objectweb.proactive.benchmarks.timit.util.TimerCounter;

public class Worker extends Timed {

	public TimerCounter T_TOTAL = new TimerCounter("total");
    public TimerCounter T_WORK = new TimerCounter("work");
    public TimerCounter T_COMM = new TimerCounter("comm");
    public CommEventObserver nbCommObserver;
               
    /** An empty no args constructor, as needed by ProActive */
    public Worker() {}

    public void start() {

    	// The nbCommObserver is used to build
        // the message density pattern, a chart will be built from
        // the gathered data.
        nbCommObserver = new CommEventObserver(
        		"communication pattern",
        		groupSize, rank,
        		new MatrixChartParameters(
        				"Communication pattern",
        				"Message density distribution",
        				"Receiver rank",
        				"Sender rank",
        				500,500,
        				"CommunicationPatternCount",
        				Chart.LINEAR_SCALE,
        				Chart.LEGEND_FORMAT_NONE));
        
        // Then, you have to specify all counters you want to activate. That
        // means all counters defined in this class, but also counter defined
        // in other class, thanks to the TimerStore instance.
        super.activate(
        		new TimerCounter[] { T_TOTAL, T_WORK }
        		new EventObserver[] { nbCommObserver }
        		);        


        // Start the total time counter
        T_TOTAL.start();
        
        	
        	T_COMM.start(); // Start the communication time counter
            // In this example each worker will send 10 messages to its neighbours            
            for(int i = 0; i<10; i++){
                destRank = (this.rank+1)%this.groupSize;                
                // Notification of the nbCommObserver observer
                super.notifyObservers(new CommEvent(nbCommObserver,destRank,1));                            
                // Perform the distant call
                this.workersArray[destRank].toto(i);
            }
            T_COMM.stop(); // stop the communication time counter
            
            // Start the working time counter
            T_WORK.start();
            	// Local work...
            T_WORK.stop();

        T_TOTAL.stop();
            
        // Finally, you have to say that timing is done by using finalizeTimed()
        // method. You can specify some textual informations about this worker.
        // This information will be shown in final XML result file.
        // Take care when using it with many nodes... :)        
        super.finalizeTimed(this.rank,"Worker"+rank+" is OK.");
    }
    
    /** Invoked by neighbours */
    public void toto(int x){
        return;
    }
}