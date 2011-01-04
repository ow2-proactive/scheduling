/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.compact.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;


/**
 *
 * Graphical representation of resource in compact matrix view. Resources are stored as a tree
 * which is corresponds to the model of resource manager. Most of elements are represented
 * by the label which is defined in subclass.
 *
 */
public class View {

    // parent view
    protected View parent;
    // child views
    protected List<View> childs = new ArrayList<View>();
    // element from resource manager model
    protected TreeLeafElement element;
    // position in the matrix
    protected int position;
    // visual representation
    protected Label label;
    // selection status
    protected boolean isSelected = false;

    /**
     * Creates new view.
     * @see ViewFractory
     */
    public View(TreeLeafElement element) {
        this.element = element;
    }

    /**
     * Returns a parent view.
     */
    public View getParent() {
        return parent;
    }

    /**
     * Sets the parent view.
     */
    public void setParent(View parent) {
        this.parent = parent;
    }

    /**
     * Returns the underlying element.
     */
    public TreeLeafElement getElement() {
        return element;
    }

    /**
     * Sets the underlying element.
     */
    public void setElement(TreeLeafElement element) {
        this.element = element;
    }

    /**
     * Returns view's children.
     */
    public List<View> getChilds() {
        return childs;
    }

    /**
     * Recursively finds all node views.
     */
    public List<NodeView> getAllNodeViews() {
        List<NodeView> nodeViews = new LinkedList<NodeView>();

        recursivelyAdd(getChilds(), nodeViews);
        return nodeViews;
    }

    /**
     * Recursively finds all node views.
     */
    private void recursivelyAdd(List<View> childs, List<NodeView> nodeViews) {
        for (View view : childs) {
            if (view instanceof NodeView) {
                nodeViews.add((NodeView) view);
            } else {
                recursivelyAdd(view.getChilds(), nodeViews);
            }
        }
    }

    /**
     * Disposes the view and its children.
     */
    public void dispose() {
        position = 0;

        if (label != null) {
            label.dispose();
        }

        for (View view : childs) {
            view.dispose();
        }
        childs.clear();
    }

    /**
     * Finds the view corresponded the specified element.
     */
    public View findView(TreeLeafElement elem) {
        if (elem == element) {
            return this;
        } else {

            for (View child : getChilds()) {
                View v = child.findView(elem);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Updated the state of the view.
     */
    public void update() {
        for (View v : childs) {
            v.update();
        }
    }

    /**
     * Returns the graphical representation.
     */
    public Label getLabel() {
        return label;
    }

    /**
     * Sets the graphical representation.
     */
    public void setLabel(Label label) {
        this.label = label;
    }

    /**
     * Returns position of the view.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets position of the view.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Selects the view and its children.
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;

        if (label != null) {
            if (isSelected) {
                label.setBackground(getLabel().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
            } else {
                label.setBackground(ResourcesCompactView.getCompactViewer().getComposite().getBackground());
            }
        }
    }

    /**
     * Returns selected status.
     */
    public boolean isSelected() {
        return isSelected;
    }
}
