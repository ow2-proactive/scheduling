package org.objectweb.proactive.ic2d.infrastructuremanager.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.monitoring.figures.AbstractRectangleFigure;
import org.objectweb.proactive.ic2d.monitoring.figures.ToolTipFigure;

public class IMFigureHost extends AbstractRectangleFigure {

	protected final static int DEFAULT_WIDTH = 25;

	private IFigure contentPane;

	protected  String  title = "";
	
	private final static Color DEFAULT_BORDER_COLOR;

	static {
		Display device = Display.getCurrent();
		DEFAULT_BORDER_COLOR = new Color(device, 0, 0, 128);
	}

	//
	// -- CONSTRUCTOR -----------------------------------------------
	//
	public IMFigureHost(String text) {
		super(text);
	}

	/**
	 * Used to display the legend.
	 *
	 */
	public IMFigureHost() {
		super();
	}

	//
	// -- PUBLIC METHOD --------------------------------------------
	//

	public IFigure getContentPane() {
		return contentPane;
	}

	public boolean isVerticalLayout() {
		return contentPane.getLayoutManager() instanceof HostVerticalLayout;
	}

	public void setVerticalLayout() {
		contentPane.setLayoutManager(new HostVerticalLayout());
	}

	public void setHorizontalLayout() {
		contentPane.setLayoutManager(new HostHorizontalLayout());
	}

	//
	// -- PROTECTED METHODS --------------------------------------------
	//
	protected void initColor() {
		Device device = Display.getCurrent();
		borderColor = DEFAULT_BORDER_COLOR;
		backgroundColor = new Color(device, 208, 208, 208);
		shadowColor = new Color(device, 230, 230, 230);
	}

	protected void initFigure() {
		BorderLayout layout = new HostBorderLayout();
		setLayoutManager(layout);
		add(label, BorderLayout.TOP);

		contentPane = new Figure();
		ToolbarLayout contentPaneLayout = new HostHorizontalLayout();
		contentPaneLayout.setSpacing(5);
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
		return null;
	}

	
	
	public void changeTitle(String newTitle){
		String text = getTextResized(newTitle);
		this.title = text;
		setToolTip(new ToolTipFigure(newTitle));
		Display.getDefault().asyncExec(this);
	}

	@Override
	public void run(){
		setTitle(title);
		repaint();
	}
	
	//
	// -- INNER CLASS --------------------------------------------
	//

	private class HostBorderLayout extends BorderLayout {

		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint){
			if(legend)
				return super.calculatePreferredSize(container, wHint, hHint).expand(/*100*/50, /*10*/0);
			return super.calculatePreferredSize(container, wHint, hHint).expand(10,0);
		}
	}

	private class HostHorizontalLayout extends ToolbarLayout {

		public HostHorizontalLayout(){
			super(true);
		}

		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint){
			return super.calculatePreferredSize(container, wHint, hHint).expand(0, 13);
		}

		public void layout(IFigure figure) {
			super.layout(figure);
			figure.translate(5, 0);
		}
	}

	private class HostVerticalLayout extends ToolbarLayout {

		public HostVerticalLayout(){
			super(false);
		}

		protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint){
			return super.calculatePreferredSize(container, wHint, hHint).expand(0, 13);
		}

		public void layout(IFigure figure) {
			super.layout(figure);
			figure.translate(5, 5);
		}
	}
}
