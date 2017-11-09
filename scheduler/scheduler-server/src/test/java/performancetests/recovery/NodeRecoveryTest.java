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
package performancetests.recovery;

import org.apache.jmeter.protocol.java.sampler.JUnitSampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * @Author Gleb Mykhailenko
 */
public class NodeRecoveryTest {

    public NodeRecoveryTest(String s) {
        System.out.println("We have just receiverd " + s);
    }

    @Test
    public void testNodeRecovery() throws Exception {
        JUnitSampler sampler = new JUnitSampler();
        final JMeterVariables variables = sampler.getThreadContext().getVariables();
        JUnitSampler jUnitSampler = (JUnitSampler) sampler.getThreadContext().getCurrentSampler();
        Integer nodesNumber  = Integer.valueOf(variables.get("nodesNumber"));

    }
}
