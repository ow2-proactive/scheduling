/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.views;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extensions.dataspaces.exceptions.DataSpacesException;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.DataServers;
import org.ow2.proactive.scheduler.gui.data.DataServers.Server;
import org.ow2.proactive.scheduler.gui.data.TableColumnSorter;


/**
 * Displays a list of currently deployed Data Servers
 * <p>
 * Allows creation and removal of DataServers
 *
 */
public class ServersView extends ViewPart {

    public static final String ID = "org.ow2.proactive.scheduler.gui.views.ServersView";

    /**
     * history file for root directory, contains up to 20 non duplicate entries
     */
    public static final File rootHistoryFile = new File(System.getProperty("user.home") +
        "/.ProActive_Scheduler/dataserver_root.history");

    /**
     * history file for DS name, contains up to 20 non duplicate entries
     */
    public static final File nameHistoryFile = new File(System.getProperty("user.home") +
        "/.ProActive_Scheduler/dataserver_name.history");

    private TableViewer viewer;
    private Table table;
    private DataServers.Server selectedServer;

    private Action addServerAction;
    private Action removeServerAction;
    private Action exploreServerAction;
    private Action copyURLAction;
    private Action startServer;
    private Action rebindServer;
    private Action stopServer;

    /*
     * Data contained by the table, will be used by the label provider 
     */
    class ViewContentProvider implements IStructuredContentProvider {
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        /*
         * will be used by LabelProvider#getColumnText() ; one Object per column
         */
        public Object[] getElements(Object parent) {
            return DataServers.getInstance().getServers().values().toArray();
        }
    }

    /*
     * defines what is actually displayed in the table,
     * force update using TableViewer#refresh()
     */
    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
        private Image img = Activator.getDefault().getImageRegistry().getDescriptor(Internal.IMG_DATA)
                .createImage();

