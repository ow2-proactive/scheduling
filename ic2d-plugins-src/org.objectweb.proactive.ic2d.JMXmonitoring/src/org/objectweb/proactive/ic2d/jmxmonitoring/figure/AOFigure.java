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
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.State;


public class AOFigure extends AbstractFigure {
    protected final static int DEFAULT_WIDTH = 40;
    private static final Color DEFAULT_BORDER_COLOR;

    // States
    public static final Color COLOR_WHEN_WAITING_FOR_REQUEST;
    public static final Color COLOR_WHEN_WAITING_BY_NECESSITY;
    public static final Color COLOR_WHEN_ACTIVE;
    public static final Color COLOR_WHEN_SERVING_REQUEST;
    public static final Color COLOR_WHEN_MIGRATING;
    public static final Color COLOR_WHEN_NOT_RESPONDING;

    // Request Queue length
    public static final int NUMBER_OF_REQUESTS_FOR_SEVERAL = 5;
    public static final int NUMBER_OF_REQUESTS_FOR_MANY = 50;
    public static final Color COLOR_REQUEST_SINGLE;
    public static final Color COLOR_REQUEST_SEVERAL;
    public static final Color COLOR_REQUEST_MANY;
    public static final int REQUEST_FIGURE_SIZE = 4;
    public static final Display device;

    static {
        device = Display.getCurrent();
        COLOR_WHEN_WAITING_FOR_REQUEST = new Color(device, 225, 225, 225);
        COLOR_WHEN_WAITING_BY_NECESSITY = new Color(device, 255, 205, 110);
        COLOR_WHEN_ACTIVE = new Color(device, 180, 255, 180); // green
        COLOR_WHEN_SERVING_REQUEST = new Color(device, 255, 255, 255); //white
        COLOR_WHEN_MIGRATING = new Color(device, 0, 0, 255); // blue
        COLOR_WHEN_NOT_RESPONDING = new Color(device, 255, 0, 0); // red

        DEFAULT_BORDER_COLOR = new Color(device, 200, 200, 200);

        COLOR_REQUEST_SINGLE = new Color(device, 0, 255, 0); // green
        COLOR_REQUEST_SEVERAL = new Color(device, 255, 0, 0); // red
        COLOR_REQUEST_MANY = new Color(device, 150, 0, 255); // violet
    }

    /** Request queue length (used to display small square int the active object) */
    private int requestQueueLength;

    /** if the active object has a security manager or not */
    private boolean isSecure = false;

    /** All connections whose target is this and source is the key */
    private Map<AOFigure, RoundedLineConnection> sourceConnections;

    /** All connections whose source is this and target is the key */
    private Map<AOFigure, RoundedLineConnection> targetConnections;
    private MouseListener mouseListener;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * @param text Text to display
     * @param viewer
     */
    public AOFigure(String text) {
        super(text);
        this.requestQueueLength = 0;
        this.sourceConnections = Collections.synchronizedMap(new Hashtable<AOFigure, RoundedLineConnection>());
        this.targetConnections = Collections.synchronizedMap(new Hashtable<AOFigure, RoundedLineConnection>());
    }

    /**
     * Used to display the legend.
     * @param state
     */
    public AOFigure(State state, int requestQueueLength, boolean secure) {
        super();
        this.setState(state);
        this.requestQueueLength = requestQueueLength;
        this.isSecure = secure;
    }

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //

    /**
     * @return
     */
    public ConnectionAnchor getAnchor() {
        return new Anchor(this);
    }

