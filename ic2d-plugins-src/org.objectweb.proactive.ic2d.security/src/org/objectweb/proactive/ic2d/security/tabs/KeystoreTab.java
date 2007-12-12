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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.ic2d.security.core.CertificateTree;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeList;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeMap;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeMapTransfer;
import org.objectweb.proactive.ic2d.security.core.KeystoreFile;
import org.objectweb.proactive.ic2d.security.core.KeystoreUtils;
import org.objectweb.proactive.ic2d.security.widgets.CertificateDetailsSection;
import org.objectweb.proactive.ic2d.security.widgets.CertificateTreeListSection;
import org.objectweb.proactive.ic2d.security.widgets.PasswordDialog;


public class KeystoreTab extends UpdatableTab {
    public static final String ID = "org.objectweb.proactive.ic2d.security.tabs.KeystoreTab";
    protected List<KeystoreFile> keystoreFileList;
    protected CertificateTreeList activeKeystore;
    private FormToolkit toolkit;
    protected Tree keystoreTree;
    protected CertificateTreeListSection activeKeystoreSection;
    protected CertificateDetailsSection certDetailsSection;

    //  protected Text passwordText;
    public KeystoreTab(CTabFolder folder, CertificateTreeList keystore,
        FormToolkit tk) {
        super(folder, SWT.NULL);
        setText("Keystore Editor");

        this.keystoreFileList = new ArrayList<KeystoreFile>();
        this.toolkit = tk;
        this.activeKeystore = keystore;

        Composite body = this.toolkit.createComposite(folder);

        body.setLayout(new GridLayout(3, true));

        createSectionLoadSave(body)
            .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

        createSectionKeystoreList(body)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSectionActiveKeystore(body)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createSectionCertDetails(body)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        setControl(body);
    }