        private Image up = Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SERVER_STARTED).createImage();
        private Image down = Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SERVER_STOPPED).createImage();

        public String getColumnText(Object obj, int index) {
            Server s = (Server) obj;
            // column 1 : rootDir
            if (index == 0) {
                return s.getRootDir();
            }
            // column 2 : protocol
            else if (index == 1) {
                String proto = "default";
                if (s.getProtocol() != null)
                    proto = s.getProtocol().toUpperCase();
                return proto;
            }
            // column 3 : name
            else if (index == 2) {
                return s.getName();
            }
            // column 4 : status
            else if (index == 3) {
                if (s.isStarted())
                    return "Running";
                else
                    return "Stopped";
            }
            // column 5 : url
            else if (index == 4) {
                return s.getUrl();
            }
            return "?";
        }

        public Image getColumnImage(Object obj, int index) {
            if (index == 0) {
                // put an image in the first column only 
                return getImage(obj);
            } else if (index == 3) {
                Server s = (Server) obj;
                if (s.isStarted())
                    return this.up;
                else
                    return this.down;
            } else {
                return null;
            }
        }

        public Image getImage(Object obj) {
            return img;
        }
    }

    public ServersView() {
    }

    /*
     * create the layout
     * 
     */
    @Override
    public void createPartControl(Composite parent) {
        table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setText("Path");
        column1.setWidth(200);

        TableColumn column11 = new TableColumn(table, SWT.LEFT);
        column11.setText("Protocol");
        column11.setWidth(70);
        
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setText("Name");
        column2.setWidth(100);

        TableColumn column3 = new TableColumn(table, SWT.LEFT);
        column3.setText("Status");
        column3.setWidth(100);

        TableColumn column4 = new TableColumn(table, SWT.LEFT);
        column4.setText("URL");
        column4.setWidth(100);

        table.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Widget widget = event.item;
                selectedServer = (DataServers.Server) widget.getData();
                removeServerAction.setEnabled(true);
                copyURLAction.setEnabled(selectedServer.isStarted());
                startServer.setEnabled(!selectedServer.isStarted());
                rebindServer.setEnabled(!selectedServer.isStarted());
                stopServer.setEnabled(selectedServer.isStarted());
            }
        });

        viewer = new TableViewer(table);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setInput(getViewSite());
        viewer.setComparator(new TableColumnSorter(viewer) {
            @Override
            protected int doCompare(Viewer v, int index, Object e1, Object e2) {
                Server s1 = (Server) e1;
                Server s2 = (Server) e2;
                switch (index) {
                    case 0:
                        File dir1 = new File(s1.getRootDir());
                        File dir2 = new File(s2.getRootDir());
                        return dir1.compareTo(dir2);
                    case 1:
                        return s1.getName().compareTo(s2.getName());
                    case 2:
                        Boolean b1 = new Boolean(s1.isStarted());
                        Boolean b2 = new Boolean(s2.isStarted());
                        return b1.compareTo(b2);
                    case 3:
                        String u1 = s1.getUrl();
                        String u2 = s2.getUrl();
                        if (u1 == null)
                            u1 = "";
                        if (u2 == null)
                            u2 = "";
                        return u1.compareTo(u2);
                    default:
                        return 0;
                }
            }
        });

        makeActions(parent);
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                ServersView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(copyURLAction);
        manager.add(startServer);
        manager.add(rebindServer);
        manager.add(stopServer);
        manager.add(new Separator());
        manager.add(addServerAction);
        manager.add(removeServerAction);
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(addServerAction);
        manager.add(removeServerAction);
        manager.add(new Separator());
        manager.add(startServer);
        manager.add(rebindServer);
        manager.add(stopServer);
        manager.add(copyURLAction);
    }

    private void makeActions(final Composite parent) {
        ImageDescriptor desc = Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SERVER_ADD);
        addServerAction = new Action("Add Data Server", desc) {
            /**
             * Pops up a new 'Add a Data Server dialog'
             */
            public void run() {
                final Shell dialog = new Shell(parent.getDisplay(),  SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
                dialog.setText("New Data Server");

                GridLayout l = new GridLayout(1, false);
                l.marginLeft = 0;
                l.marginRight = 0;
                l.horizontalSpacing = 0;
                l.marginWidth = 0;
                dialog.setLayout(l);

                final Composite content = new Composite(dialog, 0);
                GridLayout li = new GridLayout(1, false);
                li.marginLeft = 0;
                li.marginRight = 0;
                li.horizontalSpacing = 0;
                li.marginWidth = 0;
                content.setLayout(li);
                content.setLayoutData(new GridData(GridData.FILL_BOTH));

                // first line : Label
                final Label r1 = new Label(content, SWT.NULL | SWT.BOLD);
                r1.setText("Create a new Data Server");

                final Composite c1 = new Composite(content, 0);
                GridLayout l1 = new GridLayout(3, false);
                c1.setLayout(l1);
                c1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

                // second line : root dir
                final Label l1a = new Label(c1, SWT.NULL);
                l1a.setText("Root directory");

                final Combo t1a = new Combo(c1, SWT.DROP_DOWN);
                t1a.setItems(getHistory(rootHistoryFile));
                GridData t1ag = new GridData(GridData.FILL_HORIZONTAL | GridData.BEGINNING);
                t1a.setLayoutData(t1ag);

                final Button b1a = new Button(c1, SWT.PUSH);
                b1a.setText("Open");
                b1a.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
                b1a.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        DirectoryDialog fd = new DirectoryDialog(dialog, SWT.NULL);
                        String path = fd.open();
                        if (path != null) {
                            t1a.setText(path);
                        }
                    }
                });

                // third line : name
                final Label l1b = new Label(c1, SWT.NULL);
                l1b.setText("Name");

                final Combo t1b = new Combo(c1, SWT.DROP_DOWN);
                t1b.setItems(getHistory(nameHistoryFile));
                GridData t1bg = new GridData(GridData.FILL_HORIZONTAL | GridData.BEGINNING);
                t1bg.horizontalSpan = 2;
                t1b.setLayoutData(t1bg);

                // fourth line : rebind
                final Button r2 = new Button(content, SWT.CHECK);
                r2.setText("Rebind existing server");
                r2.setSelection(true);
                r2.setToolTipText("If a server with the same Name is currently bound, reuse it");

                // fifth line : autostart 
                final Button r3 = new Button(content, SWT.CHECK);
                r3.setText("Start server");
                r3.setSelection(true);
                r3.setToolTipText("Start the Server immediately, or add it in a stopped state");

                // sixth line : protocol
                final Button r4 = new Button(content, SWT.CHECK);
                r4.setText("Use default protocol (recommanded)");
                r4.setSelection(true);
                r4.setToolTipText("Override protocol configuration. For experimented users only");

                // seventh line : protocol selection
                final Composite c4 = new Composite(content, 0);
                GridLayout l4 = new GridLayout(2, false);
                c4.setLayout(l4);
                c4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

                final Label l4a = new Label(c4, SWT.NULL);
                l4a.setText("Protocol");
                l4a.setEnabled(false);

                final Combo c4b = new Combo(c4, SWT.DROP_DOWN);
                c4b.setItems(new String[] { "rmi", "rmissh", "rmissl", "http", "pnp", "pnps", "pamr" });
                GridData c4bg = new GridData(GridData.BEGINNING);
                c4b.setLayoutData(c4bg);
                c4b.setEnabled(false);

                r4.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        c4b.setEnabled(!r4.getSelection());
                        l4a.setEnabled(!r4.getSelection());
                    }
                });

                // hidden progress bar
                final Composite progress = new Composite(dialog, 0);
                GridData pgd = new GridData(GridData.FILL_BOTH);
                progress.setLayoutData(pgd);
                progress.setLayout(new GridLayout(1, false));
                pgd.exclude = true;
                progress.setVisible(false);

                final Label pl = new Label(progress, SWT.NULL);
                pl.setText("Please wait...");
                pl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

                final ProgressBar pb = new ProgressBar(progress, SWT.INDETERMINATE | SWT.HORIZONTAL);
                GridData pg = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
                pb.setLayoutData(pg);

                // hidden label
                final Label status = new Label(dialog, 0);
                GridData statusg = new GridData(SWT.LEFT, SWT.TOP, true, false);
                statusg.exclude = true;
                status.setLayoutData(statusg);
                status.setVisible(false);

                // hidden textbox
                final Text st = new Text(dialog, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
                GridData stg = new GridData(SWT.CENTER, SWT.CENTER, true, true);
                stg.exclude = true;
                st.setLayoutData(stg);
                st.setVisible(false);

                // Buttons
                final Label sep = new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL);
                GridData gs = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
                sep.setLayoutData(gs);

                Composite buttons = new Composite(dialog, 0);
                GridData g5 = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END);
                buttons.setLayoutData(g5);
                buttons.setLayout(new RowLayout(SWT.HORIZONTAL));

                final Button prevButton = new Button(buttons, SWT.PUSH);
                prevButton.setText(" < Previous ");
                prevButton.setEnabled(false);

                final Button nextButton = new Button(buttons, SWT.PUSH);
                nextButton.setText(" Next > ");

                final Button cancelButton = new Button(buttons, SWT.PUSH);
                cancelButton.setText(" Cancel ");

                final Button finishButton = new Button(buttons, SWT.PUSH);
                finishButton.setEnabled(false);
                finishButton.setText(" Finish ");

                prevButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        // hack : a widget cannot be added/removed, has to be hidden.. swt < swing
                        content.setVisible(true);
                        ((GridData) content.getLayoutData()).exclude = false;

                        status.setVisible(false);
                        ((GridData) status.getLayoutData()).exclude = true;

                        st.setVisible(false);
                        ((GridData) st.getLayoutData()).exclude = true;

                        t1a.setItems(getHistory(rootHistoryFile));
                        t1b.setItems(getHistory(nameHistoryFile));

                        nextButton.setEnabled(true);
                        prevButton.setEnabled(false);

                        Point p = dialog.getSize();
                        dialog.pack();
                        dialog.setSize(p);
                    }
                });

                nextButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        // hack : a widget cannot be added/removed, has to be hidden.. swt < swing
                        content.setVisible(false);
                        ((GridData) content.getLayoutData()).exclude = true;

                        progress.setVisible(true);
                        ((GridData) progress.getLayoutData()).exclude = false;

                        nextButton.setEnabled(false);
                        prevButton.setEnabled(false);

                        Point p = dialog.getSize();
                        dialog.pack();
                        dialog.setSize(p);

                        // final data used by the worker thread
                        final boolean rebind = r2.getSelection();
                        //     final boolean fork = r3.getSelection();
                        final StringBuilder message = new StringBuilder();
                        final StringBuilder error = new StringBuilder();
                        final String rootDir = t1a.getText();
                        final String dsName = t1b.getText();
                        final boolean[] errorOccurred = { false };
                        final boolean startServer = r3.getSelection();
                        final boolean hasProto = !r4.getSelection();
                        final String proto = c4b.getText();

                        /*
                         * server creation needs to run in a worker thread
                         */
                        Thread th = new Thread(new Runnable() {
                            public void run() {

                                try {
                                    if (dsName.trim().length() == 0) {
                                        throw new DataSpacesException("Data Server name cannot be empty.");
                                    }
                                    File f = new File(rootDir);
                                    if (!(f.exists() && f.isDirectory())) {
                                        throw new DataSpacesException(
                                            "Data Server root directory must be an existing directory");
                                    }

                                    String pro = null;
                                    if (hasProto && !proto.trim().equals(""))
                                        pro = proto;

                                    DataServers.getInstance().addServer(rootDir, dsName, rebind, startServer,
                                            pro);

                                    addHistory(rootHistoryFile, rootDir, false);
                                    addHistory(nameHistoryFile, dsName, false);

                                    message.append("Successfully created new Data Server");
                                    errorOccurred[0] = false;

                                } catch (Throwable e) {
                                    message.append("Error while creating Data Server");
                                    if (e.getMessage() != null && e.getMessage().trim().length() > 1) {
                                        message.append(":\n" + e.getMessage());
                                    }
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    PrintWriter pw = new PrintWriter(os);
                                    e.printStackTrace(pw);
                                    pw.close();
                                    error.append(os.toString());
                                    Activator.log(IStatus.INFO, "Error while creating Data Server", e);
                                    errorOccurred[0] = true;
                                }

                                /*
                                 * propagate data on screen : need to get back in SWT thread
                                 */
                                dialog.getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        if (errorOccurred[0]) {
                                            prevButton.setEnabled(true);
                                            if (error.toString().length() > 0) {
                                                st.setText(error.toString());
                                                st.setVisible(true);
                                                ((GridData) st.getLayoutData()).exclude = false;
                                                status.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true,
                                                    false));
                                            } else {
                                                status.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
                                                    true));
                                            }
                                        } else {
                                            status.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
                                                true));

                                            // notify viewer it needs to update
                                            viewer.refresh();

                                            cancelButton.setEnabled(false);
                                            finishButton.setEnabled(true);
                                        }

                                        progress.setVisible(false);
                                        ((GridData) progress.getLayoutData()).exclude = true;

                                        status.setText(message.toString());
                                        status.setVisible(true);
                                        ((GridData) status.getLayoutData()).exclude = false;

                                        Point p = dialog.getSize();
                                        dialog.pack();
                                        dialog.setSize(p);
                                    }
                                });
                            }
                        });
                        th.start();
                    }
                });

                finishButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        dialog.close();
                    }
                });

                cancelButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        dialog.close();
                    }
                });

                dialog.pack();
                dialog.setSize(400, dialog.getSize().y);
                dialog.open();
            }
        };
        addServerAction.setToolTipText("Add a new Data Server");

        ImageDescriptor desc2 = Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SERVER_REMOVE);
        removeServerAction = new Action("Remove Data Server", desc2) {
            public void run() {
                final Shell dialog = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
                dialog.setText("Remove Data Server");

                GridLayout gl = new GridLayout(2, false);
                dialog.setLayout(gl);

                Label l1 = new Label(dialog, SWT.NULL);
                l1.setText("Remove Data Server:");
                l1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

                final Combo t1 = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
                String[] items = new String[DataServers.getInstance().getServers().size()];
                int i = 0, selIndex = -1;
                for (Server srv : DataServers.getInstance().getServers().values()) {
                    items[i] = srv.getName();
                    if (srv.getName().equals(selectedServer.getName())) {
                        selIndex = i;
                    }
                    i++;
                }
                t1.setItems(items);
                t1.select(selIndex);
                t1.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL));

                Label l2 = new Label(dialog, SWT.NULL);
                l2.setText("Do you want to continue ?");
                GridData l2gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
                l2gd.horizontalSpan = 2;
                l2.setLayoutData(l2gd);

                Composite buttons = new Composite(dialog, 0);
                GridData g5 = new GridData(SWT.CENTER, SWT.BOTTOM, true, false);
                g5.horizontalSpan = 2;
                buttons.setLayoutData(g5);
                buttons.setLayout(new RowLayout(SWT.HORIZONTAL));

                // line 3 : cancel button
                Button c1 = new Button(buttons, SWT.PUSH);
                c1.setText(" Cancel ");
                c1.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        dialog.close();
                    }
                });

                // line 3 : OK button
                Button c2 = new Button(buttons, SWT.PUSH);
                c2.setText("  OK  ");
                c2.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        String name = t1.getItem(t1.getSelectionIndex());
                        try {
                            DataServers.getInstance().removeServer(name);
                        } catch (DataSpacesException e) {
                            Activator.log(IStatus.ERROR, "Failed to Stop ", e);
                            MessageDialog.openError(parent.getShell(), "Data Servers",
                                    "Failed to stop Data Server " + name);
                        }
                        viewer.refresh();
                        dialog.close();
                        table.deselectAll();
                        removeServerAction.setEnabled(false);
                        copyURLAction.setEnabled(false);
                        startServer.setEnabled(false);
                        rebindServer.setEnabled(false);
                        stopServer.setEnabled(false);
                    }
                });

                dialog.pack();
                dialog.setSize(400, dialog.getSize().y + 25);
                dialog.open();
            }
        };
        removeServerAction.setEnabled(false);
        removeServerAction.setToolTipText("Stop and Remove a Data Server");
        removeServerAction.setToolTipText("Copy the Data Server URL to clipboard");

        ImageDescriptor desc3 = Activator.getDefault().getImageRegistry().getDescriptor(Internal.IMG_COPY);
        copyURLAction = new Action("Copy server URL", desc3) {
            public void run() {
                Clipboard clipboard = new Clipboard(parent.getDisplay());
                String plainText = selectedServer.getUrl();
                String rtfText = "{\\rtf1\\b " + selectedServer.getUrl() + "}";
                TextTransfer textTransfer = TextTransfer.getInstance();
                RTFTransfer rftTransfer = RTFTransfer.getInstance();
                clipboard.setContents(new String[] { plainText, rtfText }, new Transfer[] { textTransfer,
                        rftTransfer });
                clipboard.dispose();

            }
        };
        copyURLAction.setEnabled(false);

        ImageDescriptor desc4 = Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SCHEDULERSTART);
        startServer = new Action("Start", desc4) {
            public void run() {
                Thread th = new Thread(new Runnable() {
                    public void run() {
                        if (selectedServer != null && !selectedServer.isStarted()) {
                            try {
                                selectedServer.start(false);
                                parent.getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        viewer.refresh();
                                    }
                                });

                                startServer.setEnabled(false);
                                rebindServer.setEnabled(false);
                                stopServer.setEnabled(true);
                            } catch (final DataSpacesException e) {
                                Activator.log(IStatus.ERROR, "Could not start server " +
                                    selectedServer.getName(), e);
                                parent.getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        MessageDialog.openError(parent.getShell(), "Data Server",
                                                "Could not start server " + selectedServer.getName() + ":\n" +
                                                    e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                });
                th.start();
            }
        };
        startServer.setEnabled(false);
        startServer.setToolTipText("Start a stopped Data Server");

        ImageDescriptor desc5 = Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SCHEDULERSTOP);
        stopServer = new Action("Stop", desc5) {
            public void run() {
                Thread th = new Thread(new Runnable() {
                    public void run() {
                        if (selectedServer != null && selectedServer.isStarted()) {
                            try {
                                selectedServer.stop();

                                parent.getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        viewer.refresh();
                                    }
                                });

                                startServer.setEnabled(true);
                                rebindServer.setEnabled(true);
                                stopServer.setEnabled(false);
                            } catch (final DataSpacesException e) {
                                Activator.log(IStatus.ERROR, "Could not stop server " +
                                    selectedServer.getName(), e);
                                parent.getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        MessageDialog.openError(parent.getShell(), "Data Server",
                                                "Could not stop server " + selectedServer.getName() + ":\n" +
                                                    e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                });
                th.start();
            }
        };
        stopServer.setEnabled(false);
        stopServer.setToolTipText("Stop a running Data Server");

        ImageDescriptor desc6 = Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SERVER_REBIND);
        rebindServer = new Action("Rebind", desc6) {
            public void run() {
                Thread th = new Thread(new Runnable() {
                    public void run() {
                        if (selectedServer != null && !selectedServer.isStarted()) {
                            try {
                                selectedServer.start(true);
                                parent.getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        viewer.refresh();
                                    }
                                });

                                startServer.setEnabled(false);
                                rebindServer.setEnabled(false);
                                stopServer.setEnabled(true);
                            } catch (final DataSpacesException e) {
                                Activator.log(IStatus.ERROR, "Could not rebind server " +
                                    selectedServer.getName(), e);
                                Display.getCurrent().asyncExec(new Runnable() {
                                    public void run() {
                                        MessageDialog.openError(parent.getShell(), "Data Server",
                                                "Could not rebind server " + selectedServer.getName() +
                                                    ":\n" + e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                });
                th.start();
            }
        };
        rebindServer.setEnabled(false);
        rebindServer.setToolTipText("Rebind an existing Data Server");

        exploreServerAction = new Action() {
            public void run() {
                // TODO exploring selected server
            }
        };
    }

    public static void addHistory(File file, String line, boolean keepDupesNoLimit) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, "Could not open history file " + file.getAbsolutePath(), e);
            }
        }
        String[] content = getHistory(file);
        int lim = (keepDupesNoLimit ? 100 : 20);
        int len = Math.min(lim, content.length);

        ArrayList<String> ar = new ArrayList<String>(len);
        for (int i = 0; i < len; i++) {
            if (!content[i].equals(line) || keepDupesNoLimit) {
                ar.add(content[i]);
            }
        }

        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
            pw.println(line);
            for (String str : ar) {
                pw.println(str);
            }
            pw.close();
        } catch (FileNotFoundException e) {
            Activator.log(IStatus.ERROR, "Could not open history file " + file.getAbsolutePath(), e);
        }
    }

    public static String[] getHistory(File f) {
        if (!f.exists()) {
            return new String[] {};
        } else {
            ArrayList<String> ar = new ArrayList<String>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                String line = null;

                while ((line = br.readLine()) != null) {
                    ar.add(line);
                }
            } catch (FileNotFoundException e) {
                Activator.log(IStatus.ERROR, "Could not open history file " + f.getAbsolutePath(), e);
                return null;
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, "Could not read history file " + f.getAbsolutePath(), e);
                return null;
            }

            String[] ret = new String[ar.size()];
            int i = 0;
            for (String str : ar) {
                ret[i++] = str;
            }
            return ret;
        }
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                exploreServerAction.run();
            }
        });
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}