/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.ic2d.monitoring.figures;

import java.util.Enumeration;
import java.util.Hashtable;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.monitoring.data.State;

public class AOFigure extends AbstractFigure{

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

	// initialization of the colors
	static {
		Display device = Display.getCurrent();
		COLOR_WHEN_WAITING_FOR_REQUEST = new Color(device, 225, 225, 225);
		COLOR_WHEN_WAITING_BY_NECESSITY = new Color(device, 255, 205, 110);
		COLOR_WHEN_ACTIVE = new Color(device, 180, 255, 180);// green
		COLOR_WHEN_SERVING_REQUEST = new Color(device, 255, 255, 255);
		COLOR_WHEN_MIGRATING = new Color(device, 0, 0, 255);// blue
		COLOR_WHEN_NOT_RESPONDING = new Color(device, 255, 0, 0);// red

		DEFAULT_BORDER_COLOR = new Color(device, 200, 200, 200);

		COLOR_REQUEST_SINGLE = new Color(device, 0, 255, 0); // green
		COLOR_REQUEST_SEVERAL = new Color(device, 255, 0, 0); // red
		COLOR_REQUEST_MANY = new Color(device, 150, 0, 255); // violet
	}

	/** Request queue length (used to display small square int the active object) */
	private int requestQueueLength;

	/** All connections whose target is this and source is the key */
	private Map<AOFigure, Connection> sourceConnections;

	/** All connections whose source is this and target is the key */
	private Map<AOFigure, Connection> targetConnections;

	/** Optimizes the GUI, the arrows are not drawn during a time. */
	private GUIManager manager;

	private MouseListener mouseListener;

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	/**
	 * @param text Text to display
	 * @param viewer 
	 */
	public AOFigure(String text){
		super(text);
		this.requestQueueLength = 0;
		this.sourceConnections = new Hashtable<AOFigure, Connection>();
		this.targetConnections = new Hashtable<AOFigure, Connection>();

		this.manager = new GUIManager(this);
	}

	/**
	 * Used to display the legend.
	 * @param state
	 */
	public AOFigure(State state, int requestQueueLength){
		super();
		this.setState(state);
		this.requestQueueLength = requestQueueLength;
	}

	//
	// -- PUBLIC METHODS ----------------------------------------------
	//

	/**
	 * @return
	 */
	public ConnectionAnchor getAnchor() {
		return new /*EllipseAnchor(this)/*ChopboxAnchor(this)*/Anchor(this);
	}

	/**
	 * @see AbstractFigure#paintIC2DFigure(Graphics)
	 */
	@Override
	public void paintIC2DFigure(Graphics graphics){
		// Inits
		Rectangle bounds = this.getBounds().getCopy().resize(-1, -2);
		// Shadow
		if(showShadow){
			graphics.setBackgroundColor(shadowColor);
			graphics.fillOval(bounds.getTranslated(4, 4));
		}

		// Drawings
		graphics.setForegroundColor(this.borderColor);
		graphics.setBackgroundColor(this.backgroundColor);
		graphics.fillOval(bounds);
		graphics.drawOval(bounds);

		// Paint request queue information
		if(requestQueueLength > 0) {
			int length = requestQueueLength;
			int numMany = (int)Math.ceil(length / NUMBER_OF_REQUESTS_FOR_MANY);
			length -= numMany*NUMBER_OF_REQUESTS_FOR_MANY;
			int numSeveral = (int)Math.ceil(length / NUMBER_OF_REQUESTS_FOR_SEVERAL);
			length -= numSeveral*NUMBER_OF_REQUESTS_FOR_SEVERAL;
			int numSingle = length;
			if (numSingle > 0) {
				int requestQueueX = bounds.x + ((bounds.width - (6 * numSingle)) / 2) + 2;
				int requestQueueY = bounds.y + 4;
				graphics.setBackgroundColor(COLOR_REQUEST_SINGLE);
				for (int i = 0; i < numSingle; i++) {
					graphics.fillRectangle(requestQueueX + (i * 6), requestQueueY, REQUEST_FIGURE_SIZE, REQUEST_FIGURE_SIZE);
				}
			}
			if (numSeveral > 0) {
				int requestQueueX = bounds.x + ((bounds.width - (6 * (numSeveral + numMany))) / 2) + 2;
				int requestQueueY = bounds.y + bounds.height - 6;
				graphics.setBackgroundColor(COLOR_REQUEST_SEVERAL);
				for (int i = 0; i < numSeveral; i++)
					graphics.fillRectangle(requestQueueX + (i * 6), requestQueueY, REQUEST_FIGURE_SIZE, REQUEST_FIGURE_SIZE);
			}
			if (numMany > 0) {
				int requestQueueX = bounds.x + ((bounds.width - (6 * (numSeveral + numMany))) / 2) + 
				(6 * numSeveral) + 2;
				int requestQueueY = bounds.y + bounds.height - 6;
				graphics.setBackgroundColor(COLOR_REQUEST_MANY);
				for (int i = 0; i < numMany; i++)
					graphics.fillRectangle(requestQueueX + (i * 6), requestQueueY, REQUEST_FIGURE_SIZE, REQUEST_FIGURE_SIZE);
			}
		}

		// Cleanups
		graphics.restoreState();
	}

