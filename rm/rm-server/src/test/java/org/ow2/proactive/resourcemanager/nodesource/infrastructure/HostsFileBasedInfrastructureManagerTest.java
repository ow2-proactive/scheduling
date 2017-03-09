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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.exception.RMException;


/**
 * @author ActiveEon Team
 * @since 8 Mar 2017
 */
public class HostsFileBasedInfrastructureManagerTest {

    public int retryCounter = 0;

    private HostsFileBasedInfrastructureManager hostsFileBasedInfrastructureManager;

    @Before
    public void init() {

        this.hostsFileBasedInfrastructureManager = createTestClass();
        retryCounter = 0;

    }

    @Test
    public void testRetry() throws Exception {
        int retries = 10;
        hostsFileBasedInfrastructureManager.waitBetweenDeploymentFailures = 10;
        try {
            int nbnodes = 1;
            hostsFileBasedInfrastructureManager.startNodeImplWithRetries(InetAddress.getLocalHost(), nbnodes,
                    retries);
        } catch (Exception e) {
        }

        assertThat(retryCounter, is(retries + 1));
    }

    private HostsFileBasedInfrastructureManager createTestClass() {

        return new HostsFileBasedInfrastructureManager() {

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            protected void startNodeImpl(InetAddress host, int nbNodes, List<String> depNodeURLs)
                    throws RMException {
                retryCounter++;
                throw new RMException("RM exception");

            }

            @Override
            protected void killNodeImpl(Node node, InetAddress host) throws RMException {
            }
        };
    }

}
