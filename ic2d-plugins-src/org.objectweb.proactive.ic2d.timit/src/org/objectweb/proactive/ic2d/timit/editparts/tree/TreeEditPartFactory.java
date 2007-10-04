package org.objectweb.proactive.ic2d.timit.editparts.tree;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.views.TimerTreeView;


public class TreeEditPartFactory implements EditPartFactory {
    private TimerTreeView timerTreeView;

    public TreeEditPartFactory(final TimerTreeView timerTreeView) {
        this.timerTreeView = timerTreeView;
    }

    public final EditPart createEditPart(final EditPart context,
        final Object model) {
        EditPart part = null;
        if (model instanceof TimerTreeHolder) {
            part = new TimerTreeHolderEditPart();
        } else if (model instanceof TimerObject) {
            part = new TimerEditPart(timerTreeView);
        }
        if (part != null) {
            part.setModel(model);
        }
        return part;
    }
}
