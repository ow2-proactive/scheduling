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
package org.objectweb.proactive.ic2d.timit.editparts.duration;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.objectweb.proactive.ic2d.timit.actions.DecreaseSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.ExpandSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.FitSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.IncreaseSizeAction;
import org.objectweb.proactive.ic2d.timit.data.duration.DurationChartObject;
import org.objectweb.proactive.ic2d.timit.data.duration.SequenceObject;
import org.objectweb.proactive.ic2d.timit.views.TimeLineView;


public class DurationEditPartFactory implements EditPartFactory {
    private TimeLineView view;

    public DurationEditPartFactory(TimeLineView view) {
        this.view = view;
    }

    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof DurationChartObject) {
            DurationChartEditPart d = new DurationChartEditPart((DurationChartObject) model);
            IncreaseSizeAction inc = view.getInc();
            inc.setTarget(d);
            DecreaseSizeAction dec = view.getDec();
            dec.setTarget(d);
            FitSizeAction fit = view.getFit();
            fit.setTarget(d);
            ExpandSizeAction exp = view.getExp();
            exp.setTarget(d);
            return d;
        } else if (model instanceof SequenceObject) {
            return new SequenceEditPart((SequenceObject) model);
        } else {
            return null;
        }
    }
}
