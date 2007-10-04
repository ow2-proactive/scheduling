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
package org.objectweb.proactive.infrastructuremanager.dialog;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.infrastructuremanager.IMConstants;


public class IMDialogDeploy extends Dialog {
    private final String TITLE = "Check at least one virtual node to activate";
    private Shell shell;
    private Button deployButton;
    private Button cancelButton;
    private SelectionAdapter selectionAdapter;
    private Button chooseFileButton;
    private FileDialog fileDialog;
    private Text filePath;
    private Table table;
    private Label label;
    private Composite labelAndCheckComposite;
    private Composite buttonsComposite;
    private IMAdmin admin;

    public IMDialogDeploy(Shell parent, IMAdmin admin) {
        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        this.admin = admin;

        // Init the display
        Display display = getParent().getDisplay();

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.verticalSpacing = 10;
        gridLayout.marginBottom = gridLayout.marginTop = 10;
        gridLayout.marginLeft = gridLayout.marginRight = 10;

        // Init the shell
        shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
        shell.setText(TITLE);
        shell.setLayout(gridLayout);

        // SelectionAdapter
        selectionAdapter = new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        filePath.setText(fileDialog.open());
                        addTableItems();
                    }
                };

        // Choix du fichier
        fileDialog = new FileDialog(shell, SWT.SINGLE);
        fileDialog.setText("Choose a file descriptor to deploy");
        fileDialog.setFilterExtensions(new String[] { "*.xml" });
        fileDialog.setFilterNames(new String[] { "FileDescriptor .xml" });

        String file = fileDialog.open();
        if (file != null) {
            addCompositeFileChooser();
            filePath.setText(file);
            addLabelAndCheckAll();
            addTable();
            addDeployCancelButton();
            addTableItems();
            shell.setSize(500, 400);
            shell.open();
        } else {
            shell.close();
        }

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public void addCompositeFileChooser() {
        Composite compositeFileChooser = new Composite(shell, SWT.NONE);
        compositeFileChooser.setLayout(new GridLayout(2, false));
        chooseFileButton = new Button(compositeFileChooser, SWT.PUSH);
        chooseFileButton.setText("Choose File");
        chooseFileButton.setToolTipText(
            "Click to choose another file descriptor");
        chooseFileButton.addSelectionListener(selectionAdapter);
        filePath = new Text(compositeFileChooser, SWT.BORDER | SWT.READ_ONLY);
        filePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        compositeFileChooser.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
    }

    public void addLabelAndCheckAll() {
        labelAndCheckComposite = new Composite(shell, SWT.NONE);
        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.justify = true;
        labelAndCheckComposite.setLayout(layout);
        labelAndCheckComposite.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
        // LABEL
        label = new Label(labelAndCheckComposite, SWT.CENTER);
        label.setText("Check at least one Virtual Node to activate");
        // BUTTON ALL
        Button allButton = new Button(labelAndCheckComposite, SWT.PUSH);
        allButton.setText("All");
        allButton.setToolTipText("Click to check all virtual nodes");
        allButton.setLayoutData(new RowData(50, 25));
        allButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (TableItem item : table.getItems()) {
                        item.setChecked(true);
                    }
                }
            });

        // BUTTON NONE
        Button noneButton = new Button(labelAndCheckComposite, SWT.PUSH);
        noneButton.setText("None");
        noneButton.setToolTipText("Click to uncheck all virtual nodes");
        noneButton.setLayoutData(new RowData(50, 25));
        noneButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (TableItem item : table.getItems()) {
                        item.setChecked(false);
                    }
                }
            });
    }

    public void addTable() {
        table = new Table(shell, SWT.VIRTUAL | SWT.CHECK | SWT.BORDER);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    public void addDeployCancelButton() {
        buttonsComposite = new Composite(shell, SWT.NONE);
        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.spacing = 20;
        layout.justify = true;
        buttonsComposite.setLayout(layout);
        buttonsComposite.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_CENTER));
        // BUTTON CANCEL
        cancelButton = new Button(buttonsComposite, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("Click to cancel the deployment");
        cancelButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    shell.close();
                }
            });
        // BUTTON DEPLOY
        deployButton = new Button(buttonsComposite, SWT.PUSH);
        deployButton.setText("Deploy");
        deployButton.setToolTipText("Click to confirm the deployment");
        deployButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ArrayList<String> listOfCheckedItem = new ArrayList<String>();
                    for (TableItem item : table.getItems()) {
                        if (item.getChecked()) {
                            listOfCheckedItem.add(item.getText());
                        }
                    }
                    int itemsNumber = table.getItemCount();
                    int checkedItemsNumber = listOfCheckedItem.size();
                    if (checkedItemsNumber == 0) {
                        label.setForeground(IMConstants.RED_COLOR);
                    } else {
                        try {
                            if (checkedItemsNumber == itemsNumber) {
                                admin.deployAllVirtualNodes(new File(
                                        filePath.getText()),
                                    IMConstants.nodeTransfert);
                            } else if (checkedItemsNumber == 1) {
                                admin.deployVirtualNode(new File(
                                        filePath.getText()),
                                    IMConstants.nodeTransfert,
                                    listOfCheckedItem.get(0));
                            } else {
                                String[] tab = new String[checkedItemsNumber];
                                listOfCheckedItem.toArray(tab);
                                admin.deployVirtualNodes(new File(
                                        filePath.getText()),
                                    IMConstants.nodeTransfert, tab);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        shell.close();
                    }
                }
            });
    }

    public void addTableItems() {
        try {
            table.removeAll();
            ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(filePath.getText());
            VirtualNode[] vnodes = pad.getVirtualNodes();
            for (int i = 0; i < vnodes.length; i++) {
                TableItem item = new TableItem(table, SWT.NONE, i);
                item.setText(vnodes[i].getName());
            }
            labelAndCheckComposite.setVisible(true);
            table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            table.redraw();
        } catch (ProActiveException e1) {
            e1.printStackTrace();
        }
    }
}
