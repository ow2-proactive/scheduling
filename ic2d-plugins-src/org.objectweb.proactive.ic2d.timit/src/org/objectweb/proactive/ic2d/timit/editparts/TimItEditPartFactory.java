package org.objectweb.proactive.ic2d.timit.editparts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.objectweb.proactive.ic2d.timit.data.ChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


public class TimItEditPartFactory implements EditPartFactory {
    private TimItView timItView;

    public TimItEditPartFactory(TimItView t) {
        this.timItView = t;
    }

    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof ChartContainerObject) {
            return new ChartContainerEditPart((ChartContainerObject) model,
                this.timItView);
        } else if (model instanceof ChartObject) {
            return new ChartEditPart((ChartObject) model);
        } else {
            return null;
        }
    }
}
