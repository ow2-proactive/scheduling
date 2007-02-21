package org.objectweb.proactive.core.gc;

import java.util.Collection;
import java.util.Vector;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;

/**
 * Used by the IC2D DGC plugin
 */
public class ObjectGraph {
    public static Collection<UniqueID> getReferenceList(UniqueID bodyID) {
        UniversalBody body = LocalBodyStore.getInstance().getLocalBody(bodyID);
        if (body == null) {
        	System.out.println("Body for object graph not found");
            return new Vector<UniqueID>();
        }

        return ((AbstractBody) body).getReferences();
    }
}
