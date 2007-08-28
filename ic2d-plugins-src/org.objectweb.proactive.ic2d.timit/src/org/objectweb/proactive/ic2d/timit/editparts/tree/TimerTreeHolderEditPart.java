package org.objectweb.proactive.ic2d.timit.editparts.tree;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;


public class TimerTreeHolderEditPart extends AbstractTimerTreeEditPart {
    protected List getModelChildren() {
        return ((TimerTreeHolder) getModel()).getChildren();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TimerTreeHolder.P_ADD_SOURCE)) {
            refreshChildren();
        }
    }
}
