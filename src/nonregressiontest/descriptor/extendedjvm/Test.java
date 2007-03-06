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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest.descriptor.extendedjvm;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

import testsuite.test.FunctionalTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public class Test extends FunctionalTest {
    ProActiveDescriptor descriptor;
    A a1;
    A a2;
    A a3;

    public Test() {
        super("Jvm extension in deployment descriptor",
            "Jvm extension in deployment descriptor");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        VirtualNode vn1 = descriptor.getVirtualNode("evn1");
        VirtualNode vn2 = descriptor.getVirtualNode("evn2");
        VirtualNode vn3 = descriptor.getVirtualNode("evn3");
        a1 = (A) ProActive.newActive(A.class.getName(), new Object[] {  },
                vn1.getNode());
        a2 = (A) ProActive.newActive(A.class.getName(), new Object[] {  },
                vn2.getNode());
        a3 = (A) ProActive.newActive(A.class.getName(), new Object[] {  },
                vn3.getNode());
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        String fileName = null;

        if ("ibis".equals(System.getProperty("proactive.communication.protocol"))) {
            fileName = "JVMExtensionIbis";
        } else {
            fileName = "JVMExtension";
        }
        String oldFilePath = getClass()
                                 .getResource("/nonregressiontest/descriptor/extendedjvm/" +
                fileName + ".xml").getPath();
        String newFilePath = oldFilePath.replaceFirst(fileName + ".xml",
                fileName + "-tmp.xml");

        // if tests are run from the /compile directory : getParent for root directory 
        File userDir = new File(System.getProperty("user.dir"));
        String proactiveDir;
        if (userDir.getName().equals("compile")) {
            proactiveDir = userDir.getParent();
        } else {
            proactiveDir = userDir.getPath();
        }
        searchAndReplace(oldFilePath, newFilePath, "proactive.home",
            proactiveDir);
        descriptor = ProActive.getProactiveDescriptor(getClass()
                                                          .getResource("/nonregressiontest/descriptor/extendedjvm/" +
                    fileName + "-tmp.xml").getPath());
        descriptor.activateMappings();
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        descriptor.killall(false);
    }

    public boolean postConditions() throws Exception {
        if ((a2.getTiti() != null) || (a2.getTata() == null) ||
                (a3.getTiti() == null) || (a3.getToto() == null)) {
            return false;
        }
        if ((a2.getClassPath().indexOf("ProActive.jar") < 0) ||
                (a2.getPolicy().indexOf("test") < 0)) {
            return false;
        }
        return true;
    }

    private void searchAndReplace(String oldFilePath, String newFilePath,
        String oldString, String newString) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                        oldFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(
                        newFilePath));
            while (true) {
                String oldLine = reader.readLine();
                if (oldLine == null) {
                    break;
                }
                String newLine = oldLine.replace(oldString, newString);
                writer.write(newLine);
                writer.newLine();
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.initTest();
            test.action();
            System.out.println(test.postConditions());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
