package org.objectweb.proactive.extra.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.objectweb.proactive.extra.scheduler.gui.data.SchedulerProxy;


/**
 * @author FRADJ Johann
 */
public class PriorityJobAction extends Action implements IMenuCreator {
    private static PriorityJobAction instance = null;
    private Menu fMenu;

    private PriorityJobAction() {
        setText("Change job priority");
        setToolTipText("To change a job priority");
        setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(),
                "icons/job_priority.png"));
        setMenuCreator(this);
        setEnabled(false);
    }

    /**
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
        if (fMenu != null) {
            fMenu.dispose();
        }
        fMenu = null;
    }

    /**
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        if (fMenu != null) {
            fMenu.dispose();
        }

        fMenu = new Menu(parent);
        boolean isAnAdmin = SchedulerProxy.getInstance().isAnAdmin();
        if (isAnAdmin) {
            addActionToMenu(fMenu, PriorityIdleJobAction.getInstance());
        }
        addActionToMenu(fMenu, PriorityLowestJobAction.getInstance());
        addActionToMenu(fMenu, PriorityLowJobAction.getInstance());
        addActionToMenu(fMenu, PriorityNormalJobAction.getInstance());
        if (isAnAdmin) {
            addActionToMenu(fMenu, PriorityHighJobAction.getInstance());
            addActionToMenu(fMenu, PriorityHighestJobAction.getInstance());
        }
        return fMenu;
    }

    private void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    /**
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
        return null;
    }

    public static PriorityJobAction newInstance() {
        instance = new PriorityJobAction();
        return instance;
    }

    public static PriorityJobAction getInstance() {
        return instance;
    }
}
