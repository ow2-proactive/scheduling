/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.table;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;


public class RMTableViewer extends TableViewer {

    public RMTableViewer(Composite parent) {
        super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        this.setContentProvider(new TableContentProvider());
        ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
    }

    public void init() {
        setInput(RMStore.getInstance().getModel().getRoot());
    }

    public void actualize() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                refresh();
            }
        });
    }

    public void updateItem(String nodeSource, String host, NodeState state, String nodeUrl) {
        final NodeTableItem item = new NodeTableItem(nodeSource, host, state, nodeUrl);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                update(item, null);
            }
        });
    }

    public void removeItem(String nodeUrl) {
        final NodeTableItem item = new NodeTableItem("", "", null, nodeUrl);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                remove(item);
            }
        });
    }

    public void addItem(String nodeSource, String host, NodeState state, String nodeUrl) {
        final NodeTableItem item = new NodeTableItem(nodeSource, host, state, nodeUrl);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                add(item);
            }
        });
    }

    public void tabFocused() {
        fireSelectionChanged(new SelectionChangedEvent(this, this.getSelection()));
    }
}
