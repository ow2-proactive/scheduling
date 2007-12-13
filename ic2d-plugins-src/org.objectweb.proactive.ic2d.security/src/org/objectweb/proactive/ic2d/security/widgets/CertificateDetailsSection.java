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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.core.security.TypedCertificate;


public class CertificateDetailsSection {
    private Section section;
    private Text typeText;
    private Text subjectText;
    private Text issuerText;
    private Text publicText;
    private Text privateText;

    public CertificateDetailsSection(Composite parent, FormToolkit toolkit) {
        this.section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
        this.section.setText("Certificate details");

        Composite client = toolkit.createComposite(this.section);
        client.setLayout(new GridLayout(1, false));

        toolkit.createLabel(client, "Type :");
        this.typeText = toolkit.createText(client, "");
        this.typeText.setEditable(false);
        this.typeText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        toolkit.createLabel(client, "Subject DN :");
        this.subjectText = toolkit.createText(client, "");
        this.subjectText.setEditable(false);
        this.subjectText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        toolkit.createLabel(client, "Issuer DN :");
        this.issuerText = toolkit.createText(client, "");
        this.issuerText.setEditable(false);
        this.issuerText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        toolkit.createLabel(client, "Public Key :");
        this.publicText = toolkit.createText(client, "", SWT.MULTI | SWT.WRAP);
        this.publicText.setEditable(false);
        this.publicText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        toolkit.createLabel(client, "Private Key :");
        this.privateText = toolkit.createText(client, "", SWT.MULTI | SWT.WRAP);
        this.privateText.setEditable(false);
        this.privateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.section.setClient(client);
    }

    public void update(TypedCertificate cert) {
        if (cert != null) {
            setType(cert.getType().toString());
            setSubject(cert.getCert().getSubjectX500Principal().getName());
            setIssuer(cert.getCert().getIssuerX500Principal().getName());
            setPublic(cert.getCert().getPublicKey().toString());
            if (cert.getPrivateKey() != null) {
                setPrivate(cert.getPrivateKey().toString());
            } else {
                setPrivate("");
            }
        } else {
            setType("");
            setSubject("");
            setIssuer("");
            setPublic("");
            setPrivate("");
        }
    }

    public Section get() {
        return this.section;
    }

    public void setIssuer(String issuerText) {
        this.issuerText.setText(issuerText);
    }

    public void setPrivate(String privateText) {
        this.privateText.setText(privateText);
    }

    public void setPublic(String publicText) {
        this.publicText.setText(publicText);
    }

    public void setSubject(String subjectText) {
        this.subjectText.setText(subjectText);
    }

    public void setType(String typeText) {
        this.typeText.setText(typeText);
    }
}
