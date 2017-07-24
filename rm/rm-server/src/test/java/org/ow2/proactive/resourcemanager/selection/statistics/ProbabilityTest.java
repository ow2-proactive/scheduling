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
package org.ow2.proactive.resourcemanager.selection.statistics;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


/**
 * @author ActiveEon Team
 * @since 29/06/2017
 */
public class ProbabilityTest {

    @Test
    public void testDynamicity() throws InterruptedException {
        PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.updateProperty("1000");
        try {
            Probability probability = new Probability(Probability.defaultValue());
            double lastValue = probability.value();
            for (int i = 0; i < 100; i++) {
                probability.increase();
                Assert.assertTrue(probability.value() > lastValue);
                lastValue = probability.value();
            }
            probability.decrease();
            Assert.assertTrue(probability.value() == 0);

            // wait for the dynamicity to expire
            Thread.sleep(1500);
            Assert.assertTrue(probability.value() > 0);
            Assert.assertTrue(probability.value() < lastValue);
        } finally {
            PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.updateProperty("300000");
        }
    }

    @Test
    public void testNoDynamicity() {
        PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.updateProperty("0");
        try {
            Probability probability = new Probability(Probability.defaultValue());
            double lastValue = probability.value();
            for (int i = 0; i < 100; i++) {
                probability.increase();
                Assert.assertTrue(probability.value() > lastValue);
                lastValue = probability.value();
            }
            for (int i = 0; i < 100; i++) {
                probability.decrease();
                Assert.assertTrue(probability.value() < lastValue);
                lastValue = probability.value();
            }
            Assert.assertTrue(probability.value() == Probability.defaultValue());
        } finally {
            PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.updateProperty("300000");
        }
    }
}
