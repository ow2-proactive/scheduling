package functionaltests.dataspaces;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.task.data.TaskProActiveDataspaces;

/**
 * NodeDataspace depicts the dataspace configuration on node side.
 *
 * @author ActiveEon Team
 * @see TaskProActiveDataspacesIntegrationTest
 */
public class NodeDataspace implements EndActive, Serializable {

    private DataSpaceNodeConfigurationAgent dataSpaceNodeConfigurationAgent;

    private TaskProActiveDataspaces taskProActiveDataspaces;

    public NodeDataspace() {
    }

    public void init(TaskId taskId, String namingServiceUrl) throws Exception {
        dataSpaceNodeConfigurationAgent =
                (DataSpaceNodeConfigurationAgent) PAActiveObject.newActive(
                        DataSpaceNodeConfigurationAgent.class.getName(), null, PAActiveObject.getNode());

        if (!dataSpaceNodeConfigurationAgent.configureNode()) {
            throw new IllegalStateException("Could not configure node for dataspaces");
        }

        NamingService namingServiceStub = NamingService.createNamingServiceStub(namingServiceUrl);

        taskProActiveDataspaces = new TaskProActiveDataspaces(taskId, namingServiceStub);

        System.out.println("Node scratch folder is " + getScratchFolder());
    }

    @ImmediateService
    public void copyInputDataToScratch(List<InputSelector> inputSelectors) throws FileSystemException, InterruptedException {
        taskProActiveDataspaces.copyInputDataToScratch(inputSelectors);
    }

    @ImmediateService
    public void copyScratchDataToOutput(List<OutputSelector> outputSelectors) throws FileSystemException {
        taskProActiveDataspaces.copyScratchDataToOutput(outputSelectors);
    }

    public File getGlobalSpace() {
        return new File(taskProActiveDataspaces.getGlobalURI());
    }

    public File getUserSpace() {
        return new File(taskProActiveDataspaces.getUserURI());
    }

    public File getInputSpace() {
        return new File(taskProActiveDataspaces.getInputURI());
    }

    public File getOutputSpace() {
        return new File(taskProActiveDataspaces.getOutputURI());
    }

    public File getScratchFolder() {
        return taskProActiveDataspaces.getScratchFolder();
    }

    @Override
    public void endActivity(Body body) {
        PAFuture.waitFor(dataSpaceNodeConfigurationAgent.closeNodeConfiguration());

        taskProActiveDataspaces.close();
    }

    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        NodeDataspace nodeDataspace =
                PAActiveObject.newActive(NodeDataspace.class, new Object[0]);

        System.out.println(PAActiveObject.getUrl(nodeDataspace));
    }

}