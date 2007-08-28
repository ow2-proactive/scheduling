package org.objectweb.proactive.ic2d.timit.editparts.tree;

import java.beans.PropertyChangeListener;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.objectweb.proactive.ic2d.timit.data.AbstractObject;


public abstract class AbstractTimerTreeEditPart extends AbstractTreeEditPart
    implements PropertyChangeListener {
    public void activate() {
        super.activate();
        ((AbstractObject) getModel()).addPropertyChangeListener(this);
    }

    public void deactivate() {
        ((AbstractObject) getModel()).removePropertyChangeListener(this);
        super.deactivate();
    }
}
