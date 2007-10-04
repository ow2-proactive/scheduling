package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AbstractFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public abstract class AbstractMonitoringEditPart
    extends AbstractGraphicalEditPart implements Observer, Runnable {
    protected static Map<AbstractData, AbstractMonitoringEditPart> registry = new HashMap<AbstractData, AbstractMonitoringEditPart>();
    private WorldEditPart worldEditPart;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public AbstractMonitoringEditPart(AbstractData model) {
        setModel(model);
        registry.put(model, this);
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * When an EditPart is added to the EditParts tree
     * and when its figure is added to the figure tree,
     * the method EditPart.activate() is called.
     */
    public void activate() {
        if (!isActive()) {
            getCastedModel().addObserver(this);
        }
        super.activate();
    }

    /**
     * When an EditPart is removed from the EditParts
     * tree, the method deactivate() is called.
     */
    public void deactivate() {
        if (isActive()) {
            getCastedModel().deleteObserver(this);
            ((Figure) getFigure()).removeAll();
        }
        super.deactivate();
    }

    /**
     * This method is called whenever the observed object is changed.
     * It calls the method <code>refresh()</code>.
     * @param o the observable object (instance of AbstractDataObject).
     * @param arg an argument passed to the notifyObservers  method.
     */
    public void update(Observable o, Object arg) {
        getViewer().getControl().getDisplay().asyncExec(this);
    }

    @Override
    public IFigure getContentPane() {
        return ((AbstractFigure) getFigure()).getContentPane();
    }

    /**
     * Returns the monitoring view. Or null if the parent of this object is null,
     * or if its parent isn't an instance of AbstractMonitoringEditPart.
     */
    public MonitoringView getMonitoringView() {
        return getWorldEditPart().getMonitoringView();
    }

    /**
     * Returns the current World Edit Part
     * @return The WorldEditPart, or null if the parent of this object is null.
     */
    public WorldEditPart getWorldEditPart() {
        if (worldEditPart == null) {
            worldEditPart = ((AbstractMonitoringEditPart) getParent()).getWorldEditPart();
        }
        return worldEditPart;
    }

    public void run() {
        refresh();
    }

    public void addGraphicalCommunication(GraphicalCommunication communication) {
        getWorldEditPart().addGraphicalCommunication(communication);
    }

    public void addFigureToUpdtate(IFigure figure) {
        getWorldEditPart().addFigureToUpdtate(figure);
    }

    /**
     * Convert the result of EditPart.getModel()
     * to the real type of the model.
     * @return the casted model
     */
    public abstract <T extends AbstractData> T getCastedModel();

    /**
     * Convert the result of EditPart.getFigure()
     * to the real type of the figure.
     * @return the casted figure
     */
    public abstract <T extends IFigure> T getCastedFigure();
}
