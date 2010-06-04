package functionaltests;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
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


public class TestAntFileSelector {

    private static String DSroot = TestAntFileSelector.class.getResource(
            "/functionaltests/" + TestAntFileSelector.class.getSimpleName() + ".class").getPath();

    @Test
    public void run() throws Throwable {
        DSroot = new File(DSroot).getParent();
        log(DSroot);
        //start DS server
        FileSystemServerDeployer deployer = new FileSystemServerDeployer(DSroot, true);
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
        Node n = NodeFactory
                .createLocalNode("node" + ((int) (Math.random() * 10000)), true, null, null, null);
        TestAntFileSelector tafs = PAActiveObject.newActive(TestAntFileSelector.class, new Object[] {}, n);
        tafs.configure();
        //configure node application
        DataSpacesNodes.configureApplication(n, 12, namingServiceURL);

        //go go go!
        ArrayList<String> results = tafs.test();
        int nbFound = 0;

        log("check include 1");
        File rootFile = new File(DSroot);
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
        rootFile = new File(DSroot + "/nodesource");
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
        rootFile = new File(DSroot);
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
        rootFile = new File(DSroot + "/executables");
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
        final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration(null, scratchDir);
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
