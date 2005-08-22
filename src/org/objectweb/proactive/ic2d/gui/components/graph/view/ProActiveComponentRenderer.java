/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.gui.components.graph.view;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;

import org.objectweb.fractal.gui.Constants;
import org.objectweb.fractal.gui.graph.view.ComponentPart;
import org.objectweb.fractal.gui.graph.view.ComponentRenderer;
import org.objectweb.fractal.gui.model.ClientInterface;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.fractal.gui.model.Interface;
import org.objectweb.fractal.gui.selection.model.Selection;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;


/**
 * Basic implementation of the {@link ComponentRenderer} interface. This
 * implementation draws components as illustrated in the figure below.
 *
 * <center><img src="../../../../../../../figures/crenderer.gif"/></center>
 */
public class ProActiveComponentRenderer implements ComponentRenderer, Constants {

    /**
     * Maximum h of an interface, insets included. In other words, maximum
     * value of {@link #divH}.
     */
    private final static int MAX_DIV = 16;

    /**
     * Vertical space between interfaces, and horizontal space between interface
     * names and component frame.
     */
    private final static int INSETS = 2;

    /**
     * Maximum distance under which two points are considered equal in
     * {@link #isCorner} and {@link #isBorder}.
     */
    private final static int EPS = 4;

    /**
     * The component for which the current values of the fields of this object
     * have been computed. This field is used to avoid recomputing these values
     * if it is not necessary.
     */
    Component c;

    /**
     * The component position for which the current values of the fields of this
     * object have been computed. This field is used to avoid recomputing these
     * values if it is not necessary.
     */
    Rectangle r;

    /**
     * Left border of the rectangle into which the component is drawn.
     */
    int x;

    /**
     * Top border of the rectangle into which the component is drawn.
     */
    int y;

    /**
     * Width of the rectangle into which the component is drawn.
     */
    int w;

    /**
     * Height of the rectangle into which the component is drawn.
     */
    int h;

    /**
     * List of the external server interfaces of the component that is drawn.
     */
    List sItfList;

    /**
     * List of the external client interfaces of the component that is drawn.
     */
    List cItfList;

    /**
     * Size of the component's frame. See above figure.
     */
    int borderSize;

    /**
     * Height of interfaces, including spaces between them. See above figure.
     */
    int divH;

    /**
     * Height of interfaces. See above figure.
     */
    int itfH;

    /**
     * Width of interfaces. See above figure.
     */
    int itfW;

    /**
     * itfW + INSETS. See above figure.
     */
    int itfWi;

    /**
     * itfW + borderSize. See above figure.
     */
    int bw1;

    /**
     * itfW + borderSize + itfW. See above figure.
     */
    int bw2;

    /**
     * itfW + borderSize + itfW + INSETS. See above figure.
     */
    int bw3;

    /**
     * Constructs a new {@link ProActiveComponentRenderer} component.
     */
    public ProActiveComponentRenderer() {
    }

