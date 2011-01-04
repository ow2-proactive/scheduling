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
package org.ow2.proactive.scheduler.gui.preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.ow2.proactive.scheduler.Activator;


/**
 * Manages File association for remote connection
 * <p>
 * example : associate 'vnc' with '/usr/bin/vncviewer'
 * 
 *  
 */
public class RemoteConnectionPreferences extends PreferencePage implements IWorkbenchPreferencePage {

    public static final File remoteConnPropsFile = new File(System.getProperty("user.home") +
        "/.ProActive_Scheduler/remote.apps.prop");

    private Table table = null;

    private boolean dirty = false;

    public RemoteConnectionPreferences() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Associate a remote connection method with an application available on this system.");
    }

    @Override
    protected Control createContents(final Composite parent) {

        Composite pane = new Composite(parent, 0);

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        pane.setLayout(layout);

        table = new Table(pane, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalSpan = 2;
        table.setLayoutData(gd);

        TableColumn tc1 = new TableColumn(table, SWT.LEFT);
        tc1.setText("Type");
        tc1.setWidth(100);
        TableColumn tc2 = new TableColumn(table, SWT.LEFT);
        tc2.setText("Application");
        tc2.setWidth(200);

        Button addButton = new Button(pane, SWT.PUSH);
        addButton.setText("Add...");
        GridData b1 = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
        b1.horizontalAlignment = SWT.FILL;
        addButton.setLayoutData(b1);
        addButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {

                // Dialog
                final Shell dialog = new Shell(parent.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
                dialog.setText("Add new association");

                // Dialog > Grid layout : 2 columns
                GridLayout l = new GridLayout();
                l.numColumns = 2;
                dialog.setLayout(l);

                // line 1 column 1 : appType label
                Label l1 = new Label(dialog, SWT.NULL);
                l1.setText("Application type");
                GridData g1 = new GridData(GridData.BEGINNING);
                l1.setLayoutData(g1);

                // line 2 column 2 : appType text box
                final Text t1 = new Text(dialog, SWT.SINGLE | SWT.BORDER);
                GridData g2 = new GridData(GridData.FILL_HORIZONTAL | GridData.END);
                t1.setLayoutData(g2);

                // line 2 column 1 : path label
                Label l2 = new Label(dialog, SWT.NULL);
                l2.setText("Path to executable");
                GridData g3 = new GridData(GridData.BEGINNING);
                l2.setLayoutData(g3);

                // line 2 column 2 : grid layout 2 columns 1 line
                Composite tc = new Composite(dialog, 0);
                GridLayout tcg = new GridLayout(2, false);
                tcg.marginLeft = 0;
                tcg.marginRight = 0;
                tcg.horizontalSpacing = 0;
                tcg.marginWidth = 0;
                tc.setLayout(tcg);
                GridData g4 = new GridData(GridData.FILL_HORIZONTAL | GridData.END);
                tc.setLayoutData(g4);

                // line 2 column 2 : line 1 column 1 : app text box
                final Text t2 = new Text(tc, SWT.SINGLE | SWT.BORDER);
                GridData gt2 = new GridData(GridData.FILL_HORIZONTAL | GridData.BEGINNING);
                t2.setLayoutData(gt2);

                // line 2 column 2 : line 1 column 2 : filechooser button
                Button bb = new Button(tc, SWT.PUSH);
                bb.setText("Open");
                GridData gbb = new GridData(GridData.HORIZONTAL_ALIGN_END);
                bb.setLayoutData(gbb);
                bb.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        FileDialog fd = new FileDialog(dialog, SWT.NULL);
                        String path = fd.open();
                        if (path != null) {
                            t2.setText(path);
                        }
                    }
                });

                // line 3 : horizontal separator
                Label sep = new Label(dialog, SWT.SEPARATOR | SWT.HORIZONTAL);
                GridData gs = new GridData(GridData.FILL_HORIZONTAL);
                gs.horizontalSpan = 2;
                sep.setLayoutData(gs);

                // line 4 : buttons
                Composite buttons = new Composite(dialog, 0);
                GridData g5 = new GridData(GridData.HORIZONTAL_ALIGN_END);
                g5.horizontalSpan = 2;
                buttons.setLayoutData(g5);
                buttons.setLayout(new RowLayout(SWT.HORIZONTAL));

                // line 4 : cancel button
                Button c1 = new Button(buttons, SWT.PUSH);
                c1.setText("Cancel");
                c1.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        dialog.close();
                    }
                });

                // line 4 : OK button
                Button c2 = new Button(buttons, SWT.PUSH);
                c2.setText("OK");
                c2.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        String type = t1.getText().toLowerCase();

                        if (!type.matches("[a-z]+")) {
                            MessageDialog.openError(parent.getShell(), "Add new association",
                                    "An application type needs to contain only alphabetical characters");
                            return;
                        }

                        for (int i = 0; i < table.getItemCount(); i++) {
                            if (table.getItem(i).getText(0).equals(type)) {
                                MessageDialog.openError(parent.getShell(), "Add new association",
                                        "The association table already contains an entry for type '" + type +
                                            "', remove it or choose another name");
                                return;
                            }
                        }

                        TableItem it = new TableItem(table, SWT.NORMAL);
                        it.setText(0, type);
                        it.setText(1, t2.getText());
                        dialog.close();
                    }
                });

                dialog.pack();
                dialog.setSize(400, dialog.getSize().y);
                dialog.open();

                dirty = true;
            }
        });

        Button removeButton = new Button(pane, SWT.PUSH);
        removeButton.setText("Remove");
        GridData b2 = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
        b2.horizontalAlignment = SWT.FILL;
        removeButton.setLayoutData(b2);
        removeButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                int sel = table.getSelectionIndex();
                if (sel != -1) {
                    table.remove(sel);
                    dirty = true;
                }
            }
        });
        resetTable();

        return pane;
    }

    private void resetTable() {
        table.removeAll();

        for (Entry<Object, Object> props : PreferenceInitializer.getRemoteConnectionProperties().entrySet()) {
            TableItem it = new TableItem(this.table, SWT.NORMAL);
            it.setText(0, (String) props.getKey());
            it.setText(1, (String) props.getValue());
            it.setData(props.getKey() + " " + props.getValue());
        }
    }

    private void saveProps() {
        PreferenceInitializer.getRemoteConnectionProperties().clear();
        for (TableItem it : this.table.getItems()) {
            PreferenceInitializer.getRemoteConnectionProperties().setProperty(it.getText(0), it.getText(1));
        }

        try {
            PreferenceInitializer.getRemoteConnectionProperties().store(
                    new FileOutputStream(remoteConnPropsFile, false), "application_type=/path/to/executable");
            dirty = false;
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, "Failed to write property file " +
                remoteConnPropsFile.getAbsolutePath(), e);
        }
    }

    @Override
    protected void performApply() {
        performOk();
    }

    @Override
    public boolean performCancel() {
        return true;
    }

    @Override
    protected void performDefaults() {
        resetTable();
    }

    @Override
    public boolean performOk() {
        if (dirty) {
            saveProps();
        }
        return super.performOk();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

}