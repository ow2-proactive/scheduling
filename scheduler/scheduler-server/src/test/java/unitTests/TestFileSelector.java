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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.tests.ProActiveTest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;


public class TestFileSelector extends ProActiveTest {

    private static String DS_ROOT;

    private TestFileSelector testFileSelector;

    @Before
    public void setUp() throws Exception {
        File parent =
                new File(TestFileSelector.class.getResource("/unitTests/" +
                    TestFileSelector.class.getSimpleName()
                        + ".class").toURI()).getParentFile();

        DS_ROOT = parent.getAbsolutePath() + "/../functionaltests/";

        // start DS server
        FileSystemServerDeployer deployer =
                new FileSystemServerDeployer(DS_ROOT, true);

        // start DS naming service
        NamingServiceDeployer namingServiceDeployer =
                new NamingServiceDeployer(true);

        NamingService namingService =
                NamingService.createNamingServiceStub(
                        namingServiceDeployer.getNamingServiceURL());

        // create and add predefined spaces
        Set<SpaceInstanceInfo> predefinedSpaces = new HashSet<>();
        InputOutputSpaceConfiguration isc =
                InputOutputSpaceConfiguration.createInputSpaceConfiguration(
                        deployer.getVFSRootURL(), null,
                        null, PADataSpaces.DEFAULT_IN_OUT_NAME);
        predefinedSpaces.add(new SpaceInstanceInfo(12, isc));
        namingService.registerApplication(12, predefinedSpaces);

        // create node, start active object and configure node
        Node node = NodeFactory.createLocalNode(UUID.randomUUID().toString(), true, null, null);

        testFileSelector = PAActiveObject.newActive(TestFileSelector.class, new Object[0], node);
        testFileSelector.configure();

        // configure node application
        DataSpacesNodes.configureApplication(node, 12, namingService);
    }

    public List<String> findFiles() throws Exception {
        FileSelector selector = new FileSelector();
        selector.addIncludes("T*.class", "nodesource/*.xml", "**/*.ini", "executables/*.sh");

        ArrayList<DataSpacesFileObject> results = new ArrayList<>();
        Utils.findFiles(PADataSpaces.resolveDefaultInput(), selector, results);

        List<String> strings = new ArrayList<>(results.size());
        for (DataSpacesFileObject dataSpacesFileObject : results) {
            strings.add(dataSpacesFileObject.getVirtualURI());
        }

        return strings;
    }

    @Test
    public void test() throws Throwable {
        List<String> results = testFileSelector.findFiles();

        int nbFound = 0;

        Scenario[] scenarios = {
                new Scenario(DS_ROOT, "T.*[.]class"),
                new Scenario(DS_ROOT + "/nodesource", ".*[.]xml"),
                new Scenario(DS_ROOT, ".*[.]ini", true),
                new Scenario(DS_ROOT + "/executables", ".*[.]sh")
        };

        for (Scenario scenario : scenarios) {
            nbFound += checkResults(scenario, results);
        }

        Assert.assertEquals(results.size(), nbFound);
    }

    private int checkResults(final Scenario scenario, List<String> results) {
        String[] filesMatching;

        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(scenario.pattern);
            }
        };

        File rootDirFile = new File(scenario.rootDir);

        if (scenario.recurse) {
            filesMatching = recursiveList(rootDirFile, filenameFilter);
        } else {
            filesMatching = rootDirFile.list(filenameFilter);
        }

        for (String fileMatching : filesMatching) {
            Assert.assertTrue(contains(results, fileMatching));
        }

        return filesMatching.length;
    }

    private String[] recursiveList(File root, FilenameFilter filter) {
        List<String> list = new ArrayList<>();
        for (File f : root.listFiles()) {
            if (f.isDirectory()) {
                traverse(f, filter, list);
            }
        }
        return list.toArray(new String[list.size()]);
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

    public TestFileSelector() {
    }

    public void configure() throws Exception {
        String scratchDir = System.getProperty("java.io.tmpdir");
        final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration((String) null,
            scratchDir);
        DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()),
                scratchConf);
    }

    private static final class Scenario {

        public final String rootDir;

        public final String pattern;

        public final boolean recurse;

        public Scenario(String rootDir, String pattern) {
            this(rootDir, pattern, false);
        }

        public Scenario(String rootDir, String pattern, boolean recurse) {
            this.pattern = pattern;
            this.rootDir = rootDir;
            this.recurse = recurse;
        }
    }

}
