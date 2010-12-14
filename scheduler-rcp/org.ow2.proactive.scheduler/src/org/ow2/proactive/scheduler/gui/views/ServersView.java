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
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.DataServers;
import org.ow2.proactive.scheduler.gui.data.DataServers.Server;


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

        public String getColumnText(Object obj, int index) {
            Server s = (Server) obj;
            // column 1 : rootDir
            if (index == 0) {
                return s.getRootDir();
            }
            // column 2 : url
            else if (index == 1) {
                return s.getUrl();
            }
            return "?";
        }

        public Image getColumnImage(Object obj, int index) {
            if (index == 0) {
                // put an image in the first column only 
                return getImage(obj);
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
        table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setText("Path");
        column1.setWidth(200);

        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setText("URL");
        column2.setWidth(100);

        table.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Widget widget = event.item;
                selectedServer = (DataServers.Server) widget.getData();
                removeServerAction.setEnabled(true);
                copyURLAction.setEnabled(true);
            }
        });

        viewer = new TableViewer(table);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setInput(getViewSite());

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
        manager.add(addServerAction);
        manager.add(removeServerAction);
        manager.add(copyURLAction);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(addServerAction);
        manager.add(removeServerAction);
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
                final Shell dialog = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
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

                // fifth line : fork 
                /*
                final Button r3 = new Button(content, SWT.CHECK);
                r3.setText("Fork server");
                r3.setSelection(false);
                r3.setToolTipText("Start the server in a separate process so it "
                    + "keeps running when the client is closed");
                 */

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
                        final String t1atext = t1a.getText();
                        final String t1btext = t1b.getText();
                        final boolean[] errorOccurred = { false };

                        /*
                         * server creation needs to run in a worker thread
                         */
                        Thread th = new Thread(new Runnable() {
                            public void run() {

                                // create new dataserver
                                FileSystemServerDeployer deployer = null;
                                try {
                                    /*
                                    if (fork) {
                                        int pid = 0;
                                        String url = "";

                                        // TODO
                                        // System.exec
                                        // get pid, url

                                        DataServers.getInstance().addServer(url, t1atext, t1btext, true, pid);

                                    } else {
                                     */

                                    deployer = new FileSystemServerDeployer(t1btext, t1atext, true, rebind);
                                    DataServers.getInstance().addServer(deployer, deployer.getVFSRootURL(),
                                            t1atext, t1btext);

                                    addHistory(rootHistoryFile, t1atext);
                                    addHistory(nameHistoryFile, t1btext);

                                    message.append("Successfully created new Data Server");
                                    errorOccurred[0] = false;
                                } catch (Throwable e) {
                                    message.append("Error while creating Data Server");
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    PrintWriter pw = new PrintWriter(os);
                                    e.printStackTrace(pw);
                                    pw.close();
                                    error.append(os.toString());
                                    Activator.log(IStatus.INFO, "Error while creating Data Server", e);
                                    errorOccurred[0] = true;

                                    if (deployer != null) {
                                        try {
                                            deployer.terminate();
                                        } catch (ProActiveException e1) {
                                        }
                                    }
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
        addServerAction.setToolTipText("Create or add an existing Data Server");

        ImageDescriptor desc2 = Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SERVER_REMOVE);
        removeServerAction = new Action("Remove Data Server", desc2) {
            public void run() {
                final Shell dialog = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
                dialog.setText("Remove Data Server");

                dialog.setLayout(new GridLayout());

                Label l1 = new Label(dialog, SWT.NULL);
                l1.setText("Removing Data Server '" + selectedServer.getName() + "' at URL:");
                l1.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, true));

                Text t1 = new Text(dialog, SWT.SINGLE | SWT.H_SCROLL | SWT.BORDER | SWT.READ_ONLY);
                t1.setText(selectedServer.getUrl());
                t1.setEditable(false);
                t1.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

                Label l2 = new Label(dialog, SWT.NULL);
                l2.setText("Do you want to continue ?");
                l2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

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
                        DataServers.getInstance().removeServer(selectedServer.getUrl());
                        viewer.refresh();
                        dialog.close();
                        removeServerAction.setEnabled(false);
                        copyURLAction.setEnabled(false);
                    }
                });

                dialog.pack();
                dialog.setSize(400, dialog.getSize().y + 50);
                dialog.open();
            }
        };
        removeServerAction.setEnabled(false);
        removeServerAction.setToolTipText("Remove or shutdown a Data Server");

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
        removeServerAction.setToolTipText("Copy the Data Server URL to clipboard");

        exploreServerAction = new Action() {
            public void run() {
                // TODO exploring selected server
            }
        };
    }

    private void addHistory(File file, String line) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, "Could not open history file " + file.getAbsolutePath(), e);
            }
        }
        String[] content = getHistory(file);
        int len = Math.min(20, content.length);
        ArrayList<String> ar = new ArrayList<String>(len);
        for (int i = 0; i < len; i++) {
            if (!content[i].equals(line)) {
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

    private String[] getHistory(File f) {
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

        DataServers.cleanup();
    }
}