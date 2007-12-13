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
package org.objectweb.proactive.ic2d.security.tabs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.ic2d.security.core.CertificateTree;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeList;
import org.objectweb.proactive.ic2d.security.widgets.CertificateTreeListSection;


public class CertificateGenerationTab extends UpdatableTab {
    public static final String ID = "org.objectweb.proactive.ic2d.security.tabs.CertificateChainTab";
    private FormToolkit toolkit;
    private CertificateTreeList activeKeystore;
    protected CertificateTreeList certTreeList;
    protected Combo typeCombo;
    protected Text keySizeText;
    protected Text nameText;
    protected Text validityText;
    protected CertificateTreeListSection certTreeListSection;
    private CertificateTreeListSection activeKeystoreSection;

    /**
     * The constructor.
     */
    public CertificateGenerationTab(CTabFolder folder, CertificateTreeList keystore, FormToolkit tk) {
        super(folder, SWT.NULL);
        setText("Certificate chain editor");

        this.certTreeList = new CertificateTreeList();
        this.activeKeystore = keystore;
        this.toolkit = tk;

        Composite body = this.toolkit.createComposite(folder);

        body.setLayout(new GridLayout(3, true));

        createSectionOptions(body).setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        createSectionCertTreeList(body).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSectionActiveKeystore(body).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        setControl(body);
    }

    private Section createSectionOptions(Composite parent) {
        Section section = this.toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
        section.setText("Stuff");

        Composite client = this.toolkit.createComposite(section);
        client.setLayout(new GridLayout(1, false));

        this.toolkit.createLabel(client, "Name :");
        this.nameText = this.toolkit.createText(client, "CN=");
        this.nameText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
        this.toolkit.createLabel(client, "Validity (days) :");
        this.validityText = this.toolkit.createText(client, "365");
        this.validityText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        this.toolkit.createLabel(client, "Key size :");
        this.keySizeText = this.toolkit.createText(client, "512");
        this.keySizeText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        this.toolkit.createLabel(client, "Type : ");
        this.typeCombo = new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.typeCombo.add(EntityType.DOMAIN.toString());
        this.typeCombo.add(EntityType.USER.toString());
        this.typeCombo.add(EntityType.APPLICATION.toString());
        this.typeCombo.select(0);

        createButtonNewChain(client);

        createButtonChildCert(client);

        section.setClient(client);

        return section;
    }

    private Button createButtonNewChain(Composite parent) {
        Button button = this.toolkit.createButton(parent, "Generate self signed certificate", SWT.BUTTON1);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                int keySize;
                try {
                    keySize = Integer.valueOf(CertificateGenerationTab.this.keySizeText.getText()).intValue();
                } catch (NumberFormatException nfe) {
                    System.out.println("keysizepabo!!11");
                    return;
                }
                int validity;
                try {
                    validity = Integer.valueOf(CertificateGenerationTab.this.validityText.getText())
                            .intValue();
                } catch (NumberFormatException nfe) {
                    System.out.println("validitymeuchan!!1oneoneone");
                    return;
                }
                String name = CertificateGenerationTab.this.nameText.getText();
                if (!name.matches("CN=.+")) {
                    System.out.println("nomcassai!!1oneeleven");
                    return;
                }
                System.out.println("Generate self signed certificate");
                EntityType type = EntityType.fromString(CertificateGenerationTab.this.typeCombo
                        .getItem(CertificateGenerationTab.this.typeCombo.getSelectionIndex()));

                CertificateGenerationTab.this.certTreeList.add(new CertificateTree(name, keySize, validity,
                    type));

                CertificateGenerationTab.this.certTreeListSection.updateSection();

                super.mouseUp(e);
            }
        });

        return button;
    }

    private Button createButtonChildCert(Composite parent) {
        Button button = this.toolkit.createButton(parent, "Generate child certificate", SWT.BUTTON1);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                int keySize;
                try {
                    keySize = Integer.valueOf(CertificateGenerationTab.this.keySizeText.getText()).intValue();
                } catch (NumberFormatException nfe) {
                    System.out.println("keysizepabo!!11");
                    return;
                }
                int validity;
                try {
                    validity = Integer.valueOf(CertificateGenerationTab.this.validityText.getText())
                            .intValue();
                } catch (NumberFormatException nfe) {
                    System.out.println("validitymeuchan!!1oneoneone");
                    return;
                }
                String name = CertificateGenerationTab.this.nameText.getText();
                if (!name.matches("CN=.+")) {
                    System.out.println("nomcassai!!1oneeleven");
                    return;
                }
                CertificateTree tree = CertificateGenerationTab.this.certTreeListSection.getSelectionData();
                if (tree.getCertificate().getPrivateKey() == null) {
                    System.out
                            .println("Impossible to create a child of a certificate without a private key.");
                    return;
                }
                System.out.println("Generating child certificate");
                EntityType type = EntityType.fromString(CertificateGenerationTab.this.typeCombo
                        .getItem(CertificateGenerationTab.this.typeCombo.getSelectionIndex()));
                tree.add(name, keySize, validity, type);

                CertificateGenerationTab.this.certTreeListSection.updateSection();
            }
        });

        return button;
    }

    private Section createSectionCertTreeList(Composite parent) {
        this.certTreeListSection = new CertificateTreeListSection(parent, this.toolkit, "Certificate Tree",
            this.certTreeList, true, true, true, false);
        return this.certTreeListSection.get();
    }

    private Section createSectionActiveKeystore(Composite parent) {
        this.activeKeystoreSection = new CertificateTreeListSection(parent, this.toolkit, "ActiveKeystore",
            this.activeKeystore, true, true, true, false);
        return this.activeKeystoreSection.get();
    }

    @Override
    public void update() {
        this.activeKeystoreSection.updateSection();
    }
}
