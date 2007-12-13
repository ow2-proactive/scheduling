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

import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.objectweb.proactive.ic2d.security.core.CertificateTree;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeMap;
import org.objectweb.proactive.ic2d.security.core.CertificateTreeMapTransfer;
import org.objectweb.proactive.ic2d.security.core.SimplePolicyRule;


public class EntityTableComposite extends Composite {
    protected Table entities;
    private TableViewer viewer;
    protected List<SimplePolicyRule> rules;
    protected Table rulesTable;
    private boolean isFrom;

    public EntityTableComposite(Composite parent, FormToolkit toolkit, List<SimplePolicyRule> data,
            final boolean isFrom) {
        super(parent, SWT.NULL);
        toolkit.adapt(this);
        this.rules = data;
        this.isFrom = isFrom;
        super.setLayout(new GridLayout());

        this.entities = toolkit.createTable(this, SWT.NULL);
        this.entities.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.viewer = new TableViewer(this.entities);

        this.entities.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.character == SWT.DEL) || (e.character == SWT.BS)) {
                    if (isFrom) {
                        EntityTableComposite.this.rules.get(
                                EntityTableComposite.this.rulesTable.getSelectionIndex()).removeFrom(
                                EntityTableComposite.this.entities.getSelectionIndex());
                    } else {
                        EntityTableComposite.this.rules.get(
                                EntityTableComposite.this.rulesTable.getSelectionIndex()).removeTo(
                                EntityTableComposite.this.entities.getSelectionIndex());
                    }
                    updateTable();
                }

                super.keyPressed(e);
            }
        });

        // drag n drop
        DropTarget target = new DropTarget(this.entities, DND.DROP_DEFAULT | DND.DROP_COPY);

        target.setTransfer(new Transfer[] { CertificateTreeMapTransfer.getInstance() });
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
                if (CertificateTreeMapTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    CertificateTreeMap map = (CertificateTreeMap) event.data;

                    for (CertificateTree tree : map.keySet()) {
                        String display = tree.getCertificate().getType().toString() + ":" +
                            tree.getCertificate().getCert().getSubjectDN().getName();
                        if (isFrom) {
                            EntityTableComposite.this.rules.get(
                                    EntityTableComposite.this.rulesTable.getSelectionIndex())
                                    .addFrom(display);
                        } else {
                            EntityTableComposite.this.rules.get(
                                    EntityTableComposite.this.rulesTable.getSelectionIndex()).addTo(display);
                        }
                    }

                    updateTable();
                }
            }
        });
    }

    public void updateTable() {
        updateTable(null);
    }

    public void updateTable(Table newRulesTable) {
        if (newRulesTable != null) {
            this.rulesTable = newRulesTable;
        }
        this.entities.removeAll();
        if (this.rulesTable.getSelectionIndex() != -1) {
            if (this.isFrom) {
                this.viewer.add(this.rules.get(this.rulesTable.getSelectionIndex()).getFrom().toArray());
            } else {
                this.viewer.add(this.rules.get(this.rulesTable.getSelectionIndex()).getTo().toArray());
            }
        }
    }

    public void add(String re) {
        if (isFrom) {
            EntityTableComposite.this.rules.get(EntityTableComposite.this.rulesTable.getSelectionIndex())
                    .addFrom(re);
        } else {
            EntityTableComposite.this.rules.get(EntityTableComposite.this.rulesTable.getSelectionIndex())
                    .addTo(re);
        }
        this.updateTable();
    }
}
