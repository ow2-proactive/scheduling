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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.ow2.proactive.resourcemanager.common.NodeState;


/**
 * Contains constants definitions and utility methods.
 *
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>vbodnart
 */
public final class Internal {

    /**
     * Each icon image is represented by a unique ID that can be used to find the corresponding
     * image from the registry.
     */
    public static final String IMG_ADDNODE = "add_node.gif";
    public static final String IMG_BUSY = "busy.gif";
    public static final String IMG_COLLAPSEALL = "collapseall.gif";
    public static final String IMG_CONNECT = "connect.gif";
    public static final String IMG_CREATESOURCE = "create_source.gif";
    public static final String IMG_DISCONNECT = "disconnect.gif";
    public static final String IMG_DOWN = "down.gif";
    public static final String IMG_EXPANDALL = "expandall.gif";
    public static final String IMG_FREE = "free.gif";
    public static final String IMG_HOST = "host.gif";
    public static final String IMG_REMOVENODE = "remove_node.gif";
    public static final String IMG_REMOVESOURCE = "remove_source.gif";
    public static final String IMG_RMSHUTDOWN = "rm_shutdown.gif";
    public static final String IMG_SOURCE = "source.gif";
    public static final String IMG_TORELEASE = "to_release.gif";

    /**
     * Given a node state returns the corresponding image taken from the registry of this plugin.
     * <p>
     * In case of unknown state returns {@link ISharedImages.IMG_OBJS_ERROR_TSK}.
     * @param nodeState the state of a node
     * @return the corresponding image
     */
    public static Image getImageByNodeState(final NodeState nodeState) {
        switch (nodeState) {
            case DOWN:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_DOWN);
            case FREE:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_FREE);
            case BUSY:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_BUSY);
            case TO_BE_RELEASED:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_TORELEASE);
            default:
                return Activator.getDefault().getImageRegistry().get(ISharedImages.IMG_OBJS_ERROR_TSK);
        }
    }

}