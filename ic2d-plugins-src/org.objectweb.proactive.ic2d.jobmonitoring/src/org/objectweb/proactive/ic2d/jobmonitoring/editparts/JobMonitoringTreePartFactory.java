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
package org.objectweb.proactive.ic2d.jobmonitoring.editparts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VirtualNodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


/**
 * This class represents the controller factory of the JobMonitoring plugin.
 * <p>
 * This factory is used by GEF to create adequate controllers for incoming models.
 * @author Mich&egrave;le Reynier, Jean-Michael Legait and vbodnart
 *
 */
public class JobMonitoringTreePartFactory implements EditPartFactory {

    /**
     * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
     */
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof WorldObject) {
            return new WorldTreeEditPart((WorldObject) model);
        }
        if (model instanceof VirtualNodeObject) {
            return new VNTreeEditPart((VirtualNodeObject) model);
        }
        if (model instanceof HostObject) {
            return new HostTreeEditPart((HostObject) model);
        }
        if (model instanceof RuntimeObject) {
            return new JVMTreeEditPart((RuntimeObject) model);
        }
        if (model instanceof NodeObject) {
            return new NodeTreeEditPart((NodeObject) model);
        }
        if (model instanceof ActiveObject) {
            return new AOTreeEditPart((ActiveObject) model);
        }
        return null;
    }
}
