/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://proactive.inria.fr/team_members.htm Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.resourcemanager.gui.compact.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.ow2.proactive.resourcemanager.gui.Activator;
import org.ow2.proactive.resourcemanager.gui.compact.Filter;
import org.ow2.proactive.resourcemanager.gui.compact.LabelMouseListener;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;


/**
 *
 * Graphical representation of the node.
 *
 */
public class NodeView extends View {

    public static Image[] images = new Image[] {
            Activator.getImageDescriptor("icons/free.gif").createImage(),
            Activator.getImageDescriptor("icons/busy.gif").createImage(),
            Activator.getImageDescriptor("icons/down.gif").createImage(),
            Activator.getImageDescriptor("icons/to_release.gif").createImage(), };

    public NodeView(TreeLeafElement element, Filter filter) {
        super(element);

        if (filter.showNodes) {
            label = new Label(ResourcesCompactView.getCompactViewer().getComposite(), SWT.SHADOW_NONE);
            label.setBackground(ResourcesCompactView.getCompactViewer().getComposite().getBackground());

            label.setImage(images[((Node) element).getState().ordinal()]);
            label.addMouseListener(new LabelMouseListener(this));
            label.setToolTipText(toString());
        }
    }

    @Override
    public String toString() {

        String nodeName = element.getName();
        String since = ((Node) element).getState() + " since: " + ((Node) element).getStateChangeTime();
        String vmName = element.getParent().getName();
        String hostName = element.getParent().getParent().getName();
        String nodeSourceName = element.getParent().getParent().getParent().getName();

        String tooltip = "Node: " + nodeName + "\n";
        tooltip += since + "\n";
        tooltip += "VM: " + vmName + "\n";
        tooltip += "Host: " + hostName + "\n";
        tooltip += "Node Source: " + nodeSourceName;

        return tooltip;
    }

    @Override
    public void dispose() {
        if (label != null)
            label.dispose();
    }

    @Override
    public void update() {
        if (label != null) {
            label.setImage(images[((Node) element).getState().ordinal()]);
            label.setToolTipText(toString());
        }
    }
}
