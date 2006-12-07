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
package org.objectweb.proactive.ic2d.monitoring.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.monitoring.actions.HorizontalLayoutAction;
import org.objectweb.proactive.ic2d.monitoring.actions.MonitoringContextMenuProvider;
import org.objectweb.proactive.ic2d.monitoring.actions.NewHostAction;
import org.objectweb.proactive.ic2d.monitoring.actions.NewViewAction;
import org.objectweb.proactive.ic2d.monitoring.actions.RefreshAction;
import org.objectweb.proactive.ic2d.monitoring.actions.RefreshHostAction;
import org.objectweb.proactive.ic2d.monitoring.actions.RefreshJVMAction;
import org.objectweb.proactive.ic2d.monitoring.actions.RefreshNodeAction;
import org.objectweb.proactive.ic2d.monitoring.actions.SetDepthAction;
import org.objectweb.proactive.ic2d.monitoring.actions.SetTTRAction;
import org.objectweb.proactive.ic2d.monitoring.actions.SetUpdateFrequenceAction;
import org.objectweb.proactive.ic2d.monitoring.actions.StopMonitoringAction;
import org.objectweb.proactive.ic2d.monitoring.actions.VerticalLayoutAction;
import org.objectweb.proactive.ic2d.monitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.monitoring.dnd.DragAndDrop;
import org.objectweb.proactive.ic2d.monitoring.editparts.MonitoringEditPartFactory;
import org.objectweb.proactive.ic2d.monitoring.figures.Refresher;
import org.objectweb.proactive.ic2d.monitoring.figures.RoundedLine;
import org.objectweb.proactive.ic2d.monitoring.figures.listeners.WorldListener;
import org.objectweb.proactive.ic2d.monitoring.spy.SpyEventListenerImpl;


public class MonitoringView extends ViewPart {

	public static final String ID = "org.objectweb.proactive.ic2d.monitoring.views.MonitoringView";

	private String title;

	/** the graphical viewer */
	private MonitoringViewer graphicalViewer;

	/** the overview outline page */
	//private OverviewOutlinePage overviewOutlinePage;

	private Button bProportional;
	private Button bRatio;
	private Button bFilaire;

	/** The World */
	private WorldObject world;

	private DragAndDrop dnd = new DragAndDrop();
	
	/** The graphical set of virtual nodes */
	private VirtualNodesGroup virtualNodesGroup;

	//
	// -- CONSTRUCTOR ----------------------------------------------
	//

	public MonitoringView () {
		super();
		world = new WorldObject();
		title = world.getName();
		
		// We add the Graphical Refresher to the Graphical Thread of SWT
		//Display.getDefault().asyncExec(Refresher.getInstance());
	}

	//
	// -- PUBLIC METHODS ----------------------------------------------
	//


