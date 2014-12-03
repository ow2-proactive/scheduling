package org.ow2.proactive_grid_cloud_portal.studio.storage.serializers;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive_grid_cloud_portal.studio.Workflow;
import org.ow2.proactive_grid_cloud_portal.studio.storage.Serializer;

import java.io.File;
import java.io.IOException;

public class WorkflowSerializer implements Serializer<Workflow> {
    @Override
    public Workflow serialize(File workflowDir, String id, Workflow workflow) throws IOException {
        FileUtils.forceMkdir(workflowDir);

        FileUtils.write(new File(workflowDir, "name"), workflow.getName());
        FileUtils.write(new File(workflowDir, "metadata"), workflow.getMetadata());
        FileUtils.write(new File(workflowDir, "job.xml"), workflow.getXml());

        workflow.setId(Long.parseLong(id));

        return workflow;
    }

    @Override
    public Workflow deserialize(File workflowDir, String id) throws IOException {
        String name = readFile(new File(workflowDir, "name"), id);
        String xml = readFile(new File(workflowDir, "job.xml"), id);
        String metadata = readFile(new File(workflowDir, "metadata"), id);
        return new Workflow(Long.parseLong(workflowDir.getName()), name, xml, metadata);
    }

    private String readFile(File file, String id) throws IOException {
        if (file.exists()) {
            return FileUtils.readFileToString(file);
        }
        throw new IOException(String.format("Could not find the file %s when de-serializing workflow %s", file.getAbsolutePath(), id));
    }

}
