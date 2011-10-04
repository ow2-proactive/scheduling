package org.ow2.proactive.scheduler.gui.actions;

import javax.management.ObjectName;

import org.eclipse.jface.action.Action;


public abstract class JMXAbstractAction extends Action {

    /** The name of the runtime data MBean */
    protected ObjectName mBeanName;

    public JMXAbstractAction() {
        JMXActionsManager.getInstance().addAction(this);

    }

}
