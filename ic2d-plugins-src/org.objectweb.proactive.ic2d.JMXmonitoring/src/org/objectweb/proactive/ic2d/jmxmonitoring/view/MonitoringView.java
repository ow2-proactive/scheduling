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
package org.objectweb.proactive.ic2d.jmxmonitoring.view;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.EnableDisableMonitoringAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.HorizontalLayoutAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.KillVMAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.MonitoringContextMenuProvider;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.NewHostAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.NewViewAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.P2PAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshHostAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshJVMAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshNodeAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.SetDepthAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.SetTTRAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.SetUpdateFrequenceAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.StopMonitoringAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.VerticalLayoutAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.dnd.DragAndDrop;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.MonitoringEditPartFactory;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.RoundedLine;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.DragHost;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.WorldListener;


public class MonitoringView extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView";
    private String title;

    /** the graphical viewer */
    private MonitoringViewer graphicalViewer;

    /** the overview outline page */
    //private OverviewOutlinePage overviewOutlinePage;
    private Button bProportional;
    private Button bRatio;
    private Button bFixed;

    /** The World */
    private WorldObject world;
    private DragAndDrop dnd = new DragAndDrop();
    private DragHost dragHost = new DragHost();

    /** The graphical set of virtual nodes */
    private VirtualNodesGroup virtualNodesGroup;

    //
    // -- CONSTRUCTOR ----------------------------------------------
    //
    public MonitoringView() {
        super();
        world = new WorldObject();
        title = world.getName();
    }

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //
    @Override
    public void createPartControl(Composite parent) {
        // To identify each Monitoring view.
        setPartName(title);

        FormLayout form = new FormLayout();
        parent.setLayout(form);

        final int topLimit = 46;
        final int limit = 62;

        virtualNodesGroup = new VirtualNodesGroup(parent);
        world.addObserver(virtualNodesGroup);
        FormData vnData = new FormData();
        vnData.left = new FormAttachment(0, 0);
        vnData.right = new FormAttachment(100, 0);
        vnData.top = new FormAttachment(0, 0);
        vnData.bottom = new FormAttachment(0, topLimit);
        virtualNodesGroup.getGroup().setLayoutData(vnData);

        createGraphicalViewer(parent);

        FormData graphicalViewerData = new FormData();
        graphicalViewerData.left = new FormAttachment(0, 0);
        graphicalViewerData.right = new FormAttachment(100, 0);
        graphicalViewerData.top = new FormAttachment(virtualNodesGroup.getGroup(),
                0);
        graphicalViewerData.bottom = new FormAttachment(100, -limit);
        graphicalViewer.getControl().setLayoutData(graphicalViewerData);

        //--- To change the arrow style
        FormData drawingStyleData = new FormData();
        drawingStyleData.left = new FormAttachment(0, 0);
        drawingStyleData.right = new FormAttachment(100, 0);
        drawingStyleData.top = new FormAttachment(100, -limit);
        drawingStyleData.bottom = new FormAttachment(100, 0);
        Group groupD = new Group(parent, SWT.NONE);

        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = false;
        rowLayout.pack = true;
        rowLayout.justify = false;
        rowLayout.marginLeft = 140;
        rowLayout.marginTop = 1;
        rowLayout.marginRight = 5;
        rowLayout.marginBottom = 5;
        rowLayout.spacing = 5;
        groupD.setLayout(rowLayout);
        groupD.setLayoutData(drawingStyleData);

        Group autoResetGroup = new Group(groupD, SWT.NONE);
        autoResetGroup.setText("Auto Reset");
        RowLayout autoResetLayout = new RowLayout();
        autoResetGroup.setLayout(autoResetLayout);

        final Button autoResetEnable = new Button(autoResetGroup, SWT.TOGGLE);
        autoResetEnable.setText("Enable");
        autoResetEnable.setToolTipText("Enable or Disable the Auto Reset");
        autoResetEnable.setSelection(WorldObject.DEFAULT_ENABLE_AUTO_RESET);
        autoResetEnable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean selected = autoResetEnable.getSelection();
                    world.setEnableAutoResetTime(selected);
                }
            });

        final Spinner autoReset = new Spinner(autoResetGroup, SWT.NONE);
        // Allow 0 decimal places
        autoReset.setDigits(0);
        // Set the minimum value to 1 s
        autoReset.setMinimum(WorldObject.MIN_AUTO_RESET_TIME);
        // Set the maximum value to 60 s
        autoReset.setMaximum(WorldObject.MAX_AUTO_RESET_TIME);
        // Set the increment value to 1 s
        autoReset.setIncrement(1);
        // Set the seletion to 0.800 s
        autoReset.setSelection(WorldObject.DEFAULT_AUTO_RESET_TIME);
        autoReset.setToolTipText("Auto reset time of the arrows");
        autoReset.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    int selection = autoReset.getSelection();
                    world.setAutoResetTime(selection);
                }
            });

        autoReset.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if ((e.keyCode == SWT.KEYPAD_CR) || (e.keyCode == SWT.CR) ||
                            (e.keyCode == SWT.UP) || (e.keyCode == SWT.DOWN) ||
                            (e.keyCode == SWT.ARROW_UP) ||
                            (e.keyCode == SWT.ARROW_DOWN) ||
                            (e.keyCode == SWT.PAGE_UP) ||
                            (e.keyCode == SWT.PAGE_DOWN)) {
                        int selection = autoReset.getSelection();
                        world.setAutoResetTime(selection);
                    }
                }
            });

        Label titleLabel = new Label(autoResetGroup, SWT.NONE);
        titleLabel.setText("seconds");

        Group drawingStyleGroup = new Group(groupD, SWT.NONE);
        drawingStyleGroup.setText("Drawing style");
        RowLayout drawingStyleLayout = new RowLayout();
        drawingStyleGroup.setLayout(drawingStyleLayout);

        bProportional = new Button(drawingStyleGroup, SWT.RADIO);
        bProportional.setText("Proportional");
        bProportional.addSelectionListener(new DrawingStyleButtonListener(
                parent));

        bRatio = new Button(drawingStyleGroup, SWT.RADIO);
        bRatio.setText("Ratio");
        bRatio.addSelectionListener(new DrawingStyleButtonListener(parent));

        bFixed = new Button(drawingStyleGroup, SWT.RADIO);
        bFixed.setText("Fixed");
        bFixed.addSelectionListener(new DrawingStyleButtonListener(parent));

        initStateRadioButtons();

        Group topologyGroup = new Group(groupD, SWT.NONE);
        topologyGroup.setText("Topology");
        RowLayout topologyLayout = new RowLayout();
        topologyGroup.setLayout(topologyLayout);

        Button topology = new Button(topologyGroup, SWT.TOGGLE);
        topology.setText("Display");
        topology.setToolTipText("Display or not the topology");
        topology.setSelection(RoundedLine.DEFAULT_DISPLAY_TOPOLOGY);
        topology.addSelectionListener(new DisplayTopologyListener(parent));

        Button resetTopology = new Button(topologyGroup, SWT.NONE);
        resetTopology.setText("Reset");
        resetTopology.setToolTipText("Reset the topology");
        resetTopology.setSize(3, 3);
        resetTopology.addSelectionListener(new ResetTopologyListener());

        // --------------------
        IToolBarManager toolBarManager = getViewSite().getActionBars()
                                             .getToolBarManager();

        // Adds "Monitor a new Host" action to the view's toolbar
        NewHostAction toolBarNewHost = new NewHostAction(parent.getDisplay(),
                world);
        toolBarManager.add(toolBarNewHost);

        // Adds "Set depth" action to the view's toolbar
        SetDepthAction toolBarSetDepth = new SetDepthAction(parent.getDisplay(),
                world);
        toolBarManager.add(toolBarSetDepth);

        toolBarManager.add(new Separator());

        // Adds "Set Time to refresh" action to the view's toolbar
        SetTTRAction toolBarTTR = new SetTTRAction(parent.getDisplay(),
                world.getMonitorThread());
        toolBarManager.add(toolBarTTR);

        // Adds refresh action to the view's toolbar
        RefreshAction toolBarRefresh = new RefreshAction(world.getMonitorThread());
        toolBarManager.add(toolBarRefresh);

        // Adds enable/disable monitoring action to the view's toolbar
        EnableDisableMonitoringAction toolBarEnableDisableMonitoring = new EnableDisableMonitoringAction(world);
        toolBarManager.add(toolBarEnableDisableMonitoring);

        // Adds enable/disable monitoring action to the view's toolbar
        P2PAction toolBarP2P = new P2PAction(world);
        toolBarManager.add(toolBarP2P);

        toolBarManager.add(new Separator());

        // Adds Zoom-in and Zoom-out actions to the view's toolbar
        ZoomManager zoomManager = ((ScalableFreeformRootEditPart) graphicalViewer.getRootEditPart()).getZoomManager();
        zoomManager.setZoomLevels(new double[] { 0.25, 0.5, 0.75, 1.0, 1.5 });

        ZoomInAction zoomIn = new ZoomInAction(zoomManager);
        zoomIn.setImageDescriptor(ImageDescriptor.createFromFile(
                MonitoringView.class, "zoom-in-2.gif"));
        graphicalViewer.getActionRegistry().registerAction(zoomIn);
        toolBarManager.add(zoomIn);

        ZoomOutAction zoomOut = new ZoomOutAction(zoomManager);
        zoomOut.setImageDescriptor(ImageDescriptor.createFromFile(
                MonitoringView.class, "zoom-out-2.gif"));
        graphicalViewer.getActionRegistry().registerAction(zoomOut);
        toolBarManager.add(zoomOut);

        toolBarManager.add(new Separator());

        // Adds "New Monitoring view" action to the view's toolbar
        NewViewAction toolBarNewView = new NewViewAction();
        toolBarManager.add(toolBarNewView);
    }

    /**
     * Returns the <code>GraphicalViewer</code> of this editor.
     * @return the <code>GraphicalViewer</code>
     */
    public MonitoringViewer getGraphicalViewer() {
        return graphicalViewer;
    }

    public void setFocus() { /* Do nothing */
    }

    public VirtualNodesGroup getVirtualNodesGroup() {
        return virtualNodesGroup;
    }

    public WorldObject getWorld() {
        return world;
    }

    public DragAndDrop getDragAndDrop() {
        return this.dnd;
    }

    public DragHost getDragHost() {
        return this.dragHost;
    }

    @Override
    public void dispose() {
        //TODO A faire
        //world.stopMonitoring(true);
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
    protected EditPartFactory getEditPartFactory() {
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
        WorldListener listener = new WorldListener(this);
        root.getFigure().addMouseListener(listener);
        root.getFigure().addMouseMotionListener(listener);
        graphicalViewer.setRootEditPart(root);

        // activate the viewer as selection provider for Eclipse
        getSite().setSelectionProvider(graphicalViewer);

        // initialize the viewer with input
        graphicalViewer.setEditPartFactory(getEditPartFactory());
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
        registry.registerAction(new SetTTRAction(display,
                world.getMonitorThread()));
        registry.registerAction(new RefreshHostAction());
        registry.registerAction(new RefreshJVMAction());
        registry.registerAction(new RefreshNodeAction());
        registry.registerAction(new StopMonitoringAction());
        registry.registerAction(new KillVMAction());
        registry.registerAction(new SetUpdateFrequenceAction(display));
        registry.registerAction(new VerticalLayoutAction());
        registry.registerAction(new HorizontalLayoutAction());

        // Get all available actions defined by possibly provided 
        // extensions for the extension point monitoring_action	
        try {
            IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
                                                     .getExtensionPoint(Activator.PLUGIN_ID +
                    ".actions_extension");
            if (extensionPoint == null) {
                return;
            }
            IConfigurationElement[] configs = extensionPoint.getConfigurationElements();

            for (int x = 0; x < configs.length; x++) {
                IActionExtPoint providedAction = (IActionExtPoint) configs[x].createExecutableExtension(
                        "class");
                registry.registerAction(providedAction);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initStateRadioButtons() {
        switch (RoundedLine.DEFAULT_STYLE) {
        case FIXED:
            bFixed.setSelection(true);
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

        public ActionRegistry getActionRegistry() {
            return this.registry;
        }
    }

    private class DrawingStyleButtonListener extends SelectionAdapter {
        private Composite globalcontainer;

        public DrawingStyleButtonListener(Composite globalContainer) {
            this.globalcontainer = globalContainer;
        }

        public void widgetSelected(SelectionEvent e) {
            if (((Button) e.widget).getSelection()) {
                if (e.widget.equals(bProportional)) {
                    RoundedLine.setDrawingStyle(RoundedLine.DrawingStyle.PROPORTIONAL);
                } else if (e.widget.equals(bRatio)) {
                    RoundedLine.setDrawingStyle(RoundedLine.DrawingStyle.RATIO);
                } else {
                    RoundedLine.setDrawingStyle(RoundedLine.DrawingStyle.FIXED);
                }

                // We need to have the monitoring panel in order to update the display.
                // Warning : If the order of the graphics objects change then
                // it is also necessary to change the index of the table.
                // (The index of the array is 1, because :
                // In 0 we have the virtual nodes view, and in 2 we have the panel
                // containing the buttons for the drawing style.)
                Control[] children = this.globalcontainer.getChildren();
                if ((children.length >= 2) && (children[1] != null)) {
                    children[1].redraw();
                }
            }
        }
    }

    private class DisplayTopologyListener extends SelectionAdapter {
        private Composite globalcontainer;

        public DisplayTopologyListener(Composite globalContainer) {
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
            if ((children.length >= 2) && (children[1] != null)) {
                children[1].redraw();
            }
        }
    }

    private class ResetTopologyListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            world.resetCommunications();
        }
    }
}
