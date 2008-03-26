package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolygonDecoration;


/**
 * A decoration for the connection between active objects
 * @author The ProActive Team
 *
 */
public class ConnectionArrowDecoration extends PolygonDecoration {

    private boolean visible = true;

    public ConnectionArrowDecoration() {
        super();
    }

    @Override
    public void outlineShape(Graphics g) {
        if (!visible) {
            return;
        }
        super.outlineShape(g);

    }

    public void fillShape(Graphics g) {
        if (!visible) {
            return;
        }
        super.fillShape(g);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
