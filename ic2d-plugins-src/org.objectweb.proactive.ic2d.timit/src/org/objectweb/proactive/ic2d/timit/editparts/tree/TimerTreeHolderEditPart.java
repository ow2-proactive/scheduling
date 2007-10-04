package org.objectweb.proactive.ic2d.timit.editparts.tree;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;


public class TimerTreeHolderEditPart extends AbstractTimerTreeEditPart {
    protected List getModelChildren() {
        return ((TimerTreeHolder) getModel()).getChildren();
    }

    public final void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TimerTreeHolder.P_ADD_SOURCE)) {
            refreshChildren();
        } else if (evt.getPropertyName()
                          .equals(TimerTreeHolder.P_REMOVE_SELECTED)) {
            ((TimerTreeHolder) this.getModel()).removeDummyRoot((TimerObject) evt.getNewValue());
            List<EditPart> l = this.getViewer().getSelectedEditParts();

            // In order to avoid concurrent exception create a temporary list to be filled with parts to delete
            List<EditPart> toDelete = new ArrayList<EditPart>();

            // Deactivate selected parts
            for (final EditPart e : l) {
                e.deactivate();
                toDelete.add(e);
            }

            // Remove them from the current root editpart
            for (final EditPart e : toDelete) {
                this.removeChild(e);
            }
            // Refresh children list
            this.refreshChildren();
        }
    }
}
