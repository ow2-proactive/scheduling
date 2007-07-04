package org.objectweb.proactive.ic2d.timit.data;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.birt.chart.model.Chart;
import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.monitoring.spy.Spy;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.editparts.ChartEditPart;


/**
 * This class represents the model of a chart.
 *
 * @author vbodnart
 *
 */
public class ChartObject {
    public static final boolean DEBUG = false;
    public static final String[] PROACTIVE_BASIC_LEVEL_TIMERS_NAMES = new String[] {
            "Total", "Serve", "SendRequest", "SendReply", "WaitByNecessity",
            "WaitForRequest"
        };
    
    public static final String[] PROACTIVE_DETAILED_LEVEL_TIMERS_NAMES = new String[] {
        "Total", "Serve", "SendRequest", "SendReply", "WaitByNecessity",
        "WaitForRequest", "LocalCopy", "BeforeSerialization", "Serialization", "AfterSerialization", "GroupOneWayCall", "GroupAsyncCall"
    };
    
    protected BarChartBuilder barChartBuilder;
    protected ChartContainerObject parent;
    protected Collection<BasicTimer> timersCollection;
    protected AOObject aoObject;
    protected ChartEditPart ep;
    protected boolean hasChanged;
    protected String[] currentTimerLevel = PROACTIVE_BASIC_LEVEL_TIMERS_NAMES;
    

    public ChartObject(ChartContainerObject parent,
        Collection<BasicTimer> timersCollection, AOObject aoObject) {
        this.parent = parent;
        this.timersCollection = timersCollection;
        this.hasChanged = true;
        this.aoObject = aoObject;
        this.barChartBuilder = new BarChartBuilder((this.aoObject == null)
                ? "Unknown name" : this.aoObject.getFullName());

        this.parent.addChild(this);
    }

    /**
     * Provides the cached or created chart.
     * @return The created or cached chart.
     */
    public final Chart provideChart() {
        if (this.hasChanged) {
            this.hasChanged = false;
            // Filter by names
            Collection<BasicTimer> newCol = new ArrayList<BasicTimer>();
            for( BasicTimer b : this.timersCollection ){
            	if ( contains(this.currentTimerLevel, b.getName() ) ){
            		newCol.add(b);
            		//System.out.println("ChartObject.provideChart() _____ " + b.getName());
            	}
            }
            return this.barChartBuilder.createChart(newCol, this.currentTimerLevel);
        }
        return this.barChartBuilder.chart;
    }      
    
    public String getTimerLevel(){
    	return ( this.currentTimerLevel.equals(PROACTIVE_BASIC_LEVEL_TIMERS_NAMES) ? "Basic" : "Detailed" );
    }
    
    public void switchTimerLevel(){
    	if ( this.currentTimerLevel == PROACTIVE_BASIC_LEVEL_TIMERS_NAMES ){
    		this.currentTimerLevel = PROACTIVE_DETAILED_LEVEL_TIMERS_NAMES;
    	} else {
    		this.currentTimerLevel = PROACTIVE_BASIC_LEVEL_TIMERS_NAMES;
    	}    	    	
    	this.performSnapshot();
    	
    }

    /**
     * Performs a snapshot on the associated active object and refreshes
     * the edit part.
     */
    public void performSnapshot() {
        Collection<BasicTimer> availableTimersCollection = ChartObject.performSnapshotInternal(this.aoObject, this.currentTimerLevel);
        if (availableTimersCollection != null) {
            this.timersCollection = availableTimersCollection;
            this.hasChanged = true;
            this.ep.asyncRefresh();
        }
    }

    /**
     * Returns this uniqueId of the associated active object
     * @return The uniqueId of the active object
     */
    public UniqueID getAoObjectID() {
        return this.aoObject.getID();
    }

    /**
     * Returns the parent of this
     * @return
     */
    public ChartContainerObject getParent() {
        return parent;
    }

    /**
     * A setter for the parent object
     * @param parent
     */
    public void setParent(ChartContainerObject parent) {
        this.parent = parent;
    }

    /**
     * A getter for hashChanged
     * @return hashChanged value
     */
    public boolean getHasChanged() {
        return hasChanged;
    }

    /**
     * A setter for hasChanged
     * @param hasChanged
     */
    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    /**
     * A setter for the current editPart
     * @param ep
     */
    public void setEp(ChartEditPart ep) {
        this.ep = ep;
    }
    
    /**
     * A getter for the current editPart
     * @return ep
     */
    public ChartEditPart getEp(){
    	return this.ep;
    }

    /**
     * Performs a snapshot on timers of a remote active object
     * @param aoObject The reference on the remote active object
     * @return A collection of BasicTimer
     */
    protected static final Collection<BasicTimer> performSnapshotInternal(
        final AOObject aoObject, String[] timerLevel) {
        try {
            Spy spy = ((NodeObject) aoObject.getParent()).getSpy();
            Collection<BasicTimer> availableTimersCollection = spy.getTimersSnapshotFromBody(aoObject.getID(),
            		timerLevel);
            if ((availableTimersCollection == null) ||
                    (availableTimersCollection.size() == 0)) {
                Console.getInstance(Activator.CONSOLE_NAME)
                       .log("There is no available timers for " +
                    aoObject.getFullName());
                return null;
            }
            return availableTimersCollection;
        } catch (Exception e) {
            Console console = Console.getInstance(Activator.CONSOLE_NAME);
            console.log("Cannot perform timers snapshot on " +
                aoObject.getFullName() + ". Reason : " + e.getMessage());
            if (e instanceof NullPointerException) {
                console.log(
                    "Be sure to attach a TimIt technical service to the virtual node : " +
                    aoObject.getParent().getParent().getFullName());
            }

            // e.printStackTrace();
        }
        return null;
    }
    
    /**
     * A predicate that returns true if the string val is contained
     * in the array.
     * @param arr An array of strings
     * @param val A String
     * @return True if val is contained in arr
     */
    private final static boolean contains(String[] arr, String val) {
        boolean res = false;
        for (String x : arr) {
            if (val.equals(x)) {
                res = true;
            }
        }
        return res;
    }
}
