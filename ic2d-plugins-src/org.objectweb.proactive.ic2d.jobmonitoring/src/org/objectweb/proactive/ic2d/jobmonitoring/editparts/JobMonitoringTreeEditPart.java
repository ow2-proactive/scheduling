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

import java.util.Observer;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.widgets.Tree;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;


/**
 * This abstract class represents a controller part of the JobMonitoring plugin.<p>
 * The generic parameter <code>T</code> represents a model associated to this controller (see MVC pattern).
 * @author The ProActive Team
 *
 */
public abstract class JobMonitoringTreeEditPart<T extends AbstractData> extends AbstractTreeEditPart
        implements Observer, Runnable {
    //
    // -- CONSTRUCTOR ------------------------------------------------
    //
    public JobMonitoringTreeEditPart(T model) {
        super(model);
    }

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        this.refresh();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            this.getCastedModel().addObserver(this);
        }
        super.activate();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            this.getCastedModel().deleteObserver(this);
        }
        super.deactivate();
    }

    //
    // -- PROTECTED METHODS -------------------------------------------
    //

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getText()
     */
    @Override
    protected String getText() {
        return this.getCastedModel().getName();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        if (getWidget() instanceof Tree) {
            return;
        }
        setWidgetImage(getImage());
        setWidgetText(getText());
    }

    /**
     * Returns the casted model associated to this controller.
     * @return The the casted model
     */
    @SuppressWarnings("unchecked")
    protected final T getCastedModel() {
        return (T) super.getModel();
    }
}
