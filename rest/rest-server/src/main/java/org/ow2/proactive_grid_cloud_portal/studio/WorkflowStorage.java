package org.ow2.proactive_grid_cloud_portal.studio;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WorkflowStorage {

    private final File workflowsDir;
    private final static Logger logger = Logger.getLogger(WorkflowStorage.class);

    public WorkflowStorage(File workflowsDir) {
        this.workflowsDir = workflowsDir;
    }

    public Workflow store(Workflow workflow) {
        if (!workflowsDir.exists()) {
            logger.info("Creating dir " + workflowsDir.getAbsolutePath());
            workflowsDir.mkdirs();
        }

        long projectId = 1;
        while (new File(workflowsDir.getAbsolutePath() + "/" + projectId).exists()) {
            projectId++;
        }

        File newWorkflowFile = new File(workflowsDir.getAbsolutePath() + "/" + projectId);
        logger.info("Creating dir " + newWorkflowFile.getAbsolutePath());
        newWorkflowFile.mkdirs();

        FileUtil.writeFileContent(newWorkflowFile.getAbsolutePath() + "/name", workflow.getName());
        FileUtil.writeFileContent(newWorkflowFile.getAbsolutePath() + "/metadata", workflow.getMetadata());
        FileUtil.writeFileContent(newWorkflowFile.getAbsolutePath() + "/" + workflow.getName() + ".xml",
                workflow.getXml());

        workflow.setId(projectId);

        return workflow;
    }

    public List<Workflow> read() {
        if (!workflowsDir.exists()) {
            logger.info("Creating dir " + workflowsDir.getAbsolutePath());
            workflowsDir.mkdirs();
        }

        logger.info("Getting workflows from " + workflowsDir);
        ArrayList<Workflow> projects = new ArrayList<Workflow>();
        for (File f : workflowsDir.listFiles()) {
            if (f.isDirectory()) {
                File nameFile = new File(f.getAbsolutePath() + "/name");

                if (nameFile.exists()) {

                    Workflow wf = new Workflow();
                    wf.setId(Integer.parseInt(f.getName()));
                    wf.setName(FileUtil.getFileContent(nameFile.getAbsolutePath()));

                    File xmlFile = new File(f.getAbsolutePath() + "/" + wf.getName() + ".xml");
                    if (xmlFile.exists()) {
                        wf.setXml(FileUtil.getFileContent(xmlFile.getAbsolutePath()));
                    }
                    File metadataFile = new File(f.getAbsolutePath() + "/metadata");
                    if (metadataFile.exists()) {
                        wf.setMetadata(FileUtil.getFileContent(metadataFile.getAbsolutePath()));
                    }

                    projects.add(wf);
                }
            }
        }

        logger.info(projects.size() + " workflows found");
        return projects;
    }

    public Workflow update(String workflowId, Workflow workflow) {
        File wfDir = new File(workflowsDir, workflowId);

        String name = workflow.getName();

        String oldJobName = FileUtil.getFileContent(wfDir + "/name");
        if (name != null && !name.equals(oldJobName)) {
            // new job name
            logger.info("Updating job name from " + oldJobName + " to " + name);
            FileUtil.writeFileContent(wfDir + "/name", name);
            try {
                FileUtils.forceDelete(new File(wfDir + "/" + oldJobName + ".xml"));
            } catch (IOException e) {
                logger.warn("Cannot remove old job file", e);
            }
        }

        FileUtil.writeFileContent(wfDir + "/metadata", workflow.getMetadata());
        FileUtil.writeFileContent(wfDir + "/" + name + ".xml", workflow.getXml());

        return workflow;
    }

    public void delete(String workflowId) throws IOException {
        File f = new File(workflowsDir, workflowId);
        logger.info("Deleting file/directory " + f.getAbsolutePath());
        FileUtils.forceDelete(f);
    }

}
