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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.core.config.ProActiveConfiguration;


/**
 * This class allow to pop up a dialogue to remove a source node.
 * 
 * @author The ProActive Team
 */
public class CreateSourceDialog extends Dialog {

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    private CreateSourceDialog(Shell parent) {

        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        // Load the proactive default configuration
        ProActiveConfiguration.load();

        // Init the display
        Display display = parent.getDisplay();

        // Init the shell
        final Shell shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
        shell.setText("Create a node source");
        RowLayout layout = new RowLayout(SWT.VERTICAL);
        layout.marginHeight = 20;
        layout.spacing = 20;
        layout.marginWidth = 20;
        layout.fill = true;
        layout.justify = true;
        shell.setLayout(layout);

        Button staticButton = new Button(shell, SWT.NONE);
        Button dynamicButton = new Button(shell, SWT.NONE);

        final Shell parentShell = parent;
        // button "Create a static node source"
        staticButton.setText("Create a static node source");
        staticButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                shell.close();
                CreateStaticSourceDialog.showDialog(parentShell);
            }
        });

        // button "Create a dynamic node source"
        dynamicButton.setText("Create a dynamic node source");
        dynamicButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                shell.close();
                CreateDynamicSourceDialog.showDialog(parentShell);
            }
        });

        shell.setDefaultButton(staticButton);
        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * This method pop up a dialog for trying to connect a resource manager.
     * 
     * @param parent the parent
     */
    public static void showDialog(Shell parent) {
        new CreateSourceDialog(parent);
    }
}