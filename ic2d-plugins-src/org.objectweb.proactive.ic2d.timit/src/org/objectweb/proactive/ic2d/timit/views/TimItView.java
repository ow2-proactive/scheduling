package org.objectweb.proactive.ic2d.timit.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.timit.data.ChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.editparts.TimItEditPartFactory;
import org.objectweb.proactive.ic2d.timit.figures.listeners.ChartListener;


/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */
public class TimItView extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.timit.views.TimItView";
    public static Button refreshSelectedButton;
    public static Button refreshAllButton;
    private ScrollingGraphicalViewer timItViewer;
    private ChartContainerObject chartContainer;

    /**
     * The constructor.
     */
    public TimItView() {
        this.chartContainer = new ChartContainerObject();
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     * @param Composite parent object
     */
    public void createPartControl(Composite parent) {
        FormLayout form = new FormLayout();
        parent.setLayout(form);

        final int limit = 62;

        //////////////////////////
        createGraphicalViewer(parent);

        FormData graphicalViewerData = new FormData();
        graphicalViewerData.left = new FormAttachment(0, 0);
        graphicalViewerData.right = new FormAttachment(100, 0);
        graphicalViewerData.top = new FormAttachment(0, 0);
        graphicalViewerData.bottom = new FormAttachment(100, -limit);
        this.timItViewer.getControl().setLayoutData(graphicalViewerData);

        //////////////////////////		
        FormData drawingStyleData = new FormData();
        drawingStyleData.left = new FormAttachment(0, 0);
        drawingStyleData.right = new FormAttachment(100, 0);
        drawingStyleData.top = new FormAttachment(100, -limit);
        drawingStyleData.bottom = new FormAttachment(100, 0);
        Group groupD = new Group(parent, SWT.NONE);

        RowLayout rowLayout = new RowLayout();
        rowLayout.justify = true;
        groupD.setLayout(rowLayout);
        groupD.setLayoutData(drawingStyleData);

        //////////////////////////
        Group refreshGroup = new Group(groupD, SWT.NONE);
        refreshGroup.setText("Refresh Charts");
        RowLayout refreshLayout = new RowLayout();
        refreshGroup.setLayout(refreshLayout);

        TimItView.refreshSelectedButton = new Button(refreshGroup, SWT.PUSH);
        refreshSelectedButton.setText("Refresh Selected");
        refreshSelectedButton.setToolTipText("Refresh the Selected Chart");
        refreshSelectedButton.setSelection(false);
        refreshSelectedButton.setEnabled(false);
        refreshSelectedButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (ChartListener.lastSelected != null) {
                        ((ChartObject) ChartListener.lastSelected.getModel()).performSnapshot();
                    }
                }
            });

        TimItView.refreshAllButton = new Button(refreshGroup, SWT.PUSH);
        refreshAllButton.setText("Refresh All");
        refreshAllButton.setToolTipText("Refresh All Available Charts");
        refreshAllButton.setSelection(false);
        refreshAllButton.setEnabled(false);
        refreshAllButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                for (ChartObject o : chartContainer.getChildrenList()) {
                                    o.performSnapshot();
                                }
                            }
                        });
                }
            });
    }

    /**
     * Creates the graphical viewer.
     * @param parent The Composite parent object
     */
    private void createGraphicalViewer(Composite parent) {
        // create graphical viewer
        this.timItViewer = new ScrollingGraphicalViewer();
        this.timItViewer.createControl(parent);

        // configure the viewer
        this.timItViewer.getControl().setBackground(ColorConstants.white);
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
        // root.getFigure().addMouseListener(new WorldListener(this));
        this.timItViewer.setRootEditPart(root);

        // activate the viewer as selection provider for Eclipse
        getSite().setSelectionProvider(this.timItViewer);

        // initialize the viewer with input
        this.timItViewer.setEditPartFactory(new TimItEditPartFactory());

        // //////////////////////////////////////////////////
        // Add contents				
        this.timItViewer.setContents(chartContainer);

        // ADD for resize event
        parent.addControlListener(new ControlAdapter() { /* resize listener */
                public void controlResized(ControlEvent event) {
                    chartContainer.update(true); //timItViewer.getContents().refresh();
                }
            });
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
    }

    /**
     * A getter for the chartContainer (root model)
     * @return ChartContainerObject reference
     */
    public ChartContainerObject getChartContainer() {
        return chartContainer;
    }

    /**
     * A setter for the chartContainer (root model)
     * @param chartContainer
     */
    public void setChartContainer(ChartContainerObject chartContainer) {
        this.chartContainer = chartContainer;
    }
}