    /**
     * @see AbstractFigure#paintIC2DFigure(Graphics)
     */
    @Override
    public void paintIC2DFigure(Graphics graphics) {
        // Inits
        Rectangle bounds = this.getBounds().getCopy().resize(-1, -2);

        // Shadow
        if (showShadow) {
            graphics.setBackgroundColor(shadowColor);
            graphics.fillOval(bounds.getTranslated(4, 4));
        }

        // Drawings
        graphics.setForegroundColor(this.borderColor);
        graphics.setBackgroundColor(this.backgroundColor);
        graphics.fillOval(bounds);
        graphics.drawOval(bounds);

        // Paint request queue information
        if (requestQueueLength > 0) {
            int length = requestQueueLength;
            int numMany = (int) Math.ceil(length / NUMBER_OF_REQUESTS_FOR_MANY);
            length -= (numMany * NUMBER_OF_REQUESTS_FOR_MANY);
            int numSeveral = (int) Math.ceil(length / NUMBER_OF_REQUESTS_FOR_SEVERAL);
            length -= (numSeveral * NUMBER_OF_REQUESTS_FOR_SEVERAL);
            int numSingle = length;
            if (numSingle > 0) {
                int requestQueueX = bounds.x +
                    ((bounds.width - (6 * numSingle)) / 2) + 2;
                int requestQueueY = bounds.y + 4;
                graphics.setBackgroundColor(COLOR_REQUEST_SINGLE);
                for (int i = 0; i < numSingle; i++) {
                    graphics.fillRectangle(requestQueueX + (i * 6),
                        requestQueueY, REQUEST_FIGURE_SIZE, REQUEST_FIGURE_SIZE);
                }
            }
            if (numSeveral > 0) {
                int requestQueueX = bounds.x +
                    ((bounds.width - (6 * (numSeveral + numMany))) / 2) + 2;
                int requestQueueY = (bounds.y + bounds.height) - 6;
                graphics.setBackgroundColor(COLOR_REQUEST_SEVERAL);
                for (int i = 0; i < numSeveral; i++)
                    graphics.fillRectangle(requestQueueX + (i * 6),
                        requestQueueY, REQUEST_FIGURE_SIZE, REQUEST_FIGURE_SIZE);
            }
            if (numMany > 0) {
                int requestQueueX = bounds.x +
                    ((bounds.width - (6 * (numSeveral + numMany))) / 2) +
                    (6 * numSeveral) + 2;
                int requestQueueY = (bounds.y + bounds.height) - 6;
                graphics.setBackgroundColor(COLOR_REQUEST_MANY);
                for (int i = 0; i < numMany; i++)
                    graphics.fillRectangle(requestQueueX + (i * 6),
                        requestQueueY, REQUEST_FIGURE_SIZE, REQUEST_FIGURE_SIZE);
            }
        }

        if (isSecure) {
            graphics.setForegroundColor(device.getSystemColor(SWT.COLOR_RED));
            graphics.drawText("S", 4, (bounds.height / 4) - 2);
        }

        // Cleanups
        graphics.restoreState();
    }

    /**
     * @return
     */
    public IFigure getContentPane() {
        return this;
    }

    /**
     *
     * @param state
     */
    public void setState(State state) {
        switch (state) {
        // busy
        case SERVING_REQUEST:
            this.backgroundColor = AOFigure.COLOR_WHEN_SERVING_REQUEST;
            break;

        // waiting by necessity
        case WAITING_BY_NECESSITY:
        case WAITING_BY_NECESSITY_WHILE_ACTIVE:
        case WAITING_BY_NECESSITY_WHILE_SERVING:
            this.backgroundColor = AOFigure.COLOR_WHEN_WAITING_BY_NECESSITY;
            break;

        // waiting for request
        case WAITING_FOR_REQUEST:
            this.backgroundColor = AOFigure.COLOR_WHEN_WAITING_FOR_REQUEST;
            break;

        // active
        case ACTIVE:
            this.backgroundColor = AOFigure.COLOR_WHEN_ACTIVE;
            break;

        // not responding
        case NOT_RESPONDING:
            this.backgroundColor = AOFigure.COLOR_WHEN_NOT_RESPONDING;
            break;

        // migrate
        case MIGRATING:
            this.backgroundColor = AOFigure.COLOR_WHEN_MIGRATING;
            if (mouseListener != null) {
                removeMouseListener(mouseListener);
            }
            break;
        default:
            break;
        }

        //Display.getDefault().asyncExec(this);
    }

    /**
     *
     * @param length
     */
    public void setRequestQueueLength(int length) {
        this.requestQueueLength = length;
        //Display.getDefault().asyncExec(this);
    }

