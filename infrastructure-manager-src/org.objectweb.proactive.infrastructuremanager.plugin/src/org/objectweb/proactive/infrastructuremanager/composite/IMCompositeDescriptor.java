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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.objectweb.proactive.infrastructuremanager.IMConstants;


public class IMCompositeDescriptor extends Composite {
    private ExpandItem item;
    private HashMap<String, IMCompositeVNode> vnodes;

    public IMCompositeDescriptor(Composite parent, ExpandItem ei) {
        super(parent, SWT.NONE);
        item = ei;
        vnodes = new HashMap<String, IMCompositeVNode>();
        RowLayout layout = new RowLayout(SWT.VERTICAL);
        layout.wrap = false;
        setLayout(layout);
        setBackground(IMConstants.WHITE_COLOR);

        Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Item Test");
        item.addListener(SWT.Selection,
            new Listener() {
                public void handleEvent(Event event) {
                    System.out.println("item selected: Item Test");
                }
            });
        setMenu(menu);
    }

    @Override
    public void pack(boolean b) {
        super.pack(b);
        item.setHeight(computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item.getParent().redraw();
    }

    public void addVnode(IMCompositeVNode v) {
        vnodes.put(v.getNameVNode(), v);
    }

    public HashMap<String, IMCompositeVNode> getVnodes() {
        return vnodes;
    }

    public ArrayList<String> getNamesOfExpandedVNodes() {
        ArrayList<String> res = new ArrayList<String>();
        for (IMCompositeVNode vnode : vnodes.values()) {
            if (!vnode.isCollapsed()) {
                res.add(vnode.getNameVNode());
            }
        }
        return res;
    }
}
