/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.gui;

import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.gui.data.ActiveObjectPanel;

import java.awt.RenderingHints;


public class ActiveObjectCommunicationRecorder {
    public static final int PROPORTIONAL_DRAWING_STYLE = 1;
    public static final int RATIO_DRAWING_STYLE = 2;
    public static final int FILAIRE_DRAWING_STYLE = 3;
    private static final java.awt.Color STROKE_COLOR = new java.awt.Color(0, 0,
            0, 125);
    private static final int MAX_STROKE_WIDTH_RATIO = 12;
    private static final int MAX_STROKE_WIDTH_PROPORTIONAL = 80;
    private java.util.HashMap panelToPanelsMap;
    private java.util.HashMap activeObjectToPanelMap;
    private int maxCommunicationCounter = 1;
    private int drawingStyle;
    private boolean enabled;
    private boolean antiAlias = true;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public ActiveObjectCommunicationRecorder() {
        panelToPanelsMap = new java.util.HashMap();
        activeObjectToPanelMap = new java.util.HashMap();
        enabled = true;
        drawingStyle = FILAIRE_DRAWING_STYLE;
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public void recordCommunication(ActiveObjectPanel source,
        ActiveObjectPanel dest) {
        //System.out.println("ActiveObjectCommunicationRecorder.recordCommunication e="+enabled+" s="+source.isDestroyed()+" d="+dest.isDestroyed());
        if (!enabled) {
            return;
        }
        if (source.isDestroyed() || dest.isDestroyed()) {
            return;
        }

        // try to find a mapping source <-> dest
        java.util.HashMap destMap = (java.util.HashMap) panelToPanelsMap.get(source);

        /*    if (destMap == null) { // leads to weird results???
           // try to find a mapping dest <-> source
           destMap = (java.util.HashMap) panelToPanelsMap.get(dest);
           if (destMap != null) {
             // if we did find a mapping dest <-> source we swap source and dest
             ActiveObjectPanel temp = source;
             source = dest;
             dest = temp;
           }
           } */
        if (destMap == null) {
            // new association source <-> dest
            destMap = new java.util.HashMap();
            destMap.put(dest, new int[] { 1 });
            synchronized (panelToPanelsMap) {
                panelToPanelsMap.put(source, destMap);
                activeObjectToPanelMap.put(source.getActiveObject(), source);
            }
        } else {
            // existing source
            int[] existingCounter = (int[]) destMap.get(dest);
            if (existingCounter == null) {
                // new destination
                synchronized (destMap) {
                    destMap.put(dest, new int[] { 1 });
                }
                synchronized (panelToPanelsMap) {
                    activeObjectToPanelMap.put(dest.getActiveObject(), dest);
                }
            } else {
                // existing destination
                existingCounter[0]++;
                if (existingCounter[0] > maxCommunicationCounter) {
                    maxCommunicationCounter = existingCounter[0];
                }
            }
        }
    }

    public void removeActiveObjectPanel(ActiveObjectPanel object) {
        //System.out.println("ActiveObjectCommunicationRecorder.removeActiveObjectPanel object="+object);
        synchronized (panelToPanelsMap) {
            // remove the target as a source
            panelToPanelsMap.remove(object);
            // remove the mapping ActiveObject - Panel
            activeObjectToPanelMap.remove(object.getActiveObject());
            // remove the target as a destination
            java.util.Iterator iterator = panelToPanelsMap.values().iterator();
            while (iterator.hasNext()) {
                java.util.HashMap destMap = (java.util.HashMap) iterator.next();
                synchronized (destMap) {
                    destMap.remove(object);
                }
            }
        }
    }

    public void removeActiveObject(ActiveObject object) {
        //System.out.println("ActiveObjectCommunicationRecorder.removeActiveObject object="+object);
        synchronized (panelToPanelsMap) {
            ActiveObjectPanel panel = (ActiveObjectPanel) activeObjectToPanelMap.get(object);
            if (panel == null) {
                // cannot find a panel mapping this ActiveObject
            } else {
                removeActiveObjectPanel(panel);
            }
        }
    }

    public void setDrawingStyle(int drawingStyle) {
        switch (drawingStyle) {
        case PROPORTIONAL_DRAWING_STYLE:
            this.drawingStyle = drawingStyle;
            break;
        case RATIO_DRAWING_STYLE:
            this.drawingStyle = drawingStyle;
            break;
        case FILAIRE_DRAWING_STYLE:
            this.drawingStyle = drawingStyle;
            break;
        default:
            throw new IllegalArgumentException(
                "The number passed is not a known drawing style");
        }
    }

    public int getDrawingStyle() {
        return drawingStyle;
    }

    public void clear() {
        //System.out.println("ActiveObjectCommunicationRecorder.clear");
        synchronized (panelToPanelsMap) {
            maxCommunicationCounter = 1;
            panelToPanelsMap.clear();
            activeObjectToPanelMap.clear();
        }
    }

    public void setEnabled(boolean b) {
        if (b == enabled) {
            return;
        }
        enabled = b;
        if (!enabled) {
            clear();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxCommunicationCounter() {
        return maxCommunicationCounter;
    }

    public java.util.Iterator iterator() {
        return new SourceIterator();
    }

    public void drawAllLinks(java.awt.Graphics g,
        java.awt.Point topLeftCornerScreenCoordinate) {
        //System.out.println("ActiveObjectCommunicationRecorder.drawAllLinks");
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        java.awt.Stroke oldStroke = g2.getStroke();
        float ratio = 0;
        switch (drawingStyle) {
        case PROPORTIONAL_DRAWING_STYLE:
            // proportional link : we draw has tick as the number of communication registered
            // up the the max tickness
            if (maxCommunicationCounter > MAX_STROKE_WIDTH_PROPORTIONAL) {
                ratio = ((float) MAX_STROKE_WIDTH_PROPORTIONAL) / maxCommunicationCounter;
            } else {
                ratio = 1;
            }
            break;
        case RATIO_DRAWING_STYLE:
            // the more numerous communication is the max tick. The others are smaller
            ratio = ((float) MAX_STROKE_WIDTH_RATIO) / maxCommunicationCounter;
            break;
        case FILAIRE_DRAWING_STYLE:default:
            // just draw 1 pixel tick line
            ratio = -1;
            break;
        }

        //anti alias added ebe 07/2004
        if (antiAlias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        }

        //System.out.println("ratio = "+ratio+"  proportionalLinks="+proportionalLinks+" maxCommunicationCounter="+maxCommunicationCounter);
        java.util.Iterator sourceEntryIterator = panelToPanelsMap.entrySet()
                                                                 .iterator();

        // iterating on sources
        synchronized (panelToPanelsMap) {
            while (sourceEntryIterator.hasNext()) {
                java.util.Map.Entry sourceEntry = (java.util.Map.Entry) sourceEntryIterator.next();

                // source
                ActiveObjectPanel sourcePanel = (ActiveObjectPanel) sourceEntry.getKey();
                if (sourcePanel.isDestroyed() || !sourcePanel.isVisible()) {
                    continue;
                }
                java.util.HashMap destMap = (java.util.HashMap) sourceEntry.getValue();
                java.util.Iterator destEntryIterator = destMap.entrySet()
                                                              .iterator();
                synchronized (destMap) {
                    drawOneSourceLinks(destEntryIterator, g2,
                        topLeftCornerScreenCoordinate, oldStroke, ratio,
                        sourcePanel);
                }
            } // end while
        }
    } // end method

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void drawOneSourceLinks(java.util.Iterator destEntryIterator,
        java.awt.Graphics2D g2, java.awt.Point topLeftCornerScreenCoordinate,
        java.awt.Stroke oldStroke, float ratio, ActiveObjectPanel sourcePanel) {
        // iterating on destinations
        java.awt.Point pSource = sourcePanel.getLocationOnScreen();
        int xSource = pSource.x - topLeftCornerScreenCoordinate.x;
        int ySource = pSource.y - topLeftCornerScreenCoordinate.y;
        int sourceWidth = sourcePanel.getWidth();
        while (destEntryIterator.hasNext()) {
            java.util.Map.Entry destEntry = (java.util.Map.Entry) destEntryIterator.next();

            // destination
            ActiveObjectPanel destPanel = (ActiveObjectPanel) destEntry.getKey();
            if (destPanel.isDestroyed() || !destPanel.isVisible()) {
                continue;
            }
            int communicationCount = ((int[]) destEntry.getValue())[0];
            java.awt.Point pDest = destPanel.getLocationOnScreen();
            int xDest = pDest.x - topLeftCornerScreenCoordinate.x;
            int yDest = pDest.y - topLeftCornerScreenCoordinate.y;
            int destWidth = destPanel.getWidth();

            // drawing line
            g2.setPaint(STROKE_COLOR);
            float strokeWidth;
            if (ratio == -1) {
                strokeWidth = 1.5f;
            } else {
                strokeWidth = (communicationCount * ratio) + 1;
            }
            g2.setStroke(new java.awt.BasicStroke(strokeWidth));
            boolean sameNode = sourcePanel.getActiveObject().isInsideSameNode(destPanel.getActiveObject());
//            System.out.println(nb + "------------------------------" );
//            System.out.println("source: " + sourcePanel.getActiveObject().toString());
//            System.out.println("dest: " + destPanel.getActiveObject().toString());
//            System.out.println("Source x,y: " + xSource + ","  + ySource);
//            System.out.println("Dest x,y: " + xDest + ","  + yDest);
//            System.out.println("---");
           if (sameNode) {
                drawOneArcSameNode(xSource, ySource, sourceWidth, xDest, yDest,
                    destWidth, g2);
            } else {
                drawOneArcDifferentNode(xSource, ySource, sourceWidth, xDest,
                    yDest, destWidth, g2);
            }

            g2.setStroke(oldStroke);
            g2.setPaint(java.awt.Color.black);
        } // end while
    }

    private void drawArrowHead(int xSource, int ySource, int xDest, int yDest,
        java.awt.Graphics2D g2, boolean alignVertic, boolean upWay) {
        double angle;
        if (xSource == xDest) {
            angle = Math.PI / 2;
        } else {
            angle = Math.atan((yDest - ySource) / ((double) xDest - xSource));
        }
        if (xDest < xSource) {
            angle += Math.PI;
        }

        // ebe specific for arc draw left and right
        if (alignVertic == true) {
            if (upWay == true) {
                angle = Math.PI;
            } else {
                angle = 0;
            }
        }

        g2.drawLine(xDest, yDest,
            xDest - (int) (Math.cos(angle - (Math.PI / 4)) * 6),
            yDest - (int) (Math.sin(angle - (Math.PI / 4)) * 6));
        g2.drawLine(xDest, yDest,
            xDest - (int) (Math.cos(angle + (Math.PI / 4)) * 6),
            yDest - (int) (Math.sin(angle + (Math.PI / 4)) * 6));
        g2.drawLine(xDest - (int) (Math.cos(angle + (Math.PI / 4)) * 6),
            yDest - (int) (Math.sin(angle + (Math.PI / 4)) * 6),
            xDest - (int) (Math.cos(angle - (Math.PI / 4)) * 6),
            yDest - (int) (Math.sin(angle - (Math.PI / 4)) * 6));
    }

    private void drawCommunicationPointDifferentNode(int xSource, int ySource,
        int sourceWidth, int xDest, int yDest, int destWidth,
        java.awt.Graphics2D g2, boolean alignVertic, boolean upWay) {
        // Look for the good corner to join...
        if (alignVertic == true) {
            if (upWay == true) {
                xDest += destWidth; //left draw	
            } else { //right draw
            }
        } else {
            if (Math.abs(xSource - xDest) > Math.abs((xSource + sourceWidth) -
                        xDest)) {
                xSource += sourceWidth;
            }
            if (Math.abs(xDest - xSource) > Math.abs((xDest + destWidth) -
                        xSource)) {
                xDest += destWidth;
            }
        }

        //    g2.fillOval(xDest - 4, yDest + 9, 8, 8);  // xySource before, but that was obviously wrong
        drawArrowHead(xSource, ySource + 13, xDest, yDest + 13, g2,
            alignVertic, upWay);
    }

    private void drawCommunicationPointSameNode(int xSource, int ySource,
        int sourceWidth, int xDest, int yDest, int destWidth,
        java.awt.Graphics2D g2) {
        // draw a little black circle meaning : com point
        if (ySource > yDest) {
            //      g2.fillOval(xDest - 4 + destWidth, yDest + 9, 8, 8);
        	// no longer a black circle but an outlined arrow
            drawArrowHead(xDest + destWidth + 100, yDest + 13,
                xDest + destWidth, yDest + 13, g2, false, false);
        } else {
            //      g2.fillOval(xDest - 4, yDest + 9, 8, 8);
            drawArrowHead(xDest - 100, yDest + 13, xDest, yDest + 13, g2,
                false, false);
        }
    }

    private void drawOneArcDifferentNode(int xSource, int ySource,
        int sourceWidth, int xDest, int yDest, int destWidth,
        java.awt.Graphics2D g2) {
        //		System.out.println("-->source : x=" + xSource + " y=" + ySource + " width=" + sourceWidth);
        //		System.out.println("-->destin  : x=" + xDest + " y=" + yDest + " width=" + destWidth);
        boolean alignVertic = false;
        boolean upWay = false;

        // align vertical -> arc arrow
        if (xSource == xDest) {
            alignVertic = true;
            int shape = Math.abs(ySource - yDest) / 3;

            // com way : 
            if (ySource > yDest) { //up  -> right draw
                upWay = true;
                //g2.setColor(java.awt.Color.GREEN);
                g2.drawArc(xSource - shape + sourceWidth, yDest + 13,
                    shape * 2, Math.abs(ySource - yDest), 90, -180);
            } else { //down  left Draw
                upWay = false;
                //g2.setColor(java.awt.Color.ORANGE);
                g2.drawArc(xSource - shape, ySource + 13, shape * 2,
                    Math.abs(ySource - yDest), 90, 180);
            }
        } else { // align Horizontal line arrow
            alignVertic = false;
            //			 Look for the good corner to join...
            int tmpxSource = xSource;
            int tmpxDest = xDest;
            if (Math.abs(xSource - xDest) > Math.abs((xSource + sourceWidth) -
                        xDest)) {
                tmpxSource = tmpxSource + sourceWidth;
            }
            if (Math.abs(xDest - xSource) > Math.abs((xDest + destWidth) -
                        xSource)) {
                tmpxDest = tmpxDest + destWidth;
            }
            g2.drawLine(tmpxSource, ySource + 13, tmpxDest, yDest + 13);
        }
        drawCommunicationPointDifferentNode(xSource, ySource, sourceWidth,
            xDest, yDest, destWidth, g2, alignVertic, upWay);
    }

    private void drawOneArcSameNode(int xSource, int ySource, int sourceWidth,
        int xDest, int yDest, int destWidth, java.awt.Graphics2D g2) {
        //Shape changing...
    	int shape = Math.abs(ySource - yDest) / 3;
    	  // ebe 13 sept 2004 AO can call themselves -> jacobi examples
    	if ((xSource == xDest) && (ySource == yDest)) {
    		g2.drawOval(xSource - shape -25, ySource+7 , 22, 15);
    	
		}
    	else {
    	   
        if (ySource > yDest) {
            g2.drawArc(xSource - shape + sourceWidth, yDest + 13, shape * 2,
                Math.abs(ySource - yDest), 90, -180);
        } else {
            g2.drawArc(xSource - shape, ySource + 13, shape * 2,
                Math.abs(ySource - yDest), 90, 180);
        }
    	}
        drawCommunicationPointSameNode(xSource, ySource, sourceWidth, xDest,
            yDest, destWidth, g2);
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    private class SourceIterator implements java.util.Iterator {
        private java.util.Iterator myIterator;

        public SourceIterator() {
            myIterator = panelToPanelsMap.values().iterator();
        }

        public boolean hasNext() {
            return myIterator.hasNext();
        }

        public Object next() {
            return ((java.util.HashMap) myIterator.next()).values().iterator();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    } // end inner class SourceIterator

    /**
     * @param antiAlias The antiAlias to set.
     */
    public void setAntiAlias(boolean antiAlias) {
        this.antiAlias = antiAlias;
    }

    /**
     * @return Returns the antiAlias.
     */
    public boolean isAntiAlias() {
        return antiAlias;
    }
}
