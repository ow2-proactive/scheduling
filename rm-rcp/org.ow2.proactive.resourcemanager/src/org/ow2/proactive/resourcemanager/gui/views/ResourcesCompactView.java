/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.views;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.gui.compact.CompactViewer;
import org.ow2.proactive.resourcemanager.gui.compact.Filter;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.handlers.ConnectHandler;


/**
 * Compact matrix view of resource pool.
 */
public class ResourcesCompactView extends ViewPart {

    public static final String ID = "org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView";
    private static CompactViewer compactView;
    private static ResourcesCompactView instance;

    public ResourcesCompactView() {
        instance = this;
    }

    @Override
    public void createPartControl(Composite parent) {
        compactView = new CompactViewer(parent);

        if (!RMStore.isConnected()) {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    ConnectHandler.getHandler().execute(Display.getDefault().getActiveShell());
                }
            });
        }

        compactView.init();
        hookContextMenu();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("PopupMenu");
        menuMgr.add(new Separator("top"));
        menuMgr.setRemoveAllWhenShown(true);
        Menu menu = menuMgr.createContextMenu(compactView.getComposite());
        compactView.getComposite().setMenu(menu);
        getSite().registerContextMenu(menuMgr, compactView);
    }

    /**
     * Called when view is closed
     * sacrifices tabViewer to garbage collector
     */
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        compactView.dispose();
        compactView = null;
    }

    @Override
    public void setFocus() {
    }

    public static CompactViewer getCompactViewer() {
        return compactView;
    }

    public void putTreeViewOnTop() {
        try {
            this.getViewSite().getWorkbenchWindow().getActivePage().showView(ResourceExplorerView.ID);
        } catch (PartInitException e) {
            Activator.log(IStatus.ERROR, "An error occured. ", e);
        }
    }

    public static ResourcesCompactView getInstance() {
        return instance;
    }

    public void repaint(Filter filter) {
        compactView.repaint(filter);
    }
}
