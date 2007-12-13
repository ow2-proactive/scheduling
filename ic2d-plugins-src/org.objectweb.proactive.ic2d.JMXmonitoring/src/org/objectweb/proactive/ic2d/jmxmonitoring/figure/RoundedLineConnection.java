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

import java.util.List;

import org.eclipse.draw2d.AnchorListener;
import org.eclipse.draw2d.ArrowLocator;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;


/**
 * An implementation of {@link Connection} based on RoundedLine.
 */
public class RoundedLineConnection extends RoundedLine implements Connection, AnchorListener {
    // Use to display the topology
    private boolean oldState;
    private ConnectionAnchor endAnchorSave;

    /**
     * The connection anchors
     */
    private ConnectionAnchor startAnchor;

    /**
     * The connection anchors
     */
    private ConnectionAnchor endAnchor;

    /**
     * The road to follow
     */
    private ConnectionRouter connectionRouter = ConnectionRouter.NULL;

    /**
     * The decorations
     */
    private RotatableDecoration startArrow;

    /**
     * The decorations
     */
    private RotatableDecoration endArrow;

    /**
     * Decorates connection with an arrow.
     */
    private RotatableDecoration targetDecoration;
    {
        setLayoutManager(new DelegatingLayout());
        addPoint(new Point(0, 0));
        addPoint(new Point(100, 100));
    }

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RoundedLineConnection(Color c) {
        super();
        targetDecoration = initDecoration();
        setTargetDecoration(targetDecoration);
        this.oldState = RoundedLine.DEFAULT_DISPLAY_TOPOLOGY;
        setLineWidth(drawingStyleSize());
        setForegroundColor(c);
        setLineStyle(Graphics.LINE_SOLID);
    }

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //

    /**
     * @see Shape#outlineShape(Graphics)
     */
    @Override
    protected void outlineShape(Graphics g) {
        super.outlineShape(g);
        if (RoundedLine.displayTopology() == oldState) {
            return;
        } else {
            oldState = RoundedLine.displayTopology();
            if (oldState) {
                endAnchor = endAnchorSave;
                add(endArrow);
            } else {
                endAnchor = null;
                remove(endArrow);
            }
            this.repaint();
        }
    }

    /**
     * Hooks the source and target anchors.
     * @see Figure#addNotify()
     */
    @Override
    public void addNotify() {
        super.addNotify();
        hookSourceAnchor();
        hookTargetAnchor();
    }

    /**
     * Called by the anchors of this connection when they have moved, revalidating this
     * roundedline connection.
     * @param anchor the anchor that moved
     */
    public void anchorMoved(ConnectionAnchor anchor) {
        revalidate();
    }

    /**
     * Returns the bounds which holds all the points in this roundedline connection. Returns any
     * previously existing bounds, else calculates by unioning all the children's
     * dimensions.
     * @return the bounds
     */
    @Override
    public Rectangle getBounds() {
        if (bounds == null) {
            super.getBounds();
            for (int i = 0; i < getChildren().size(); i++) {
                IFigure child = (IFigure) getChildren().get(i);
                bounds.union(child.getBounds());
            }
        }
        return bounds;
    }

    /**
     * Returns the <code>ConnectionRouter</code> used to layout this connection. Will not
     * return <code>null</code>.
     * @return this connection's router
     */
    public ConnectionRouter getConnectionRouter() {
        return connectionRouter;
    }

    /**
     * Returns this connection's routing constraint from its connection router.  May return
     * <code>null</code>.
     * @return the connection's routing constraint
     */
    public Object getRoutingConstraint() {
        if (getConnectionRouter() != null) {
            return (List) getConnectionRouter().getConstraint(this);
        } else {
            return null;
        }
    }

    /**
     * @return the anchor at the start of this roundedline connection (may be null)
     */
    public ConnectionAnchor getSourceAnchor() {
        return startAnchor;
    }

    /**
     * @return the anchor at the end of this roundedline connection (may be null)
     */
    public ConnectionAnchor getTargetAnchor() {
        return endAnchor;
    }

    /**
     * Layouts this roundedline. If the start and end anchors are present, the connection router
     * is used to route this, after which it is laid out. It also fires a moved method.
     */
    public void layout() {
        if ((getSourceAnchor() != null) && (getTargetAnchor() != null)) {
            connectionRouter.route(this);
        }

        Rectangle oldBounds = bounds;
        super.layout();
        bounds = null;

        if (!getBounds().contains(oldBounds)) {
            getParent().translateToParent(oldBounds);
            getUpdateManager().addDirtyRegion(getParent(), oldBounds);
        }

        repaint();
        fireFigureMoved();
    }

