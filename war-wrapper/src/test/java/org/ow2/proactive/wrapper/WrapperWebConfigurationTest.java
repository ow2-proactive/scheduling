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
package org.ow2.proactive.wrapper;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * Created by root on 05/04/17.
 */
public class WrapperWebConfigurationTest {

    private String paHost;

    private WrapperWebConfiguration wrapperWebConfiguration;

    @Before
    public void setUp() throws Exception {

        wrapperWebConfiguration = new WrapperWebConfiguration();
        paHost = ProActiveInet.getInstance().getHostname();

    }

    @Test
    public void testgetStartedUrl() {

        if (PASchedulerProperties.SCHEDULER_HOME.isSet()) {

            String expected = "http://" + paHost + ":9080";

            String actual = wrapperWebConfiguration.getStartedUrl();

            assertThat(actual).isEqualTo(expected);
        }
    }
}
