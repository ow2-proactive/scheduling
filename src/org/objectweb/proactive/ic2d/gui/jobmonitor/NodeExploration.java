package org.objectweb.proactive.ic2d.gui.jobmonitor;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.BasicMonitoredObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataAssociation;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredAO;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredHost;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJVM;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredJob;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredNode;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredObjectSet;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredVN;
import org.objectweb.proactive.ic2d.util.HostRTFinder;
import org.objectweb.proactive.ic2d.util.HttpHostRTFinder;
import org.objectweb.proactive.ic2d.util.IC2DMessageLogger;
import org.objectweb.proactive.ic2d.util.IbisHostRTFinder;
import org.objectweb.proactive.ic2d.util.JiniHostRTFinder;
import org.objectweb.proactive.ic2d.util.RMIHostRTFinder;

import java.io.IOException;

import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;


public class NodeExploration implements JobMonitorConstants {
    private int maxDepth;
    private DataAssociation asso;
    private DefaultListModel skippedObjects;
    private Map aos;
    private Set visitedVM;
    private Map runtimes;
    private IC2DMessageLogger controller;
    private String protocol;
    private HostRTFinder runtimeFinder;

    public NodeExploration(DataAssociation asso,
        DefaultListModel skippedObjects, IC2DMessageLogger controller,
        String protocol) {
        this.maxDepth = 3;
        this.asso = asso;
        this.skippedObjects = skippedObjects;
        this.aos = new HashMap();
        this.runtimes = new HashMap();
        this.controller = controller;
        this.runtimeFinder = initiateFinder(protocol);
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        if (maxDepth > 0) {
            this.maxDepth = maxDepth;
        }
    }

    public void setMaxDepth(String maxDepth) {
        try {
            setMaxDepth(Integer.parseInt(maxDepth.trim()));
        } catch (NumberFormatException e) {
        }
    }

    private void log(Throwable e) {
        controller.log(e, false);
    }

