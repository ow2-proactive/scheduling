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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.ic2d.security.core.CertificateTree;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeList;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeMap;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeMapTransfer;


public class CertificateTreeListSection {
    private Section section;
    private Tree tree;
    protected CertificateTreeList certTreeList;

    public CertificateTreeListSection(Composite parent, FormToolkit toolkit,
        String title, CertificateTreeList data, boolean allowDeletion,
        boolean allowDrag, boolean allowDrop, boolean withChecks) {
        this.section = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR);
        this.section.setText(title);
        this.certTreeList = data;

        Composite client = toolkit.createComposite(this.section);
        client.setLayout(new GridLayout());

        int style = withChecks ? (SWT.SINGLE | SWT.CHECK) : SWT.SINGLE;
        this.tree = toolkit.createTree(client, style);
        this.tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        if (allowDeletion) {
            this.tree.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if ((e.character == SWT.DEL) ||
                                (e.character == SWT.BS)) {
                            CertificateTreeListSection.this.certTreeList.remove(getSelectionData());

                            updateSection();
                        }

                        super.keyPressed(e);
                    }
                });
        }

        if (allowDrag) {
            DragSource source = new DragSource(this.tree, DND.DROP_COPY);

            source.setTransfer(new Transfer[] {
                    CertificateTreeMapTransfer.getInstance()
                });

            source.addDragListener(new DragSourceAdapter() {
                    @Override
                    public void dragSetData(DragSourceEvent event) {
                        // Provide the data of the requested type.
                        CertificateTreeMap map = new CertificateTreeMap();
                        map.put(getSelectionData(),
                            getSelectionData().getCertChain());
                        event.data = map;
                    }
                });
        }

        if (allowDrop) {
            DropTarget target = new DropTarget(this.tree,
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

                            for (Entry<CertificateTree, List<TypedCertificate>> entry : map.entrySet()) {
                                CertificateTree newTree = CertificateTree.newTree(entry.getValue());
                                newTree.merge(entry.getKey());
                                CertificateTreeListSection.this.certTreeList.add(newTree.getRoot());
                            }

                            updateSection();
                        }
                    }
                });
        }

        this.section.setClient(client);
    }

    public void updateSection() {
        // Get expansion state
        Map<TypedCertificate, Boolean> expanded = new HashMap<TypedCertificate, Boolean>();
        for (TreeItem item : this.tree.getItems()) {
            expanded.putAll(getExpanded(item));
        }

        // Get check state
        Map<TypedCertificate, Boolean> checked = new HashMap<TypedCertificate, Boolean>();
        for (TreeItem item : this.tree.getItems()) {
            checked.putAll(getChecked(item));
        }

        this.tree.removeAll();
        for (CertificateTree subTree : this.certTreeList) {
            newTreeItem(this.tree, subTree);
        }

        for (TreeItem item : this.tree.getItems()) {
            setChecked(item, checked);
        }

        for (TreeItem item : this.tree.getItems()) {
            setExpanded(item, expanded);
        }
    }

    private Map<TypedCertificate, Boolean> getExpanded(TreeItem item) {
        Map<TypedCertificate, Boolean> state = new HashMap<TypedCertificate, Boolean>();

        state.put(((CertificateTree) item.getData()).getCertificate(),
            new Boolean(item.getExpanded()));
        for (TreeItem child : item.getItems()) {
            state.putAll(getExpanded(child));
        }

        return state;
    }

    private Map<TypedCertificate, Boolean> getChecked(TreeItem item) {
        Map<TypedCertificate, Boolean> state = new HashMap<TypedCertificate, Boolean>();

        if (!item.getGrayed()) {
            state.put(((CertificateTree) item.getData()).getCertificate(),
                new Boolean(item.getChecked()));
        }
        for (TreeItem child : item.getItems()) {
            state.putAll(getChecked(child));
        }

        return state;
    }

    private void setExpanded(TreeItem item,
        Map<TypedCertificate, Boolean> expanded) {
        TypedCertificate cert = ((CertificateTree) item.getData()).getCertificate();
        if (expanded.containsKey(cert)) {
            item.setExpanded(expanded.get(cert).booleanValue());
            for (TreeItem child : item.getItems()) {
                setExpanded(child, expanded);
            }
        }
    }

    private void setChecked(TreeItem item,
        Map<TypedCertificate, Boolean> checked) {
        TypedCertificate cert = ((CertificateTree) item.getData()).getCertificate();
        if (checked.containsKey(cert)) {
            item.setChecked(checked.get(cert).booleanValue());
        } else if (!item.getGrayed()) {
            item.setChecked(true);
        }
        for (TreeItem child : item.getItems()) {
            setChecked(child, checked);
        }
    }

    private TreeItem newTreeItem(Widget parent, CertificateTree newTree) {
        TreeItem item = null;
        if (parent instanceof Tree) {
            Tree treeParent = (Tree) parent;
            item = new TreeItem(treeParent, SWT.NONE);
        } else if (parent instanceof TreeItem) {
            TreeItem itemParent = (TreeItem) parent;
            item = new TreeItem(itemParent, SWT.NONE);
        }

        if (item == null) {
            return null;
        }

        item.setData(newTree);
        item.setText(newTree.getName());
        boolean hasPrivateKey = newTree.getCertificate().getPrivateKey() != null;
        item.setGrayed(!hasPrivateKey);
        if (hasPrivateKey) {
            item.setForeground(new Color(null, 255, 0, 0));
        }
        for (CertificateTree subTree : newTree.getChildren()) {
            newTreeItem(item, subTree);
        }
        return item;
    }

    public CertificateTree getSelectionData() {
        return (CertificateTree) this.tree.getSelection()[0].getData();
    }

    public Section get() {
        return this.section;
    }

    public Tree getTree() {
        return this.tree;
    }
}
