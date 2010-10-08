package org.ow2.proactive.resourcemanager.gui.topology.prefuse;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import prefuse.controls.ControlAdapter;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;


/**
 * Interactive drag control that is "aggregate-aware"
 */
public class AggregateDragControl extends ControlAdapter {

    private VisualItem activeItem;
    protected Point2D down = new Point2D.Double();
    protected Point2D temp = new Point2D.Double();
    protected boolean dragged;

    /**
     * Creates a new drag control that issues repaint requests as an item is
     * dragged.
     */
    public AggregateDragControl() {
    }

    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        prefuse.Display d = (prefuse.Display) e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        if (!(item instanceof AggregateItem))
            setFixed(item, true);
    }

    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    public void itemExited(VisualItem item, MouseEvent e) {
        if (activeItem == item) {
            activeItem = null;
            setFixed(item, false);
        }
        prefuse.Display d = (prefuse.Display) e.getSource();
        d.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    public void itemPressed(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e))
            return;
        dragged = false;
        prefuse.Display d = (prefuse.Display) e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), down);
        if (item instanceof AggregateItem)
            setFixed(item, true);
    }

    /**
     * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    public void itemReleased(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e))
            return;
        if (dragged) {
            activeItem = null;
            setFixed(item, false);
            dragged = false;
        }
    }

    /**
     * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
     *      java.awt.event.MouseEvent)
     */
    public void itemDragged(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e))
            return;
        dragged = true;
        prefuse.Display d = (prefuse.Display) e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX() - down.getX();
        double dy = temp.getY() - down.getY();

        move(item, dx, dy);

        down.setLocation(temp);
    }

    protected static void setFixed(VisualItem item, boolean fixed) {
        if (item instanceof AggregateItem) {
            Iterator items = ((AggregateItem) item).items();
            while (items.hasNext()) {
                setFixed((VisualItem) items.next(), fixed);
            }
        } else {
            item.setFixed(fixed);
        }
    }

    protected static void move(VisualItem item, double dx, double dy) {
        if (item instanceof AggregateItem) {
            Iterator items = ((AggregateItem) item).items();
            while (items.hasNext()) {
                move((VisualItem) items.next(), dx, dy);
            }
        } else {
            double x = item.getX();
            double y = item.getY();
            item.setStartX(x);
            item.setStartY(y);
            item.setX(x + dx);
            item.setY(y + dy);
            item.setEndX(x + dx);
            item.setEndY(y + dy);
        }
    }

} // end of class AggregateDragControl