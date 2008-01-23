package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolygonDecoration;


/**
 * A decoration for the connection between active objects
 * @author ProActiveTeam
 *
 */
public class ConnectionArrowDecoration extends PolygonDecoration {

    public ConnectionArrowDecoration() {
        super();
    }

    @Override
    public void outlineShape(Graphics g) {
        if (!RoundedLine.displayTopology())
            return;
        super.outlineShape(g);

    }

    public void fillShape(Graphics g) {
        if (!RoundedLine.displayTopology())
            return;
        super.fillShape(g);
    }

}
