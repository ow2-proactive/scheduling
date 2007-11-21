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
package org.objectweb.proactive.ic2d.security.views;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.core.security.crypto.Session;
import org.objectweb.proactive.ic2d.security.core.CertificateTree;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeList;
import org.objectweb.proactive.ic2d.security.core.SimplePolicyRule;
import org.objectweb.proactive.ic2d.security.tabs.CertificateGenerationTab;
import org.objectweb.proactive.ic2d.security.tabs.KeystoreTab;
import org.objectweb.proactive.ic2d.security.tabs.RuleTab;
import org.objectweb.proactive.ic2d.security.tabs.SessionTab;
import org.objectweb.proactive.ic2d.security.tabs.UpdatableTab;


public class PolicyEditorView extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.security.views.PolicyEditorView";
    private CertificateTreeList keystore;
    protected CTabFolder tabFolder;
    private ScrolledForm form;
    private CertificateGenerationTab cgt;
    private KeystoreTab kt;
    private RuleTab rt;
    private SessionTab st;

    public PolicyEditorView() {
        this.keystore = new CertificateTreeList();
    }

    @Override
    public void createPartControl(Composite parent) {
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        toolkit.setBorderStyle(SWT.BORDER);

        this.form = toolkit.createScrolledForm(parent);

        this.form.setText("Policy Editor");
        this.form.getBody().setLayout(new GridLayout());
        this.tabFolder = new CTabFolder(this.form.getBody(), SWT.FLAT |
                SWT.TOP);
        toolkit.adapt(this.tabFolder, true, true);
        this.tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        Color selectedColor = toolkit.getColors().getColor(FormColors.SEPARATOR);
        this.tabFolder.setSelectionBackground(new Color[] {
                selectedColor, toolkit.getColors().getBackground()
            }, new int[] { 50 });
        this.tabFolder.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ((UpdatableTab) PolicyEditorView.this.tabFolder.getSelection()).update();
                }
            });

        toolkit.paintBordersFor(this.tabFolder);
        this.cgt = new CertificateGenerationTab(this.tabFolder, this.keystore,
                toolkit);
        this.kt = new KeystoreTab(this.tabFolder, this.keystore, toolkit);
        this.rt = new RuleTab(this.tabFolder, this.keystore, toolkit);
        this.st = new SessionTab(this.tabFolder, toolkit);

        this.tabFolder.setSelection(0);
    }

    public void update(CertificateTreeList list,
        List<SimplePolicyRule> policies, String appName,
        List<String> authorizedUsers, Hashtable<Long, Session> sessions) {
        if (list != null) {
            this.keystore.clear();
            this.keystore.addAll(list);
        }

        this.cgt.update();

        this.kt.update();

        this.rt.update();
        this.rt.setAppName(appName);
        this.rt.setRules(policies);
        this.rt.setAuthorizedUsers(authorizedUsers);

        this.st.setSessions(sessions);
        this.st.update();
    }

    public CertificateTreeList getKeystore() {
        return this.keystore;
    }

    public String getAppName() {
        return this.rt.getAppName();
    }

    public Map<CertificateTree, Boolean> getKeysToKeep() {
        return this.kt.getSelected();
    }

    @Override
    public void setFocus() {
        this.form.setFocus();
    }

    public RuleTab getRt() {
        return this.rt;
    }
}
