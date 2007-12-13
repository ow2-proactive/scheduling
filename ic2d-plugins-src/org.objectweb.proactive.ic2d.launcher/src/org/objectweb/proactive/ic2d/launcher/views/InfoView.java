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
package org.objectweb.proactive.ic2d.launcher.views;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.Launcher;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.launcher.Activator;
import org.objectweb.proactive.ic2d.launcher.actions.Launch;
import org.objectweb.proactive.ic2d.launcher.editors.PathEditorInput;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptor;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptor.FileState;
import org.objectweb.proactive.ic2d.launcher.files.XMLDescriptorSet;
import org.objectweb.proactive.ic2d.launcher.perspectives.LauncherPerspective;


public class InfoView extends ViewPart implements Observer {
    public static final String ID = "org.objectweb.proactive.ic2d.launcher.views.InfoView";
    private TableViewer viewer;

    // Launches an application
    private Action action1;

    // Display some informations about the application
    private Action action2;

    // Kills the application
    private Action action3;

    // Open the descriptor into an XML editor.
    private Action doubleClickAction;

    //
    // -- CONSTRUCTORS ---------------------------------------------
    //

    /**
     * The constructor.
     */
    public InfoView() {
        XMLDescriptorSet.getInstance().addObserver(this);
    }

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        ViewContentProvider vcp = new ViewContentProvider();
        viewer.setContentProvider(vcp);
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new NameSorter());
        viewer.setInput(getViewSite());
        makeActions();
        hookContextMenu();
        // TODO Fix the bug of the double click action
        //hookDoubleClickAction();
        contributeToActionBars();
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     *  @see Observer#update(Observable o, Object arg)
     */
    public void update(Observable o, Object arg) {
        if (o instanceof XMLDescriptorSet) {
            if (arg instanceof XMLDescriptor) { // A new file has been added to the XMLDescriptorSet
                XMLDescriptor file = (XMLDescriptor) arg;
                file.addObserver(this);
            }
            if (this.viewer != null) {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IPerspectiveRegistry perspectiveRegistry = workbench.getPerspectiveRegistry();
                IPerspectiveDescriptor perspective = perspectiveRegistry
                        .findPerspectiveWithId(LauncherPerspective.ID);
                if ((perspective != null) && !this.viewer.getControl().isDisposed()) {
                    this.viewer.refresh();
                }
            }
        } else if (o instanceof XMLDescriptor) {
            if (arg instanceof String) {
                String message = (String) arg;
                showError((XMLDescriptor) o, message);
            } else {
                if (this.viewer != null) {
                    IWorkbench workbench = PlatformUI.getWorkbench();
                    IPerspectiveRegistry perspectiveRegistry = workbench.getPerspectiveRegistry();
                    IPerspectiveDescriptor perspective = perspectiveRegistry
                            .findPerspectiveWithId(LauncherPerspective.ID);
                    if ((perspective != null) & !this.viewer.getControl().isDisposed()) {
                        this.viewer.refresh();
                    }
                }
            }
        }
    }

    //
    // -- PRIVATE METHODS ---------------------------------------------
    //
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                InfoView.this.fillContextMenu(manager);
            }
        });

        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(action1);
        manager.add(new Separator());
        manager.add(action2);
        manager.add(new Separator());
        manager.add(action3);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(action1);
        manager.add(action2);
        manager.add(action3);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(action1);
        manager.add(action2);
        manager.add(action3);
    }

    private void makeActions() {
        // Action 1 : Launches an application
        action1 = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                if ((((IStructuredSelection) selection).size() == 0) ||
                    (((IStructuredSelection) selection).getFirstElement() == null)) {
                    return;
                }
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                IWorkbenchWindow workbenchWindow = getSite().getWorkbenchWindow();
                IWorkbenchPage page = workbenchWindow.getActivePage();
                Launch.launch(page, obj.toString());
            }
        };
        action1.setText("Launch the application");
        action1.setToolTipText("Launch the application");
        action1.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "launch.gif"));

        // Action 2 : Display some informations about the application
        action2 = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                if ((((IStructuredSelection) selection).size() == 0) ||
                    (((IStructuredSelection) selection).getFirstElement() == null)) {
                    return;
                }
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                showInfo(obj.toString());
            }
        };
        action2.setText("Informations");
        action2.setToolTipText("Informations");
        action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJS_INFO_TSK));

        // Action 3 : Kills the application
        action3 = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                if ((((IStructuredSelection) selection).size() == 0) ||
                    (((IStructuredSelection) selection).getFirstElement() == null)) {
                    return;
                }
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                XMLDescriptor file = XMLDescriptorSet.getInstance().getFile(obj.toString());
                FileState state = file.getState();
                if (state != FileState.LAUNCHED) {
                    showWarning("The " + file.getShortName() + " application  was not launched!");
                    return;
                }

                Launcher launcher = file.getLauncher();
                if (launcher.isActivated()) {
                    try {
                        launcher.getProActiveDescriptor().killall(true);
                        file.setState(FileState.KILLED);
                    } catch (ProActiveException e) {
                        Console.getInstance(Activator.CONSOLE_NAME).logException(e);
                    }
                }
            }
        };
        action3.setText("Kill the application");
        action3.setToolTipText("Kill the application");
        action3.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "kill.gif"));

        // doubleClickAction : Open the descriptor into an XML editor.
        doubleClickAction = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();

                IWorkbenchWindow workbenchWindow = getSite().getWorkbenchWindow();
                IWorkbenchPage page = /*workbenchWindow.getActivePage();*/getSite().getPage();
                IWorkbench workbench = workbenchWindow.getWorkbench();

                // Opens the corresponding page.
                openPerspective(workbench, page);

                // Opens the corresponding editor.
                openEditor(workbench, page, obj.toString());
            }
        };
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    /**
     * Shows the informations about the file to the screen.
     * @param path The file's path
     */
    private void showInfo(String path) {
        String msg = null;
        XMLDescriptor file = XMLDescriptorSet.getInstance().getFile(path);
        String ID = XMLDescriptorSet.getInstance().getFile(path).getJobID();
        if (ID != null) {
            ID = "ID = " + ID + "\n";
        }
        String name = "Name = " + file.getShortName() + "\n";
        String status = "Status =" + file.getState().toString();
        if (ID != null) {
            msg = ID + name + status;
        } else {
            msg = name + status;
        }
        msg = msg + "\nPath = " + path;
        MessageDialog.openInformation(viewer.getControl().getShell(), "Info", msg);
    }

    /**
     * Shows an error message.
     * @param file The source xml descriptor.
     * @param message
     */
    private void showError(XMLDescriptor file, String message) {
        MessageDialog.openError(viewer.getControl().getShell(), "Error", message);
    }

    /**
     * Shows a warning message.
     * @param message The massage to display.
     */
    private void showWarning(String message) {
        MessageDialog.openWarning(viewer.getControl().getShell(), "Warning", message);
    }

    /**
     * Opens the perspective corresponding to the Launcher.
     * @param workbench
     * @param page
     */
    private void openPerspective(IWorkbench workbench, IWorkbenchPage page) {
        IPerspectiveRegistry reg = workbench.getPerspectiveRegistry();
        page.setPerspective(reg.findPerspectiveWithId(LauncherPerspective.ID));
    }

    /**
     * Opens the editor corresponding to the file having this file path.
     * @param workbench
     * @param page
     * @param path The file path.
     */
    private void openEditor(IWorkbench workbench, IWorkbenchPage page, String path) {
        IEditorInput input = new PathEditorInput(new Path(path));
        IEditorRegistry editorRegistry = workbench.getEditorRegistry();
        IEditorDescriptor descriptor = editorRegistry.getDefaultEditor(path);
        String editorId = descriptor.getId();
        try {
            if (page != null) {
                page.openEditor(input, editorId);
            }
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

    //
    // -- INNER CLASSES ---------------------------------------------
    //

    /*
     * The content provider class is responsible for
     * providing objects to the view. It can wrap
     * existing objects in adapters or simply return
     * objects as-is. These objects may be sensitive
     * to the current input of the view, or ignore
     * it and always show the same content.
     */
    class ViewContentProvider implements IStructuredContentProvider {
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            return XMLDescriptorSet.getInstance().getFilePaths();
        }
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object obj, int index) {
            return XMLDescriptorSet.getInstance().getFileNameToDisplay((String) obj);
        }

        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }

        public Image getImage(Object obj) {
            String image = XMLDescriptorSet.getInstance().getFile((String) obj).getImage();
            return new Image(Display.getCurrent(), this.getClass().getResourceAsStream(image));
        }
    }

    class NameSorter extends ViewerSorter {
    }
}
