package functionaltests;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.FileUtils;
import org.ow2.tests.FunctionalTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;


public class MultipleRMTBase extends FunctionalTest {

    protected File config1;

    protected File config2;

    private List<File> tempFiles = new ArrayList<File>();

    @Before
    public void initConfigs() throws Exception {
        /*
         * Create two copies of default test RM configurations, change path to the directory used by
         * Derby (two resource managers should use two different directories)
         */

        File configurationFile = new File(RMTHelper.functionalTestRMProperties.toURI());

        Properties config = new Properties();
        config.load(new FileInputStream(configurationFile));

        String hibernateConfigFile = config.getProperty(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG
                .getKey());
        if (hibernateConfigFile == null) {
            Assert.fail("Can't find hibernate config");
        }
        hibernateConfigFile = PASchedulerProperties.getAbsolutePath(hibernateConfigFile);

        String hibernateConfig = new String(FileToBytesConverter.convertFileToByteArray(new File(
            hibernateConfigFile)));
        String defaultDB = "jdbc:derby:build/TEST_RM_DB;create=true";
        if (!hibernateConfig.contains(defaultDB)) {
            Assert.fail("Hibernate config doesn't contain expected string");
        }

        File derbyDir1 = new File("RM_DB1");
        if (derbyDir1.isDirectory()) {
            FileUtils.removeDir(derbyDir1);
        }
        tempFiles.add(derbyDir1);
        File derbyDir2 = new File("RM_DB2");
        if (derbyDir2.isDirectory()) {
            FileUtils.removeDir(derbyDir2);
        }
        tempFiles.add(derbyDir2);

        File hibernateConfig1 = new File(System.getProperty("java.io.tmpdir") + File.separator +
            "dbconfig1.xml");
        tempFiles.add(hibernateConfig1);
        File hibernateConfig2 = new File(System.getProperty("java.io.tmpdir") + File.separator +
            "dbconfig2.xml");
        tempFiles.add(hibernateConfig2);

        writeStringToFile(hibernateConfig1, hibernateConfig.replace(defaultDB,
                "jdbc:derby:RM_DB1;create=true"));
        writeStringToFile(hibernateConfig2, hibernateConfig.replace(defaultDB,
                "jdbc:derby:RM_DB2;create=true"));

        config1 = new File(System.getProperty("java.io.tmpdir") + File.separator + "rmconfig1.txt");
        tempFiles.add(config1);
        config2 = new File(System.getProperty("java.io.tmpdir") + File.separator + "rmconfig2.txt");
        tempFiles.add(config2);

        config.put(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getKey(), hibernateConfig1
                .getAbsolutePath());
        config.store(new FileOutputStream(config1), null);

        config.put(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getKey(), hibernateConfig2
                .getAbsolutePath());
        config.store(new FileOutputStream(config2), null);
    }

    @After
    public void cleanup() {
        for (File tmpFile : tempFiles) {
            if (tmpFile.isDirectory()) {
                FileUtils.removeDir(tmpFile);
            } else {
                tmpFile.delete();
            }
        }
    }

    private static void writeStringToFile(File file, String string) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            out.write(string.getBytes());
        } finally {
            out.close();
        }
    }

}
