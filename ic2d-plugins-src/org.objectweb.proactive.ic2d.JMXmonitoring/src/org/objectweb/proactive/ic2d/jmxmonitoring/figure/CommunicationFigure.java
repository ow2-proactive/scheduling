package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.WorldEditPart;


public class CommunicationFigure extends PolylineConnection {
    private boolean showMe = false;
    private boolean oldState;
    private int lineWidth = 1;

    protected void outlineShape(Graphics g) {
        g.setAntialias(SWT.ON);
        //	super.outlineShape(g);
        g.setLineWidth(lineWidth);

        if (!WorldEditPart.displayTopology()) {
            //We check if this connection has to be drawn

            IFigure startFigure = this.getSourceAnchor().getOwner();
            IFigure endFigure = this.getTargetAnchor().getOwner();
            showMe = false;
            if (startFigure instanceof AOFigure) {
                if (((AOFigure) startFigure).getShowConnections())
                    showMe = true;
            }

            if (endFigure instanceof AOFigure) {
                if (((AOFigure) endFigure).getShowConnections())
                    showMe = true;
            }
        }

        if (WorldEditPart.displayTopology() || showMe) {
            this.getTargetDecoration().setVisible(true);
            super.outlineShape(g);
            //TODO: fix method drawShape and use it instead super.outlineShape(g)
            ///in order to drwaw arcs instead of lines
            //  drawShape(g);

        } else {
            this.getTargetDecoration().setVisible(false);
        }

        if (WorldEditPart.displayTopology() == oldState) {
            return;
        } else {
            oldState = WorldEditPart.displayTopology();

            //            if (oldState) {
            //                endAnchor = endAnchorSave;
            //                add(endArrow);
            //            } else {
            //                endAnchor = null;
            //                remove(endArrow);
            //            }
            //  this.repaint();
            super.outlineShape(g);
            //TODO: fix method drawShape and use it instead super.outlineShape(g)
            ///in order to drwaw arcs instead of lines
            //drawShape(g);
        }

    }//outlineshape

    private void drawShape(Graphics g) {
        PointList pointList = getPoints();
        Point source = pointList.getFirstPoint();
        Point target = pointList.getLastPoint();

        drawArc(g, source, target);
    }

    private void drawArc(Graphics g, Point source, Point target) {
        g.setLineWidth(1);

        // Sets the anti-aliasing
        g.setAntialias(SWT.ON);

        int xSource = source.x;
        int ySource = source.y;

        int xTarget = target.x;
        int yTarget = target.y;

        // Shape changing...
        int distanceY = Math.abs(ySource - yTarget);
        int distanceX = Math.abs(xSource - xTarget);
        int shapeX = distanceX / 3;
        int shapeY = distanceY / 3;

        if ((xSource == xTarget) && (ySource == yTarget)) { //TODO not yet tested
            g.drawOval(xSource - shapeY - 25, ySource + 7, 30, 15);
        } else {
            if (xSource == xTarget) {
                if (ySource > yTarget) {
                    this.setBounds(new Rectangle(xTarget - shapeY, yTarget, shapeY * 2, Math.abs(ySource -
                        yTarget)));
                    g.drawArc(xTarget - shapeY, yTarget, shapeY * 2, Math.abs(ySource - yTarget), 90, -180);

                    //g.drawRectangle(this.getBounds());
                } else {
                    this.setBounds(new Rectangle(xSource - shapeY, ySource, shapeY * 2, Math.abs(ySource -
                        yTarget)));
                    g.drawArc(xSource - shapeY, ySource, shapeY * 2, Math.abs(ySource - yTarget), 90, 180);

                }
            } else if (ySource == yTarget) {
                if (xSource > xTarget) {
                    this.setBounds(new Rectangle(xTarget, yTarget - (shapeX / 4),
                        Math.abs(xSource - xTarget), shapeX / 2));
                    g.drawArc(xTarget, yTarget - (shapeX / 4), Math.abs(xSource - xTarget), shapeX / 2, 0,
                            180);
                    //g.drawRectangle(this.getBounds());
                } else {
                    this.setBounds(new Rectangle(xSource, ySource - (shapeX / 4),
                        Math.abs(xSource - xTarget), shapeX / 2));
                    g.drawArc(xSource, ySource - (shapeX / 4), Math.abs(xSource - xTarget), shapeX / 2, 0,
                            -180);
                    // g.drawRectangle(this.getBounds());
                }
            } else {
                g.drawLine(source, target);

            }
        }
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public Rectangle getBounds() {
        return super.getBounds();
        //	        if (bounds == null) {
        //	            super.getBounds();
        //	            for (int i = 0; i < getChildren().size(); i++) {
        //	                IFigure child = (IFigure) getChildren().get(i);
        //	                bounds.union(child.getBounds());
        //	            }
        //	        }
        //	        return bounds;
    }

}
