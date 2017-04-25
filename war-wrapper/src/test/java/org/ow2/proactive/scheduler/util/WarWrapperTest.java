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
import static org.ow2.proactive.utils.ClasspathUtils.findSchedulerHome;

import java.io.File;
import java.io.IOException;
import java.security.KeyException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.wrapper.WrapperSingleton;


/**
 * Created by root on 06/04/17.
 */

public class WarWrapperTest {

    private static Credentials credentials;

    private static WarWrapper warWrapper;

    private static String PA_HOME = null;

    @BeforeClass
    public static void setup() throws KeyException, IOException {

        findPAHome();

        if (null != PA_HOME)
            System.setProperty(PASchedulerProperties.SCHEDULER_HOME.getKey(), PA_HOME);

        warWrapper = WrapperSingleton.getInstance();
        warWrapper.launchProactiveServer();
        credentials = warWrapper.getCredentials();
    }


    @Test
    public void testConfigureSchedulerAndRMAndPAHomes() throws Exception {

        PA_HOME = WarWrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        System.setProperty(PASchedulerProperties.SCHEDULER_HOME.getKey(), PA_HOME);

        String expected = PA_HOME;

        WrapperSingleton.getInstance().configureSchedulerAndRMAndPAHomes();

        String actual = System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey());

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void testStopRM() throws Exception {

        boolean expected = true;

        boolean actual = warWrapper.stopRM(credentials);

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void testStopScheduler() throws Exception {

        boolean expected = true;

        boolean actual = warWrapper.stopScheduler(credentials);

        assertThat(actual).isEqualTo(expected);

    }

    private static void findPAHome() {

        File currentFile = new File(WarWrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        boolean continueLoop = true;

        while (currentFile.getParentFile() != null && continueLoop) {

            for (String subDirectory: currentFile.getParentFile().list()){

                if (subDirectory.equals("config"))  {
                    PA_HOME = currentFile.getParentFile().getPath();
                    continueLoop = false;
                    break;
                }
            }

            currentFile = currentFile.getParentFile();
        }
    }
}
