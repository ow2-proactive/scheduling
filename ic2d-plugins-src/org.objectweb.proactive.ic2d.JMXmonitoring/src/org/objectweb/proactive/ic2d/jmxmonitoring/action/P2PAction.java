package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class P2PAction extends Action {
    public static final String ENABLE_DISABLE_P2P_MONITORING = "Enable Disable P2P monitoring";

    /** The world */
    private WorldObject world;

    public P2PAction(WorldObject world) {
        super("Show P2P objects", AS_CHECK_BOX);
        this.world = world;
        this.setId(ENABLE_DISABLE_P2P_MONITORING);
        setChecked(!WorldObject.HIDE_P2PNODE_MONITORING);
        setToolTipText("Show P2P Objects");
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "p2p.gif"));
    }

    @Override
    public void run() {
        world.hideP2P(!isChecked());
    }
}
