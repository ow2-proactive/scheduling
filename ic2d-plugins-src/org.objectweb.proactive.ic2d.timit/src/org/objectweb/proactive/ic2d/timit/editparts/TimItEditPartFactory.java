package org.objectweb.proactive.ic2d.timit.editparts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.objectweb.proactive.ic2d.timit.data.ChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;


public class TimItEditPartFactory implements EditPartFactory {
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof ChartContainerObject) {
            return new ChartContainerEditPart((ChartContainerObject) model);
        } else if (model instanceof ChartObject) {
            return new ChartEditPart((ChartObject) model);
        } else {
            return null;
        }
    }
}
