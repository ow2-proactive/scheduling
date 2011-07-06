/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.compact.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.ow2.proactive.resourcemanager.gui.Internal;
import org.ow2.proactive.resourcemanager.gui.compact.Filter;
import org.ow2.proactive.resourcemanager.gui.compact.LabelMouseListener;
import org.ow2.proactive.resourcemanager.gui.data.model.Host;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;


/**
 *
 * Graphical representation of the node.
 *
 */
public class NodeView extends View {

    protected NodeView(TreeLeafElement element) {
        super(element);
    }

    public NodeView(TreeLeafElement element, Filter filter) {
        super(element);

        if (filter.showNodes) {
            label = new Label(ResourcesCompactView.getCompactViewer().getComposite(), SWT.SHADOW_NONE);
            label.setBackground(ResourcesCompactView.getCompactViewer().getComposite().getBackground());
            label.setImage(Internal.getImageByNodeState(((Node) element).getState()));
            label.addMouseListener(new LabelMouseListener(this));
            label.setToolTipText(toString());
        }
    }

    @Override
    public String toString() {

        String nodeName = element.getName();
        String since = ((Node) element).getState() + " since: " + ((Node) element).getStateChangeTime();
        String provider = ((Node) element).getProvider() == null ? "" : ((Node) element).getProvider();
        String owner = ((Node) element).getOwner() == null ? "" : ((Node) element).getOwner();
        String vmName = element.getParent().getName();
        Host host = (Host) element.getParent().getParent();
        String hostName = host.getName();
        String nodeSourceName = element.getParent().getParent().getParent().getName();

        String tooltip = "Node: " + nodeName + "\n";
        tooltip += since + "\n";
        tooltip += "JVM: " + vmName + "\n";
        tooltip += (host.isVirtual() ? "VM: " : "Host: ") + hostName + "\n";
        tooltip += "Node Source: " + nodeSourceName + "\n";
        tooltip += "Provider: " + provider + "\n";
        tooltip += "Used by: " + owner;

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
            label.setImage(Internal.getImageByNodeState(((Node) element).getState()));
            label.setToolTipText(toString());
        }
    }
}
