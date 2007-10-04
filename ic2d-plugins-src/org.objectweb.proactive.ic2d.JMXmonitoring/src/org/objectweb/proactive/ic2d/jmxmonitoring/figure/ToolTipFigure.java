package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;


public class ToolTipFigure extends Figure {
    public ToolTipFigure(String text) {
        BorderLayout layout = new BorderLayout();
        setLayoutManager(layout);

        Label label = new Label();
        label.setText(text);
        add(label, BorderLayout.CENTER);
    }
}
