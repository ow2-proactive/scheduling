/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://proactive.inria.fr/team_members.htm Contributor(s):
 * 
 * ################################################################
 */
package org.ow2.proactive.resourcemanager.gui.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.dialog.nodesources.ConfigurablePanel;
import org.ow2.proactive.resourcemanager.gui.dialog.nodesources.NodeSourceName;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;


/**
 * This class allow to pop up a dialogue to remove a source node.
 * 
 * @author The ProActive Team
 */
public class CreateSourceDialog extends Dialog {

    public class NodeSourceButtons extends Composite {

        public NodeSourceButtons(Shell parent, int style) {
            super(parent, style);

            FormLayout layout = new FormLayout();
            layout.marginHeight = 5;
            layout.marginWidth = 5;
            setLayout(layout);

            Button okButton = new Button(this, SWT.NONE);
            Button cancelButton = new Button(this, SWT.NONE);

            okButton.setText("OK");
            cancelButton.setText("Cancel");

            FormData okFormData = new FormData();
            okFormData.left = new FormAttachment(1, 100);
            okFormData.width = 100;
            okButton.setLayoutData(okFormData);
            shell.setDefaultButton(okButton);

            FormData cancelFormData = new FormData();
            cancelFormData.left = new FormAttachment(okButton, 10);
            cancelFormData.width = 100;
            cancelButton.setLayoutData(cancelFormData);

            okButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {

                    try {
                        validateForm();

                        RMAdmin admin = RMStore.getInstance().getRMAdmin();
                        admin.createNodesource(name.getNodeSourceName(), infrastructure.getSelectedClass()
                                .getName(), infrastructure.getParameters(), policy.getSelectedClass()
                                .getName(), policy.getParameters());

                        shell.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        String message = e.getMessage();
                        if (e.getCause() != null) {
                            message = e.getCause().getMessage();
                        }
                        MessageDialog.openError(ResourceExplorerView.getRMShell(),
                                "Cannot create nodesource", message);
                    }
                }
            });

            cancelButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    shell.close();
                }
            });

            pack();
        }

        protected void checkSubclass() {
        }

    }

    private Shell shell;
    private NodeSourceName name;
    private ConfigurablePanel infrastructure;
    private ConfigurablePanel policy;

    private CreateSourceDialog(Shell parent) throws RMException {

        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        // Load the proactive default configuration
        ProActiveConfiguration.load();

        // Init the display
        Display display = parent.getDisplay();

        // Init the shell
        shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
        shell.setText("Create a node source");
        RowLayout layout = new RowLayout(SWT.VERTICAL);
        layout.spacing = 5;
        shell.setLayout(layout);

        // creation
        name = new NodeSourceName(shell, SWT.NONE);
        infrastructure = new ConfigurablePanel(shell, "Node source infrastructure");
        infrastructure.addComboValue("", null);

        policy = new ConfigurablePanel(shell, "Node source policy");
        policy.addComboValue("", null);

        RMAdmin admin = RMStore.getInstance().getRMAdmin();
        ArrayList<String> supportedInfrastructures = admin.getSupportedNodeSourceInfrastructures();

        for (String className : supportedInfrastructures) {
            try {
                Class<?> cls = Class.forName(className);
                infrastructure.addComboValue(beautifyName(cls.getSimpleName()), cls);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> supportedPolicies = admin.getSupportedNodeSourcePolicies();

        for (String className : supportedPolicies) {
            try {
                Class<?> cls = Class.forName(className);
                policy.addComboValue(beautifyName(cls.getSimpleName()), cls);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        new NodeSourceButtons(shell, SWT.NONE);

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * This method pop up a dialog for trying to connect a resource manager.
     * 
     * @param parent the parent
     */
    public static void showDialog(Shell parent) {
        try {
            new CreateSourceDialog(parent);
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            MessageDialog.openError(ResourceExplorerView.getRMShell(), "Cannot create nodesource", message);
        }
    }

    private void validateForm() {
        if (name.getNodeSourceName().length() == 0) {
            throw new RuntimeException("Node source name cannot be empty");
        }

        if (infrastructure.getSelectedClass() == null) {
            throw new RuntimeException("Select node source infrastructure type");
        }

        if (policy.getSelectedClass() == null) {
            throw new RuntimeException("Select node source policy type");
        }
    }

    public static String beautifyName(String name) {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i == 0) {
                buffer.append(Character.toUpperCase(ch));
            } else if (i > 0 && Character.isUpperCase(ch)) {
                boolean nextCharInAupperCase = (i < name.length() - 1) &&
                    Character.isUpperCase(name.charAt(i + 1));
                if (!nextCharInAupperCase) {
                    buffer.append(" " + ch);
                } else {
                    buffer.append(ch);
                }
            } else {
                buffer.append(ch);
            }
        }

        return buffer.toString();
    }
}