	@Override
	public void createPartControl(Composite parent){
		
		// To identify each Monitoring view.
		setPartName(title);
		
		FormLayout form = new FormLayout ();
		parent.setLayout (form);

		final int limit = 50;

		virtualNodesGroup = new VirtualNodesGroup(parent);
		world.addObserver(virtualNodesGroup);
		FormData vnData = new FormData();
		vnData.left = new FormAttachment (0, 0);
		vnData.right = new FormAttachment (100, 0);
		vnData.top = new FormAttachment (0, 0);
		vnData.bottom = new FormAttachment (0, limit);
		virtualNodesGroup.getGroup().setLayoutData(vnData);

		createGraphicalViewer(parent);

		FormData graphicalViewerData = new FormData ();
		graphicalViewerData.left = new FormAttachment (0, 0);
		graphicalViewerData.right = new FormAttachment (100, 0);
		graphicalViewerData.top = new FormAttachment (virtualNodesGroup.getGroup(), 0);
		graphicalViewerData.bottom = new FormAttachment (100, -limit);
		graphicalViewer.getControl().setLayoutData(graphicalViewerData);

		//--- To change the arrow style

		FormData drawingStyleData = new FormData();
		drawingStyleData.left = new FormAttachment (0, 0);
		drawingStyleData.right = new FormAttachment (100, 0);
		drawingStyleData.top = new FormAttachment (100, -limit);
		drawingStyleData.bottom = new FormAttachment (100, 0);
		Group groupD = new Group(parent, SWT.NONE);


		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = true;
		rowLayout.justify = false;
		rowLayout.marginLeft = 100;
		rowLayout.marginTop = 8;
		rowLayout.marginRight = 5;
		rowLayout.marginBottom = 0;
		rowLayout.spacing = 5;
		groupD.setLayout(rowLayout);
		groupD.setLayoutData(drawingStyleData);


		Button topology = new Button(groupD, SWT.CHECK);
		topology.setText("Display topology");
		topology.setSelection(RoundedLine.DEFAULT_DISPLAY_TOPOLOGY);
		topology.addSelectionListener(new DisplayTopologyListener(parent));

		bProportional = new Button(groupD, SWT.RADIO);
		bProportional.setText("Proportional");
		bProportional.addSelectionListener(new DrawingStyleButtonListener(parent));

		bRatio = new Button(groupD, SWT.RADIO);
		bRatio.setText("Ratio");
		bRatio.addSelectionListener(new DrawingStyleButtonListener(parent));

		bFilaire = new Button(groupD, SWT.RADIO);
		bFilaire.setText("Filaire");
		bFilaire.addSelectionListener(new DrawingStyleButtonListener(parent));

		initStateRadioButtons();

		Button resetTopology = new Button(groupD, SWT.NONE);
		resetTopology.setText("Reset Topology");
		resetTopology.addSelectionListener(new ResetTopologyListener());

		Button monitoringEnable = new Button(groupD, SWT.CHECK);
		monitoringEnable.setText("Monitoring enable");
		monitoringEnable.setSelection(SpyEventListenerImpl.DEFAULT_IS_MONITORING);
		monitoringEnable.addSelectionListener(new EnableMonitoringListener());


		// --------------------

		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		// Adds "Monitor a new Host" action to the view's toolbar
		NewHostAction toolBarNewHost = new NewHostAction(parent.getDisplay(), world);
		toolBarManager.add(toolBarNewHost);

		// Adds "Set depth" action to the view's toolbar
		SetDepthAction toolBarSetDepth = new SetDepthAction(parent.getDisplay(), world);
		toolBarManager.add(toolBarSetDepth);

		toolBarManager.add(new Separator());

		// Adds "Set Time to refresh" action to the view's toolbar
		SetTTRAction toolBarTTR = new SetTTRAction(parent.getDisplay(), world.getMonitorThread());
		toolBarManager.add(toolBarTTR);

		// Adds refresh action to the view's toolbar
		RefreshAction toolBarRefresh = new RefreshAction(world.getMonitorThread());
		toolBarManager.add(toolBarRefresh);

		toolBarManager.add(new Separator());

		// Adds Zoom-in and Zoom-out actions to the view's toolbar
		ZoomManager zoomManager = ((ScalableFreeformRootEditPart)graphicalViewer.getRootEditPart()).getZoomManager();
		zoomManager.setZoomLevels(new double[]{0.25, 0.5, 0.75, 1.0, 1.5});

		ZoomInAction zoomIn = new ZoomInAction(zoomManager);
		zoomIn.setImageDescriptor(ImageDescriptor.createFromFile(MonitoringView.class, "zoom-in-2.gif"));
		graphicalViewer.getActionRegistry().registerAction(zoomIn);
		toolBarManager.add(zoomIn);

		ZoomOutAction zoomOut = new ZoomOutAction(zoomManager);
		zoomOut.setImageDescriptor(ImageDescriptor.createFromFile(MonitoringView.class, "zoom-out-2.gif"));
		graphicalViewer.getActionRegistry().registerAction(zoomIn);
		toolBarManager.add(zoomOut);

		toolBarManager.add(new Separator());

		// Adds "New Monitoring view" action to the view's toolbar
		NewViewAction toolBarNewView = new NewViewAction();
		toolBarManager.add(toolBarNewView);


		//graphicalViewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.NONE), MouseWheelZoomHandler.SINGLETON);

//		FigureCanvas fc = (FigureCanvas)graphicalViewer/*root.getViewer()*/.getControl();

//		if(fc != null) {
//		fc.addListener(SWT.MouseWheel, new Listener() {
//		public void handleEvent(Event event) {
//		ActionRegistry registry = getActionRegistry();
//		IAction action = registry.getAction((event.count > 0) ? 
//		GEFActionConstants.ZOOM_IN :
//		GEFActionConstants.ZOOM_OUT);

//		action.run();
//		}
//		});
//		}

//		Create the drag source on the tree
		/*		DragSource ds = new DragSource(getGraphicalViewer().getControl(), DND.DROP_MOVE);
		ds.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		ds.addDragListener(new DragSourceAdapter() {

			public void dragStart(DragSourceEvent event){
				System.out.println(".dragStart()");
				//event.doit = false;
			}

			public void dragFinished(DragSourceEvent event){
				System.out.println(".dragFinished()");
			}

			public void dragSetData(DragSourceEvent event) {
				// Set the data to be the first selected item's text
				//event.data = tree.getSelection()[0].getText();
				event.data = "Essai";
				System.out.println(".dragSetData()");
			}
		});


		// Create the drop target on the text field
		DropTarget dt = new DropTarget(getGraphicalViewer().getControl(), DND.DROP_MOVE);
		dt.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		dt.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				// Set the text field's text to the text being dropped
				//text.setText((String)event.data);
				System.out.println(".drop()");
			}
		});
		 */
	}


