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
package org.ow2.proactive.scheduler.util;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.IOException;
import java.security.KeyException;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class WarWrapperTest {

    private static final String DEFAULT_HOME = ".";

    private static Credentials credentials;

    private static WarWrapper warWrapper;

    private static String PA_HOME = DEFAULT_HOME;

    @BeforeClass
    public static void setup() throws KeyException, IOException {

        findPAHome();

        if (PA_HOME != DEFAULT_HOME) {
            System.setProperty(PASchedulerProperties.SCHEDULER_HOME.getKey(), PA_HOME);

            warWrapper = WarWrapper.INSTANCE;
            warWrapper.launchProactive();
            credentials = warWrapper.getCredentials();
        }
    }

    @Test
    public void testStopRM() throws Exception {

        if (PA_HOME != DEFAULT_HOME) {

            boolean expected = true;

            boolean actual = warWrapper.stopRM(credentials);

            assertThat(actual).isEqualTo(expected);

        } else {
            System.out.println("Method 'StopRM' is not tested because PA_HOME is not correctly set");
        }
    }

    @Test
    public void testStopScheduler() throws Exception {

        if (PA_HOME != DEFAULT_HOME) {

            boolean expected = true;

            boolean actual = warWrapper.stopScheduler(credentials);

            assertThat(actual).isEqualTo(expected);

        } else {
            System.out.println("Method 'StopScheduler' is not tested because PA_HOME is not correctly set");
        }

    }

    private static void findPAHome() {

        File currentFile = new File(WarWrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        while (!Objects.equals(currentFile, null) && PA_HOME == DEFAULT_HOME) {

            if (!Objects.equals(currentFile.getParentFile(), null)) {

                for (String subDirectory : currentFile.getParentFile().list()) {

                    if (subDirectory.equals("config")) {

                        if (!Objects.equals(currentFile.getParentFile().getPath(), null)) {
                            PA_HOME = currentFile.getParentFile().getPath();
                        }
                        break;
                    }
                }
            }

            currentFile = currentFile.getParentFile();
        }
    }

    @AfterClass
    public static void cleanup() {

        System.clearProperty(PASchedulerProperties.SCHEDULER_HOME.getKey());

    }
}