    // -------------------------------------------------------------------------
    // Implementation of the ComponentRenderer interface
    // -------------------------------------------------------------------------
    public void drawComponent(final Graphics g, final Component c,
        final Selection s, final Rectangle r, final Color color,
        final boolean expanded, final int mode, final int state) {
        initialize(c, r);
        Object sel = ((s == null) ? null : s.getSelection());

        // draw component frame
        g.translate(x, y);
        if ((c.getMasterComponent() != null) && (sel != c)) {
            ((Graphics2D) g).setStroke(DASHED_STROKE);
        } else {
            ((Graphics2D) g).setStroke(NORMAL_STROKE);
        }

        /*
           g.setColor(color);
           g.fillRect(itfW, 0, w - 2*itfW, h);
           g.setColor(sel == c ? SELECTION_COLOR : Color.black);
           g.drawRect(itfW, 0, w-2*itfW-1, h-1);
           if (sel == c) {
              g.drawRect(itfW - 1, -1, w - 2*itfW + 1, h + 1);
           }
         */
        int[] PXO = {
                itfW + 4, (itfW + w) - (2 * itfW) + 3,
                (itfW + w) - (2 * itfW) + 3, itfW + 11, itfW + 4
            };
        int[] PYO = { 4, 4, h + 3, h + 3, h - 4 };
        g.setColor(Color.lightGray);
        g.fillPolygon(PXO, PYO, 5);
        int[] PX = {
                itfW, (itfW + w) - (2 * itfW) - 1, (itfW + w) - (2 * itfW) - 1,
                itfW + 7, itfW
            };
        int[] PY = { 0, 0, h - 1, h - 1, h - 8 };
        int[] PXS = {
                itfW - 1, (itfW + w) - (2 * itfW), (itfW + w) - (2 * itfW),
                itfW + 7, itfW - 1
            };
        int[] PYS = { -1, -1, h, h, h - 8 };
        g.setColor(color);
        g.fillPolygon(PX, PY, 5);
        g.setColor((sel == c) ? SELECTION_COLOR : Color.black);
        g.drawPolygon(PX, PY, 5);
        if (sel == c) {
            g.drawPolygon(PXS, PYS, 5);
        }

        if (c.isComposite()) {
            g.setColor(Color.white);
            g.fillRect(bw1, borderSize, w - (2 * bw1), h - (2 * borderSize));
            g.setColor(Color.black);
            g.drawRect(bw1, borderSize, w - (2 * bw1) - 1,
                h - (2 * borderSize) - 1);
        }

        g.setColor(color);
        if (state != NO_INSTANCE) {
            int[] TX = { itfW, itfW + 9, itfW + 9, itfW + 7 };
            int[] TY = { h - 9, h - 9, h - 1, h - 1 };
            if (state == STARTED) {
                g.setColor(Color.green);
            } else if (state == STOPPED) {
                g.setColor(Color.red);
            }
            g.fillPolygon(TX, TY, 4);
            g.setColor(Color.black);
            g.drawPolygon(TX, TY, 4);
        }

        if (!expanded) {
            // draw pseudo sub components ?
        }

        /*
           // ----- ADMIN
           if (state != NO_INSTANCE) {
             if (state == STARTED) {
               g.setColor(Color.green);
           //        g.fillRect(w-bw1+1, 2, 8, 8);
                   g.fillRect(w-bw1-6, 0, 8, 8);
                 } else if (state == STOPPED) {
                   g.setColor(Color.red);
                   g.fillRect(w-bw1-6, 0, 8, 8);
                 }
                 g.setColor(Color.black);
                 g.drawRect(w-bw1-6, 0, 8, 8);
               }
         */

        // -----
        g.setFont(NAME_FONT);
        String name = c.getName();
        if (name.length() == 0) {
            name = "<missing>";
            g.setColor(ERROR_COLOR);
        } else {
            g.setColor(Color.black);
        }
        drawString(g, name, itfWi, (divH / 2) - (itfH / 2) + INSETS,
            w - (2 * itfWi), itfH, 0, true);

        // MATT
        //		g.setFont(VIRTUAL_NODE_FONT);
        //		String virtualNode = c.getVirtualNode();
        //		g.setColor(Color.BLUE);
        //		drawString (g, virtualNode, itfWi, h- divH/2 - itfH/2 - INSETS, w - 2*itfWi, itfH, 0, true);
        //
        if (c instanceof ProActiveComponent) {
            g.setFont(org.objectweb.proactive.ic2d.gui.components.Constants.EXPORTED_VIRTUAL_NODES_FONT);
            String exportedVirtualNodes = ((ProActiveComponent) c).getExportedVirtualNodesAfterComposition();
            g.setColor(Color.RED);
            //	drawString (g, exportedVirtualNodes, itfWi, h+ divH/2, w, itfH, 0, true);
            drawString(g, exportedVirtualNodes, itfWi,
                h - (divH / 2) - (itfH / 2) - INSETS, w - (2 * itfWi), itfH, 0,
                true);
        }

        // ----- drawing interfaces
        g.setFont(PROVIDED_FONT);
        ((Graphics2D) g).setStroke(NORMAL_STROKE);
        int h = divH + INSETS;
        int hh = h + (divH / 2);
        int hhh = hh - (itfH / 2);
        int hhi = hh + (itfH / 2);

        int ep = 2;

        for (int i = 0; i < sItfList.size(); ++i) {
            Interface itf = (Interface) sItfList.get(i);
            if (sel == itf) {
                g.setColor(SELECTION_COLOR);
                g.fillRect(-1, hhh - 1, itfW + 1, itfH + 3);
            } else if (c.isComposite() && expanded &&
                    (sel == itf.getComplementaryInterface())) {
                g.setColor(SELECTION_COLOR);
                g.fillRect(bw1 + 1, hhh - 1, itfW + 1, itfH + 3);
            }

            g.setColor((itf.getStatus() == Interface.OK) ? PROVIDED_COLOR
                                                         : ERROR_COLOR);
            if (mode == 0) {
                name = itf.getName();
            } else if (mode == 1) {
                name = itf.getSignature();
            } else {
                name = " ";
            }
            name = ajustName(g, name, (w - (2 * bw1)) / 2);

            if (isMasterCollectionItf(itf)) {
                if (expanded) {
                    drawString(g, name, bw3 + 1, hhh + INSETS, w - (2 * bw3),
                        itfH, 0, true);
                    g.drawLine(bw1, hh, bw2, hh);
                    g.drawLine(bw2, hhh, bw2, hh + (itfH / 2));
                    g.drawLine(bw2 - 2, hhh, bw2 - 2, hh + (itfH / 2));
                }
            } else {
                if (c.isComposite() && expanded) {
                    drawString(g, name, bw3 + 1, hhh + INSETS, w - (2 * bw3),
                        itfH, 0, true);
                    g.drawLine(bw1, hh, bw2, hh);
                    g.drawLine(bw2, hhh, bw2, hhi);
                    g.drawLine(bw2 - 1, hhh, bw2 - 1, hhi);
                } else {
                    if (itf.isCollection()) {
                        g.setColor(Color.gray);
                        ep = 1;
                    }
                    drawString(g, name, itfW + INSETS, hhh + INSETS,
                        w - (2 * itfWi), itfH, 0, true);
                }
                g.drawLine(0, hh, itfW - 1, hh);
                g.drawLine(0, hhh, 0, hhi);
                g.drawLine(1, hhh, 1, hhi);
            }
            h += divH;
            hh += divH;
            hhh += divH;
            hhi += divH;
        }

        g.setFont(REQUIRED_FONT);
        h = divH + INSETS;
        hh = h + (divH / 2);
        hhh = hh - (itfH / 2);
        hhi = hh + (itfH / 2);
        for (int i = 0; i < cItfList.size(); ++i) {
            Interface itf = (Interface) cItfList.get(i);
            if (sel == itf) {
                g.setColor(SELECTION_COLOR);
                g.fillRect(w - itfW, hhh - 1, itfW + 1, itfH + 3);
            } else if (c.isComposite() && expanded &&
                    (sel == itf.getComplementaryInterface())) {
                g.setColor(SELECTION_COLOR);
                g.fillRect(w - bw2 - 1, hhh - 1, itfW, itfH + 3);
            }
            g.setColor((itf.getStatus() == Interface.OK) ? REQUIRED_COLOR
                                                         : ERROR_COLOR);
            //			name = itf.getName();
            if (mode == 0) {
                name = itf.getName();
            } else if (mode == 1) {
                name = itf.getSignature();
            } else {
                name = " ";
            }

            name = ajustName(g, name, (w - (2 * bw1)) / 2);

            Color col = g.getColor();
            if (isMasterCollectionItf(itf)) {
                drawString(g, name, itfW + INSETS, hhh + INSETS,
                    w - (2 * itfWi), itfH, 0, false);
                g.drawLine(w - itfW, hh, w - 1, hh);
                g.drawLine(w - 3, hhh, w - 3, hhi);
                g.drawLine(w, hhh, w, hhi);
            } else {
                if (c.isComposite() && expanded) {
                    drawString(g, name, bw3, hhh + INSETS, w - (2 * bw3), itfH,
                        0, false);
                    g.drawLine(w - bw2, hh, w - bw1 - 1, hh);
                    g.drawLine(w - bw2, hhh, w - bw2, hhi);
                } else {
                    if (itf.isCollection()) {
                        g.setColor(Color.gray);
                        ep = 1;
                    }
                    drawString(g, name, itfWi, hhh + INSETS, w - (2 * itfWi),
                        itfH, 0, false);
                }
                g.drawLine(w - itfW, hh, w - 1, hh);
                g.drawLine(w - 1, hhh, w - 1, hhi);
                if (ep == 2) {
                    g.drawLine(w, hhh, w, hhi);
                }
            }
            g.setColor(col);
            h += divH;
            hh += divH;
            hhh += divH;
            hhi += divH;
        }
        g.translate(-x, -y);
    }

