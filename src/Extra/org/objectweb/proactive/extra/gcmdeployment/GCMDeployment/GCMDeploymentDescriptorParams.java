package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.io.File;
import java.util.Set;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;


public class GCMDeploymentDescriptorParams {

    /** The GCM Descriptor describing the resources to be used */
    private File GCMDescriptor;

    /** The resource provider ID */
    private String id;

    /** Set of file transfer to be performed by the Resource provider */
    private Set<FileTransferBlock> ftBlocks;

    public File getGCMDescriptor() {
        return GCMDescriptor;
    }

    public String getId() {
        return id;
    }

    public Set<FileTransferBlock> getFtBlocks() {
        return ftBlocks;
    }

    public void setFtBlocks(Set<FileTransferBlock> ftBlocks) {
        this.ftBlocks = ftBlocks;
    }

    public void setGCMDescriptor(File descriptor) {
        try {
            Helpers.checkDescriptorFileExist(descriptor);
            GCMDescriptor = descriptor;
        } catch (IllegalArgumentException e) {
            GCMD_LOGGER.warn(getClass().getName() +
                ".setGCMDescriptor called with a bad descriptor", e);
        }
    }

    public void setId(String id) {
        if (id == null) {
            GCMD_LOGGER.warn(this.getClass().getName() +
                ".setId called with id==null", new Exception());
            return;
        }

        if (id.equals("")) {
            GCMD_LOGGER.warn(this.getClass().getName() +
                ".setId called with id==\"\"", new Exception());
            return;
        }

        this.id = id;
    }
}
