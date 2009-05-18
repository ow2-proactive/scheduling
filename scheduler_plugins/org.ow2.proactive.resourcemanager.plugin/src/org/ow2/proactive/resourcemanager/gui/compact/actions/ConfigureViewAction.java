package org.ow2.proactive.resourcemanager.gui.compact.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.ow2.proactive.resourcemanager.gui.compact.Filter;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;


public class ConfigureViewAction implements IViewActionDelegate {

    private ResourcesCompactView view;
    private Filter filter = new Filter();

    private class ConfigureViewDialog extends Dialog {
        public ConfigureViewDialog(Shell parent) {
            super(parent);

            // Init the shell
            final Shell shell = new Shell(parent, SWT.BORDER | SWT.CLOSE);
            shell.setText("Configure view");
            FormLayout layout = new FormLayout();
            layout.marginHeight = 15;
            layout.marginWidth = 20;
            shell.setLayout(layout);

            // creation
            Label showLabel = new Label(shell, SWT.NONE);
            final Button showNodeSource = new Button(shell, SWT.CHECK);
            showNodeSource.setSelection(filter.showNodeSources);
            final Button showHost = new Button(shell, SWT.CHECK);
            showHost.setSelection(filter.showHosts);
            final Button showJVM = new Button(shell, SWT.CHECK);
            showJVM.setSelection(filter.showJVMs);
            final Button showNode = new Button(shell, SWT.CHECK);
            showNode.setSelection(filter.showNodes);

            Button okButton = new Button(shell, SWT.NONE);
            Button cancelButton = new Button(shell, SWT.NONE);

            showLabel.setText("Show :");
            FormData urlFormData = new FormData();
            urlFormData.top = new FormAttachment(0, 5);
            showLabel.setLayoutData(urlFormData);

            showNodeSource.setText("node sources");
            FormData nsFormData = new FormData();
            nsFormData.top = new FormAttachment(showLabel, 5, SWT.BOTTOM);
            showNodeSource.setLayoutData(nsFormData);

            showHost.setText("hosts");
            FormData hostFormData = new FormData();
            hostFormData.top = new FormAttachment(showNodeSource, 5, SWT.BOTTOM);
            showHost.setLayoutData(hostFormData);

            showJVM.setText("JVM");
            FormData jvmFormData = new FormData();
            jvmFormData.top = new FormAttachment(showHost, 5, SWT.BOTTOM);
            showJVM.setLayoutData(jvmFormData);

            showNode.setText("nodes");
            FormData nodesFormData = new FormData();
            nodesFormData.top = new FormAttachment(showJVM, 5, SWT.BOTTOM);
            showNode.setLayoutData(nodesFormData);

            // button "OK"
            okButton.setText("OK");
            okButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    filter.showNodeSources = showNodeSource.getSelection();
                    filter.showHosts = showHost.getSelection();
                    filter.showJVMs = showJVM.getSelection();
                    filter.showNodes = showNode.getSelection();

                    view.repaint(filter);

                    shell.close();
                }
            });

            FormData okFormData = new FormData();
            okFormData.top = new FormAttachment(showNode, 30);
            okFormData.width = 70;
            okButton.setLayoutData(okFormData);
            shell.setDefaultButton(okButton);

            // button "CANCEL"
            cancelButton.setText("Cancel");
            cancelButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    shell.close();
                }
            });

            FormData cancelFormData = new FormData();
            cancelFormData.top = new FormAttachment(showNode, 30);
            cancelFormData.left = new FormAttachment(okButton, 10, SWT.RIGHT);
            cancelFormData.width = 70;
            cancelButton.setLayoutData(cancelFormData);

            shell.setMinimumSize(150, 100);
            shell.pack();
            shell.open();
        }
    };

    @Override
    public void init(IViewPart view) {
        this.view = (ResourcesCompactView) view;
    }

    @Override
    public void run(IAction action) {
        new ConfigureViewDialog(Display.getCurrent().getActiveShell());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