    private String ajustName(Graphics g, String name, int max) {
        FontMetrics fm = g.getFontMetrics();
        int nameWidth = fm.stringWidth(name);
        int paddWidth = fm.stringWidth(".. ");

        if ((nameWidth > (max)) && (name.length() > 3)) {
            while (nameWidth > (max - paddWidth)) {
                int len = name.length();
                if (len < 4) {
                    break;
                }
                name = name.substring(0, --len);
                nameWidth = fm.stringWidth(name);
            }
            name = name + ".. ";
        }
        return name;
    }

    public ComponentPart getComponentPart(final Component c, final Rectangle r,
        final boolean expanded, final int x0, final int y0) {
        // eliminates trivial cases
        if ((x0 < r.x) || (x0 > (r.x + r.width)) || (y0 < r.y) ||
                (y0 > (r.y + r.height))) {
            return null;
        }

        // tests corners
        initialize(c, r);
        int part = isCorner(x + itfW, y, w - (2 * itfW), h, x0, y0);
        if (part != -1) {
            return new ComponentPart(c, null, part, r);
        }

        // tests interfaces
        int h = divH + INSETS;
        int hhh = (h + (divH / 2)) - (itfH / 2);
        for (int i = 0; i < sItfList.size(); ++i) {
            Interface itf = (Interface) sItfList.get(i);
            if (isMasterCollectionItf(itf)) {
                if ((x0 >= (x + bw1)) && (x0 <= (x + bw2)) &&
                        (y0 >= (y + hhh)) && (y0 <= (y + hhh + itfH))) {
                    return new ComponentPart(c,
                        itf.getComplementaryInterface(),
                        ComponentPart.INTERFACE, r);
                }
            } else {
                if ((y0 >= (y + hhh)) && (y0 <= (y + hhh + itfH))) {
                    if (x0 <= (x + itfW)) {
                        return new ComponentPart(c, itf,
                            ComponentPart.INTERFACE, r);
                    } else if ((x0 >= (x + bw1)) && (x0 <= (x + bw2))) {
                        if (c.isComposite() && expanded) {
                            return new ComponentPart(c,
                                itf.getComplementaryInterface(),
                                ComponentPart.INTERFACE, r);
                        }
                    }
                }
            }
            h += divH;
            hhh += divH;
        }

        h = divH + INSETS;
        hhh = (h + (divH / 2)) - (itfH / 2);
        for (int i = 0; i < cItfList.size(); ++i) {
            Interface itf = (Interface) cItfList.get(i);
            if (isMasterCollectionItf(itf)) {
                if ((x0 >= ((x + w) - itfW)) && (y0 >= (y + hhh)) &&
                        (y0 <= (y + hhh + itfH))) {
                    return new ComponentPart(c, itf, ComponentPart.INTERFACE, r);
                }
            } else if ((y0 >= (y + hhh)) && (y0 <= (y + hhh + itfH))) {
                if (x0 >= ((x + w) - itfW)) {
                    return new ComponentPart(c, itf, ComponentPart.INTERFACE, r);
                } else if ((x0 >= ((x + w) - bw2)) && (x0 <= ((x + w) - bw1))) {
                    if (c.isComposite() && expanded) {
                        return new ComponentPart(c,
                            itf.getComplementaryInterface(),
                            ComponentPart.INTERFACE, r);
                    }
                }
            }
            h += divH;
            hhh += divH;
        }

        // tests borders
        part = isBorder(x + itfW, y, w - (2 * itfW), this.h, x0, y0);
        if (part != -1) {
            return new ComponentPart(c, null, part, r);
        }

        // tests header
        if ((x0 >= (x + itfW)) && (x0 <= ((x + w) - itfW)) && (y0 >= y) &&
                (y0 <= (y + divH))) {
            return new ComponentPart(c, null, ComponentPart.HEADER, r);
        }

        // tests content
        if ((x0 >= (x + itfW)) && (x0 <= ((x + w) - itfW)) &&
                (y0 >= (y + divH)) && (y0 <= (y + this.h))) {
            return new ComponentPart(c, null, ComponentPart.CONTENT, r);
        }
        return null;
    }

