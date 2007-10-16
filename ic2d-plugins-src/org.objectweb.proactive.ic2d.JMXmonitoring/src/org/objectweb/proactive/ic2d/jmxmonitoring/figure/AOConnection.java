/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;


public class AOConnection {
    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * Connects two figure by a connection, only if there isn't already a connection.
     * @param panel The panel containing the two figures.
     * @param source The source figure.
     * @param target The target figure.
     * @param sourceID The source ID.
     * @param targetID The target ID.
     */
    public static RoundedLineConnection createConnection(AOFigure source,
        AOFigure target, Color color) {
        RoundedLineConnection connection = new RoundedLineConnection(color);

        Point sourceCenter = source.getLocation()
                                   .getTranslated(source.getBounds().width / 2,
                source.getBounds().height / 2);
        Point targetCenter = target.getLocation()
                                   .getTranslated(target.getBounds().width / 2,
                target.getBounds().height / 2);

        Position position = getPosition(sourceCenter, targetCenter);

        Anchor sourceAnchor = (Anchor) source.getAnchor();
        Anchor targetAnchor = (Anchor) target.getAnchor();

        sourceAnchor.useRelativePosition(position);
        targetAnchor.useRelativePosition(position);

        connection.setSourceAnchor(sourceAnchor);
        connection.setTargetAnchor(targetAnchor);

        BendpointConnectionRouter router = new BendpointConnectionRouter();

        List<RelativeBendpoint> bendPoints = new ArrayList<RelativeBendpoint>();

        RelativeBendpoint middle = calculPoint(connection, sourceCenter,
                targetCenter, position);
        if (middle != null) {
            bendPoints.add(middle);
        }

        router.setConstraint(connection, bendPoints);

        connection.setConnectionRouter(router);

        return connection;
    }

    /**
     * Calculate a relative point in order to display the arc of circle
     * @param connection The connection
     * @param source The source of connection
     * @param target The target of connection
     * @param position The relative position of the target to the source
     */
    private static RelativeBendpoint calculPoint(Connection connection,
        Point source, Point target, Position position) {
        double distance = source.getDistance(target);
        RelativeBendpoint point = new RelativeBendpoint(connection);
        int value = (int) (0.4 * distance);
        if (source == target) { // If the source and the target are the same point
            position = Position.NORTH;
            value = 90;
        }
        switch (position) {
        case SAME:
        case NORTH:
            point.setRelativeDimensions(new Dimension(value, 0),
                new Dimension(value, 0));
            break;
        case SOUTH:
            point.setRelativeDimensions(new Dimension(-value, 0),
                new Dimension(-value, 0));
            break;
        case EAST:
            point.setRelativeDimensions(new Dimension(0, (value / 2) + 50),
                new Dimension(0, (value / 2) + 50));
            break;
        case WEST:
            point.setRelativeDimensions(new Dimension(0, -((value / 2) + 50)),
                new Dimension(0, -((value / 2) + 50)));
            break;
        default:
            return null;
        }
        return point;
    }

    /**
     * Calculates the relative position of the B Point to the A Point.
     */
    private static Position getPosition(Point A, Point B) {
        if (A.x == B.x) {
            if (A.y < B.y) {
                return Position.SOUTH;
            } else if (A.y > B.y) {
                return Position.NORTH;
            } else {
                return Position.SAME;
            }
        } else if (A.y == B.y) {
            if (A.x < B.x) {
                return Position.EAST;
            } else if (A.x > B.x) {
                return Position.WEST;
            }
        }
        return Position.UNKNOWN;
    }
}
