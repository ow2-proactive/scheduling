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
package unitTests.calcium.system;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import static org.junit.Assert.*;
import org.junit.Test;


public class TestHashSum {
    @Test
    public void TestSha1Sum() throws Exception {
        String shakespeare = "If music be the food of love, play on\n"
            + "Give me excess of it, that, surfeiting,\n" + "The appetite may sicken, and so die.";

        File testfile = new File(System.getProperty("java.io.tmpdir"), "test-calcium-hashsum-shakespeare");

        if (testfile.exists()) {
            testfile.delete();
        }

        assertFalse(testfile.exists());

        PrintWriter out = new PrintWriter(new FileWriter(testfile));
        out.println(shakespeare);
        out.close();

        assertTrue(testfile.exists());

        String hexStringHash = org.objectweb.proactive.extensions.calcium.system.HashSum.hashsum(testfile,
                "SHA-1");
        assertTrue(hexStringHash.equals("404d69b17da9a666fe8db79eec8483d94a43babc"));

        testfile.delete();

        assertFalse(testfile.exists());
    }
}
