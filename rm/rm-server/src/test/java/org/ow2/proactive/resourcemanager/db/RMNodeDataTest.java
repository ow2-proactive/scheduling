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
package org.ow2.proactive.resourcemanager.db;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;


/**
 * @author ActiveEon Team
 * @since 02/10/17
 */
public class RMNodeDataTest {

    private static final Client OWNER = new Client();

    private static final Client PROVIDER = OWNER;

    private static final String JVM_NAME = "jvmName";

    private static final String NAME = "name";

    private static final String URL = "url";

    private static final String HOSTNAME = "hostname";

    @Test
    public void testEqualNodeDataHaveSameHashcode() {
        RMNodeData nodeData1 = new RMNodeData(NAME,
                                              URL,
                                              OWNER,
                                              PROVIDER,
                                              null,
                                              NodeState.DEPLOYING,
                                              1L,
                                              HOSTNAME,
                                              new String[] {},
                                              JVM_NAME);
        RMNodeData nodeData2 = new RMNodeData(NAME,
                                              URL,
                                              OWNER,
                                              PROVIDER,
                                              null,
                                              NodeState.DEPLOYING,
                                              1L,
                                              HOSTNAME,
                                              new String[] {},
                                              JVM_NAME);
        assertThat(nodeData1).isEqualTo(nodeData2);
        assertThat(nodeData1.equals(nodeData2)).isTrue();
        assertThat(nodeData1.hashCode()).isEqualTo(nodeData2.hashCode());
    }

    @Test
    public void testIfHashCodeIsDifferentEqualsIsFalse() {
        RMNodeData nodeData1 = new RMNodeData(NAME + "1",
                                              URL + "1",
                                              OWNER,
                                              PROVIDER,
                                              null,
                                              NodeState.DEPLOYING,
                                              1L,
                                              HOSTNAME,
                                              new String[] {},
                                              JVM_NAME);
        RMNodeData nodeData2 = new RMNodeData(NAME + "2",
                                              URL + "2",
                                              OWNER,
                                              PROVIDER,
                                              null,
                                              NodeState.DEPLOYING,
                                              1L,
                                              HOSTNAME,
                                              new String[] {},
                                              JVM_NAME);
        assertThat(nodeData1).isNotEqualTo(nodeData2);
        assertThat(nodeData1.equals(nodeData2)).isFalse();
        assertThat(nodeData1.hashCode()).isNotEqualTo(nodeData2.hashCode());
    }
}
