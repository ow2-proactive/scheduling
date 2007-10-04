package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.dialog.SetUpdateFrequenceDialog;


public class SetUpdateFrequenceAction extends Action {
    public static final String SET_UPDATE_FREQUENCE = "Set update frequence";
    private Display display;
    private NodeObject node;

    public SetUpdateFrequenceAction(Display display) {
        this.setId(SET_UPDATE_FREQUENCE);
        this.display = display;
        this.setText("Set Update Frequence...");
        this.setToolTipText("Set Update Frequence");
    }

    public void setNode(NodeObject node) {
        this.node = node;
    }

    @Override
    public void run() {
        new SetUpdateFrequenceDialog(display.getActiveShell(), node);
    }
}
