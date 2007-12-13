/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package unitTests.deployment.listGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.process.ListGenerator;


public class TestListGenerator {
    final static private String validResource = TestListGenerator.class.getResource("data.valid.txt")
            .getFile();
    final static private String invalidResource = TestListGenerator.class.getResource("data.invalid.txt")
            .getFile();

    /*
    @Test
    public void singleTest() {
        ListGenerator.generateNames("");
    }
     */
    @Test
    public void testValid() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(validResource));

        while (true) {
            String question = br.readLine();
            String response = br.readLine();
            br.readLine(); // Empty line

            if (question == null) { // End of File
                break;
            }

            if (response == null) {
                throw new IllegalArgumentException("Illegal format for a data file: " + question);
            }

            Assert.assertEquals("question=\"" + question + "\"", response, concat(ListGenerator
                    .generateNames(question)));
        }
    }

    @Test
    public void testInvalid() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(invalidResource));

        while (true) {
            String question = br.readLine();
            br.readLine(); // Empty line

            if (question == null) { // End of File
                break;
            }

            try {
                List<String> ret = ListGenerator.generateNames(question);
                Assert.fail("Question=" + question + "\" response=\"" + concat(ret) + "\"");
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
