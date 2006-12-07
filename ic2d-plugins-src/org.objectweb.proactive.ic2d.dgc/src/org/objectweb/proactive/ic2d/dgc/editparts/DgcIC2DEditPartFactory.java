package org.objectweb.proactive.ic2d.dgc.editparts;

import org.eclipse.gef.EditPart;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.editparts.MonitoringEditPartFactory;
import org.objectweb.proactive.ic2d.monitoring.views.MonitoringView;


public class DgcIC2DEditPartFactory extends MonitoringEditPartFactory {
	
    public DgcIC2DEditPartFactory(MonitoringView monitoringView) {
		super(monitoringView);
	}

	public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof AOObject) {
            return new DgcAOEditPart((AOObject) model);
        }

        return super.createEditPart(context, model);
    }
}