    public Point getInterfacePosition(final Component c, final Rectangle r,
        final Interface i) {
        initialize(c, r);

        int baseX = r.x + r.width;
        int baseY = r.y + (divH / 2) + INSETS;

        if (!(i instanceof ClientInterface)) {
            if (i.isInternal()) {
                int index = cItfList.indexOf(i.getComplementaryInterface());
                return new Point(baseX - bw2, baseY + (divH * (index + 1)));
            } else {
                int index = sItfList.indexOf(i);
                return new Point(r.x, baseY + (divH * (index + 1)));
            }
        } else {
            if (i.isInternal()) {
                int index = sItfList.indexOf(i.getComplementaryInterface());
                return new Point(r.x + bw2, baseY + (divH * (index + 1)));
            } else {
                int index = cItfList.indexOf(i);
                return new Point(baseX, baseY + (divH * (index + 1)));
            }
        }
    }

    public Rectangle getSubComponentArea(final Component c, final Rectangle r) {
        initialize(c, r);
        int xp = x + INSETS + itfW + borderSize;
        int yp = y + INSETS + borderSize;
        int widthp = w - (2 * (INSETS + itfW + borderSize));
        int heightp = h - (2 * (INSETS + borderSize));
        return new Rectangle(xp, yp, widthp, heightp);
    }

