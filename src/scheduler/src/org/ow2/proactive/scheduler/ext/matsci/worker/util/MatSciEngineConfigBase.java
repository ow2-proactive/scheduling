/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
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
