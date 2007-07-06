package org.objectweb.proactive.core.filter;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActiveInternalObject;


/**
 * A ProActiveInternalObjectFilter is a filter which doesn't accepts all non functional bodies,
 * (in other words implementing the ProActiveInternalObject interface).
 * @author ProActive Team
 */
public class ProActiveInternalObjectFilter implements Filter {
    public boolean filter(Body body) {
        return !(body.getReifiedObject() instanceof ProActiveInternalObject);
    }
}
