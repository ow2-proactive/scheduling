package org.objectweb.proactive.ic2d.gui.jobmonitor;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntimeAdapter;
import org.objectweb.proactive.ic2d.gui.IC2DGUIController;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataAssociation;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredAO;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredHost;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJVM;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJob;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredNode;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredVN;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.DefaultListModel;


public class NodeExploration implements JobMonitorConstants {
    private static final String PA_JVM = "PA_JVM";
    private int maxDepth;
    private DataAssociation asso;
    private DefaultListModel skippedObjects;
    private Map aos;
    private Set visitedVM;
    private Map runtimes;
    private IC2DGUIController controller;

    public NodeExploration(DataAssociation asso,
        DefaultListModel skippedObjects, IC2DGUIController controller) {
        this.maxDepth = 10;
        this.asso = asso;
        this.skippedObjects = skippedObjects;
        this.aos = new HashMap();
        this.runtimes = new HashMap();
        this.controller = controller;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        if (maxDepth > 0) {
            this.maxDepth = maxDepth;
        }
    }

    private void log(Throwable e) {
        controller.log(e, false);
    }

    /* url : "//host/object" */
    private ProActiveRuntime resolveURL(String url) {
        try {
            StringTokenizer tokenizer = new StringTokenizer(url, "/");
            String host = tokenizer.nextToken();
            String name = tokenizer.nextToken();

            Registry registry = LocateRegistry.getRegistry(host);
            RemoteProActiveRuntime r = (RemoteProActiveRuntime) registry.lookup(name);
            return new RemoteProActiveRuntimeAdapter(r);
        } catch (Exception e) {
            log(e);
            return null;
        }
    }

    private ProActiveRuntime urlToRuntime(String url) {
        ProActiveRuntime rt = (ProActiveRuntime) runtimes.get(url);
        if (rt != null) {
            return rt;
        }

        rt = resolveURL(url);
        if (rt != null) {
            runtimes.put(url, rt);
        }

        return rt;
    }

    private List getKnownRuntimes(ProActiveRuntime from) {
        List known;
        String[] parents;

        try {
            ProActiveRuntime[] registered = from.getProActiveRuntimes();
            known = new LinkedList(Arrays.asList(registered));
            parents = from.getParents();
        } catch (ProActiveException e) {
            log(e);
            return new LinkedList();
        }

        for (int i = 0; i < parents.length; i++) {
            ProActiveRuntime rt = urlToRuntime(parents[i]);
            if (rt != null) {
                known.add(urlToRuntime(parents[i]));
            }
        }

        return known;
    }

    public void exploreHost(String hostname, int port) {
        Registry registry;
        String[] list;
        MonitoredHost hostObject = new MonitoredHost(hostname);

        if (skippedObjects.contains(hostObject)) {
            return;
        }

        try {
            registry = LocateRegistry.getRegistry(hostname, port);
            list = registry.list();
        } catch (Exception e) {
            log(e);
            return;
        }

        for (int idx = 0; idx < list.length; ++idx) {
            String id = list[idx];
            if (id.indexOf(PA_JVM) != -1) {
                ProActiveRuntime part;

                try {
                    RemoteProActiveRuntime r = (RemoteProActiveRuntime) registry.lookup(id);
                    part = new RemoteProActiveRuntimeAdapter(r);
                    handleProActiveRuntime(part, 1);
                } catch (Exception e) {
                    log(e);
                    continue;
                }
            }
        }
    }

