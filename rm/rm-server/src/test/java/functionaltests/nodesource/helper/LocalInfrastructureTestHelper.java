/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.nodesource.helper;

import java.io.File;
import java.io.IOException;

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionaltests.utils.RMTHelper;


public class LocalInfrastructureTestHelper {

    public static byte[] getCredentialsBytes() throws IOException {
        String credentialsPath = PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString());
        return FileToBytesConverter.convertFileToByteArray(new File(credentialsPath));
    }

    public static Object[] getParameters(int numberOfNodes) {
        try {
            return new Object[] { getCredentialsBytes(), numberOfNodes, RMTHelper.DEFAULT_NODES_TIMEOUT, "" };
        } catch (IOException e) {
            RMTHelper.log("Could not get credentials to create local infrastructure: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

}
