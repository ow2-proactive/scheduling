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
package org.objectweb.proactive.ic2d.timit.editparts.tree;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeNodeObject;
import org.objectweb.proactive.ic2d.timit.views.TimerTreeView;


public class TimerEditPart extends AbstractTimerTreeEditPart {
    public static final Color HEADER_COLOR = new Color(Display.getCurrent(),
            225, 225, 255);
    protected TimerTreeView timerTreeView;
    private TreeItem widgetTreeItem;

    public TimerEditPart(final TimerTreeView timerTreeView) {
        this.timerTreeView = timerTreeView;
    }

    @Override
    protected final void createEditPolicies() {
        super.createEditPolicies();
        if (super.getWidget() instanceof TreeItem) {
            this.widgetTreeItem = (TreeItem) super.getWidget();
            this.widgetTreeItem.setText(new String[TimerTreeView.NUMBER_OF_COLUMNS]); // 5 columns
            TimerTreeNodeObject model = (TimerTreeNodeObject) getModel();

            // If is header choose different color
            if ((model.getParent() == null) &&
                    (model.getCurrentTimer() == null)) {
                this.widgetTreeItem.setBackground(HEADER_COLOR);
            }
        }
    }

    protected final void refreshVisuals() {
        TimerTreeNodeObject model = (TimerTreeNodeObject) getModel();
        if ((this.widgetTreeItem != null) && !this.widgetTreeItem.isDisposed()) {
            if ((model.getParent() == null) &&
                    (model.getCurrentTimer() == null)) {
                this.setWidgetText(model.getLabelName());
            } else {
                // 5 columns available
                this.widgetTreeItem.setText(TimerTreeView.NAME_COLUMN,
                    model.getLabelName()); // Name
                this.widgetTreeItem.setText(TimerTreeView.TIME_COLUMN,
                    model.getFormatedCurrentTotalTimeInDouble()); // Time
                this.widgetTreeItem.setText(TimerTreeView.TOTAL_PERCENT_COLUMN,
                    model.getFormatedPercentageFromTotal()); // Total %
                this.widgetTreeItem.setText(TimerTreeView.INVOCATIONS_COLUMN,
                    "" + model.getCurrentTimer().getStartStopCoupleCount()); // Invocations
                this.widgetTreeItem.setText(TimerTreeView.PARENT_PERCENT_COLUMN,
                    model.getFormatedPercentageFromParent()); // Parent %
            }
        }
    }

    protected final List<TimerTreeNodeObject> getModelChildren() {
        return ((TimerTreeNodeObject) getModel()).getChildren();
    }

    @Override
    protected final void fireSelectionChanged() {
        super.fireSelectionChanged();
        TimerTreeNodeObject model = (TimerTreeNodeObject) getModel();

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

    public final void setExpandedChildren(final boolean state) {
        List<TimerEditPart> children = getChildren();
        for (TimerEditPart t : children) {
            ((TreeItem) t.getWidget()).setExpanded(state);
            t.setExpandedChildren(state);
        }
    }

    public final void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TimerTreeNodeObject.P_CHILDREN)) {
            super.refresh();
            for (final TimerTreeNodeObject child : getModelChildren()) {
                child.firePropertyChange(TimerTreeNodeObject.P_CHILDREN, null,
                    null);
            }

            // Incoming event to update the name of the current TimerObject
            // representation
        } else if (evt.getPropertyName().equals(TimerTreeNodeObject.P_LABEL)) {
            super.refresh();
            // Incoming event to update the selection of the TimerObject
        } else if (evt.getPropertyName().equals(TimerTreeNodeObject.P_SELECTION)) {
            // Reveal this edit part inside the viewer
            this.getViewer().reveal(this);
            // Select this edit part visually
            this.getViewer().select(this);
        } else if (evt.getPropertyName()
                          .equals(TimerTreeNodeObject.P_EXPAND_STATE)) {
            boolean newState = (Boolean) evt.getNewValue();
            // A full refresh is needed
            this.removeAllAndRefreshChildren();
            ((TreeItem) this.widget).setExpanded(newState);
            this.setExpandedChildren(newState);
        } else if (evt.getPropertyName().equals(TimerTreeNodeObject.P_SORT)) {
            final boolean up = (Boolean) evt.getNewValue();
            final int sortBy = (Integer) evt.getOldValue(); // don't bother        	
            switch (sortBy) {
            case TimerTreeView.TIME_COLUMN:
                ((TimerTreeNodeObject) getModel()).sortChildrenByTime(up);
                break;
            case TimerTreeView.TOTAL_PERCENT_COLUMN:
                ((TimerTreeNodeObject) getModel()).sortChildrenByTotalPercent(up);
                break;
            case TimerTreeView.INVOCATIONS_COLUMN:
                ((TimerTreeNodeObject) getModel()).sortChildrenByInvocations(up);
                break;
            case TimerTreeView.PARENT_PERCENT_COLUMN:
                ((TimerTreeNodeObject) getModel()).sortChildrenByParentPercent(up);
                break;
            default:
                return;
            }
            for (TimerTreeNodeObject child : getModelChildren()) {
                child.firePropertyChange(TimerTreeNodeObject.P_SORT,
                    evt.getOldValue(), evt.getNewValue());
            }
            // A full refresh is needed
            this.removeAllAndRefreshChildren();
        }
    }

    private final void removeAllAndRefreshChildren() {
        // In order to avoid concurrent exception create a temporary list to be filled with parts to delete
        List<EditPart> toDelete = new ArrayList<EditPart>(super.getChildren()
                                                               .size());

        // Deactivate selected parts
        for (final EditPart e : (List<EditPart>) super.getChildren()) {
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
