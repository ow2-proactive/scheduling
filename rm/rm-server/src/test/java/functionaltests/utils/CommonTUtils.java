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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package functionaltests.utils;

import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


public class CommonTUtils {

    public static void cleanupRMActiveObjectRegistry() throws Exception {
        cleanupActiveObjectRegistry(PAResourceManagerProperties.RM_NODE_NAME.getValueAsString(),
                RMConstants.NAME_ACTIVE_OBJECT_RMCORE, RMConstants.NAME_ACTIVE_OBJECT_RMADMIN,
                RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION, RMConstants.NAME_ACTIVE_OBJECT_RMUSER,
                RMConstants.NAME_ACTIVE_OBJECT_RMMONITORING);
    }

    public static void cleanupActiveObjectRegistry(String... namesToRemove) throws Exception {
        try {
            RemoteObjectFactory factory = AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory();
            for (URI uri : factory.list(new URI(RMTHelper.getLocalUrl()))) {
                for (String name : namesToRemove) {
                    if (uri.getPath().endsWith(name)) {
                        factory.unregister(uri);
                        break;
                    }
                }
            }
        } catch (ProActiveException ignored) {
        }
    }

}
