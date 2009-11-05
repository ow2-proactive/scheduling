/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.ow2.proactive.resourcemanager.gui.compact.Filter;
import org.ow2.proactive.resourcemanager.gui.compact.LabelMouseListener;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;


/**
 *
 * Graphical representation of the JVM.
 *
 */
public class JVMView extends View {
    // JVM image
    private static Image jvmImage = PlatformUI.getWorkbench().getSharedImages().getImage(
            ISharedImages.IMG_OBJ_ELEMENT);

    public JVMView(TreeLeafElement element, Filter filter) {
        super(element);

        if (filter.showJVMs) {
            label = new Label(ResourcesCompactView.getCompactViewer().getComposite(), SWT.SHADOW_NONE);
            label.setBackground(ResourcesCompactView.getCompactViewer().getComposite().getBackground());

            label.setImage(jvmImage);
            label.setToolTipText(toString());
            label.addMouseListener(new LabelMouseListener(this));
        }
    }

    @Override
    public String toString() {
        return "JVM: " + element.getName();
    }
}
