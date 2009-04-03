package org.ow2.proactive.resourcemanager.gui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.core.ProActiveException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.gui.Activator;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


public class ShutdownDialog extends Dialog {
    private Shell shell = null;
    private Button okButton;
    private Button cancelButton;

    public ShutdownDialog(final Shell parent) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        /* Init the display */
        Display display = getParent().getDisplay();
        FormData fd;

        /* Init the shell */
        shell = new Shell(getParent());
        shell.setText("shutdwon Resource Manager");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        final Label titleLabel = new Label(shell, SWT.CENTER);
        titleLabel.setText("Are you sure to shutdown Resource manager ?");
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(100, -10);
        titleLabel.setLayoutData(fd);

        final Button preemptCheck = new Button(shell, SWT.CHECK | SWT.CENTER);
        preemptCheck.setText("Wait tasks ends on busy nodes");
        fd = new FormData();
        fd.top = new FormAttachment(titleLabel, 10);
        fd.left = new FormAttachment(0, 40);
        preemptCheck.setLayoutData(fd);

        //button "OK"
        this.okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {
                    RMStore.getInstance().getRMAdmin().shutdown(!preemptCheck.getSelection());
                } catch (ProActiveException e) {
                    // TODO Auto-generated catch block
                	Activator.log(IStatus.ERROR, "Error when shutting down. ", e);
                    e.printStackTrace();
                } catch (RMException e) {
                    MessageDialog.openError(parent, "Access denied", e.getMessage());
                }
                shell.close();
            }
        });

        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(preemptCheck, 30);
        okFormData.left = new FormAttachment(20, -10);
        okFormData.right = new FormAttachment(50, -20);
        okButton.setLayoutData(okFormData);

        // button "CANCEL"
        this.cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        // button "CANCEL"
        cancelButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                shell.close();
            }
        });

        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(preemptCheck, 30);
        cancelFormData.left = new FormAttachment(50, 20);
        cancelFormData.right = new FormAttachment(80, 10);
        cancelButton.setLayoutData(cancelFormData);
        shell.setDefaultButton(cancelButton);

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
        new ShutdownDialog(parent);
    }
}
