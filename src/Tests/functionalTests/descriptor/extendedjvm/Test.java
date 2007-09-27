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
package functionalTests.descriptor.extendedjvm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Jvm extension in deployment descriptor
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = -6030295227851414810L;
    ProActiveDescriptor descriptor;
    A a1;
    A a2;
    A a3;

    @org.junit.Test
    public void action() throws Exception {
        VirtualNode vn1 = descriptor.getVirtualNode("evn1");
        VirtualNode vn2 = descriptor.getVirtualNode("evn2");
        VirtualNode vn3 = descriptor.getVirtualNode("evn3");
        a1 = (A) ProActiveObject.newActive(A.class.getName(), new Object[] {  },
                vn1.getNode());
        a2 = (A) ProActiveObject.newActive(A.class.getName(), new Object[] {  },
                vn2.getNode());
        a3 = (A) ProActiveObject.newActive(A.class.getName(), new Object[] {  },
                vn3.getNode());

        assertTrue(a2.getTiti() == null);
        assertTrue(a2.getTata() != null);
        assertTrue(a3.getTiti() != null);
        assertTrue(a3.getToto() != null);

        assertTrue(a2.getClassPath().contains("ProActive.jar"));
        assertTrue(a2.getPolicy().contains("test"));
    }

    @Before
    public void initTest() throws Exception {
        String fileName = null;

        if ("ibis".equals(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue())) {
            fileName = "JVMExtensionIbis";
        } else {
            fileName = "JVMExtension";
        }
        String oldFilePath = getClass()
                                 .getResource("/functionalTests/descriptor/extendedjvm/" +
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
        descriptor = ProDeployment.getProactiveDescriptor(getClass()
                                                          .getResource("/functionalTests/descriptor/extendedjvm/" +
                    fileName + "-tmp.xml").getPath());
        descriptor.activateMappings();
    }

    @After
    public void endTest() throws Exception {
        descriptor.killall(false);
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
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
