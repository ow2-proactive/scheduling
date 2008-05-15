package org.objectweb.proactive.ic2d.chartit.editparts;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.chartit.canvas.AbstractCachedCanvas;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.IChartModelListener;


/**
 * Uses canvas, non-GEF based implementation.
 * @author vbodnart
 *
 * @param <M>
 * @param <C>
 */
public abstract class AbstractChartItEditPart<C extends AbstractCachedCanvas> implements IChartItEditPart {

    /**
     * The model of this edit part
     */
    protected final ChartModel chartModel;

    /**
     * The used canvas
     */
    protected C canvas;

    /**
     * Creates an new instance of edit part with the specified model
     * @param chartModel
     */
    public AbstractChartItEditPart(final ChartModel dataElementModel) {
        this.chartModel = dataElementModel;
    }

    /**
     * Subclass must fill the client with a specific canvas.
     * Warning ! 
     */
    public abstract void fillSWTCompositeClient(final Composite client, final int style);

    /**
     * Activates this edit part and registers a dispose listener to the canvas
     */
    public void init() {
        // Activate this edit part
        this.activate();

        // Add a dispose listener to deactivate this edit part
        this.canvas.addDisposeListener(new DisposeListener() {
            public final void widgetDisposed(final DisposeEvent e) {
                deactivate();
            }
        });
    }

    /**
     * Returns the model
     * @return
     */
    public ChartModel getModel() {
        return this.chartModel;
    }

    /**
     * Returns the canvas
     * @return
     */
    public C getCanvas() {
        return this.canvas;
    }

    /**
     * 
     */
    public void activate() {
        this.chartModel.setChartModelListener(this);
    }

    /**
     * 
     */
    public void deactivate() {
        this.chartModel.setChartModelListener(null);
    }

    public void modelChanged(int type, Object oldValue, Object newValue) {
        if (type == IChartModelListener.CHANGED) {
            Display.getDefault().asyncExec(this);
        }
    }
}
