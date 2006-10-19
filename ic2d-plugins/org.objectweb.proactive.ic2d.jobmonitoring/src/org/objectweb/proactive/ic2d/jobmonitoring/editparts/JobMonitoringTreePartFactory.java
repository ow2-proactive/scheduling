/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jobmonitoring.editparts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.monitoring.data.HostObject;
import org.objectweb.proactive.ic2d.monitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.monitoring.data.VMObject;
import org.objectweb.proactive.ic2d.monitoring.data.VNObject;
import org.objectweb.proactive.ic2d.monitoring.data.WorldObject;


public class JobMonitoringTreePartFactory implements EditPartFactory {

	
	/**
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object model) {
		if(model instanceof WorldObject)
			return new WorldTreeEditPart((AbstractDataObject)model);
		if(model instanceof VNObject)
			return new VNTreeEditPart((AbstractDataObject)model);
		if(model instanceof HostObject)
			return new HostTreeEditPart((AbstractDataObject)model);
		if(model instanceof VMObject)
			return new JVMTreeEditPart((AbstractDataObject)model);
		if(model instanceof NodeObject)
			return new NodeTreeEditPart((AbstractDataObject)model);
		if(model instanceof AOObject)
			return new AOTreeEditPart((AbstractDataObject)model);
		return null;
	}

}
