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
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VNObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;
import org.objectweb.proactive.ic2d.jobmonitoring.util.JobMonitoringTreeUtil;


/**
 * This class represents the controller part of the HostObject model.
 * @author Mich&egrave;le Reynier, Jean-Michael Legait and vbodnart
 *
 */
public class HostTreeEditPart extends JobMonitoringTreeEditPart<HostObject> {
    public static final Image HOST_IMAGE = new Image(Display.getCurrent(),
            HostTreeEditPart.class.getResourceAsStream("host_icon.png"));

    //
    // -- CONSTRUCTOR ------------------------------------------------
    //

    /**
     * The contructor of this controller part.
     * @param model The instance HostObject model associated to this controller
     */
    public HostTreeEditPart(HostObject model) {
        super(model);
    }

    /**
     * @see java.util.Observer#update(Observable, Object)
     */
    public final void update(final Observable o, final Object arg) {
        if (arg.getClass() != MVCNotification.class) {
            return;
        }
        final MVCNotification notif = (MVCNotification) arg;
        final MVCNotificationTag mvcNotificationTag = notif.getMVCNotification();
        final Object data = notif.getData();
        switch (mvcNotificationTag) {
        case STATE_CHANGED:
            if (data == State.NOT_MONITORED) {
                this.deactivate();
                return;
            }
            break;
        case HOST_OBJECT_UPDATED_OSNAME_AND_VERSON:
            if (data.getClass() == String.class) {
                setWidgetText((String) data);
            }
            break;
        default:
        } //switch

        // Asynchronous refresh
        getViewer().getControl().getDisplay().asyncExec(this);
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getImage()
     */
    @Override
    protected final Image getImage() {
        return HOST_IMAGE;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected final List<RuntimeObject> getModelChildren() {
        return JobMonitoringTreeUtil.getHostChildren(getCastedModel(),
            (VNObject) getParent().getModel());
    }
}