	/**
	 * Returns the <code>GraphicalViewer</code> of this editor.
	 * @return the <code>GraphicalViewer</code>
	 */
	public MonitoringViewer getGraphicalViewer() {
		return graphicalViewer;
	}
	
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	public VirtualNodesGroup getVirtualNodesGroup(){
		return virtualNodesGroup;
	}

	public WorldObject getWorld() {
		return world;
	}
	
	public DragAndDrop getDragAndDrop(){
		return this.dnd;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	/*   public Object getAdapter(Class adapter) {
    	if (adapter == IContentOutlinePage.class) {
            return getOverviewOutlinePage();
    	}
    	// the super implementation handles the rest
        return super.getAdapter(adapter);
    }*/


	//
	// -- PROTECTED METHODS -------------------------------------------
	//

	/**
	* Returns the <code>EditPartFactory</code> that the
	* <code>GraphicalViewer</code> will use.
	* @return the <code>EditPartFactory</code>
	*/
	protected EditPartFactory getEditPartFactory(){
		return new MonitoringEditPartFactory(this);
	}


	/**
	 * Returns the overview for the outline view.
	 * 
	 * @return the overview
	 */
	/* protected OverviewOutlinePage getOverviewOutlinePage() {
        if (null == overviewOutlinePage && null != getGraphicalViewer()) {
            RootEditPart rootEditPart = getGraphicalViewer().getRootEditPart();
            if (rootEditPart instanceof ScalableFreeformRootEditPart) {
                overviewOutlinePage =
                    new OverviewOutlinePage((ScalableFreeformRootEditPart) rootEditPart);
            }
        }

        return overviewOutlinePage;
    }*/

	//
	// -- PRIVATE METHODS -------------------------------------------
	//

	private void createGraphicalViewer(Composite parent) {
		// create graphical viewer
		graphicalViewer = new MonitoringViewer();
		graphicalViewer.createControl(parent);

		// configure the viewer
		graphicalViewer.getControl().setBackground(ColorConstants.white);
		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
		root.getFigure().addMouseListener(new WorldListener(this));
		graphicalViewer.setRootEditPart(root);

		// activate the viewer as selection provider for Eclipse
		getSite().setSelectionProvider(graphicalViewer);

		// initialize the viewer with input
		graphicalViewer.setEditPartFactory(getEditPartFactory());
//		world.addObserver(MonitorThread.getInstance());
		graphicalViewer.setContents(world);

		createActions(parent.getDisplay());
		ContextMenuProvider contextMenu = new MonitoringContextMenuProvider(graphicalViewer);
		graphicalViewer.setContextMenu(contextMenu);
		getSite().registerContextMenu(contextMenu, graphicalViewer);
	}

	private void createActions(Display display) {
		ActionRegistry registry = graphicalViewer.getActionRegistry();

		registry.registerAction(new NewHostAction(display, world));
		registry.registerAction(new SetDepthAction(display, world));
		registry.registerAction(new RefreshAction(world.getMonitorThread()));
		registry.registerAction(new SetTTRAction(display, world.getMonitorThread()));
		registry.registerAction(new RefreshHostAction());
		registry.registerAction(new RefreshJVMAction());
		registry.registerAction(new RefreshNodeAction());
		registry.registerAction(new StopMonitoringAction());
		registry.registerAction(new SetUpdateFrequenceAction(display));
		registry.registerAction(new VerticalLayoutAction());
		registry.registerAction(new HorizontalLayoutAction());
	}

	private void initStateRadioButtons(){
		switch (RoundedLine.DEFAULT_STYLE) {
		case FILAIRE:
			bFilaire.setSelection(true);
			break;
		case PROPORTIONAL:
			bProportional.setSelection(true);
			break;
		case RATIO:
			bRatio.setSelection(true);
			break;
		}
	}



	//
	// -- INNER CLASSES -------------------------------------------
	//


	public class MonitoringViewer extends ScrollingGraphicalViewer {

		private ActionRegistry registry;

		public MonitoringViewer() {
			this.registry = new ActionRegistry();
		}

		public ActionRegistry getActionRegistry () {
			return this.registry;
		}
	}

	private class DrawingStyleButtonListener extends SelectionAdapter {

		private Composite globalcontainer;

		public DrawingStyleButtonListener(Composite globalContainer){
			this.globalcontainer = globalContainer;
		}

		public void widgetSelected(SelectionEvent e) {
			if(((Button) e.widget).getSelection()){
				if(e.widget.equals(bProportional))
					RoundedLine.setDrawingStyle(RoundedLine.DrawingStyle.PROPORTIONAL);
				else if(e.widget.equals(bRatio))
					RoundedLine.setDrawingStyle(RoundedLine.DrawingStyle.RATIO);
				else
					RoundedLine.setDrawingStyle(RoundedLine.DrawingStyle.FILAIRE);
				// We need to have the monitoring panel in order to update the display.
				// Warning : If the order of the graphics objects change then
				// it is also necessary to change the index of the table.
				// (The index of the array is 1, because :
				// In 0 we have the virtual nodes view, and in 2 we have the panel
				// containing the buttons for the drawing style.)
				Control[] children = this.globalcontainer.getChildren();
				if(children.length>=2 && children[1]!=null)
					children[1].redraw();
			}
		}
	}

	private class DisplayTopologyListener extends SelectionAdapter {

		private Composite globalcontainer;

		public DisplayTopologyListener(Composite globalContainer){
			this.globalcontainer = globalContainer;
		}

		public void widgetSelected(SelectionEvent e) {
			RoundedLine.setDisplayTopology(((Button) e.widget).getSelection());
			// We need to have the monitoring panel in order to update the display.
			// Warning : If the order of the graphics objects change then
			// it is also necessary to change the index of the table.
			// (The index of the array is 1, because :
			// In 0 we have the virtual nodes view, and in 2 we have the panel
			// containing the buttons for the drawing style.)
			Control[] children = this.globalcontainer.getChildren();
			if(children.length>=2 && children[1]!=null)
				children[1].redraw();
		}
	}

	private class EnableMonitoringListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			SpyEventListenerImpl.SetMonitoring(((Button) e.widget).getSelection());
		}
	}

	private class ResetTopologyListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {
			world.resetCommunications();
		}
	}
}