    // -------------------------------------------------------------------------
    // Other methods
    // -------------------------------------------------------------------------

    /**
     * Prepares this component to draw the given component.
     *
     * @param c the component that will be drawn.
     * @param r where the component will be drawn.
     */
    protected void initialize(final Component c, final Rectangle r) {
        if ((c == this.c) && (r == this.r)) {
            return;
        }
        this.c = c;
        this.r = r;
        x = r.x;
        y = r.y;
        w = r.width;
        h = r.height;
        sItfList = c.getServerInterfaces();
        cItfList = c.getClientInterfaces();

        int divisions = 2 + Math.max(sItfList.size(), cItfList.size());
        divH = Math.min(MAX_DIV, (h - (2 * INSETS)) / divisions);
        //		borderSize = divH;
        borderSize = Math.min(5, (h - (2 * INSETS)) / divisions);
        itfH = (int) (divH * 0.75);
        itfW = itfH / 2;
        itfWi = itfW + INSETS;
        bw1 = itfW + borderSize;
        bw2 = (2 * itfW) + borderSize;
        bw3 = (2 * itfW) + borderSize + INSETS;
    }

    /**
     * Draws a string in the given rectangle.
     *
     * @param g the graphics to be used to draw the string.
     * @param s the string to be drawn.
     * @param x left border of the rectangle where s must be drawn.
     * @param y top border of the rectangle where s must be drawn.
     * @param dx w of the rectangle where s must be drawn.
     * @param dy h of the rectangle where s must be drawn.
     * @param insets horizontal insets to be removed from (x,y,dx,dy).
     * @param left if the string must drawn left justified or right justified.
     */
    protected void drawString(final Graphics g, final String s, final int x,
        final int y, final int dx, final int dy, final int insets,
        final boolean left) {
        int size = dy - (2 * insets);
        g.setFont(g.getFont().deriveFont((float) size));
        FontMetrics fm = g.getFontMetrics();
        double descent = ((double) fm.getDescent()) / fm.getHeight() * size;
        int y0 = (y + dy) - insets - (int) Math.round(descent);
        Shape shape = g.getClip();
        g.clipRect(x, y, dx, dy);
        if (left) {
            g.drawString(s, x + insets, y0);
        } else {
            g.drawString(s, (x + dx) - insets - fm.stringWidth(s), y0);
        }
        g.setClip(shape);
    }