    private Composite createSectionLoadSave(Composite parent) {
        Composite client = this.toolkit.createComposite(parent);
        client.setLayout(new GridLayout(4, false));

        createButtonLoad(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createButtonSave(client)
            .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        //this.toolkit.createLabel(client, "Keystore Password :");

        //this.passwordText = this.toolkit.createText(client, "");
        //this.passwordText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
        //        false));
        return client;
    }

    private Button createButtonLoad(Composite parent) {
        Button b = this.toolkit.createButton(parent, "Load a keystore",
                SWT.BUTTON1);
        b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    FileDialog fd = new FileDialog(new Shell(), SWT.OPEN);
                    fd.setText("Open a keystore");
                    fd.setFilterExtensions(new String[] { "*.p12", "*.crt", "*.*" });
                    String name = fd.open();
                    try {
                        new PasswordDialog(null);

                        KeystoreTab.this.keystoreFileList.add(new KeystoreFile(
                                name, KeystoreUtils.loadKeystore(name, "ha")));
                    } catch (NoSuchAlgorithmException e1) {
                        e1.printStackTrace();
                    } catch (CertificateException e1) {
                        e1.printStackTrace();
                    } catch (FileNotFoundException e1) {
                        ErrorDialog.openError(Display.getCurrent()
                                                     .getActiveShell(),
                            "File Error", "Unable to open file",
                            new Status(IStatus.ERROR, ID, IStatus.OK,
                                "See details", e1));
                        return;
                    } catch (IOException e1) {
                        ErrorDialog.openError(Display.getCurrent()
                                                     .getActiveShell(),
                            "Keystore Error", "Unable to open keystore",
                            new Status(IStatus.ERROR, ID, IStatus.OK,
                                "See details", e1));
                        return;
                    } catch (KeyStoreException e1) {
                        e1.printStackTrace();
                    } catch (NoSuchProviderException e1) {
                        e1.printStackTrace();
                    } catch (UnrecoverableKeyException eke) {
                        eke.printStackTrace();
                    }
                    updateKeystoreTree();

                    super.mouseUp(e);
                }
            });

        return b;
    }

    private Button createButtonSave(Composite parent) {
        Button b = this.toolkit.createButton(parent, "Save active keystore",
                SWT.BUTTON1);
        b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    Map<CertificateTree, Boolean> keepPrivateKeyMap = getSelected();

                    FileDialog fd = new FileDialog(new Shell(), SWT.SAVE);
                    fd.setText("Save the active keystore as");
                    fd.setFilterExtensions(new String[] { "*.p12", "*.crt", "*.*" });
                    String name = fd.open();
                    try {
                        KeystoreUtils.saveKeystore(name, "ha",
                            KeystoreTab.this.activeKeystore, keepPrivateKeyMap);
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                    } catch (KeyStoreException kse) {
                        kse.printStackTrace();
                    } catch (NoSuchProviderException nspe) {
                        nspe.printStackTrace();
                    } catch (NoSuchAlgorithmException nsae) {
                        nsae.printStackTrace();
                    } catch (CertificateException ce) {
                        ce.printStackTrace();
                    } catch (IOException oie) {
                        oie.printStackTrace();
                    } catch (UnrecoverableKeyException uke) {
                        uke.printStackTrace();
                    }

                    super.mouseUp(e);
                }
            });

        return b;
    }

    private Section createSectionKeystoreList(Composite parent) {
        Section section = this.toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR);
        section.setText("Loaded Keystores List");

        Composite client = this.toolkit.createComposite(section);
        client.setLayout(new GridLayout());

        createTreeKeystore(client)
            .setLayoutData(new GridData(GridData.FILL_BOTH));

        section.setClient(client);

        return section;
    }

    public Map<CertificateTree, Boolean> getSelected() {
        Map<CertificateTree, Boolean> map = new HashMap<CertificateTree, Boolean>();
        for (TreeItem item : this.activeKeystoreSection.getTree().getItems()) {
            addSelected(item, map);
        }
        return map;
    }

    private void addSelected(TreeItem item, Map<CertificateTree, Boolean> map) {
        CertificateTree tree = (CertificateTree) item.getData();
        if (item.getChecked() &&
                (tree.getCertificate().getPrivateKey() != null)) {
            map.put(tree, new Boolean(true));
        }
        for (TreeItem child : item.getItems()) {
            addSelected(child, map);
        }
    }

    private Tree createTreeKeystore(Composite parent) {
        this.keystoreTree = this.toolkit.createTree(parent, SWT.SINGLE);
        this.keystoreTree.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Object data = KeystoreTab.this.keystoreTree.getSelection()[0].getData();

                    if (data instanceof CertificateTree) {
                        CertificateTree ct = (CertificateTree) data;

                        KeystoreTab.this.certDetailsSection.update(ct.getCertificate());
                    } else {
                        KeystoreTab.this.certDetailsSection.update(null);
                    }

                    super.widgetSelected(e);
                }
            });

        this.keystoreTree.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.character == SWT.DEL) || (e.character == SWT.BS)) {
                        TreeItem ks = KeystoreTab.this.keystoreTree.getSelection()[0];
                        CertificateTree selectedTree = (CertificateTree) ks.getData();

                        while (ks.getParentItem() != null) {
                            ks = ks.getParentItem();
                        }

                        CertificateTreeList list = (CertificateTreeList) ks.getData();
                        list.remove(selectedTree);

                        updateKeystoreTree();
                    }

                    super.keyPressed(e);
                }
            });

        // drag n drop
        DragSource source = new DragSource(this.keystoreTree, DND.DROP_COPY);

        source.setTransfer(new Transfer[] {
                CertificateTreeMapTransfer.getInstance()
            });

        source.addDragListener(new DragSourceAdapter() {
                @Override
                public void dragSetData(DragSourceEvent event) {
                    // Provide the data of the requested type.
                    CertificateTreeMap map = new CertificateTreeMap();
                    Object data = KeystoreTab.this.keystoreTree.getSelection()[0].getData();

                    if (data instanceof KeystoreFile) {
                        KeystoreFile ksf = (KeystoreFile) data;

                        for (CertificateTree tree : ksf) {
                            map.put(tree, tree.getCertChain());
                        }
                    } else if (data instanceof CertificateTree) {
                        CertificateTree ct = (CertificateTree) data;

                        map.put(ct, ct.getCertChain());
                    }

                    event.data = map;
                }
            });

        return this.keystoreTree;
    }

    protected void updateKeystoreTree() {
        this.keystoreTree.removeAll();

        for (KeystoreFile keystore : this.keystoreFileList) {
            TreeItem ksItem = new TreeItem(this.keystoreTree, SWT.NONE);
            ksItem.setData(keystore);
            ksItem.setText(keystore.getName());
            for (CertificateTree tree : keystore) {
                newTreeItem(ksItem, tree);
            }
        }
    }

    private TreeItem newTreeItem(TreeItem parent, CertificateTree tree) {
        TreeItem item = new TreeItem(parent, SWT.NONE);

        item.setData(tree);
        item.setText(tree.getName());
        if (tree.getCertificate().getPrivateKey() != null) {
            item.setForeground(new Color(null, 255, 0, 0));
        }
        for (CertificateTree subTree : tree.getChildren()) {
            newTreeItem(item, subTree);
        }
        return item;
    }

    private Section createSectionActiveKeystore(Composite parent) {
        this.activeKeystoreSection = new CertificateTreeListSection(parent,
                this.toolkit, "ActiveKeystore", this.activeKeystore, true,
                false, true, true);

        this.activeKeystoreSection.getTree().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    KeystoreTab.this.certDetailsSection.update(KeystoreTab.this.activeKeystoreSection.getSelectionData()
                                                                                                     .getCertificate());

                    super.widgetSelected(e);
                }
            });

        return this.activeKeystoreSection.get();
    }

    private Section createSectionCertDetails(Composite parent) {
        this.certDetailsSection = new CertificateDetailsSection(parent,
                this.toolkit);

        return this.certDetailsSection.get();
    }

    @Override
    public void update() {
        this.activeKeystoreSection.updateSection();
    }
}
