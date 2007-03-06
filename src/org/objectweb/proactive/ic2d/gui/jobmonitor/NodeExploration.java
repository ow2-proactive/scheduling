/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.gui.jobmonitor;

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

import javax.swing.DefaultListModel;

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


public class NodeExploration implements JobMonitorConstants {
    private int maxDepth;
    private DataAssociation asso;
    private DefaultListModel skippedObjects;
    private Set<String> visitedVM;
    private Map<String, ProActiveRuntime> runtimes;
    private IC2DMessageLogger controller;

    public NodeExploration(DataAssociation asso,
        DefaultListModel skippedObjects, IC2DMessageLogger controller) {
        this.maxDepth = 3;
        this.asso = asso;
        this.skippedObjects = skippedObjects;
        this.runtimes = new HashMap<String, ProActiveRuntime>();
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
        try {
            return RuntimeFactory.getRuntime(url, UrlBuilder.getProtocol(url));
        } catch (Exception e) {
            log(e);
            return null;
        }
    }

    private ProActiveRuntime urlToRuntime(String url) {
        ProActiveRuntime rt = runtimes.get(url);
        if (rt != null) {
            return rt;
        }

        rt = resolveURL(url);
        if (rt != null) {
            runtimes.put(url, rt);
        }

        return rt;
    }

    private List<ProActiveRuntime> getKnownRuntimes(ProActiveRuntime from) {
        List<ProActiveRuntime> known;
        String[] parents;

        try {
            ProActiveRuntime[] registered = from.getProActiveRuntimes();
            known = new ArrayList<ProActiveRuntime>(Arrays.asList(registered));
            parents = from.getAcquaintances();
        } catch (ProActiveException e) {
            log(e);
            return new ArrayList<ProActiveRuntime>();
        }

        for (int i = 0; i < parents.length; i++) {
            ProActiveRuntime rt = urlToRuntime(parents[i]);
            if (rt != null) {
                known.add(urlToRuntime(parents[i]));
            }
        }

        return known;
    }

    public void exploreHost(String hostname, int port, String protocol) {
        //Registry registry;
        //String[] list;
        HostRTFinder runtimeFinder = initiateFinder(protocol);
        MonitoredHost hostObject = new MonitoredHost(hostname, port, protocol);
        if (skippedObjects.contains(hostObject)) {
            return;
        }
        ArrayList foundRuntimes = null;
        try {
            foundRuntimes = runtimeFinder.findPARuntimes(hostname, port);
        } catch (IOException e) {
            //if an IOException is thrown when connecting the registry, the host is removed
            skippedObjects.addElement(hostObject);
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

        url = pr.getURL();

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
        String monitoredProtocol = UrlBuilder.getProtocol(url);

        MonitoredHost hostObject = new MonitoredHost(hostname,
                jvmObject.getPort(), monitoredProtocol);
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
        } catch (Exception e) {
            	skippedObjects.addElement(jvmObject);
                log(e);
            
            return;
        }

        asso.addChild(hostObject, jvmObject);
        asso.addChild(jobObject, jvmObject);

        if (depth < maxDepth) {
            List<ProActiveRuntime> known = getKnownRuntimes(pr);
            Iterator<ProActiveRuntime> iter = known.iterator();
            while (iter.hasNext())
                handleProActiveRuntime(iter.next(), depth +
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
                MonitoredVN vnObject = new MonitoredVN(vnName, jobID);
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
        } catch (Exception e) {
        	if(! skippedObjects.contains(jvmObject)){
        		log(e);
        		skippedObjects.addElement(jvmObject);
        	}
            
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
        visitedVM = new TreeSet<String>();
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
        if (protocol.equals("rmi:") || protocol.equals("rmissh:")) {
            return new RMIHostRTFinder(controller, skippedObjects);
        } else if (protocol.equals("http:")) {
            return new HttpHostRTFinder(controller, skippedObjects);
        } else if (protocol.equals("jini:")) {
            return new JiniHostRTFinder(controller, skippedObjects);
        } else if (protocol.equals("ibis:")) {
            return new IbisHostRTFinder(controller, skippedObjects);
        }
        return null;
    }
}
