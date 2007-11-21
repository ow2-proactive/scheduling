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

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.core.security.Authorization;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.ic2d.security.core.CertificateTree;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeList;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeMap;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeMapTransfer;
import org.objectweb.proactive.ic2d.security.core.PolicyFile;
import org.objectweb.proactive.ic2d.security.core.PolicyTools;
import org.objectweb.proactive.ic2d.security.core.SimplePolicyRule;
import org.objectweb.proactive.ic2d.security.widgets.CertificateTreeListSection;
import org.objectweb.proactive.ic2d.security.widgets.EntityTableComposite;


public class RuleTab extends UpdatableTab {
    private CertificateTreeList activeKeystore;
    private CertificateTreeListSection activeKeystoreSection;
    protected List<SimplePolicyRule> rules;
    protected List<String> authorizedUsers;
    private FormToolkit toolkit;
    private EntityTableComposite fromTable;
    private EntityTableComposite toTable;
    protected Button requestCheck;
    protected Combo reqAuthCombo;
    protected Combo reqIntCombo;
    protected Combo reqConfCombo;
    protected Button replyCheck;
    protected Combo repAuthCombo;
    protected Combo repIntCombo;
    protected Combo repConfCombo;
    protected Button aoCreationCheck;
    protected Button migrationCheck;
    protected Table rulesTable;
    private TableViewer rulesTableViewer;
    protected Table usersTable;
    private TableViewer usersTableViewer;
    protected Text applicationNameText;
    protected Text keystoreText;

    public RuleTab(CTabFolder folder, CertificateTreeList keystore,
        FormToolkit toolkit) {
        super(folder, SWT.NULL);
        setText("Rule Editor");

        this.activeKeystore = keystore;
        this.rules = new ArrayList<SimplePolicyRule>();
        this.toolkit = toolkit;
        this.authorizedUsers = new ArrayList<String>();

        Composite body = toolkit.createComposite(folder);
        body.setLayout(new GridLayout(3, true));

        createSectionActiveKeystore(body)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSectionRuleEdition(body)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSectionRules(body)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        updateRuleEditor();

        setControl(body);
    }

    private Section createSectionActiveKeystore(Composite parent) {
        this.activeKeystoreSection = new CertificateTreeListSection(parent,
                this.toolkit, "ActiveKeystore", this.activeKeystore, false,
                true, false, false);
        return this.activeKeystoreSection.get();
    }

    private Section createSectionRuleEdition(Composite parent) {
        Section section = this.toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR);
        section.setText("Rule edition");

        Composite client = this.toolkit.createComposite(section);
        client.setLayout(new GridLayout());

        this.toolkit.createLabel(client, "From");
        this.fromTable = new EntityTableComposite(client, this.toolkit,
                this.rules, true);
        this.fromTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        this.toolkit.createLabel(client, "To");
        this.toTable = new EntityTableComposite(client, this.toolkit,
                this.rules, false);
        this.toTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        createCheckRequest(client);

