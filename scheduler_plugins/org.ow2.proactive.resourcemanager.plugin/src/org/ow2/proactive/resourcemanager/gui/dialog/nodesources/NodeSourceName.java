package org.ow2.proactive.resourcemanager.gui.dialog.nodesources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class NodeSourceName extends Composite {

    private Text nameText;

    public NodeSourceName(Shell parent, int style) {
        super(parent, style);
        setLayout(new FormLayout());

        Label nameLabel = new Label(this, SWT.SHADOW_NONE | SWT.CENTER);
        nameLabel.setText("Node source name : ");
        nameText = new Text(this, SWT.BORDER);

        FormData fd = new FormData();
        fd.top = new FormAttachment(1, 10);
        fd.left = new FormAttachment(1, 2);
        nameLabel.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(3, 10);
        fd.left = new FormAttachment(nameLabel);
        fd.width = 200;
        nameText.setLayoutData(fd);
    }

    protected void checkSubclass() {
    }

    public String getNodeSourceName() {
        return nameText.getText();
    }
}