    /* url : "//host:port/object" */
    private ProActiveRuntime resolveURL(String url) {
        Pattern p = Pattern.compile("(.*//)?([^:]+):?([0-9]*)/(.+)");
        Matcher m = p.matcher(url);
        if (!m.matches()) {
            return null;
        }

        String host = m.group(2);
        String port = m.group(3);
        String object = m.group(4);

        try {
            return RuntimeFactory.getRuntime(url, UrlBuilder.getProtocol(url));
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
            known = new ArrayList(Arrays.asList(registered));
            parents = from.getAcquaintances();
        } catch (ProActiveException e) {
            log(e);
            return new ArrayList();
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
        //Registry registry;
        //String[] list;
        MonitoredHost hostObject = new MonitoredHost(hostname, port);
        if (skippedObjects.contains(hostObject)) {
            return;
        }
        ArrayList foundRuntimes = null;
        try {
            foundRuntimes = runtimeFinder.findPARuntimes(hostname, port);
        } catch (IOException e) {
            log(e);
        }
        if (foundRuntimes != null) {
            for (int idx = 0; idx < foundRuntimes.size(); ++idx) {
                ProActiveRuntime part = (ProActiveRuntime) foundRuntimes.get(idx);
                handleProActiveRuntime(part, 1);
            }
        }
    }

    public void exploreNode(String nodeUrl, String protocol) {
        try {
            ProActiveRuntime part = RuntimeFactory.getRuntime(nodeUrl, protocol);
            handleProActiveRuntime(part, 1);
        } catch (ProActiveException e) {
            log(e);
        }
    }

    private void handleProActiveRuntime(ProActiveRuntime pr, int depth) {
        //should never occur since if pr is a RemoteProActiveRuntime
        // a classCastException should be thrown
        //        if (pr instanceof RemoteProActiveRuntime &&
        //                !(pr instanceof RemoteProActiveRuntimeAdapter)) {
        //            try {
        //                pr = new RemoteProActiveRuntimeAdapter((RemoteProActiveRuntime) pr);
        //            } catch (ProActiveException e) {
        //                log(e);
        //                return;
        //            }
        //        }
        VMInformation infos = pr.getVMInformation();
        String vmName = infos.getName();

        String url;
        try {
            url = pr.getURL();
        } catch (ProActiveException e) {
            log(e);
            return;
        }
        MonitoredJVM jvmObject = new MonitoredJVM(url, depth);

        if (visitedVM.contains(vmName) || skippedObjects.contains(jvmObject)) {
            return;
        }

        String jobID = pr.getJobID();
        MonitoredJob jobObject = new MonitoredJob(jobID);
        if (skippedObjects.contains(jobObject)) {
            return;
        }

        String hostname = UrlBuilder.getHostNameorIP(infos.getInetAddress());

        MonitoredHost hostObject = new MonitoredHost(hostname,
                jvmObject.getPort());
        if (skippedObjects.contains(hostObject)) {
            return;
        }

        //        try {
        //            System.out.println("OK for "+ pr.getURL()+ " " +vmName );
        //        } catch (ProActiveException e1) {
        //            e1.printStackTrace();
        //        }
        visitedVM.add(vmName);

        try {
            String[] nodes = pr.getLocalNodeNames();
            for (int i = 0; i < nodes.length; ++i) {
                String nodeName = nodes[i];
                if (nodeName.indexOf("SpyListenerNode") == -1) {
                    handleNode(pr, jvmObject, vmName, nodeName);
                }
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
            String runtimeUrl = pr.getURL();
            String protocol = UrlBuilder.getProtocol(runtimeUrl);
            String host = null;
            try {
                host = UrlBuilder.getHostNameFromUrl(runtimeUrl);
            } catch (UnknownHostException e1) {
                log(e1);
                e1.printStackTrace();
            }
            int port = UrlBuilder.getPortFromUrl(runtimeUrl);
            String nodeUrl = UrlBuilder.buildUrl(host, nodeName, protocol, port);
            Node node = new NodeImpl(pr, nodeUrl,
                    UrlBuilder.getProtocol(nodeUrl), pr.getJobID(nodeUrl));
            MonitoredNode nodeObject = new MonitoredNode(node,
                    jvmObject.getFullName());
            if (skippedObjects.contains(nodeObject)) {
                return;
            }

            String jobID = pr.getJobID(pr.getURL() + "/" + nodeName);
            MonitoredJob jobObject = new MonitoredJob(jobID);
            if (skippedObjects.contains(jobObject)) {
                return;
            }

            String vnName = pr.getVNName(nodeName);

            //String vnName = node.getVnName();
            MonitoredJob vnJobIDObject = null;
            if (vnName != null) {
                MonitoredVN vnObject = new MonitoredVN(vnName);
                if (skippedObjects.contains(vnObject)) {
                    return;
                }

                //VirtualNode vn = pr.getVirtualNode(vnName);
                //if (vn != null) {
                //here we guess that the VirtualNode has the same jobID
                //than the node. This assumption is pertinent since the node is 
                //bult with the jobID of the vn.
                vnJobIDObject = new MonitoredJob(node.getNodeInformation()
                                                     .getJobID());
                if (skippedObjects.contains(vnJobIDObject)) {
                    return;
                }

                asso.addChild(vnJobIDObject, vnObject);
                //}
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
            UniversalBody rba = (UniversalBody) aoWrapper.get(0);

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
            MonitoredAO aoObject = new MonitoredAO(className,
                    rba.getID().toString());
            if (!skippedObjects.contains(aoObject)) {
                asso.addChild(nodeObject, aoObject);
                asso.addChild(jobObject, aoObject);
            }
        }
    }

    public void exploreKnownJVM() {

        /*
         * We clone the set to avoid ConcurrentModificationException because we modify
         * it when traversing the network.
         */
        Iterator iter = ((MonitoredObjectSet) asso.getJVM().clone()).iterator();
        while (iter.hasNext()) {
            MonitoredJVM jvmObject = (MonitoredJVM) iter.next();
            ProActiveRuntime pr = urlToRuntime(jvmObject.getFullName());
            if (pr != null) {
                handleProActiveRuntime(pr, jvmObject.getDepth());
            }
        }
    }

    private void killJVM(MonitoredJVM jvm) {
        ProActiveRuntime part = urlToRuntime(jvm.getFullName());
        try {
            part.killRT(false);
        } catch (Exception e) {
            log(e);
        }
    }

    private void killJob(MonitoredJob job) {
        MonitoredObjectSet jvms = asso.getValues(job, JVM, null);
        Iterator iter = jvms.iterator();

        while (iter.hasNext()) {
            MonitoredJVM jvm = (MonitoredJVM) iter.next();
            killJVM(jvm);
        }
    }

    public void killObjects(MonitoredObjectSet objects) {
        Iterator iter = objects.iterator();
        while (iter.hasNext()) {
            BasicMonitoredObject o = (BasicMonitoredObject) iter.next();
            int key = o.getKey();
            if (key == JOB) {
                killJob((MonitoredJob) o);
            }

            if (key == JVM) {
                killJVM((MonitoredJVM) o);
            }
        }
    }

    public void startExploration() {
        visitedVM = new TreeSet();
    }

    public void endExploration() {
        visitedVM = null;
        asso.updateReallyDeleted();
    }

    /**
     * @param protocol
     * @return
     */
    private HostRTFinder initiateFinder(String protocol) {
        if (protocol.equals("rmi:")) {
            return new RMIHostRTFinder(controller);
        } else if (protocol.equals("http:")) {
            return new HttpHostRTFinder(controller);
        } else if (protocol.equals("jini:")) {
            return new JiniHostRTFinder(controller);
        } else if (protocol.equals("ibis:")) {
            return new IbisHostRTFinder(controller);
        }
        return null;
    }
}
