package org.objectweb.proactive.ic2d.infrastructuremanager.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.ic2d.infrastructuremanager.data.IMData;

public class IMDialogSetTTR extends Dialog {

	
	private Shell shell = null;
	
	private Text text;
	private Button okButton;
	private Button cancelButton;
	
	private IMData imData;
	
	public IMDialogSetTTR(Shell parent, IMData data) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		
		this.imData = data;
		
		/* Init the display */
		Display display = getParent().getDisplay();
		
		/* Init the shell */
		shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
		shell.setText("Set Time To Refresh");
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		shell.setLayout(layout);
		
		Label titleLabel = new Label(shell, SWT.NONE);
		titleLabel.setText("Please enter the new time to refresh control");
		FormData titleLabelFormData = new FormData();
		titleLabelFormData.left = new FormAttachment(10, 0);
		titleLabel.setLayoutData(titleLabelFormData);
		
		this.text = new Text(shell, SWT.BORDER);
		text.setText(imData.getTTR()+"");
		FormData textFormData = new FormData();
		textFormData.top = new FormAttachment(titleLabel, 5);
		textFormData.left = new FormAttachment(40, 0);
		textFormData.right = new FormAttachment(50, 0);
		text.setLayoutData(textFormData);
		
		Label secondsLabel = new Label(shell, SWT.NONE);
		secondsLabel.setText("seconds");
		FormData secondsLabelFormData = new FormData();
		secondsLabelFormData.top = new FormAttachment(titleLabel, 8);
		secondsLabelFormData.left = new FormAttachment(text, 2);
		secondsLabel.setLayoutData(secondsLabelFormData);

		
		// button "OK"
		this.okButton = new Button(shell, SWT.NONE);
		okButton.setText("OK");
		okButton.addSelectionListener(new SetTTRListener());
		FormData okFormData = new FormData();
		okFormData.top = new FormAttachment(secondsLabel, 20);
		okFormData.left = new FormAttachment(25, 20);
		okFormData.right = new FormAttachment(50, -10);
		okButton.setLayoutData(okFormData);
		shell.setDefaultButton(okButton);
		
		// button "CANCEL"
		this.cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SetTTRListener());
		FormData cancelFormData = new FormData();
		cancelFormData.top = new FormAttachment(secondsLabel, 20);
		cancelFormData.left = new FormAttachment(50, 10);
		cancelFormData.right = new FormAttachment(75, -20);
		cancelButton.setLayoutData(cancelFormData);
		
		shell.pack();
		shell.open();


		while(!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
	}
	
	
	private class SetTTRListener extends SelectionAdapter {
		
		public void widgetSelected(SelectionEvent e) {
			if(e.widget == okButton) {
				int ttr = Integer.parseInt(text.getText());
				imData.setTTR(ttr);
			}
			shell.close();
		}
	}

}
