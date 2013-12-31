/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package unitTests;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.Selector;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastFileSelector;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.tests.ProActiveTest;
import junit.framework.Assert;
import org.junit.Test;


public class TestAntFileSelector extends ProActiveTest {

    private static URL DSroot = TestAntFileSelector.class.getResource("/unitTests/" +
        TestAntFileSelector.class.getSimpleName() + ".class");

    private static String DSrootString;

    @Test
    public void run() throws Throwable {
        File parent = new File(DSroot.toURI()).getParentFile();
        DSrootString = parent.getAbsolutePath() + "/../functionaltests/";

        log(DSrootString);
        //start DS server
        FileSystemServerDeployer deployer = new FileSystemServerDeployer(DSrootString, true);
        final String URL = deployer.getVFSRootURL();
        //start DS naming service
        NamingServiceDeployer namingServiceDeployer = new NamingServiceDeployer(true);
        String namingServiceURL = namingServiceDeployer.getNamingServiceURL();
        NamingService namingService = NamingService.createNamingServiceStub(namingServiceURL);
        //create and add predefined spaces
        Set<SpaceInstanceInfo> predefinedSpaces = new HashSet<SpaceInstanceInfo>();
        InputOutputSpaceConfiguration isc = InputOutputSpaceConfiguration.createInputSpaceConfiguration(URL,
                null, null, PADataSpaces.DEFAULT_IN_OUT_NAME);
        predefinedSpaces.add(new SpaceInstanceInfo(12, isc));
        namingService.registerApplication(12, predefinedSpaces);
        //create node, start active object and configure node
        Node n = NodeFactory.createLocalNode("node" + ((int) (Math.random() * 10000)), true, null, null);
        TestAntFileSelector tafs = PAActiveObject.newActive(TestAntFileSelector.class, new Object[] {}, n);
        tafs.configure();
        //configure node application
        DataSpacesNodes.configureApplication(n, 12, namingServiceURL);

        //go go go!
        ArrayList<String> results = tafs.test();
        int nbFound = 0;

        log("check include 1");
        File rootFile = new File(DSrootString);
        String[] checkReg = rootFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches("T.*[.]class");
            }
        });
        nbFound += checkReg.length;
        for (String s : checkReg) {
            Assert.assertTrue(contains(results, s));
        }

        log("check include 2");
        rootFile = new File(DSrootString + "/nodesource");
        checkReg = rootFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(".*[.]xml");
            }
        });
        nbFound += checkReg.length;
        for (String s : checkReg) {
            Assert.assertTrue(contains(results, s));
        }

        log("check include 3");
        rootFile = new File(DSrootString);
        checkReg = recursiveList(rootFile, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(".*[.]ini");
            }
        });
        nbFound += checkReg.length;
        for (String s : checkReg) {
            Assert.assertTrue(contains(results, s));
        }

        log("check include 4");
        rootFile = new File(DSrootString + "/executables");
        checkReg = rootFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(".*[.]sh");
            }
        });
        nbFound += checkReg.length;
        for (String s : checkReg) {
            Assert.assertTrue(contains(results, s));
        }

        Assert.assertEquals(results.size(), nbFound);
    }

    private String[] recursiveList(File root, FilenameFilter filter) {
        List<String> list = new ArrayList<String>();
        for (File f : root.listFiles()) {
            if (f.isDirectory()) {
                traverse(f, filter, list);
            }
        }
        return list.toArray(new String[] {});
    }

    private void traverse(File root, FilenameFilter filter, List<String> list) {
        for (File f : root.listFiles()) {
            if (f.isDirectory()) {
                traverse(f, filter, list);
            }
        }
        Collections.addAll(list, root.list(filter));
    }

    private boolean contains(List<String> tab, String match) {
        for (String s : tab) {
            if (s.substring(s.lastIndexOf("/") + 1).equals(match)) {
                return true;
            }
        }
        return false;
    }

    public TestAntFileSelector() {
    }

    public void configure() throws Exception {
        String scratchDir = System.getProperty("java.io.tmpdir");
        final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration((String) null, scratchDir);
        DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()),
                scratchConf);
    }

    public ArrayList<String> test() throws Exception {
        FastFileSelector ant = new FastFileSelector();
        ant.setIncludes(new String[] { "T*.class", "nodesource/*.xml", "**/*.ini", "executables/*.sh" });
        ant.setCaseSensitive(true);

        ArrayList<DataSpacesFileObject> results = new ArrayList<DataSpacesFileObject>();
        Selector.findFiles(PADataSpaces.resolveDefaultInput(), ant, true, results);

        log("RESULTS : size = " + results.size());
        ArrayList<String> strings = new ArrayList<String>();
        for (DataSpacesFileObject dataSpacesFileObject : results) {
            log(dataSpacesFileObject.getVirtualURI());
            strings.add(dataSpacesFileObject.getVirtualURI());
        }

        return strings;
    }

    private void log(String s) {
        System.out.println("----------- " + s);
    }

}
