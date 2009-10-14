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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.ow2.proactive.resourcemanager.gui.Activator;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeElementType;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;


public class TreeLabelProvider extends ColumnLabelProvider {

    public Image getImage(Object obj) {
        switch (((TreeLeafElement) obj).getType()) {
            case HOST:
                return Activator.getImageDescriptor("icons/host.gif").createImage();
            case NODE:
                switch (((Node) obj).getState()) {
                    case DOWN:
                        return Activator.getImageDescriptor("icons/down.gif").createImage();
                    case FREE:
                        return Activator.getImageDescriptor("icons/free.gif").createImage();
                    case BUSY:
                        return Activator.getImageDescriptor("icons/busy.gif").createImage();
                    case TO_BE_RELEASED:
                        return Activator.getImageDescriptor("icons/to_release.gif").createImage();
                }
                break;
            case SOURCE:
                return Activator.getImageDescriptor("icons/source.gif").createImage();
            case VIRTUAL_MACHINE:
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
        return null;
    }

    public String getText(Object element) {
        return element.toString();
    }

    public int getToolTipDisplayDelayTime(Object object) {
        return 800;
    }

    public int getToolTipTimeDisplayed(Object object) {
        return 3000;
    }

    public Point getToolTipShift(Object object) {
        return new Point(5, 5);
    }

    public boolean useNativeToolTip(Object object) {
        return false;
    }

    public String getToolTipText(Object obj) {
        if (((TreeLeafElement) obj).getType() == TreeElementType.NODE) {
            switch (((Node) obj).getState()) {
                case DOWN:
                    return "Node is down or unreachable";
                case FREE:
                    return "Node is ready to perform tasks";
                case BUSY:
                    return "Node is currently performing a task";
                case TO_BE_RELEASED:
                    return "Node is busy and will be removed at task's end";
            }
        }
        return null;
    }
}