    /**
     * Called just before the receiver is being removed from its parent. Results in removing
     * itself from the connection router.
     *
     * @since 2.0
     */
    public void removeNotify() {
        unhookSourceAnchor();
        unhookTargetAnchor();
        connectionRouter.remove(this);
        super.removeNotify();
    }

    /**
     *
     */
    @Override
    public void revalidate() {
        super.revalidate();
        getConnectionRouter().invalidate(this);
    }

    /**
     * Sets the connection router which handles the layout of this roundedline. Generally set by
     * the parent handling the roundedline connection.
     * @param cr the connection router
     */
    public void setConnectionRouter(ConnectionRouter cr) {
        if (cr == null) {
            cr = ConnectionRouter.NULL;
        }
        if (connectionRouter != cr) {
            connectionRouter.remove(this);
            Object old = connectionRouter;
            connectionRouter = cr;
            firePropertyChange(Connection.PROPERTY_CONNECTION_ROUTER, old, cr);
            revalidate();
        }
    }

    /**
     * Sets the routing constraint for this connection.
     * @param cons the constraint
     */
    public void setRoutingConstraint(Object cons) {
        if (connectionRouter != null) {
            connectionRouter.setConstraint(this, cons);
        }
        revalidate();
    }

    /**
     * Sets the anchor to be used at the start of this roundedline connection.
     * @param anchor the new source anchor
     */
    public void setSourceAnchor(ConnectionAnchor anchor) {
        if (anchor == startAnchor) {
            return;
        }
        unhookSourceAnchor();
        //No longer needed, revalidate does this.
        //getConnectionRouter().invalidate(this);
        startAnchor = anchor;
        if (getParent() != null) {
            hookSourceAnchor();
        }
        revalidate();
    }

    /**
     * Sets the decoration to be used at the start of the {@link Connection}.
     * @param dec the new source decoration
     * @since 2.0
     */
    public void setSourceDecoration(RotatableDecoration dec) {
        if (startArrow == dec) {
            return;
        }
        if (startArrow != null) {
            remove(startArrow);
        }
        startArrow = dec;
        if (startArrow != null) {
            add(startArrow, new ArrowLocator(this, ConnectionLocator.SOURCE));
        }
    }

    /**
     * Sets the anchor to be used at the end of the roundedline connection. Removes this listener
     * from the old anchor and adds it to the new anchor.
     * @param anchor the new target anchor
     */
    public void setTargetAnchor(ConnectionAnchor anchor) {
        if (anchor == endAnchor) {
            return;
        }
        unhookTargetAnchor();
        //No longer needed, revalidate does this.
        //getConnectionRouter().invalidate(this);
        endAnchor = anchor;
        if (endAnchor != null) {
            endAnchorSave = endAnchor;
        }
        if (getParent() != null) {
            hookTargetAnchor();
        }
        revalidate();
    }

    /**
     * Sets the decoration to be used at the end of the {@link Connection}.
     * @param dec the new target decoration
     */
    public void setTargetDecoration(RotatableDecoration dec) {
        if (endArrow == dec) {
            return;
        }
        if (endArrow != null) {
            remove(endArrow);
        }
        endArrow = dec;
        if (endArrow != null) {
            add(endArrow, new ArrowLocator(this, ConnectionLocator.TARGET));
        }
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     * @return the source decoration (may be null)
     */
    protected RotatableDecoration getSourceDecoration() {
        return startArrow;
    }

    /**
     * @return the target decoration (may be null)
     *
     * @since 2.0
     */
    protected RotatableDecoration getTargetDecoration() {
        return endArrow;
    }

    //
    // -- PRIVATED METHODS -------------------------------------------
    //
    private void unhookSourceAnchor() {
        if (getSourceAnchor() != null) {
            getSourceAnchor().removeAnchorListener(this);
        }
    }

    private void unhookTargetAnchor() {
        if (getTargetAnchor() != null) {
            getTargetAnchor().removeAnchorListener(this);
        }
    }

    /**
     * Initialize a decoration
     * @return A target decoration
     */
    private RotatableDecoration initDecoration() {
        RotatableDecoration decoration;
        PointList points = new PointList();
        points.addPoint(-1, 1);
        points.addPoint(0, 0);
        points.addPoint(-1, -1);
        decoration = new PolygonDecoration();
        ((PolygonDecoration) decoration).setTemplate(points);
        return decoration;
    }

    private void hookSourceAnchor() {
        if (getSourceAnchor() != null) {
            getSourceAnchor().addAnchorListener(this);
        }
    }

    private void hookTargetAnchor() {
        if (getTargetAnchor() != null) {
            getTargetAnchor().addAnchorListener(this);
        }
    }
}