    /**
     * Adds a connection between this and <code>target</code>.
     * <code>this</code> is the source and <code>target</code> is the target
     * @param target the target of the connection
     * @param panel the connection is added to this panel
     */
    public void addConnection(AOFigure target, IFigure panel, Color color) {
        if (targetConnections.get(target) != null) {
            targetConnections.get(target).addOneCommunication();
            return;
        }
        RoundedLineConnection connection = AOConnection.createConnection(this,
                target, color);
        this.targetConnections.put(target, connection);
        target.sourceConnections.put(this, connection);
        panel.add(connection);
    }

    /**
     * Removes all connections linked with <code>this</code>.
     * @param panel The panel wich contains all connections
     */
    public void removeConnections(final IFigure panel) {
        if (panel == null) {
            return;
        }

        List<AOFigure> targetList = new ArrayList<AOFigure>(targetConnections.keySet());
        for (int i = 0; i < targetList.size(); i++) {
            AOFigure target = targetList.get(i);
            final Connection connection = targetConnections.get(target);
            if ((connection != null) && (connection.getParent() == panel)) {
                panel.remove(connection);
            }
            this.targetConnections.remove(target);
        }

        List<AOFigure> sourceList = new ArrayList<AOFigure>(sourceConnections.keySet());
        for (int i = 0; i < sourceList.size(); i++) {
            AOFigure source = sourceList.get(i);
            source.targetConnections.remove(this);
            Connection connection = sourceConnections.get(source);
            if ((connection != null) && (connection.getParent() == panel)) {
                panel.remove(connection);
            }
            this.sourceConnections.remove(source);
        }
    }

    @Override
    public void refresh() {
        Display.getDefault().asyncExec(this);
    }

    @Override
    public void addMouseListener(MouseListener listener) {
        this.mouseListener = listener;
        super.addMouseListener(listener);
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     *
     * @see AbstractFigure#initColor()
     */
    @Override
    protected void initColor() {
        Device device = Display.getCurrent();

        borderColor = DEFAULT_BORDER_COLOR;
        backgroundColor = new Color(device, 225, 225, 225);
        shadowColor = new Color(device, 230, 230, 230);
    }

    /**
     *
     * @see AbstractFigure#initFigure()
     */
    @Override
    protected void initFigure() {
        LayoutManager layout = new AOBorderLayout();
        setLayoutManager(layout);
        add(label, BorderLayout.CENTER);
    }

    /**
     *
     * @return
     */
    @Override
    protected Color getDefaultBorderColor() {
        return DEFAULT_BORDER_COLOR;
    }

    //
    // -- INNER CLASS -------------------------------------------
    //
    private class AOBorderLayout extends BorderLayout {
        @Override
        protected Dimension calculatePreferredSize(IFigure container,
            int wHint, int hHint) {
            if (legend) {
                return new Dimension(50,
                    super.calculatePreferredSize(container, wHint, hHint)
                         .expand(0, 8).height);
            }
            return new Dimension(100,
                super.calculatePreferredSize(container, wHint, hHint)
                     .expand(0, 15).height);
        }
    }

    public class RequestQueueFigure extends Figure {
        private Color color;

        public RequestQueueFigure(Color color) {
            this.color = color;
            setLayoutManager(new RequestQueueLayout());
        }

        public void paintFigure(Graphics graphics) {
            super.paintFigure(graphics);
            graphics.setBackgroundColor(color);
            graphics.fillRectangle(bounds.x, bounds.y, REQUEST_FIGURE_SIZE,
                REQUEST_FIGURE_SIZE);
            graphics.restoreState();
        }

        class RequestQueueLayout extends BorderLayout {
            @Override
            protected Dimension calculatePreferredSize(IFigure container,
                int wHint, int hHint) {
                return new Dimension(REQUEST_FIGURE_SIZE, REQUEST_FIGURE_SIZE);
            }
        }
    }

    public boolean isSecure() {
        return isSecure;
    }

    public void setSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }
}
