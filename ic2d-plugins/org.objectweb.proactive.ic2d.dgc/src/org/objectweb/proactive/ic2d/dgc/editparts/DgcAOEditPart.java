package org.objectweb.proactive.ic2d.dgc.editparts;

import java.util.Observable;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.dgc.data.DgcAOObjectWrapper;
import org.objectweb.proactive.ic2d.dgc.data.ObjectGraph;
import org.objectweb.proactive.ic2d.dgc.figures.DgcAOFigure;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.State;
import org.objectweb.proactive.ic2d.monitoring.editparts.AOEditPart;


public class DgcAOEditPart extends AOEditPart {
    public DgcAOEditPart(AOObject model) {
        super(model);
    }

    protected IFigure createFigure() {
        return new DgcAOFigure(getCastedModel().getFullName());
    }

	protected Color getArrowColor() {
		return new Color(Display.getCurrent(), 0, 0, 255);
	}
    
    public void update(Observable o, Object arg) {
    	ObjectGraph.addObject((AOObject) o);
        if (arg == State.NOT_MONITORED) {
            AOObject model = this.getCastedModel();
            ((DgcAOFigure) super.getCastedFigure()).updateDgcState(model);
            super.update(o, arg);
        } else if (arg instanceof DgcAOObjectWrapper) {
            AOObject ao = ((DgcAOObjectWrapper) arg).getWrappedObject();
            super.update(o, ao);
        }
    }
}
