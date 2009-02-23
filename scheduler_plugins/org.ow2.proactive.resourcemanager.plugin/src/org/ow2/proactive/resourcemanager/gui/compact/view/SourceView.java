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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.ow2.proactive.resourcemanager.gui.compact.CompactViewer;
import org.ow2.proactive.resourcemanager.gui.compact.LabelMouseListener;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;


/**
 *
 * Graphical representation of the node source.
 *
 */
public class SourceView extends View {
    // node source image
    private static Image nodeSourceImage = ImageDescriptor.createFromFile(CompactViewer.class,
            "icons/source.gif").createImage();

    public SourceView(TreeLeafElement element) {
        super(element);

        label = new Label(ResourcesCompactView.getCompactViewer().getComposite(), SWT.SHADOW_NONE);
        label.setBackground(ResourcesCompactView.getCompactViewer().getComposite().getBackground());

        label.setImage(nodeSourceImage);
        label.setToolTipText(toString());
        label.addMouseListener(new LabelMouseListener(this));

    }

    public String toString() {
        return "Node source: " + element.getName();
    }
}
