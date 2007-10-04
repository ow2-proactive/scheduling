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
import java.util.Observer;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;


/**
 * @author Jean-Michael Legait and Michèle Reynier
 *
 */
public abstract class JobMonitoringTreeEditPart extends AbstractTreeEditPart
    implements Observer {
    //
    // -- CONSTRUCTOR ------------------------------------------------
    //
    public JobMonitoringTreeEditPart(AbstractData model) {
        super(model);
    }

    //
    // -- PUBLIC METHODS ----------------------------------------------
    //

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg) {
        Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    //				refreshChildren();
                    //				refreshVisuals();
                    refresh();
                }
            });
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            ((AbstractData) getModel()).addObserver(this);
        }
        super.activate();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            ((AbstractData) getModel()).deleteObserver(this);
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
        return getCastedModel().getName();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#getImage()
     */
    @Override
    protected Image getImage() {
        return new Image(Display.getCurrent(),
            this.getClass()
                .getResourceAsStream(getCastedModel().getType().toLowerCase() +
                "_icon.png"));
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        //		System.out.println("JobMonitoringTreeEditPart.refreshVisuals() "+
        //				getCastedModel().getFullName()+" "+getWidget().getClass().getName());
        if (getWidget() instanceof Tree) {
            return;
        }
        //		TreeItem item = (TreeItem)getWidget();
        //		if (image != null)
        //			image.setBackground(item.getParent().getBackground());
        setWidgetImage(getImage());
        setWidgetText(getText());
        //		((TreeItem)getWidget()).getParent().redraw();
    }

    protected void refreshChildren() {
        //		System.out.println("JobMonitoringTreeEditPart.refreshChildren() "+getCastedModel().getFullName());
        super.refreshChildren();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List getModelChildren() {
        return getCastedModel().getMonitoredChildrenAsList();
    }

    //
    // -- PRIVATE METHODS -------------------------------------------
    //
    private AbstractData getCastedModel() {
        return (AbstractData) getModel();
    }
}
