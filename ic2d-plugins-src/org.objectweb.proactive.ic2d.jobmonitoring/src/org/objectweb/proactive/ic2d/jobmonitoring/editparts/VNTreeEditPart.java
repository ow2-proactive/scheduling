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
import java.util.Observable;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VirtualNodeObject;
import org.objectweb.proactive.ic2d.jobmonitoring.util.JobMonitoringTreeUtil;


/**
 * This class represents the controller part of the VNObject model.
 * @author Mich&egrave;le Reynier, Jean-Michael Legait and vbodnart
 *
 */
public class VNTreeEditPart extends JobMonitoringTreeEditPart<VirtualNodeObject> {
    public static final Image VN_IMAGE = new Image(Display.getCurrent(),
            VNTreeEditPart.class.getResourceAsStream("vn_icon.png"));

    //
    // -- CONSTRUCTOR ------------------------------------------------
    //

    /**
     * The contructor of this controller part.
     * @param model The instance VNObject model associated to this controller
     */
    public VNTreeEditPart(final VirtualNodeObject model) {
        super(model);
    }

    /**
     * @see java.util.Observer#update(Observable, Object)
     */
    public final void update(final Observable o, final Object arg) {
        // No need to do a full refresh just refresh the children
        this.refreshChildren();
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getImage()
     */
    @Override
    protected final Image getImage() {
        return VN_IMAGE;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected final List<HostObject> getModelChildren() {
        return JobMonitoringTreeUtil.getVNChildren(getCastedModel());
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getText()
     */
    @Override
    protected final String getText() {
        return getCastedModel().getName() + " (" + getCastedModel().getJobID() +
        ")";
    }
}
