package org.objectweb.proactive.ic2d.dgc.editparts;

import java.util.Observable;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.dgc.data.ObjectGraph;
import org.objectweb.proactive.ic2d.dgc.figures.DgcAOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.AOEditPart;


public class DgcAOEditPart extends AOEditPart {
    public DgcAOEditPart(ActiveObject model) {
        super(model);
    }

    @Override
    protected IFigure createFigure() {
        return new DgcAOFigure(getCastedModel().getName());
    }

    @Override
    protected Color getArrowColor() {
        return new Color(Display.getCurrent(), 0, 0, 255);
    }

    @Override
    public void update(Observable o, Object arg) {
        ObjectGraph.addObject((ActiveObject) o);
        ActiveObject model = this.getCastedModel();
        ((DgcAOFigure) super.getCastedFigure()).updateDgcState(model);
        super.update(o, arg);
    }
}