        createCompositeRequest(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createCheckReply(client);

        createCompositeReply(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createCheckAoCreation(client);

        createCheckMigration(client);

        section.setClient(client);

        return section;
    }

    private Button createCheckRequest(Composite parent) {
        this.requestCheck = this.toolkit.createButton(parent,
                "Authorize requests", SWT.CHECK);
        this.requestCheck.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setRequest(RuleTab.this.requestCheck.getSelection());
                    enableRequestEditor(RuleTab.this.requestCheck.getSelection());

                    super.widgetSelected(e);
                }
            });

        return this.requestCheck;
    }

    private Composite createCompositeRequest(Composite parent) {
        Composite client = this.toolkit.createComposite(parent);
        client.setLayout(new GridLayout(3, true));

        this.toolkit.createLabel(client, "Authentication");
        this.toolkit.createLabel(client, "Confidentiality");
        this.toolkit.createLabel(client, "Integrity");

        this.reqAuthCombo = createRODCombo(client);
        this.reqAuthCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setReqAuth(Authorization.fromString(
                            RuleTab.this.reqAuthCombo.getItem(
                                RuleTab.this.reqAuthCombo.getSelectionIndex())));

                    super.widgetSelected(e);
                }
            });
        this.reqConfCombo = createRODCombo(client);
        this.reqConfCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setReqConf(Authorization.fromString(
                            RuleTab.this.reqConfCombo.getItem(
                                RuleTab.this.reqConfCombo.getSelectionIndex())));

                    super.widgetSelected(e);
                }
            });
        this.reqIntCombo = createRODCombo(client);
        this.reqIntCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setReqInt(Authorization.fromString(
                            RuleTab.this.reqIntCombo.getItem(
                                RuleTab.this.reqIntCombo.getSelectionIndex())));

                    super.widgetSelected(e);
                }
            });

        return client;
    }

    private Button createCheckReply(Composite parent) {
        this.replyCheck = this.toolkit.createButton(parent, "Authorize reply",
                SWT.CHECK);
        this.replyCheck.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setReply(RuleTab.this.replyCheck.getSelection());
                    enableReplyEditor(RuleTab.this.replyCheck.getSelection());

                    super.widgetSelected(e);
                }
            });

        return this.replyCheck;
    }

    private Composite createCompositeReply(Composite parent) {
        Composite client = this.toolkit.createComposite(parent);
        client.setLayout(new GridLayout(3, true));

        this.toolkit.createLabel(client, "Authentication");
        this.toolkit.createLabel(client, "Confidentiality");
        this.toolkit.createLabel(client, "Integrity");

        this.repAuthCombo = createRODCombo(client);
        this.repAuthCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setRepAuth(Authorization.fromString(
                            RuleTab.this.repAuthCombo.getItem(
                                RuleTab.this.repAuthCombo.getSelectionIndex())));

                    super.widgetSelected(e);
                }
            });
        this.repConfCombo = createRODCombo(client);
        this.repConfCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setRepConf(Authorization.fromString(
                            RuleTab.this.repConfCombo.getItem(
                                RuleTab.this.repConfCombo.getSelectionIndex())));

                    super.widgetSelected(e);
                }
            });
        this.repIntCombo = createRODCombo(client);
        this.repIntCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setRepInt(Authorization.fromString(
                            RuleTab.this.repIntCombo.getItem(
                                RuleTab.this.repIntCombo.getSelectionIndex())));

                    super.widgetSelected(e);
                }
            });

        return client;
    }

    private static Combo createRODCombo(Composite parent) {
        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        combo.add(Authorization.REQUIRED.toString());
        combo.add(Authorization.OPTIONAL.toString());
        combo.add(Authorization.DENIED.toString());
        combo.select(1);

        return combo;
    }

    private static void selectRODCombo(Authorization authorization, Combo combo) {
        switch (authorization) {
        case REQUIRED:
            combo.select(0);
            break;
        case OPTIONAL:
            combo.select(1);
            break;
        case DENIED:
            combo.select(2);
            break;
        }
    }

    private Button createCheckAoCreation(Composite parent) {
        this.aoCreationCheck = this.toolkit.createButton(parent, "AOCreation",
                SWT.CHECK);
        this.aoCreationCheck.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setAoCreation(RuleTab.this.aoCreationCheck.getSelection());

                    super.widgetSelected(e);
                }
            });

        return this.aoCreationCheck;
    }

    private Button createCheckMigration(Composite parent) {
        this.migrationCheck = this.toolkit.createButton(parent, "Migration",
                SWT.CHECK);
        this.migrationCheck.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RuleTab.this.rules.get(RuleTab.this.rulesTable.getSelectionIndex())
                                      .setMigration(RuleTab.this.migrationCheck.getSelection());

                    super.widgetSelected(e);
                }
            });

        return this.migrationCheck;
    }

    private Section createSectionRules(Composite parent) {
        Section section = this.toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR);
        section.setText("Rules");

        Composite client = this.toolkit.createComposite(section);
        client.setLayout(new GridLayout());

        createTableRules(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createCompositeOrderButtons(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createCompositeButtons(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createCompositeFields(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        this.toolkit.createLabel(client, "Authorized users :");
        createTableUsers(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        section.setClient(client);
        return section;
    }

    private Composite createCompositeOrderButtons(Composite parent) {
        Composite client = this.toolkit.createComposite(parent);
        client.setLayout(new GridLayout(2, true));

        createButtonUp(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createButtonDown(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        return client;
    }

    private Button createButtonUp(Composite parent) {
        Button button = this.toolkit.createButton(parent, "^", SWT.BUTTON1);
        button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    int index = RuleTab.this.rulesTable.getSelectionIndex();
                    if ((index == -1) || (index == 0)) {
                        return;
                    }
                    RuleTab.this.rules.set(index,
                        RuleTab.this.rules.set(index - 1,
                            RuleTab.this.rules.get(index)));
                    RuleTab.this.rulesTable.setSelection(index - 1);
                    updateRulesTable();

                    super.mouseUp(e);
                }
            });

        return button;
    }

    private Button createButtonDown(Composite parent) {
        Button button = this.toolkit.createButton(parent, "v", SWT.BUTTON1);
        button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    int index = RuleTab.this.rulesTable.getSelectionIndex();
                    if ((index == -1) ||
                            (index == (RuleTab.this.rules.size() - 1))) {
                        return;
                    }
                    RuleTab.this.rules.set(index,
                        RuleTab.this.rules.set(index + 1,
                            RuleTab.this.rules.get(index)));
                    RuleTab.this.rulesTable.setSelection(index + 1);
                    updateRulesTable();

                    super.mouseUp(e);
                }
            });

        return button;
    }

    private Composite createCompositeButtons(Composite parent) {
        Composite client = this.toolkit.createComposite(parent);
        client.setLayout(new GridLayout(3, true));

        createButtonNewRule(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createButtonSaveRules(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createButtonLoadPolicy(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        return client;
    }

    private Button createButtonNewRule(Composite parent) {
        Button button = this.toolkit.createButton(parent, "New rule",
                SWT.BUTTON1);
        button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    RuleTab.this.rules.add(new SimplePolicyRule());
                    updateRulesTable();

                    super.mouseUp(e);
                }
            });

        return button;
    }

    private Button createButtonSaveRules(Composite parent) {
        Button button = this.toolkit.createButton(parent, "Save rules",
                SWT.BUTTON1);
        button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    FileDialog fd = new FileDialog(new Shell(), SWT.SAVE);
                    fd.setText("Save active Policy file");
                    fd.setFilterExtensions(new String[] { "*.policy", "*.*" });
                    String fileName = fd.open();
                    PolicyFile policy = new PolicyFile(RuleTab.this.applicationNameText.getText(),
                            RuleTab.this.keystoreText.getText(),
                            RuleTab.this.rules, RuleTab.this.authorizedUsers);
                    PolicyTools.writePolicyFile(fileName, policy);

                    super.mouseUp(e);
                }
            });

        return button;
    }

    private Button createButtonLoadPolicy(Composite parent) {
        Button button = this.toolkit.createButton(parent, "Load rules",
                SWT.BUTTON1);
        button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    FileDialog fd = new FileDialog(new Shell(), SWT.OPEN);
                    fd.setText("Load Policy file");
                    fd.setFilterExtensions(new String[] { "*.policy", "*.*" });
                    String name = fd.open();
                    PolicyFile policy = null;
                    try {
                        policy = PolicyTools.readPolicyFile(name);
                        RuleTab.this.rules.clear();
                        RuleTab.this.rules.addAll(policy.getRules());
                        RuleTab.this.applicationNameText.setText(policy.getApplicationName());
                        RuleTab.this.keystoreText.setText(policy.getKeystorePath());
                        RuleTab.this.authorizedUsers.clear();
                        RuleTab.this.authorizedUsers.addAll(policy.getAuthorizedUsers());
                    } catch (ParserConfigurationException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    updateRulesTable();
                    updateUsersTable();

                    super.mouseUp(e);
                }
            });

        return button;
    }

    private Composite createCompositeFields(Composite parent) {
        Composite client = this.toolkit.createComposite(parent);
        client.setLayout(new GridLayout(3, false));

        this.toolkit.createLabel(client, "Application name :");
        this.applicationNameText = this.toolkit.createText(client, "");
        this.applicationNameText.setLayoutData(new GridData(SWT.FILL, SWT.TOP,
                true, false, 2, 1));

        this.toolkit.createLabel(client, "Keystore :");
        this.keystoreText = this.toolkit.createText(client, "");
        this.keystoreText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
                false));
        Button button = this.toolkit.createButton(client, "...", SWT.BUTTON1);
        button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    FileDialog fd = new FileDialog(new Shell(), SWT.OPEN);
                    fd.setText("Choose keystore");
                    fd.setFilterExtensions(new String[] { "*.p12", "*.*" });
                    String name = fd.open();
                    if (name != null) {
                        RuleTab.this.keystoreText.setText(name);
                    }

                    super.mouseUp(e);
                }
            });

        return client;
    }

    private Table createTableUsers(Composite parent) {
        this.usersTable = this.toolkit.createTable(parent, SWT.NULL);
        this.usersTable.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.character == SWT.DEL) || (e.character == SWT.BS)) {
                        RuleTab.this.authorizedUsers.remove(RuleTab.this.usersTable.getSelectionIndex());
                        updateUsersTable();
                    }

                    super.keyPressed(e);
                }
            });
        this.usersTableViewer = new TableViewer(this.usersTable);

        // drag n drop
        DropTarget target = new DropTarget(this.usersTable,
                DND.DROP_DEFAULT | DND.DROP_COPY);

        target.setTransfer(new Transfer[] {
                CertificateTreeMapTransfer.getInstance()
            });
        target.addDropListener(new DropTargetAdapter() {
                @Override
                public void dragEnter(DropTargetEvent event) {
                    if (event.detail == DND.DROP_DEFAULT) {
                        event.detail = DND.DROP_COPY;
                    }
                }

                @Override
                public void dragOperationChanged(DropTargetEvent event) {
                    if (event.detail == DND.DROP_DEFAULT) {
                        event.detail = DND.DROP_COPY;
                    }
                }

                @Override
                public void drop(DropTargetEvent event) {
                    if (CertificateTreeMapTransfer.getInstance()
                                                      .isSupportedType(event.currentDataType)) {
                        CertificateTreeMap map = (CertificateTreeMap) event.data;

                        for (CertificateTree tree : map.keySet()) {
                            if (tree.getCertificate().getType() == EntityType.USER) {
                                RuleTab.this.authorizedUsers.add(tree.getCertificate()
                                                                     .toString());
                            }
                        }

                        updateUsersTable();
                    }
                }
            });

        return this.usersTable;
    }

    protected void updateUsersTable() {
        int selection = this.usersTable.getSelectionIndex();
        this.usersTable.removeAll();

        this.usersTableViewer.add(this.authorizedUsers.toArray());

        if (this.usersTable.getItemCount() == 0) {
            this.usersTable.deselectAll();
        } else {
            if (selection >= this.usersTable.getItemCount()) {
                selection--;
            }
            this.usersTable.setSelection(selection);
        }
    }

    protected void updateRulesTable() {
        int selection = this.rulesTable.getSelectionIndex();
        this.rulesTable.removeAll();

        this.rulesTableViewer.add(this.rules.toArray());

        if (this.rulesTable.getItemCount() == 0) {
            this.rulesTable.deselectAll();
        } else {
            if (selection >= this.rulesTable.getItemCount()) {
                selection--;
            }
            this.rulesTable.setSelection(selection);
        }
        updateRuleEditor();
    }

    private Table createTableRules(Composite parent) {
        this.rulesTable = this.toolkit.createTable(parent, SWT.NULL);
        this.rulesTable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateRuleEditor();

                    super.widgetSelected(e);
                }
            });
        this.rulesTable.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.character == SWT.DEL) || (e.character == SWT.BS)) {
                        RuleTab.this.rules.remove(RuleTab.this.rulesTable.getSelectionIndex());
                        updateRulesTable();
                    }

                    super.keyPressed(e);
                }
            });
        this.rulesTableViewer = new TableViewer(this.rulesTable);

        return this.rulesTable;
    }

    protected void updateRuleEditor() {
        this.fromTable.updateTable(this.rulesTable);
        this.toTable.updateTable(this.rulesTable);

        if (this.rulesTable.getSelectionIndex() != -1) {
            SimplePolicyRule selectedRule = this.rules.get(this.rulesTable.getSelectionIndex());

            this.requestCheck.setEnabled(true);

            this.replyCheck.setEnabled(true);

            this.aoCreationCheck.setEnabled(true);
            this.migrationCheck.setEnabled(true);

            this.requestCheck.setSelection(selectedRule.isRequest());

            enableRequestEditor(selectedRule.isRequest());

            selectRODCombo(selectedRule.getReqAuth(), this.reqAuthCombo);
            selectRODCombo(selectedRule.getReqInt(), this.reqIntCombo);
            selectRODCombo(selectedRule.getReqConf(), this.reqConfCombo);

            this.replyCheck.setSelection(selectedRule.isReply());

            enableReplyEditor(selectedRule.isReply());

            selectRODCombo(selectedRule.getRepAuth(), this.repAuthCombo);
            selectRODCombo(selectedRule.getRepInt(), this.repIntCombo);
            selectRODCombo(selectedRule.getRepConf(), this.repConfCombo);

            this.aoCreationCheck.setSelection(selectedRule.isAoCreation());
            this.migrationCheck.setSelection(selectedRule.isMigration());
        } else {
            this.requestCheck.setEnabled(false);

            enableRequestEditor(false);

            this.replyCheck.setEnabled(false);

            enableReplyEditor(false);

            this.aoCreationCheck.setEnabled(false);
            this.migrationCheck.setEnabled(false);
        }
    }

    protected void enableRequestEditor(boolean enable) {
        this.reqAuthCombo.setEnabled(enable);
        this.reqIntCombo.setEnabled(enable);
        this.reqConfCombo.setEnabled(enable);
    }

    protected void enableReplyEditor(boolean enable) {
        this.repAuthCombo.setEnabled(enable);
        this.repIntCombo.setEnabled(enable);
        this.repConfCombo.setEnabled(enable);
    }

    public void setAppName(String name) {
        this.applicationNameText.setText(name);
    }

    public String getAppName() {
        return this.applicationNameText.getText();
    }

    public void setAuthorizedUsers(List<String> users) {
        this.authorizedUsers.clear();
        this.authorizedUsers.addAll(users);
        updateUsersTable();
    }

    public List<String> getAuthorizedUsers() {
        return this.authorizedUsers;
    }

    public List<SimplePolicyRule> getRules() {
        return this.rules;
    }

    public void setRules(List<SimplePolicyRule> policies) {
        this.rules.clear();
        this.rules.addAll(policies);
        updateRulesTable();
    }

    @Override
    public void update() {
        this.activeKeystoreSection.updateSection();
        updateRulesTable();
    }
}
