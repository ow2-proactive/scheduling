package org.ow2.proactive_grid_cloud_portal.studio.storage;

import org.ow2.proactive_grid_cloud_portal.studio.Script;
import org.ow2.proactive_grid_cloud_portal.studio.Workflow;
import org.ow2.proactive_grid_cloud_portal.studio.storage.generators.NameAsIdGenerator;
import org.ow2.proactive_grid_cloud_portal.studio.storage.generators.SmallestAvailableIdGenerator;
import org.ow2.proactive_grid_cloud_portal.studio.storage.serializers.ScriptSerializer;
import org.ow2.proactive_grid_cloud_portal.studio.storage.serializers.WorkflowSerializer;

import java.io.File;


public class FileStorageSupport {

    private final File userWorkflowsDir;
    private final File templateWorkflowsDir;

    public FileStorageSupport(File userWorkflowsDir, File templateWorkflowsDir) {
        this.userWorkflowsDir = userWorkflowsDir;
        this.templateWorkflowsDir = templateWorkflowsDir;
    }

    public File getWorkflowsDir(String userName) {
        return new File(userWorkflowsDir, userName);
    }

    public FileStorage<Workflow> getTemplateStorage() {
        templateWorkflowsDir.mkdirs();
        return new FileStorage<>(templateWorkflowsDir, new WorkflowSerializer(),
                new SmallestAvailableIdGenerator());
    }

    public FileStorage<Workflow> getWorkflowStorage(String userName) {
        File workflowsDir = new File(getWorkflowsDir(userName), "workflows");
        workflowsDir.mkdirs();
        return new FileStorage<>(workflowsDir, new WorkflowSerializer(), new SmallestAvailableIdGenerator());
    }

    public FileStorage<Script> getScriptStorage(String userName) {
        File scriptsDir = new File(getWorkflowsDir(userName), "scripts");
        scriptsDir.mkdirs();
        return new FileStorage<>(scriptsDir, new ScriptSerializer(), new NameAsIdGenerator());

    }
}
