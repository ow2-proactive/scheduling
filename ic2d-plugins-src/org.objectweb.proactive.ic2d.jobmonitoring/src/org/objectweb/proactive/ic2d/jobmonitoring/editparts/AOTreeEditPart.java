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
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.AOEditPart;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


/**
 * This class represents the controller part of the ActiveObject model.
 * @author The ProActive Team
 *
 */
public class AOTreeEditPart extends JobMonitoringTreeEditPart<ActiveObject> {
    public static final Image AO_IMAGE = new Image(Display.getCurrent(), AOTreeEditPart.class
            .getResourceAsStream("ao_icon.png"));

    /**
     * The contructor of this controller part.
     * @param model The instance ActiveObject model associated to this controller
     */
    public AOTreeEditPart(final ActiveObject model) {
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
        if ((mvcNotificationTag == MVCNotificationTag.STATE_CHANGED) && (data == State.NOT_MONITORED)) {
            deactivate();
            return;
        }
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getImage()
     */
    @Override
    protected final Image getImage() {
        return AO_IMAGE;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected final List<AbstractData> getModelChildren() {
        return AOEditPart.emptyList;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getText()
     */
    @Override
    protected final String getText() {
        return getCastedModel().getName() + "(" + getCastedModel().getJobId() + ")";
    }
}
