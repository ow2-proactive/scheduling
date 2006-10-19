package org.objectweb.proactive.ic2d.dgc.figures;

import org.eclipse.draw2d.Label;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.monitoring.figures.AOFigure;


class DgcLabel extends Label {
    private String text;

    public DgcLabel(String text) {
        super(text);
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public String getSubStringText() {
        return getText();
    }

    public void setText(String text) {
        this.text = text;
    }
}


public class DgcAOFigure extends AOFigure {
    public DgcAOFigure(String text) {
        super(text);
        this.label = new DgcLabel(text + "\nDGC_STATE");
        this.initFigure();
    }

    public void updateDgcState(AOObject model) {
        NodeObject node = (NodeObject) model.getParent();
        String state = node.getSpy().getDgcState(model.getID());
        ((DgcLabel) this.label).setText(model.getFullName() + "\n" + state);
    }
}
