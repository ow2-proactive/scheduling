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

import java.util.List;

import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VNObject;
import org.objectweb.proactive.ic2d.jobmonitoring.util.JobMonitoringTreeUtil;


/**
 * @author Mich&egrave;le Reynier and Jean-Michael Legait
 *
 */
public class HostTreeEditPart extends JobMonitoringTreeEditPart {
    //
    // -- CONSTRUCTOR ------------------------------------------------
    //

    /**
     * @param model
     */
    public HostTreeEditPart(AbstractData model) {
        super(model);
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List getModelChildren() {
        return JobMonitoringTreeUtil.getHostChildren(getCastedModel(),
            (VNObject) getParent().getModel());
    }

    //
    // -- PRIVATE METHODS -------------------------------------------
    //
    private HostObject getCastedModel() {
        return (HostObject) getModel();
    }
}
