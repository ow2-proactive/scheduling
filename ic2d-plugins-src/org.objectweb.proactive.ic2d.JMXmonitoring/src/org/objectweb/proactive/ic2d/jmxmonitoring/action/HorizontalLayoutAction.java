package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.HostFigure;


public class HorizontalLayoutAction extends Action {
    public static final String HORIZONTAL_LAYOUT = "horizontal layout";
    private HostFigure host;

    public HorizontalLayoutAction() {
        super("Horizontal", IAction.AS_RADIO_BUTTON);
        this.setId(HORIZONTAL_LAYOUT);
    }

    public void setHost(HostFigure host) {
        this.host = host;
    }

    @Override
    public void run() {
        host.setHorizontalLayout();
    }
}
