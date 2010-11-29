package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import org.objectweb.proactive.core.UniqueID;


/**
 * MatSciEngineConfigBase
 *
 * @author The ProActive Team
 */
public abstract class MatSciEngineConfigBase implements MatSciEngineConfig {

    protected static MatSciEngineConfigBase currentConf = null;
    protected static MatSciEngineConfigBase lastConf = null;

    public static void setCurrentConfiguration(MatSciEngineConfigBase conf) {
        lastConf = currentConf;
        currentConf = conf;
    }

    public static boolean hasChangedConf() {
        return (lastConf != null) && (!lastConf.equals(currentConf));
    }

    public static MatSciEngineConfigBase getCurrentConfiguration() {
        return currentConf;
    }

    public static String getNodeName() throws Exception {

        return UniqueID.getCurrentVMID().toString().replace('-', '_').replace(':', '_');
        //return PAActiveObject.getNode().getVMInformation().getName().replace('-', '_')+"_"+ PAActiveObject.getNode().getNodeInformation().getName().replace('-', '_');
    }

}
