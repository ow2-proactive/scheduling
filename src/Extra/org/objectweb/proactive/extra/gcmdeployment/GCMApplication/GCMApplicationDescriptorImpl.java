package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.Executor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentResources;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import org.objectweb.proactive.extra.gcmdeployment.core.DeploymentNode;
import org.objectweb.proactive.extra.gcmdeployment.core.DeploymentTree;
import org.objectweb.proactive.extra.gcmdeployment.core.VMNodes;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeInternal;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.Group;


public class GCMApplicationDescriptorImpl implements GCMApplicationDescriptor {

    /** The descriptor file */
    private File gadFile = null;

    /** A parser dedicated to this GCM Application descriptor */
    private GCMApplicationParser gadParser = null;

    /** All the Virtual Nodes defined in this application */
    private Map<String, VirtualNodeInternal> virtualNodes = null;
    private DeploymentTree deploymentTree;
    private Map<String, GCMDeploymentDescriptor> selectedDeploymentDesc;

    public GCMApplicationDescriptorImpl(String filename)
        throws IllegalArgumentException {
        this(new File(filename));
    }

    public GCMApplicationDescriptorImpl(File file)
        throws IllegalArgumentException {
        gadFile = Helpers.checkDescriptorFileExist(file);
        try {
            gadParser = new GCMApplicationParserImpl(gadFile);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        // 1. Load all GCM Deployment Descriptor
        Map<String, GCMDeploymentDescriptor> gdds;
        gdds = gadParser.getResourceProviders();

        // 2. Get Virtual Node and Command Builder
        virtualNodes = gadParser.getVirtualNodes();

        CommandBuilder commandBuilder = gadParser.getCommandBuilder();

        // 3. Select the GCM Deployment Descriptors to be used
        selectedDeploymentDesc = selectGCMD(virtualNodes, gdds);

        // 4. Build the runtime tree
        buildDeploymentTree();

        // 5. Start the deployment
        for (GCMDeploymentDescriptor gdd : selectedDeploymentDesc.values()) {
            gdd.start(commandBuilder);
        }

        /**
         * If this GCMA describes a distributed application. The Runtime has
         * been started and will populate Virtual Nodes etc. We let the user
         * code, interact with its Middleware.
         *
         * if a "script" is described. The command has been started on each
         * machine/VM/core and we can safely return
         */
    }

    protected void buildDeploymentTree() {
        deploymentTree = new DeploymentTree();

        // make root node from local JVM
        DeploymentNode rootNode = new DeploymentNode();

        rootNode.setDeploymentDescriptorPath(""); // no deployment descriptor here

        try {
            rootNode.setApplicationDescriptorPath(gadFile.getCanonicalPath());
        } catch (IOException e) {
            rootNode.setApplicationDescriptorPath("");
        }

        ProActiveRuntimeImpl proActiveRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
        VMNodes vmNodes = new VMNodes(proActiveRuntime.getVMInformation());

        //                    vmNodes.addNode(<something>); - TODO cmathieu
        rootNode.addVMNodes(vmNodes);

        deploymentTree.setRootNode(rootNode);

        // Build leaf nodes
        for (GCMDeploymentDescriptor gdd : selectedDeploymentDesc.values()) {
            DeploymentNode deploymentNode = new DeploymentNode();

            GCMDeploymentDescriptorImpl gddi = (GCMDeploymentDescriptorImpl) gdd;

            GCMDeploymentResources resources = gddi.getResources();

            deploymentNode.setDeploymentDescriptorPath(gddi.getParser()
                                                           .getDescriptorFilePath());

            deploymentTree.addNode(deploymentNode, rootNode);

            for (Group group : resources.getGroups()) {
                DeploymentNode leafNode = new DeploymentNode();
                leafNode.setDeploymentDescriptorPath(deploymentNode.getDeploymentDescriptorPath());
                // TODO ...
            }
        }
    }

    /**
     * Select the GCM Deployment descriptor to be used
     *
     * A Virtual Node is a consumer, and a GCM Deployment Descriptor a producer.
     * We try to fulfill the consumers needs with as few as possible producer.
     *
     * @param vns
     *            Virtual Nodes asking for some resources
     * @param gdds
     *            GCM Deployment Descriptor providing some resources
     * @return A
     */
    static private Map<String, GCMDeploymentDescriptor> selectGCMD(
        Map<String, VirtualNodeInternal> vns,
        Map<String, GCMDeploymentDescriptor> gdds) {
        // TODO: Implement this method
        return gdds;
    }

    private long getRequiredCapacity() {
        int cap = 0;
        for (VirtualNodeInternal vn : virtualNodes.values()) {
            cap += vn.getRequiredCapacity();
        }

        return cap;
    }

    public VirtualNode getVirtualNode(String vnName)
        throws IllegalArgumentException {
        VirtualNode ret = virtualNodes.get(vnName);
        if (ret == null) {
            throw new IllegalArgumentException("Virtual Node " + vnName +
                " does not exist");
        }
        return ret;
    }

    public Map<String, ?extends VirtualNode> getVirtualNodes() {
        return virtualNodes;
    }

    public void kill() {
        // TODO Auto-generated method stub
    }

    @SuppressWarnings("unused")
    static public class TestGCMApplicationDescriptorImpl {
    }

    public boolean allProcessExited() {
        // TODO Auto-generated method stub
        return false;
    }

    public void awaitTermination() {
        try {
            Executor.getExecutor().awaitTermination();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
