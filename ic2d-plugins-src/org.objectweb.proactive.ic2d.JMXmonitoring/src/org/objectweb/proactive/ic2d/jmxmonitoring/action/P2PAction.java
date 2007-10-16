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
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class P2PAction extends Action {
    public static final String ENABLE_DISABLE_P2P_MONITORING = "Enable Disable P2P monitoring";

    /** The world */
    private WorldObject world;

    public P2PAction(WorldObject world) {
        super("Show P2P objects", AS_CHECK_BOX);
        this.world = world;
        this.setId(ENABLE_DISABLE_P2P_MONITORING);
        setChecked(!WorldObject.HIDE_P2PNODE_MONITORING);
        setToolTipText("Show P2P Objects");
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "p2p.gif"));
    }

    @Override
    public void run() {
        world.hideP2P(!isChecked());
    }
}
