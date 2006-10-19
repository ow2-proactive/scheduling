package org.objectweb.proactive.ic2d.monitoring.editparts;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.monitoring.figures.AbstractFigure;
import org.objectweb.proactive.ic2d.monitoring.views.MonitoringView;

public abstract class AbstractMonitoringEditPart extends AbstractGraphicalEditPart implements Observer {

	
	//
	// -- CONSTRUCTORS -----------------------------------------------
	//
	
	public AbstractMonitoringEditPart(AbstractDataObject model) {
		setModel(model);
	}
	
	
	//
	// -- PUBLICS METHODS -----------------------------------------------
	//
	
	/**
	 * When an EditPart is added to the EditParts tree
	 * and when its figure is added to the figure tree,
	 * the method EditPart.activate() is called.
	 */
	public void activate(){
		if (!isActive())
			((AbstractDataObject)getModel()).addObserver(this);
		super.activate();
	}
	
	/**
	 * When an EditPart is removed from the EditParts
	 * tree, the method deactivate() is called.
	 */
	public void deactivate(){
		if (isActive()) {
			((AbstractDataObject)getModel()).deleteObserver(this);
			((Figure)getFigure()).removeAll();
//			if(getFigure().getParent() != null)
//				getFigure().getParent().remove(getFigure());
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
		Display.getDefault().asyncExec(new Runnable() {
			public void run () {
				refresh();		
			}
		});
	}
	
	@Override
	public IFigure getContentPane() {
		return ((AbstractFigure)getFigure()).getContentPane();
	}

//	public void removeChildVisual(EditPart childEditPart) {
//		super.removeChildVisual(childEditPart);
//	}

	/**
	 * Returns the monitoring view. Or null if the parent of this object is null,
	 * or if its parent isn't an instance of AbstractMonitoringEditPart.
	 */
	public MonitoringView getMonitoringView(){
		EditPart parent = getParent();
		if(parent == null || !(parent instanceof AbstractMonitoringEditPart))
			return null;
		else
			return ((AbstractMonitoringEditPart) parent).getMonitoringView();
	}
}