	/**
	 * @return
	 */
	public IFigure getContentPane() {
		System.out.println("AOFigure : getContentPane");
		return this;
	}

	/**
	 * 
	 * @param state
	 */
	public void setState(State state){
		switch (state) {
		// busy
		case SERVING_REQUEST:
			this.backgroundColor = AOFigure.COLOR_WHEN_SERVING_REQUEST;
			break;
			// waiting by necessity
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
			removeMouseListener(mouseListener);
			break;
		default:
			break;
		}
		//this.repaint();
		if(this.manager!=null) this.manager.repaint();
	}


	/**
	 * 
	 * @param length
	 */
	public void setRequestQueueLength(int length) {
		this.requestQueueLength = length;
		if(this.manager!=null) this.manager.repaint();
	}

	/**
	 * Adds a connection between this and <code>target</code>.
	 * <code>this</code> is the source and <code>target</code> is the target
	 * @param target the target of the connection
	 * @param panel the connection is added to this panel
	 */
	public void addConnection(AOFigure target, IFigure panel, Color color) {
		if(targetConnections.get(target) != null) {
			((RoundedLine)targetConnections.get(target)).addOneCommunication();
			return;
		}
		Connection connection = AOConnection.createConnection(this, target, color);
		this.targetConnections.put(target, connection);
		target.sourceConnections.put(this, connection);
		panel.add(connection);

	}

	/**
	 * Removes all connections linked with <code>this</code>.
	 * @param panel The panel wich contains all connections
	 */
	public void removeConnections(IFigure panel) {
		for (Enumeration<AOFigure> e = ((Hashtable<AOFigure, Connection>)targetConnections).keys() ; e.hasMoreElements() ;) {
			AOFigure target = e.nextElement();
			target.sourceConnections.remove(this);
			panel.remove(targetConnections.get(target));
			this.targetConnections.remove(target);
		}

		for (Enumeration<AOFigure> e = ((Hashtable<AOFigure, Connection>)sourceConnections).keys() ; e.hasMoreElements() ;) {
			AOFigure source = e.nextElement();
			source.targetConnections.remove(this);
			panel.remove(sourceConnections.get(source));
			this.sourceConnections.remove(source);
		}
	}

	@Override
	public void repaint() {
		super.repaint();
	}

	@Override
	public void refresh(){
		if(Display.getDefault()!=null){
			Display.getDefault().asyncExec(new Runnable() {
				public void run () {
					repaint();
				}});
		}
	}

	@Override
	public void addMouseListener(MouseListener listener){
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
	protected void initFigure(){
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
		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
			if(legend)
				return new Dimension(50, super.calculatePreferredSize(container, wHint, hHint).expand(0, 8).height);
			return new Dimension(100,super.calculatePreferredSize(container, wHint, hHint).expand(0, 15).height);
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
			graphics.fillRectangle(bounds.x, bounds.y, REQUEST_FIGURE_SIZE, REQUEST_FIGURE_SIZE);
			graphics.restoreState();
		}

		class RequestQueueLayout extends BorderLayout {
			@Override
			protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
				return new Dimension(REQUEST_FIGURE_SIZE,REQUEST_FIGURE_SIZE);
			}
		}
	}
}
