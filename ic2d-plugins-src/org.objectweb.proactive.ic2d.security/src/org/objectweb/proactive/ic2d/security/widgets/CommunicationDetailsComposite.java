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
package org.objectweb.proactive.ic2d.security.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.objectweb.proactive.core.security.Communication;


public class CommunicationDetailsComposite extends Composite {
    private Text communicationText;
    private Text authenticationText;
    private Text confidentialityText;
    private Text integrityText;

    public CommunicationDetailsComposite(Composite parent, FormToolkit toolkit, String name) {
        super(parent, SWT.NULL);
        toolkit.adapt(this);

        super.setLayout(new GridLayout());

        toolkit.createLabel(this, name);

        toolkit.createLabel(this, "Communication :");
        this.communicationText = toolkit.createText(this, "");
        this.communicationText.setEditable(false);
        this.communicationText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        toolkit.createLabel(this, "Authentication :");
        this.authenticationText = toolkit.createText(this, "");
        this.authenticationText.setEditable(false);
        this.authenticationText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        toolkit.createLabel(this, "Confidentiality :");
        this.confidentialityText = toolkit.createText(this, "");
        this.confidentialityText.setEditable(false);
        this.confidentialityText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        toolkit.createLabel(this, "Integrity :");
        this.integrityText = toolkit.createText(this, "");
        this.integrityText.setEditable(false);
        this.integrityText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    public void updateCommunication(Communication c) {
        this.communicationText.setText(c.getCommunication() ? "Authorized" : "Forbidden");
        this.authenticationText.setText(c.getAuthentication().toString());
        this.confidentialityText.setText(c.getConfidentiality().toString());
        this.integrityText.setText(c.getIntegrity().toString());
    }
}
