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
package org.objectweb.proactive.ic2d.dgc.editparts;

import java.util.Observable;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.dgc.data.ObjectGraph;
import org.objectweb.proactive.ic2d.dgc.figures.DgcAOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.AOEditPart;


public class DgcAOEditPart extends AOEditPart {
    public DgcAOEditPart(ActiveObject model) {
        super(model);
    }

    @Override
    protected IFigure createFigure() {
        return new DgcAOFigure(getCastedModel().getName());
    }

    @Override
    protected Color getArrowColor() {
        return new Color(Display.getCurrent(), 0, 0, 255);
    }

    @Override
    public void update(Observable o, Object arg) {
        ObjectGraph.addObject((ActiveObject) o);
        ActiveObject model = this.getCastedModel();
        ((DgcAOFigure) super.getCastedFigure()).updateDgcState(model);
        super.update(o, arg);
    }
}
