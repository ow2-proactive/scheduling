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

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.tests.ProActiveTest;
import functionaltests.utils.TestRM;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;


public class MultipleRMTBase extends ProActiveTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    protected File config1;

    protected File config2;

    @Before
    public void initConfigs() throws Exception {
        /*
         * Create two copies of default RM test configurations, and
         * then change the path to the directory used by the database.
         * Two resource managers should use two different directories.
         */
        File configurationFile = new File(TestRM.functionalTestRMProperties.toURI());

        Properties config = new Properties();

        try (FileInputStream fis = new FileInputStream(configurationFile)) {
            config.load(fis);
        }

        String hibernateConfigFile =
                config.getProperty(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getKey());

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

        writeStringToFile(hibernateConfig1, hibernateConfig.replace(defaultJdbcUrl,
                "jdbc:hsqldb:file:" + db1.getAbsolutePath() + "/rm1;create=true;hsqldb.tx=mvcc;hsqldb.write_delay=false"));
        writeStringToFile(hibernateConfig2, hibernateConfig.replace(defaultJdbcUrl,
                "jdbc:hsqldb:file:" + db2.getAbsolutePath() + "/rm2;create=true;hsqldb.tx=mvcc;hsqldb.write_delay=false"));

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
