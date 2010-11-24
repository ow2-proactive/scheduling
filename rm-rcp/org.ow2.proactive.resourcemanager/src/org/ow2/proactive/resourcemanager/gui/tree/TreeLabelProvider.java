/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.gui.Internal;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.DeployingNode;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeElementType;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;


public class TreeLabelProvider extends ColumnLabelProvider {

    @Override
    public Image getImage(Object obj) {
        switch (((TreeLeafElement) obj).getType()) {
            case HOST:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_HOST);
            case NODE:
                return Internal.getImageByNodeState(((Node) obj).getState());
            case PENDING_NODE:
                return Internal.getImageByNodeState(((DeployingNode) obj).getState());
            case SOURCE:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_SOURCE);
            case VIRTUAL_MACHINE:
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        return element.toString();
    }

    @Override
    public int getToolTipDisplayDelayTime(Object object) {
        return 800;
    }

    @Override
    public int getToolTipTimeDisplayed(Object object) {
        return 3000;
    }

    @Override
    public Point getToolTipShift(Object object) {
        return new Point(5, 5);
    }

    @Override
    public boolean useNativeToolTip(Object object) {
        return false;
    }

    @Override
    public String getToolTipText(Object obj) {
        TreeElementType type = ((TreeLeafElement) obj).getType();
        if (type == TreeElementType.NODE || type == TreeElementType.PENDING_NODE) {
            switch (((Node) obj).getState()) {
                case CONFIGURING:
                    return "Node is configuring";
                case DOWN:
                    return "Node is down or unreachable";
                case FREE:
                    return "Node is ready to perform tasks";
                case BUSY:
                    return "Node is currently performing a task";
                case TO_BE_REMOVED:
                    return "Node is busy and will be removed at task's end";
                case DEPLOYING:
                    return "Node deployment has been triggered";
                case LOST:
                    return "Node deployment has failed";
            }
        }
        return null;
    }
}