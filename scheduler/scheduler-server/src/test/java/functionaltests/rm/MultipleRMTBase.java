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
package functionaltests.rm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.tests.ProActiveTest;

import functionaltests.utils.TestRM;


public class MultipleRMTBase extends ProActiveTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    protected static File config1;

    protected static File config2;

    protected static void initConfigs() throws Exception {
        /*
         * Create two copies of default RM test configurations, and
         * then change the path to the directory used by the database.
         * Two resource managers should use two different directories.
         */
        File configurationFile = new File(TestRM.FUNCTIONAL_TEST_RM_PROPERTIES.toURI());

        Properties config = new Properties();

        try (FileInputStream fis = new FileInputStream(configurationFile)) {
            config.load(fis);
        }

        String hibernateConfigFile = config.getProperty(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getKey());

        if (hibernateConfigFile == null) {
            Assert.fail("Can't find hibernate config");
        }

        hibernateConfigFile = PASchedulerProperties.getAbsolutePath(hibernateConfigFile);

        String hibernateConfig = new String(Files.readAllBytes(Paths.get(hibernateConfigFile)));

        String defaultJdbcUrl = "jdbc:hsqldb:file:build/TEST_RM_DB;create=true;hsqldb.tx=mvcc;hsqldb.write_delay=false";

        if (!hibernateConfig.contains(defaultJdbcUrl)) {
            Assert.fail("Hibernate config doesn't contain expected string");
        }

        File db1 = folder.newFolder("rm1");
        File db2 = folder.newFolder("rm2");

        Path hibernateConfig1 = folder.newFile("dbconfig1.xml").toPath();
        Path hibernateConfig2 = folder.newFile("dbconfig2.xml").toPath();

        writeStringToFile(hibernateConfig1,
                          hibernateConfig.replace(defaultJdbcUrl,
                                                  "jdbc:hsqldb:file:" + db1.getAbsolutePath() +
                                                                  "/rm1;create=true;hsqldb.tx=mvcc;hsqldb.write_delay=false"));
        writeStringToFile(hibernateConfig2,
                          hibernateConfig.replace(defaultJdbcUrl,
                                                  "jdbc:hsqldb:file:" + db2.getAbsolutePath() +
                                                                  "/rm2;create=true;hsqldb.tx=mvcc;hsqldb.write_delay=false"));

        config1 = folder.newFile("rmconfig1.txt");
        config2 = folder.newFile("rmconfig2.txt");

        config.put(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getKey(), hibernateConfig1.toString());
        config.store(Files.newOutputStream(config1.toPath()), null);

        config.put(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getKey(), hibernateConfig2.toString());
        config.store(Files.newOutputStream(config2.toPath()), null);
    }

    private static void writeStringToFile(Path file, String string) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(file, Charset.defaultCharset())) {
            bw.write(string);
        }
    }

}
