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
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VNObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jobmonitoring.util.JobMonitoringTreeUtil;


/**
 * This class represents the controller part of the RuntimeObject model.
 * @author Mich&egrave;le Reynier, Jean-Michael Legait and vbodnart
 *
 */
public class JVMTreeEditPart extends JobMonitoringTreeEditPart<RuntimeObject> {
    public static final Image JVM_IMAGE = new Image(Display.getCurrent(),
            JVMTreeEditPart.class.getResourceAsStream("jvm_icon.png"));

    //
    // -- CONSTRUCTOR ------------------------------------------------
    //

    /**
     * The contructor of this controller part.
     * @param model The instance RuntimeObject model associated to this controller
     */
    public JVMTreeEditPart(final RuntimeObject model) {
        super(model);
    }

    /**
     * @see java.util.Observer#update(Observable, Object)
     */
    @Override
    public final void update(final Observable o, final Object arg) {
        if (arg.getClass() != MVCNotification.class) {
            return;
        }
        final MVCNotification notif = (MVCNotification) arg;
        final MVCNotificationTag mvcNotificationTag = notif.getMVCNotification();
        switch (mvcNotificationTag) {
        case RUNTIME_OBJECT_RUNTIME_KILLED:
            this.deactivate();
            return;
        default:
        }
        // Asynchronous refresh
        getViewer().getControl().getDisplay().syncExec(this);
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getImage()
     */
    @Override
    protected final Image getImage() {
        return JVM_IMAGE;
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected final List<NodeObject> getModelChildren() {
        List<NodeObject> res;
        if ((getParent() != null) && (getParent().getParent() != null) &&
                (getParent().getParent().getModel() != null)) {
            res = JobMonitoringTreeUtil.getJVMChildren(getCastedModel(),
                    (VNObject) getParent().getParent().getModel());
        } else {
            // Return an empty list to avoid NullPointerException
            res = new java.util.ArrayList<NodeObject>(0);
        }
        return res;
    }
}
