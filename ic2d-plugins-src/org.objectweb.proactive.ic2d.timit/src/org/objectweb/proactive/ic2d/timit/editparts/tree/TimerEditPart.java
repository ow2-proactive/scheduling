package org.objectweb.proactive.ic2d.timit.editparts.tree;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.swt.widgets.TreeItem;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.views.TimerTreeView;


public class TimerEditPart extends AbstractTimerTreeEditPart {
    protected TimerTreeView timerTreeView;

    public TimerEditPart(TimerTreeView timerTreeView) {
        this.timerTreeView = timerTreeView;
    }

    protected List<TimerObject> getModelChildren() {
        return ((TimerObject) getModel()).getChildren();
    }

    @Override
    protected void fireSelectionChanged() {
        super.fireSelectionChanged();
        TimerObject model = (TimerObject) getModel();

        // If root is selected
        if ((model.getParent() == null) && (model.getCurrentTimer() == null)) {
            // Set the target for delete tree action
            if (this.getSelected() == TimerEditPart.SELECTED_PRIMARY) {
                this.timerTreeView.getDeleteTreeAction().setTarget(model);
            } else if (this.getSelected() == TimerEditPart.SELECTED_NONE) {
                this.timerTreeView.getDeleteTreeAction().setTarget(null);
            }
        }
    }

    protected void refreshVisuals() {
        TimerObject model = (TimerObject) getModel();
        setWidgetText(model.getLabelName());
    }

    public void setExpandedChildren(boolean state) {
        List<TimerEditPart> children = getChildren();
        for (TimerEditPart t : children) {
            ((TreeItem) t.getWidget()).setExpanded(state);
            t.setExpandedChildren(state);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TimerObject.P_CHILDREN)) {
            super.refresh();
            for (TimerObject child : getModelChildren()) {
                child.firePropertyChange(TimerObject.P_CHILDREN, null, null);
            }

            // Incoming event to update the name of the current TimerObject
            // representation
        } else if (evt.getPropertyName().equals(TimerObject.P_LABEL)) {
            super.refresh();
            // Incoming event to update the selection of the TimerObject
        } else if (evt.getPropertyName().equals(TimerObject.P_SELECTION)) {
            // Reveal this edit part inside the viewer
            this.getViewer().reveal(this);
            // Select this edit part visually
            this.getViewer().select(this);
        } else if (evt.getPropertyName().equals(TimerObject.P_EXPAND_STATE)) {
            boolean newState = (Boolean) evt.getNewValue();
            ((TreeItem) this.widget).setExpanded(newState);
            this.setExpandedChildren(newState);
        }
    }
}
