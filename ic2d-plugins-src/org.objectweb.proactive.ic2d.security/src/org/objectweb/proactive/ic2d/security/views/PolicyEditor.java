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
package org.objectweb.proactive.ic2d.security.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */
public class PolicyEditor extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.security.views.PolicyEditor";
    private Action action1;
    private Action action2;
    private Action doubleClickAction;
    private FormToolkit toolkit;
    private ScrolledForm form;

    /**
     * The constructor.
     */
    public PolicyEditor() {
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite parent) {
        this.toolkit = new FormToolkit(parent.getDisplay());
        this.form = this.toolkit.createScrolledForm(parent);
        this.form.setText("");

        GridLayout layout = new GridLayout();
        this.form.getBody().setLayout(layout);

        Hyperlink link = this.toolkit.createHyperlink(this.form.getBody(),
                "Click here.", SWT.WRAP);
        link.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    System.out.println("Link activated!");
                }
            });

        FormText ft = this.toolkit.createFormText(this.form.getBody(), false);
        ft.setToolTipText("blaaaaaaaaaaaaaaaaaaaaaaa");
    }

    /*private void hookContextMenu() {
            MenuManager menuMgr = new MenuManager("#PopupMenu");
            menuMgr.setRemoveAllWhenShown(true);
            menuMgr.addMenuListener(new IMenuListener() {
                    public void menuAboutToShow(IMenuManager manager) {
                            PolicyEditor.this.fillContextMenu(manager);
                    }
            });
            Menu menu = menuMgr.createContextMenu(this.viewer.getControl());
            this.viewer.getControl().setMenu(menu);
            getSite().registerContextMenu(menuMgr, this.viewer);
    }

    private void contributeToActionBars() {
            IActionBars bars = getViewSite().getActionBars();
            fillLocalPullDown(bars.getMenuManager());
            fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
            manager.add(this.action1);
            manager.add(new Separator());
            manager.add(this.action2);
    }

    private void fillContextMenu(IMenuManager manager) {
            manager.add(this.action1);
            manager.add(this.action2);
            // Other plug-ins can contribute there actions here
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
            manager.add(this.action1);
            manager.add(this.action2);
    }

    private void makeActions() {
            this.action1 = new Action() {
                    @Override
                    public void run() {
                            showMessage("Action 1 executed");
                    }
            };
            this.action1.setText("Action 1");
            this.action1.setToolTipText("Action 1 tooltip");
            this.action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
                    getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

            this.action2 = new Action() {
                    @Override
                    public void run() {
                            showMessage("Action 2 executed");
                    }
            };
            this.action2.setText("Action 2");
            this.action2.setToolTipText("Action 2 tooltip");
            this.action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
                            getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
            this.doubleClickAction = new Action() {
                    @Override
                    public void run() {
                            ISelection selection = PolicyEditor.this.viewer.getSelection();
                            Object obj = ((IStructuredSelection)selection).getFirstElement();
                            showMessage("Double-click detected on "+obj.toString());
                    }
            };
    }

    private void hookDoubleClickAction() {
            this.viewer.addDoubleClickListener(new IDoubleClickListener() {
                    public void doubleClick(DoubleClickEvent event) {
                            PolicyEditor.this.doubleClickAction.run();
                    }
            });
    }
    private void showMessage(String message) {
            MessageDialog.openInformation(
                    this.viewer.getControl().getShell(),
                    "Policy Editor",
                    message);
    }
    */

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
    }
}
