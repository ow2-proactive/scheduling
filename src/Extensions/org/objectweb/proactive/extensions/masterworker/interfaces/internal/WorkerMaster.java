package org.objectweb.proactive.extensions.masterworker.interfaces.internal;

import java.io.Serializable;


/**
 * The Master seen from the Worker point of view
 * 
 * @author The ProActive Team
 */
public interface WorkerMaster extends TaskProvider<Serializable>, MasterIntern {
}
