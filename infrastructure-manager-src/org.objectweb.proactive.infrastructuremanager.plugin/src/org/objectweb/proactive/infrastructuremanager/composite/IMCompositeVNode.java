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
package org.objectweb.proactive.infrastructuremanager.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.objectweb.proactive.infrastructuremanager.IMConstants;


public class IMCompositeVNode extends Composite {
    private IMCompositeDescriptor parent;
    private Composite headerComposite;
    private Composite nodesComposite;
    private Button button;
    private RowData data;
    private String nameVNode;
    private boolean isCollapse = true;

    public IMCompositeVNode(IMCompositeDescriptor p, String name) {
        super(p, SWT.NONE);
        nameVNode = name;
        parent = p;
        parent.addVnode(this);
        setBackground(IMConstants.WHITE_COLOR);

        data = new RowData();
        data.exclude = isCollapse;
        setLayout(new RowLayout(SWT.VERTICAL));

        RowLayout rowLayoutHorizontal = new RowLayout(SWT.HORIZONTAL);
        rowLayoutHorizontal.pack = true;

        // Composite Header
        headerComposite = new Composite(this, SWT.NONE);
        headerComposite.setBackground(IMConstants.WHITE_COLOR);
        headerComposite.setLayout(rowLayoutHorizontal);
        button = new Button(headerComposite, SWT.ARROW | SWT.RIGHT);
        button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    isCollapse = !isCollapse;
                    data.exclude = isCollapse;
                    nodesComposite.setVisible(!isCollapse);
                    if (nodesComposite.isVisible()) {
                        button.setAlignment(SWT.DOWN);
                    } else {
                        button.setAlignment(SWT.RIGHT);
                    }
                    parent.pack(true);
                }
            });

        Label label = new Label(headerComposite, SWT.NONE);
        label.setText(nameVNode);
        label.setBackground(IMConstants.WHITE_COLOR);

        // Composite des nodes
        nodesComposite = new Composite(this, SWT.NONE);
        nodesComposite.setBackground(IMConstants.WHITE_COLOR);
        nodesComposite.setLayout(rowLayoutHorizontal);
        nodesComposite.setLayoutData(data);
        nodesComposite.setVisible(false);
    }

    public Composite getNodesComposite() {
        return nodesComposite;
    }

    public String getNameVNode() {
        return nameVNode;
    }

    public boolean isCollapsed() {
        return isCollapse;
    }

    public void setCollapse(boolean b) {
        isCollapse = b;
        data.exclude = isCollapse;
        nodesComposite.setVisible(!isCollapse);
        if (nodesComposite.isVisible()) {
            button.setAlignment(SWT.DOWN);
        } else {
            button.setAlignment(SWT.RIGHT);
        }
        parent.pack(true);
    }
}
