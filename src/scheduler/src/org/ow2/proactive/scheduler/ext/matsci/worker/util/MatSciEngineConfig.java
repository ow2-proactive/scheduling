package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import java.io.Serializable;


/**
 * MatSciEngineConfig
 *
 * @author The ProActive Team
 */
public interface MatSciEngineConfig extends Serializable {

    String getVersion();

    String getFullCommand();

}
