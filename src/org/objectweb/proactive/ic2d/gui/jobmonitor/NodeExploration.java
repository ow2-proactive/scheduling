package org.objectweb.proactive.ic2d.gui.jobmonitor;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntimeAdapter;
import org.objectweb.proactive.ic2d.gui.IC2DGUIController;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataAssociation;

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

        if (isSkipped(HOST, hostname)) {
            return;
        }

        try {
            registry = LocateRegistry.getRegistry(hostname, port);
            list = registry.list();
        } catch (Exception e) {
            log(e);
            return;
        }

        visitedVM = new TreeSet();
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
        visitedVM = null;
    }

    private boolean isSkipped(int key, String fullname) {
        BasicMonitoredObject object = BasicMonitoredObject.create(key, fullname);
        return skippedObjects.contains(object);
    }

    private void addChild(int fromKey, String fromName, int toKey, String toName) {
        BasicMonitoredObject fromObject = BasicMonitoredObject.create(fromKey,
                fromName);
        BasicMonitoredObject toObject = BasicMonitoredObject.create(toKey,
                toName);

        asso.addChild(fromObject, toObject);
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

        String vmName = pr.getVMInformation().getName();

        if (visitedVM.contains(vmName) || isSkipped(JVM, vmName)) {
            return;
        }

        String jobID = pr.getJobID();
        if (isSkipped(JOB, jobID)) {
            return;
        }

        String hostname = pr.getVMInformation().getInetAddress()
                            .getCanonicalHostName();
        if (isSkipped(HOST, hostname)) {
            return;
        }

        visitedVM.add(vmName);

        addChild(HOST, hostname, JVM, vmName);
        addChild(JOB, jobID, JVM, vmName);

        try {
            String[] nodes = pr.getLocalNodeNames();
            for (int i = 0; i < nodes.length; ++i) {
                String nodeName = nodes[i];
                if (!isSkipped(NODE, nodeName)) {
                    handleNode(pr, vmName, nodeName);
                }
            }
        } catch (ProActiveException e) {
            log(e);
        }

        if (depth < maxDepth) {
            List known = getKnownRuntimes(pr);
            Iterator iter = known.iterator();
            while (iter.hasNext())
                handleProActiveRuntime((ProActiveRuntime) iter.next(), depth +
                    1);
        }
    }

    private void handleNode(ProActiveRuntime pr, String vmName, String nodeName) {
        try {
            String jobID = pr.getJobID(pr.getURL() + "/" + nodeName);
            if (isSkipped(JOB, jobID)) {
                return;
            }

            String vnName = pr.getVNName(nodeName);
            String vnJobID = null;
            if (vnName != null) {
                if (isSkipped(VN, vnName)) {
                    return;
                }

                VirtualNode vn = pr.getVirtualNode(vnName);
                if (vn != null) {
                    vnJobID = vn.getJobID();
                    if (isSkipped(JOB, vnJobID)) {
                        return;
                    }

                    addChild(JOB, vnJobID, VN, vnName);
                }

                addChild(VN, vnName, NODE, nodeName);
            }

            addChild(JVM, vmName, NODE, nodeName);
            addChild(JOB, jobID, NODE, nodeName);

            ArrayList activeObjects = pr.getActiveObjects(nodeName);
            handleActiveObjects(nodeName, activeObjects);
        } catch (ProActiveException e) {
            log(e);
            return;
        }
    }

    private void handleActiveObjects(String nodeName, ArrayList activeObjects) {
        for (int i = 0, size = activeObjects.size(); i < size; ++i) {
            ArrayList aoWrapper = (ArrayList) activeObjects.get(i);
            RemoteBodyAdapter rba = (RemoteBodyAdapter) aoWrapper.get(0);

            String className = (String) aoWrapper.get(1);
            if (className.equalsIgnoreCase(
                        "org.objectweb.proactive.ic2d.spy.Spy")) {
                continue;
            }

            String jobID = rba.getJobID();
            if (isSkipped(JOB, jobID)) {
                continue;
            }

            className = className.substring(className.lastIndexOf(".") + 1);
            String aoName = (String) aos.get(rba.getID());
            if (aoName == null) {
                aoName = className + "#" + (aos.size() + 1);
                aos.put(rba.getID(), aoName);
            }

            if (!isSkipped(AO, aoName)) {
                addChild(NODE, nodeName, AO, aoName);
                addChild(JOB, rba.getJobID(), AO, aoName);
            }
        }
    }
}