    private void handleProActiveRuntime(ProActiveRuntime pr, int depth) {
        if (pr instanceof RemoteProActiveRuntime &&
                !(pr instanceof RemoteProActiveRuntimeAdapter)) {
            try {
                pr = new RemoteProActiveRuntimeAdapter((RemoteProActiveRuntime) pr);
            } catch (ProActiveException e) {
                log(e);
                return;
            }
        }

        VMInformation infos = pr.getVMInformation();
        String vmName = infos.getName();

        MonitoredJVM jvmObject = new MonitoredJVM(infos.getInetAddress()
                                                       .getCanonicalHostName(),
                vmName, depth);

        if (visitedVM.contains(vmName) || skippedObjects.contains(jvmObject)) {
            return;
        }

        String jobID = pr.getJobID();
        MonitoredJob jobObject = new MonitoredJob(jobID);
        if (skippedObjects.contains(jobObject)) {
            return;
        }

        String hostname = pr.getVMInformation().getInetAddress()
                            .getCanonicalHostName();
        MonitoredHost hostObject = new MonitoredHost(hostname);
        if (skippedObjects.contains(hostObject)) {
            return;
        }

        visitedVM.add(vmName);

        try {
            String[] nodes = pr.getLocalNodeNames();
            for (int i = 0; i < nodes.length; ++i) {
                String nodeName = nodes[i];
                handleNode(pr, jvmObject, vmName, nodeName);
            }
        } catch (ProActiveException e) {
            log(e);
            return;
        }

        asso.addChild(hostObject, jvmObject);
        asso.addChild(jobObject, jvmObject);

        if (depth < maxDepth) {
            List known = getKnownRuntimes(pr);
            Iterator iter = known.iterator();
            while (iter.hasNext())
                handleProActiveRuntime((ProActiveRuntime) iter.next(), depth +
                    1);
        }
    }

    private void handleNode(ProActiveRuntime pr, MonitoredJVM jvmObject,
        String vmName, String nodeName) {
        try {
            MonitoredNode nodeObject = new MonitoredNode(nodeName);
            if (skippedObjects.contains(nodeObject)) {
                return;
            }

            String jobID = pr.getJobID(pr.getURL() + "/" + nodeName);
            MonitoredJob jobObject = new MonitoredJob(jobID);
            if (skippedObjects.contains(jobObject)) {
                return;
            }

            String vnName = pr.getVNName(nodeName);
            MonitoredJob vnJobIDObject = null;
            if (vnName != null) {
                MonitoredVN vnObject = new MonitoredVN(vnName);
                if (skippedObjects.contains(vnObject)) {
                    return;
                }

                VirtualNode vn = pr.getVirtualNode(vnName);
                if (vn != null) {
                    vnJobIDObject = new MonitoredJob(vn.getJobID());
                    if (skippedObjects.contains(vnJobIDObject)) {
                        return;
                    }

                    asso.addChild(vnJobIDObject, vnObject);
                }

                asso.addChild(vnObject, nodeObject);
            }

            ArrayList activeObjects = pr.getActiveObjects(nodeName);
            handleActiveObjects(nodeObject, activeObjects);

            asso.addChild(jvmObject, nodeObject);
            asso.addChild(jobObject, nodeObject);
        } catch (ProActiveException e) {
            log(e);
            return;
        }
    }

    private void handleActiveObjects(MonitoredNode nodeObject,
        ArrayList activeObjects) {
        for (int i = 0, size = activeObjects.size(); i < size; ++i) {
            ArrayList aoWrapper = (ArrayList) activeObjects.get(i);
            RemoteBodyAdapter rba = (RemoteBodyAdapter) aoWrapper.get(0);

            String className = (String) aoWrapper.get(1);
            if (className.equalsIgnoreCase(
                        "org.objectweb.proactive.ic2d.spy.Spy")) {
                continue;
            }

            String jobID = rba.getJobID();
            MonitoredJob jobObject = new MonitoredJob(jobID);
            if (skippedObjects.contains(jobObject)) {
                continue;
            }

            className = className.substring(className.lastIndexOf(".") + 1);
            String aoName = (String) aos.get(rba.getID());
            if (aoName == null) {
                aoName = className + "#" + (aos.size() + 1);
                aos.put(rba.getID(), aoName);
            }

            MonitoredAO aoObject = new MonitoredAO(aoName);
            if (skippedObjects.contains(aoObject)) {
                asso.addChild(nodeObject, aoObject);
                asso.addChild(jobObject, aoObject);
            }
        }
    }

    public void exploreKnownJVM() {
        Iterator iter = asso.getJVM().iterator();
        while (iter.hasNext()) {
            MonitoredJVM jvmObject = (MonitoredJVM) iter.next();
            if (jvmObject.isDeleted())
            	continue;
            ProActiveRuntime pr = urlToRuntime(jvmObject.getFullName());
            if (pr != null) {
                handleProActiveRuntime(pr, jvmObject.getDepth());
            }
        }
    }
    
    public void startExploration() {
    	visitedVM = new TreeSet();
    }
    
    public void endExploration() {
    	visitedVM = null;
    }
}
