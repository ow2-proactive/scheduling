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

import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.rules.Timeout;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;


/**
 * We need this class only to increase timeout rule for performance tests.
 */
@SuppressWarnings("squid:S2187")
public class BaseRecoveryTest extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    public static final String SUCCESS = "SUCCESS";

    public static final String FAILURE = "FAILURE";

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue() * 10,
                                             TimeUnit.MILLISECONDS);

    public static String makeCSVString(Object... strings) {
        StringBuilder builder = new StringBuilder();
        builder.append(strings[0].toString());
        for (int i = 1; i < strings.length; ++i) {
            builder.append(',');
            builder.append(strings[i].toString());
        }
        return builder.toString();
    }
}
