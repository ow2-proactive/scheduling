package org.objectweb.proactive.ic2d.gui.jobmonitor;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.rmi.*;
import org.objectweb.proactive.ic2d.gui.IC2DGUIController;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataAssociation;

import java.rmi.*;
import java.rmi.registry.*;

import java.util.*;


public class NodeExploration implements JobMonitorConstants {
    private static final String PA_JVM = "PA_JVM";
    private int maxDepth;
    private DataAssociation asso;
    private Vector filteredJobs;
    private IC2DGUIController controller;
    private Map aos;
    private Set visitedVM;
    private Map runtimes;

    public NodeExploration(DataAssociation asso, Vector filteredJobs,
        IC2DGUIController controller) {
        this.maxDepth = 10;
        this.asso = asso;
        this.filteredJobs = filteredJobs;
        this.controller = controller;
        this.aos = new HashMap();
        this.runtimes = new HashMap();
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        if (maxDepth > 0) {
            this.maxDepth = maxDepth;
        }
    }

    /* url : "//host/object" */
    private ProActiveRuntime resolveURL(String url) {
    	StringTokenizer tokenizer = new StringTokenizer(url, "/");
    	String host = null;
    	String name = null;
    	try {
    		host = tokenizer.nextToken();
    		name = tokenizer.nextToken();
    	} catch (NoSuchElementException nsee) {
    		controller.log(nsee);
    		return null;
    	}
    	
    	try {
    		Registry registry = LocateRegistry.getRegistry(host);
    		RemoteProActiveRuntime r = (RemoteProActiveRuntime) registry.lookup(name);
    		return new RemoteProActiveRuntimeAdapter(r);
    	} catch (RuntimeException e) { /* hehe */
    		throw e;
    	} catch (Exception e) {
    		controller.log(e);
    		return null;
    	}
    }
    
    private ProActiveRuntime urlToRuntime(String url) {
    	ProActiveRuntime rt = (ProActiveRuntime) runtimes.get(url);
    	if (rt != null)
    		return rt;
    	
    	rt = resolveURL(url);
    	if (rt != null)
    		runtimes.put(url, rt);
    	
    	return rt;
    }
    
    private List getKnownRuntimes(ProActiveRuntime from) {
    	ProActiveRuntime[] registered;

    	try {
    		registered = from.getProActiveRuntimes();
    	} catch (ProActiveException pae) {
    		controller.log(pae);;
    		registered = new ProActiveRuntime[0];
    	}

    	List known = new LinkedList(Arrays.asList(registered));
    	
    	String[] parents = from.getParents();
    	for (int i = 0; i < parents.length; i++) {
    		ProActiveRuntime rt = urlToRuntime(parents[i]);
    		if (rt != null)
    			known.add(urlToRuntime(parents[i]));
    	}

    	return known;
    }
    
    public void exploreHost(String hostname, int port) {
        try {
            visitedVM = new TreeSet();
            Registry registry = LocateRegistry.getRegistry(hostname, port);
            String[] list = registry.list();

            for (int idx = 0; idx < list.length; ++idx) {
                String id = list[idx];
                if (id.indexOf(PA_JVM) != -1) {
                    RemoteProActiveRuntime r = (RemoteProActiveRuntime) registry.lookup(id);
                    List x = new ArrayList();
                    try {
                        ProActiveRuntime part = new RemoteProActiveRuntimeAdapter(r);
                        x.add(part);

                        ProActiveRuntime[] runtimes = r.getProActiveRuntimes();
                        x.addAll(Arrays.asList(runtimes));

                        for (int i = 0, size = x.size(); i < size; ++i) {
                            handleProActiveRuntime((ProActiveRuntime) x.get(i), 1);
                        }
                    } catch (ProActiveException e) {
                    	controller.log("Unexpected ProActive exception caught while obtaining runtime reference from the RemoteProActiveRuntime instance - The RMI reference might be dead: ", e);
                    } catch (RemoteException e) {
                        controller.log("Unexpected remote exception caught while getting proactive runtimes: ", e);
                    }
                }
            }
        } catch (RemoteException e) {
        	controller.log("Unexpected exception caught while getting registry reference: ", e);
        } catch (NotBoundException e) {
        	controller.log("Unexpected not bound exception caught while looking up object reference: ", e);
        } finally {
            visitedVM = null;
        }
    }

    private void handleProActiveRuntime(ProActiveRuntime pr, int depth)
        throws ProActiveException {
    	
    	if (pr instanceof RemoteProActiveRuntime && !(pr instanceof RemoteProActiveRuntimeAdapter))
    		pr = new RemoteProActiveRuntimeAdapter((RemoteProActiveRuntime) pr);

        String vmName = pr.getVMInformation().getName();
    	
        if (isJobFiltered(pr.getJobID()) || visitedVM.contains(vmName))
            return;
   
        visitedVM.add(vmName);

        String jobId = pr.getJobID();
        String hostname = pr.getVMInformation().getInetAddress()
                            .getCanonicalHostName();

        asso.addChild(JOB, jobId, vmName);

        String[] nodes = pr.getLocalNodeNames();

        //		System.out.println ("Found " + nodes.length + " nodes on this runtime");
        for (int i = 0; i < nodes.length; ++i) {
            String nodeName = nodes[i];
            String vnName = pr.getVNName(nodeName);

            ArrayList activeObjects = null;
            try {
                activeObjects = pr.getActiveObjects(nodeName);
            } catch (ProActiveException e) {
                controller.log("Unexpected ProActive exception caught while obtaining the active objects list",
                    e);
            }

            asso.addChild(JVM, vmName, nodeName);
            asso.addChild(HOST, hostname, JVM, vmName);
            if (vnName != null) {
                asso.addChild(VN, vnName, NODE, nodeName);
            }
            if (activeObjects != null)
            	handleActiveObjects(nodeName, activeObjects);
        }

        if (depth < maxDepth) {
        	List known = getKnownRuntimes(pr);
        	Iterator iter = known.iterator();
            while (iter.hasNext())
                handleProActiveRuntime((ProActiveRuntime) iter.next(), depth + 1);
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

            asso.addChild(NODE, nodeName, aoName);
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