    /**
     * Returns the corner of the given rectangle to which the given point
     * corresponds.
     *
     * @param x left border of the rectangle.
     * @param y top border of the rectangle.
     * @param w w of the rectangle.
     * @param h h of the rectangle.
     * @param x0 x coordinate of the point.
     * @param y0 y coordinate of the point.
     * @return the type of the corner to which the given point corresponds, or -1
     *      if it does not corresponds to any corner.
     */
    protected int isCorner(final int x, final int y, final int w, final int h,
        final int x0, final int y0) {
        if ((Math.abs(x0 - x) < EPS) && (Math.abs(y0 - y) < EPS)) {
            return ComponentPart.TOP_LEFT_CORNER;
        }
        if ((Math.abs(x0 - (x + w)) < EPS) && (Math.abs(y0 - y) < EPS)) {
            return ComponentPart.TOP_RIGHT_CORNER;
        }
        if ((Math.abs(x0 - x) < EPS) && (Math.abs(y0 - (y + h)) < EPS)) {
            return ComponentPart.BOTTOM_LEFT_CORNER;
        }
        if ((Math.abs(x0 - (x + w)) < EPS) && (Math.abs(y0 - (y + h)) < EPS)) {
            return ComponentPart.BOTTOM_RIGHT_CORNER;
        }
        return -1;
    }

    /**
     * Returns the border of the given rectangle to which the given point
     * corresponds.
     *
     * @param x left border of the rectangle.
     * @param y top border of the rectangle.
     * @param w w of the rectangle.
     * @param h h of the rectangle.
     * @param x0 x coordinate of the point.
     * @param y0 y coordinate of the point.
     * @return the type of the border to which the given point corresponds, or -1
     *      if it does not corresponds to any border.
     */
    protected int isBorder(final int x, final int y, final int w, final int h,
        final int x0, final int y0) {
        if ((Math.abs(x0 - x) < EPS) && (y0 >= y) && (y0 <= (y + h))) {
            return ComponentPart.LEFT_BORDER;
        }
        if ((Math.abs(y0 - y) < EPS) && (x0 >= x) && (x0 <= (x + w))) {
            return ComponentPart.TOP_BORDER;
        }
        if ((Math.abs(x0 - (x + w)) < EPS) && (y0 >= y) && (y0 <= (y + h))) {
            return ComponentPart.RIGHT_BORDER;
        }
        if ((Math.abs(y0 - (y + h)) < EPS) && (x0 >= x) && (x0 <= (x + w))) {
            return ComponentPart.BOTTOM_BORDER;
        }
        return -1;
    }

    /**
     * Returns <tt>true</tt> if the given interface is a master collection
     * interface.
     *
     * @param itf a component interface.
     * @return <tt>true</tt> if the given interface is a master collection
     *      interface.
     */
    private boolean isMasterCollectionItf(final Interface itf) {
        return itf.isCollection() &&
        (itf.getMasterCollectionInterface() == null);
    }
}
