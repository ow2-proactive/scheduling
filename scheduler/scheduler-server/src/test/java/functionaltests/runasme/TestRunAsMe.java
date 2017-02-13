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
package functionaltests.runasme;

import static org.junit.Assume.assumeNotNull;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;


/**
 * @author ActiveEon Team
 * @since 05/01/2017
 */
public class TestRunAsMe extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    public static final String RUNASME_USERNAME_PROPNAME = "runasme.user";

    public static final String RUNASME_PASSWORD_PROPNAME = "runasme.pwd";

    protected static String username;

    protected static String password;

    protected static void setupUser() {
        username = System.getProperty(RUNASME_USERNAME_PROPNAME);
        assumeNotNull(username);
        password = System.getProperty(RUNASME_PASSWORD_PROPNAME);
        assumeNotNull(password);
    }
}
