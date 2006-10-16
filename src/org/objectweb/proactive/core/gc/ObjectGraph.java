package org.objectweb.proactive.core.gc;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;


public class ObjectGraph {
    public static Collection<UniqueID> getReferenceList(UniqueID bodyID) {
        UniversalBody body = LocalBodyStore.getInstance().getLocalBody(bodyID);
        if (body == null) {
        	System.out.println("Body " + bodyID + " not found");
            return new Vector<UniqueID>();
        }
        Collection<UniversalBodyProxy> bodies = ((AbstractBody) body).getReferences();
        Collection<UniqueID> res = new Vector<UniqueID>();
        for (Iterator<UniversalBodyProxy> iter = bodies.iterator(); iter.hasNext();) {
            res.add(iter.next().getBodyID());
        }
        return res;
    }
}
