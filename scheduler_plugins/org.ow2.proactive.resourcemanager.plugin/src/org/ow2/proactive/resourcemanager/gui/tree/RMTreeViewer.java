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
package org.ow2.proactive.resourcemanager.gui.tree;

import java.util.Collections;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.ViewPart;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;


/**
 * @author The ProActive Team
 */
public class RMTreeViewer extends TreeViewer {

    public RMTreeViewer(ViewPart view, Composite parent) {
        super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        this.setContentProvider(new TreeContentProvider());
        ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
    }

    public void init() {
        setInput(RMStore.getInstance().getModel().getRoot());
    }

    public void actualize(TreeLeafElement element) {
        final TreeLeafElement elem = element;
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                refresh(elem, true);
            }
        });
    }

    public void treeFocused() {
        fireSelectionChanged(new SelectionChangedEvent(this, this.getSelection()));
    }

    public void select(TreeLeafElement item) {
        Widget w = findItem(item);
        if (w != null) {
            setSelection(Collections.singletonList(w));
        }
    }
}