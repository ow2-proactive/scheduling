package org.objectweb.proactive.ic2d.chronolog.editparts;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.ic2d.chronolog.canvas.AbstractCachedCanvas;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;


/**
 * Uses canvas, non-GEF based implementation.
 * @author vbodnart
 *
 * @param <M>
 * @param <C>
 */
public abstract class AbstractChronologEditPart<M extends AbstractTypeModel<?>, C extends AbstractCachedCanvas>
        implements IChronologEditPart<M> {

    /**
     * The model of this edit part
     */
    protected final M dataElementModel;

    /**
     * The used canvas
     */
    protected C canvas;

    /**
     * Creates an new instance of edit part with the specified model
     * @param dataElementModel
     */
    public AbstractChronologEditPart(final M dataElementModel) {
        this.dataElementModel = dataElementModel;
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
    public M getModel() {
        return this.dataElementModel;
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
        this.dataElementModel.getPropertyChangeSupport().addPropertyChangeListener(this);
    }

    /**
     * 
     */
    public void deactivate() {
        this.dataElementModel.getPropertyChangeSupport().removePropertyChangeListener(this);
    }
}
