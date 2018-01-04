package performancetests.verification;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.*;

public class VerifyChangeTest {

    static {
        PropertyConfigurator.configure(VerifyChangeTest.class.getResource("/performancetests/config/log4j.properties"));
    }

    private static final Logger LOGGER = Logger.getLogger(VerifyChangeTest.class);

    /**
     * When any performance metric changes more or less than
     * given threshold then this test fails
     */
    private static final Double THRESHOLD = Double.parseDouble(System.getProperty("threshold"));

    @Test
    public void changeShouldNotBeThanThreshold() throws IOException {

        final List<File> twoLastFiles = getTwoLastFiles();

        final File newFile = twoLastFiles.get(0);
        final File previousFile = twoLastFiles.get(1);

        final Map<String, Long> prediousReport = readReport(previousFile);
        final Map<String, Long> newReport = readReport(newFile);

        final Map<String, Double> compared = compareReports(prediousReport, newReport);

        boolean toNotify = false;
        String notifyReport = "Some performance metrics have been changed bigger than " + (THRESHOLD * 100) + "%:\n";
        final Iterator<Map.Entry<String, Double>> iterator = compared.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Double> entry = iterator.next();
            if(Math.abs(entry.getValue()) > THRESHOLD){
                toNotify = true;
                notifyReport += entry.getKey() + " has changed by " + String.format("%.2f", entry.getValue())  +
                        "% from " + prediousReport.get(entry.getKey()) + "ms to " + newReport.get(entry.getKey()) +  "ms;\n";
            }
        }
        if(toNotify){
            LOGGER.info(notifyReport);
        }

        assertFalse(notifyReport, toNotify);
    }

    private Map<String, Double> compareReports(Map<String, Long> previousReport, Map<String, Long> newReport) {
        assertEquals(previousReport.size(), newReport.size());
        Map<String, Double> result = new HashMap<>(previousReport.size());

        final Iterator<String> iterator = previousReport.keySet().iterator();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            assertTrue(previousReport.containsKey(key));
            assertTrue(newReport.containsKey(key));

            final Long previousValue = previousReport.get(key);
            final Long newValue = newReport.get(key);

            double change = (newValue.doubleValue() / previousValue.doubleValue() - 1) * 100;
            result.put(key, change);

        }

        assertEquals(previousReport.size(), result.size());
        return result;
    }

    private List<File> getTwoLastFiles() {
        final String pathToStorage = System.getProperty("pathToStorage");
        File folder = new File(pathToStorage);
        final File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("performance");
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File fileA, File fileB) {
                return Long.valueOf(fileA.lastModified()).compareTo(fileB.lastModified());
            }
        });

        List<File> result = new ArrayList<>(2);

        result.add(files[files.length - 1]);
        result.add(files[files.length - 2]);

        assertEquals(2, result.size());
        return result;
    }

    private Map<String, Long> readReport(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        Map<String, Long> result = new HashMap<>(8);

        String line = null;
        while ((line = br.readLine()) != null) {
            final String[] values = line.split(",");
            String key = values[1] + "-" + values[2];
            String value = values[5];
            result.put(key, Long.parseLong(value));
        }

        return result;
    }


}

