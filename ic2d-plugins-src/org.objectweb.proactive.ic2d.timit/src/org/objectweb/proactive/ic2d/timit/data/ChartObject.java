package org.objectweb.proactive.ic2d.timit.data;

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
    public static final String[] PROACTIVE_LEVEL_TIMERS_NAMES = new String[] {
            "Total", "Serve", "SendRequest", "SendReply", "WaitByNecessity",
            "WaitForRequest"
        };
    protected BarChartBuilder barChartBuilder;
    protected ChartContainerObject parent;
    protected Collection<BasicTimer> timersCollection;
    protected AOObject aoObject;
    protected ChartEditPart ep;
    protected boolean hasChanged;

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
    public Chart provideChart() {
        if (this.hasChanged) {
            this.hasChanged = false;
            return this.barChartBuilder.createChart(this.timersCollection);
        }
        return this.barChartBuilder.chart;
    }

    /**
     * Performs a snapshot on the associated active object and refreshes
     * the edit part.
     */
    public void performSnapshot() {
        Collection<BasicTimer> availableTimersCollection = ChartObject.performSnapshotInternal(this.aoObject);
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
     * Performs a snapshot on timers of a remote active object
     * @param aoObject The reference on the remote active object
     * @return A collection of BasicTimer
     */
    protected static final Collection<BasicTimer> performSnapshotInternal(
        final AOObject aoObject) {
        try {
            Spy spy = ((NodeObject) aoObject.getParent()).getSpy();
            Collection<BasicTimer> availableTimersCollection = spy.getTimersSnapshotFromBody(aoObject.getID(),
                    ChartObject.PROACTIVE_LEVEL_TIMERS_NAMES);
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
}
