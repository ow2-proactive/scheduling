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
package org.ow2.proactive.resourcemanager.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.jmx.Chronological;
import org.rrd4j.core.RrdDb;


public class RRDSigarDataStoreTest {

    private static final int TEN_SECONDS = 10 * 1000;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @BeforeClass
    public static void configureLog4J() {
        BasicConfigurator.configure(new NullAppender());
    }

    @Test
    public void samplesAreCreated_2Beans() throws Exception {
        File rrdFile = createTempRRDFile();

        Fake fakeBean = new Fake();
        Fake fakeBean2 = new Fake();
        MBeanServer beanServer = MBeanServerFactory.createMBeanServer();
        beanServer.registerMBean(fakeBean, new ObjectName("java.lang:type=Memory"));
        beanServer.registerMBean(fakeBean2, new ObjectName("sigar:Type=Mem"));

        RRDSigarDataStore store = new RRDSigarDataStore(beanServer, rrdFile.getPath(), 4, Logger.getLogger("test"));
        RrdDb dataBase = new RrdDb(rrdFile.getPath());

        // sample 5 times every 10 seconds
        long firstSampleTime = System.currentTimeMillis();
        for (int i = 1; i <= 5; i++) {
            store.sample(dataBase, firstSampleTime + i * TEN_SECONDS);
        }

        assertEquals((firstSampleTime + 5 * TEN_SECONDS) / 1000, dataBase.getLastUpdateTime());

        assertEquals(42, dataBase.getDatasource("ValueMemory").getLastValue(), 0.001);
        assertEquals(42, dataBase.getDatasource("ValueMem").getLastValue(), 0.001);
    }

    private File createTempRRDFile() throws IOException {
        File rrdFolder = temp.newFolder();
        return new File(rrdFolder, "test.rrd");
    }

    private static class Fake implements FakeMBean {

        @Override
        public double getValue() {
            return 42;
        }

        @Override
        public void setValue(double value) {
        }
    }

    public interface FakeMBean {
        @Chronological
        double getValue();

        void setValue(double value);
    }
}
