package org.objectweb.proactive.ic2d.timit.views;

import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.timit.actions.CollapseAllAction;
import org.objectweb.proactive.ic2d.timit.actions.DeleteTreeAction;
import org.objectweb.proactive.ic2d.timit.actions.ExpandAllAction;
import org.objectweb.proactive.ic2d.timit.actions.SaveToXmlAction;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.editparts.tree.TreeEditPartFactory;


// TODO : FILTER ALL DEPENDANT COMPUTATIONS IF THIS VIEW IS NOT ACTIVE
public class TimerTreeView extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.timit.views.TreeView";
    protected TreeViewer treeViewer;
    protected TimerTreeHolder timerTreeHolder;
    protected SaveToXmlAction saveToXmlAction;
    protected ExpandAllAction expandAllAction;
    protected CollapseAllAction collapseAllAction;
    protected DeleteTreeAction deleteTreeAction;

    public TimerTreeView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
        this.treeViewer = new TreeViewer();

        this.treeViewer.createControl(parent);

        this.treeViewer.setEditDomain(new DefaultEditDomain(null));

        this.treeViewer.setEditPartFactory(new TreeEditPartFactory(this));

        if (this.timerTreeHolder == null) {
            this.timerTreeHolder = new TimerTreeHolder();
            this.treeViewer.setContents(this.timerTreeHolder);
        }

        // --------------------
        IToolBarManager toolBarManager = getViewSite()
                                             .getActionBars().getToolBarManager();

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
}
