package org.objectweb.proactive.ic2d.timit.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.util.ExecuteReport;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


public final class ReportDropDownAction extends Action implements IMenuCreator {
    private final TimItView timItView;

    private Menu fMenu;

    public ReportDropDownAction(final TimItView timItView) {
        this.timItView = timItView;
        setMenuCreator(this);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/generate_report.gif"), null)));
    }

    public Menu getMenu(Menu parent) {
        return null;
    }

    public Menu getMenu(Control parent) {
        if (fMenu != null)
            fMenu.dispose();
        fMenu = new Menu(parent);
        // Create an item for html report
        new ActionContributionItem(new GenerateReportAndSaveAction(this.timItView, ExecuteReport.HTML_FORMAT))
                .fill(fMenu, -1);
        // Create an item for pdf report
        new ActionContributionItem(new GenerateReportAndSaveAction(this.timItView, ExecuteReport.PDF_FORMAT))
                .fill(fMenu, -1);
        return fMenu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
        if (fMenu != null) {
            fMenu.dispose();
            fMenu = null;
        }
    }
}