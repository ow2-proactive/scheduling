package org.objectweb.proactive.ic2d.dgc.editparts;

import org.eclipse.gef.EditPart;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.MonitoringEditPartFactory;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class DgcIC2DEditPartFactory extends MonitoringEditPartFactory {
    public DgcIC2DEditPartFactory(MonitoringView monitoringView) {
        super(monitoringView);
    }

    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof ActiveObject) {
            return new DgcAOEditPart((ActiveObject) model);
        }

        return super.createEditPart(context, model);
    }
}
