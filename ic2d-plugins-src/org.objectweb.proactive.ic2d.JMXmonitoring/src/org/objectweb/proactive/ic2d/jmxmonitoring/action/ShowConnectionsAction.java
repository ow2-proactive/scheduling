package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AbstractFigure;


public class ShowConnectionsAction extends Action {

    public static final String SHOW_CONNECTIONS = "Show connections";
    private AbstractFigure object;

    public ShowConnectionsAction() {
        this.setId(SHOW_CONNECTIONS);
        // this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "stop_monitoring.gif"));
    }

    public void setObject(AbstractFigure figure) {
        this.object = figure;
        String showOrHide = "Hide";
        if (!object.getShowConnections())
            showOrHide = "Show";
        this.setText(showOrHide + " connections for this object");
        this.setToolTipText(showOrHide + "  connections for this object");
    }

    @Override
    public void run() {
        object.switchShowConnections();
        if (object.getShowConnections()) {
            object.setHighlight(new Color(AOFigure.device, 150, 0, 255));
        } else
            object.setHighlight(null);
    }

}
