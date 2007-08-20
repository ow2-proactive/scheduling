package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;


public class VirtualNodeImpl implements VirtualNodeInternal {
    private long requiredCapacity;
    private String id;
    private List<GCMDeploymentDescriptor> providers;

    /** All File Transfer Block associated to this VN */
    private List<FileTransferBlock> fts;

    public VirtualNodeImpl() {
        fts = new ArrayList<FileTransferBlock>();
        providers = new ArrayList<GCMDeploymentDescriptor>();
    }

    public long getRequiredCapacity() {
        return requiredCapacity;
    }

    public void setRequiredCapacity(long requiredCapacity) {
        this.requiredCapacity = requiredCapacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<GCMDeploymentDescriptor> getProviders() {
        return providers;
    }

    public void addProvider(GCMDeploymentDescriptor provider) {
        providers.add(provider);
    }

    public void addProviders(Collection<GCMDeploymentDescriptor> providers) {
        providers.addAll(providers);
    }

    public void addFileTransfertBlock(FileTransferBlock ftb) {
        fts.add(ftb);
    }

    public String getName() {
        return id;
    }

    public void check() throws IllegalStateException {
        if (providers.size() == 0) {
            throw new IllegalStateException("providers is empty in " + this);
        }
    }

    public void checkDirectMode() throws IllegalStateException {
        // TODO Auto-generated method stub
    }
}
