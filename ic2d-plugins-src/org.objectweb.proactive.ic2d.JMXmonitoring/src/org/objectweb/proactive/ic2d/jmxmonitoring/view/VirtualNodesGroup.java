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
package org.objectweb.proactive.ic2d.jmxmonitoring.view;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VirtualNodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.VNColors;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;


public class VirtualNodesGroup implements Observer {
    private Group group;
    private Map<Button, VirtualNodeObject> buttons = new HashMap<Button, VirtualNodeObject>();
    private Map<VirtualNodeObject, Button> virtualNodes = new HashMap<VirtualNodeObject, Button>();
    private VNColors vnColors;

    //
    // -- CONSTRUCTOR -----------------------------------------------
    //
    public VirtualNodesGroup(Composite parent) {
        vnColors = new VNColors();

        group = new Group(parent, SWT.NONE);
        group.setText("Virtual nodes");

        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = false;
        rowLayout.pack = true;
        rowLayout.justify = false;
        rowLayout.marginLeft = 5;
        rowLayout.marginTop = 0;
        rowLayout.marginRight = 5;
        rowLayout.marginBottom = 5;
        rowLayout.spacing = 10;
        group.setLayout(rowLayout);
    }

    //
    // -- PUBLIC METHOD -----------------------------------------------
    //
    public void update(Observable o, Object arg) {
        // <"Message",VNObject>
        if (!(arg instanceof MVCNotification)) {
            return;
        }

        MVCNotification notif = (MVCNotification) arg;
        Object data = notif.getData();
        if ((notif.getMVCNotification() == MVCNotificationTag.WORLD_OBJECT_ADD_VIRTUAL_NODE) &&
            (data instanceof Hashtable)) { //<"Add a virtual node",VNObject>
            @SuppressWarnings("unchecked")
            Hashtable<String, VirtualNodeObject> table = (Hashtable<String, VirtualNodeObject>) data;
            final VirtualNodeObject vnAdded = (VirtualNodeObject) table.get(WorldObject.ADD_VN_MESSAGE);
            if (vnAdded != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        Button b = new Button(group, SWT.CHECK);
                        b.setForeground(vnColors.getColor(vnAdded.getKey())); //DOESN'T WORK !!!
                        b.setText(vnAdded.getName());
                        b.addSelectionListener(new VirtualNodeButtonListener());
                        buttons.put(b, vnAdded);
                        virtualNodes.put(vnAdded, b);
                        group.pack(true);
                    }
                });
            }
        } else if ((notif.getMVCNotification() == MVCNotificationTag.WORLD_OBJECT_REMOVE_VIRTUAL_NODE) &&
            (data instanceof Hashtable)) { //<"Remove a virtual node",VNObject>
            @SuppressWarnings("unchecked")
            Hashtable<String, VirtualNodeObject> table = (Hashtable) data;
            final VirtualNodeObject vnRemoved = (VirtualNodeObject) table.get(WorldObject.REMOVE_VN_MESSAGE);
            if (vnRemoved != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        Button b = virtualNodes.get(vnRemoved);
                        virtualNodes.remove(vnRemoved);
                        buttons.remove(b);
                        if (!b.isDisposed()) {
                            b.dispose();
                        }
                        if (!group.isDisposed()) {
                            group.pack();
                        }
                    }
                });
            }
        }
    }

    public Group getGroup() {
        return group;
    }

    public Color getColor(VirtualNodeObject vn) {
        Button b = virtualNodes.get(vn);
        if (b == null) {
            return null;
        }
        if (!b.getSelection()) {
            return null;
        }
        return vnColors.getColor(vn.getKey());
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    private class VirtualNodeButtonListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            VirtualNodeObject vn = buttons.get(e.widget);
            List<AbstractData> nodes = vn.getMonitoredChildrenAsList();
            for (int i = 0, size = nodes.size(); i < size; i++) {
                NodeObject node = (NodeObject) nodes.get(i);
                node.setHighlight(((Button) e.widget).getSelection());
            }
        }
    }
}
