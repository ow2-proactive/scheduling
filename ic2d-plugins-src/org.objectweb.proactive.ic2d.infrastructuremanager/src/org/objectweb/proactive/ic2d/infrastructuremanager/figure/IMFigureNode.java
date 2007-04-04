package org.objectweb.proactive.ic2d.infrastructuremanager.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;
import org.objectweb.proactive.ic2d.infrastructuremanager.IMConstants;
import org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewInfrastructure;
import org.objectweb.proactive.ic2d.monitoring.figures.AbstractRectangleFigure;

public class IMFigureNode extends AbstractRectangleFigure{

	protected final static int DEFAULT_WIDTH = 17;

	private IFigure contentPane;

	/**
	 * Create a new node figure
	 * @param text The text to display
	 * @param protocol The protocol used
	 */
	public IMFigureNode(IMNode imNode, IMViewInfrastructure view) {
		super(imNode.getNodeName());
		try {
			if(imNode.isFree()) {
				setStatus(IMConstants.STATUS_AVAILABLE);
				view.incrFree();
			}
			else {
				setStatus(IMConstants.STATUS_BUSY);
				view.incrBusy();
			}
		}		
		catch(NodeException e) {
			setStatus(IMConstants.STATUS_DOWN);
			view.incrDown();
		}
	}
	
	public IMFigureNode(String status) {
		super();
		setStatus(status);
	}
	

	public IFigure getContentPane() {
		return contentPane;
	}

	public void setStatus(String status){
		if(status.equals(IMConstants.STATUS_AVAILABLE)) {
			backgroundColor = IMConstants.AVAILABLE_COLOR;			
		}
		else if(status.equals(IMConstants.STATUS_BUSY)) {
			backgroundColor = IMConstants.BUSY_COLOR;			
		}
		else if(status.equals(IMConstants.STATUS_DOWN)) {
			backgroundColor = IMConstants.DOWN_COLOR;			
		}
	}

	//
	// -- PROTECTED METHODS --------------------------------------------
	//
	protected void initColor() {
		Device device = Display.getCurrent();
		borderColor = IMConstants.DEFAULT_BORDER_COLOR;
		shadowColor = new Color(device, 230, 230, 230);
	}

	protected void initFigure() {
		BorderLayout layout = new NodeBorderLayout();
		layout.setVerticalSpacing(5);
		setLayoutManager(layout);

		add(label, BorderLayout.TOP);

		contentPane = new Figure();
		ToolbarLayout contentPaneLayout = new NodeToolbarLayout();
		contentPaneLayout.setSpacing(0);
		contentPaneLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		contentPane.setLayoutManager(contentPaneLayout);
		add(contentPane, BorderLayout.CENTER);
	}

	@Override
	protected int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	protected Color getDefaultBorderColor() {
		return IMConstants.DEFAULT_BORDER_COLOR;
	}

	//
	// -- INNER CLASS --------------------------------------------
	//

	private class NodeBorderLayout extends BorderLayout {

		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint){
			if(legend) {
				return super.calculatePreferredSize(container, wHint, hHint).expand(50, -5);
			}
			return super.calculatePreferredSize(container, wHint, hHint).expand(20,-10);
		}
	}

	private class NodeToolbarLayout extends ToolbarLayout {

		public NodeToolbarLayout() {
			super(false);
		}


		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint){
			return super.calculatePreferredSize(container, wHint, hHint).expand(0,15);
		}

	}
}
