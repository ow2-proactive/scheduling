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
import java.util.Vector;


public class NodeExploration implements JobMonitorConstants {
    private static final String PA_JVM = "PA_JVM";
    private int maxDepth;
    private DataAssociation asso;
    private Vector filteredJobs;
    private Map aos;
    private Set visitedVM;
    private Map runtimes;
    private IC2DGUIController controller;

    public NodeExploration(DataAssociation asso, Vector filteredJobs,
        IC2DGUIController controller) {
        this.maxDepth = 10;
        this.asso = asso;
        this.filteredJobs = filteredJobs;
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

    private void addChild(int fromKey, String fromName, int toKey, String toName) {
        asso.addChild(BasicMonitoredObject.create(fromKey, fromName),
            BasicMonitoredObject.create(toKey, toName));
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

        if (isJobFiltered(pr.getJobID()) || visitedVM.contains(vmName)) {
            return;
        }

        visitedVM.add(vmName);

        String jobId = pr.getJobID();
        String hostname = pr.getVMInformation().getInetAddress()
                            .getCanonicalHostName();

        addChild(HOST, hostname, JVM, vmName);
        addChild(JOB, pr.getJobID(), JVM, vmName);

        try {
            String[] nodes = pr.getLocalNodeNames();
            for (int i = 0; i < nodes.length; ++i) {
                String nodeName = nodes[i];
                handleNode(pr, vmName, nodeName);
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
        String vnName;

        addChild(JVM, vmName, NODE, nodeName);
        try {
            addChild(JOB, pr.getJobID(pr.getURL() + "/" + nodeName), NODE,
                nodeName);
            vnName = pr.getVNName(nodeName);

            ArrayList activeObjects = null;
            activeObjects = pr.getActiveObjects(nodeName);

            if (vnName != null) {
                addChild(VN, vnName, NODE, nodeName);
                VirtualNode vn = pr.getVirtualNode(vnName);
                if (vn != null) {
                    addChild(JOB, vn.getJobID(), VN, vnName);
                }
            }

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

            //			System.out.println ("Active object " + (i + 1) + " / " + size + " class: " + aoWrapper.get (1));
            String className = (String) aoWrapper.get(1);
            if (className.equalsIgnoreCase(
                        "org.objectweb.proactive.ic2d.spy.Spy")) {
                continue;
            }

            className = className.substring(className.lastIndexOf(".") + 1);
            String aoName = (String) aos.get(rba.getID());
            if (aoName == null) {
                aoName = className + "#" + (aos.size() + 1);
                aos.put(rba.getID(), aoName);
            }

            addChild(NODE, nodeName, AO, aoName);
            addChild(JOB, rba.getJobID(), AO, aoName);
        }
    }

    private boolean isJobFiltered(String jobId) {
        for (int i = 0, size = filteredJobs.size(); i < size; ++i) {
            String job = (String) filteredJobs.get(i);
            if (job.equalsIgnoreCase(jobId)) {
                return true;
            }
        }

        return false;
    }
}
