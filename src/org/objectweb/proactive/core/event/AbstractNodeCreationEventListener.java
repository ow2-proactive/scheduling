/*
 * Created on 29 juil. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;


/**
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractNodeCreationEventListener
    implements NodeCreationEventListener {
    public AbstractNodeCreationEventListener(VirtualNode vn) throws ProActiveException {
        if (!vn.isLookup()) {
            ((VirtualNodeImpl) vn).addNodeCreationEventListener(this);
        }else{
            throw new ProActiveException("Cannot instantiate a listener on a VirtualNodeLookup");
        }
    }
}
