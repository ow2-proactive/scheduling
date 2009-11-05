/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
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

    @Override
    protected void checkSubclass() {
    }

    public String getNodeSourceName() {
        return nameText.getText();
    }
}
