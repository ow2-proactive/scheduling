package unitTests.deployment.listGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.process.ListGenerator;


public class TestListGenerator {
    final static private String validResource = "/unitTests/listGenerator/data.valid.txt";
    final static private String invalidResource = "/unitTests/listGenerator/data.invalid.txt";

    /*
    @Test
    public void singleTest() {
        ListGenerator.generateNames("");
    }
        */
    @Test
    public void testValid() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(
                    getClass().getResource(validResource).getFile()));

        while (true) {
            String question = br.readLine();
            String response = br.readLine();
            br.readLine(); // Empty line

            if (question == null) { // End of File
                break;
            }

            if (response == null) {
                throw new IllegalArgumentException(
                    "Illegal format for a data file: " + question);
            }

            Assert.assertEquals("question=\"" + question + "\"", response,
                concat(ListGenerator.generateNames(question)));
        }
    }

    @Test
    public void testInvalid() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(
                    getClass().getResource(invalidResource).getFile()));

        while (true) {
            String question = br.readLine();
            br.readLine(); // Empty line

            if (question == null) { // End of File
                break;
            }

            try {
                List<String> ret = ListGenerator.generateNames(question);
                Assert.fail("Question=" + question + "\" response=\"" +
                    concat(ret) + "\"");
            } catch (IllegalArgumentException e) {
                // An IllegalArguementException is expected
            }
        }
    }

    static private String concat(List<String> lstr) {
        String ret = "";
        for (String str : lstr)
            ret += (str + " ");

        if (ret.length() > 1) {
            ret = ret.substring(0, ret.length() - 1);
        }

        return ret;
    }
}
