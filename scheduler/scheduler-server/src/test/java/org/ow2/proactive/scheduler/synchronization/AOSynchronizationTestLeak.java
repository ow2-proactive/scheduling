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
package org.ow2.proactive.scheduler.synchronization;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.reflection.ClassInfo;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.tests.ProActiveTestClean;


/**
 * @author ActiveEon Team
 * @since 14/05/2021
 */
public class AOSynchronizationTestLeak extends ProActiveTestClean {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    private static final int NB_RUNS = 2000;

    private File tempFolder;

    private AOSynchronization synchronization;

    @Before
    public void init() throws IOException {
        tempFolder = folder.newFolder();
        synchronization = new AOSynchronization(tempFolder.getAbsolutePath());
    }

    /**
     * Run a groovy closure many times and check that there is no leak in org.codehaus.groovy.reflection.ClassInfo
     * @throws CompilationException
     */
    @Test
    public void testGroovyClassInfoLeak() throws CompilationException {

        for (int i = 0; i < NB_RUNS; i++) {
            synchronization.evaluateClosure("true", Boolean.class);
        }
        int classFullSize = ClassInfo.fullSize();
        System.out.println("ClassInfo loaded classes = " + classFullSize);
        // The number of loaded classes should not increase linearly with NB_RUNS
        Assert.assertThat(classFullSize, Matchers.lessThan(NB_RUNS));
    }

    @After
    public void cleanUp() {
        synchronization.close();
    }

}
