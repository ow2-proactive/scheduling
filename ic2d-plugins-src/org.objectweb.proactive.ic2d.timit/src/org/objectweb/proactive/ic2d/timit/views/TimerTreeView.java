package org.objectweb.proactive.ic2d.timit.views;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.timit.actions.CollapseAllAction;
import org.objectweb.proactive.ic2d.timit.actions.DeleteTreeAction;
import org.objectweb.proactive.ic2d.timit.actions.ExpandAllAction;
import org.objectweb.proactive.ic2d.timit.actions.SaveToXmlAction;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.editparts.tree.TreeEditPartFactory;


// TODO : FILTER ALL DEPENDANT COMPUTATIONS IF THIS VIEW IS NOT ACTIVE
public class TimerTreeView extends ViewPart {
    public static final int NUMBER_OF_COLUMNS = 5;
    public static final int NAME_COLUMN = 0;
    public static final int TIME_COLUMN = 1;
    public static final int TOTAL_PERCENT_COLUMN = 2;
    public static final int INVOCATIONS_COLUMN = 3;
    public static final int PARENT_PERCENT_COLUMN = 4;
    public static final String ID = "org.objectweb.proactive.ic2d.timit.views.TreeView";
    protected TreeViewer treeViewer;
    protected TimerTreeHolder timerTreeHolder;
    protected SaveToXmlAction saveToXmlAction;
    protected ExpandAllAction expandAllAction;
    protected CollapseAllAction collapseAllAction;
    protected DeleteTreeAction deleteTreeAction;
    private EditDomain editDomain;

    public TimerTreeView() {
        super();
        this.timerTreeHolder = new TimerTreeHolder();
    }

    @Override
    public void createPartControl(Composite parent) {
        this.treeViewer = new TreeViewer();
        this.treeViewer.createControl(parent);
        this.editDomain = new EditDomain();

        this.treeViewer.setEditDomain(this.editDomain);

        this.treeViewer.setEditPartFactory(new TreeEditPartFactory(this));
        this.treeViewer.setContents(this.timerTreeHolder);

        RootTreeEditPart t = (RootTreeEditPart) this.treeViewer.getRootEditPart();

        final Tree tree = (Tree) t.getWidget();

        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeColumn nameColumn = new TreeColumn(tree, SWT.CENTER);
        nameColumn.setText("Name");
        nameColumn.setWidth(200);

        TreeColumn timeColumn = new TreeColumn(tree, SWT.RIGHT);
        timeColumn.setText("Time [ms]");
        timeColumn.setWidth(100);

        TreeColumn totalPercentColumn = new TreeColumn(tree, SWT.CENTER);
        totalPercentColumn.setText("Total [%]");
        totalPercentColumn.setWidth(100);

        TreeColumn invocationsColumn = new TreeColumn(tree, SWT.CENTER);
        invocationsColumn.setText("Invocations");
        invocationsColumn.setWidth(80);

        TreeColumn parentPercentColumn = new TreeColumn(tree, SWT.CENTER);
        parentPercentColumn.setText("Parent [%]");
        parentPercentColumn.setWidth(100);

        //////////////////////////////////////////
        Listener sortListener = new Listener() {
                public void handleEvent(Event e) {
                    if (tree.getItems().length == 0) {
                        return;
                    }

                    // determine new sort column and direction
                    TreeColumn sortColumn = tree.getSortColumn();
                    TreeColumn currentColumn = (TreeColumn) e.widget;
                    int dir = tree.getSortDirection();
                    if (sortColumn == currentColumn) {
                        dir = (dir == SWT.UP) ? SWT.DOWN : SWT.UP;
                    } else {
                        tree.setSortColumn(currentColumn);
                        dir = SWT.UP;
                    }
                    final int columnIndex = tree.indexOf(currentColumn);
                    boolean up = (dir == SWT.UP);
                    for (final TimerObject t : timerTreeHolder.getDummyRoots()) {
                        // Get first child of each dummy root and fire sort event
                        TimerObject target = t.getChildren().get(0);
                        target.firePropertyChange(TimerObject.P_SORT,
                            columnIndex, up);
                        target.firePropertyChange(TimerObject.P_EXPAND_STATE,
                            null, true);
                    }
                    tree.setSortDirection(dir);
                }
            };
        timeColumn.addListener(SWT.Selection, sortListener);
        totalPercentColumn.addListener(SWT.Selection, sortListener);
        invocationsColumn.addListener(SWT.Selection, sortListener);
        parentPercentColumn.addListener(SWT.Selection, sortListener);

        // --------------------
        IToolBarManager toolBarManager = getViewSite().getActionBars()
                                             .getToolBarManager();

        // Adds "DeleteTreeAction" action to the view's toolbar
        this.deleteTreeAction = new DeleteTreeAction();
        toolBarManager.add(deleteTreeAction);

        toolBarManager.add(new Separator());

        // Adds "SaveToXmlAction" action to the view's toolbar
        this.saveToXmlAction = new SaveToXmlAction(this.timerTreeHolder);
        toolBarManager.add(saveToXmlAction);

        // Adds "ExpandAllAction" action to the view's toolbar
        this.expandAllAction = new ExpandAllAction(this.timerTreeHolder);
        toolBarManager.add(expandAllAction);

        // Adds "CollapseAllAction" action to the view's toolbar
        this.collapseAllAction = new CollapseAllAction(this.timerTreeHolder);
        toolBarManager.add(collapseAllAction);
    }

    @Override
    public void setFocus() {
    }

    public DeleteTreeAction getDeleteTreeAction() {
        return deleteTreeAction;
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }
}
