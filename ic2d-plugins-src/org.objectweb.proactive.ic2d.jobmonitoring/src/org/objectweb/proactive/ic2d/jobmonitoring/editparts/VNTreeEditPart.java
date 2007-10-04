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

import java.util.List;
import java.util.Observable;

import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.VNObject;
import org.objectweb.proactive.ic2d.jobmonitoring.util.JobMonitoringTreeUtil;


/**
 * @author Mich&egrave;le Reynier and Jean-Michael Legait
 *
 */
public class VNTreeEditPart extends JobMonitoringTreeEditPart {
    //
    // -- CONSTRUCTOR ------------------------------------------------
    //

    /**
     * @param model
     */
    public VNTreeEditPart(AbstractData model) {
        super(model);
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg) {
        final Observable obs = o;
        final Object obj = arg;
        Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    ((WorldTreeEditPart) getParent()).update(obs, obj);
                    //				((JobMonitoringTreeEditPart)getParent()).refreshVisuals();
                    //				refreshChildren();
                    //				refreshVisuals();
                    refresh();
                }
            });
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List getModelChildren() {
        return JobMonitoringTreeUtil.getVNChildren(getCastedModel());
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getText()
     */
    @Override
    protected String getText() {
        return getCastedModel().getName() + " (" + getCastedModel().getJobID() +
        ")";
    }

    //
    // -- PRIVATE METHODS -------------------------------------------
    //
    private VNObject getCastedModel() {
        return (VNObject) getModel();
    }
}
