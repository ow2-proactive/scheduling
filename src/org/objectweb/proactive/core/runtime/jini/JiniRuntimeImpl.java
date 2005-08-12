/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.runtime.jini;

import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Vector;

import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.discovery.DiscoveryEvent;
import net.jini.lease.LeaseRenewalEvent;
import net.jini.lookup.entry.Name;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.rmi.RmiProActiveRuntime;
import org.objectweb.proactive.core.runtime.rmi.RmiProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.PolicyServer;


/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 *          @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public class JiniRuntimeImpl extends RmiProActiveRuntimeImpl
    implements java.io.Serializable, net.jini.discovery.DiscoveryListener,
        net.jini.lease.LeaseListener, RmiProActiveRuntime {
    //    protected transient ProActiveRuntimeImpl proActiveRuntime;
    //    protected String proActiveRuntimeURL;
    //ServiceRegistar table used afterwards to register node service
    //Vector is used because the size is unknown and this class is synchronized
    protected java.util.Vector registrarsTable;

    //table used to store references on runtime registration in order to be 
    //able to unregister it from all lookup services. There is no need for a Hashtable 
    //since there is only one key, but it is for coding purpose.
    protected java.util.Hashtable jiniRuntimeMap;

    //table used to handle node's registration when discovery event is received after the node's creation
    protected java.util.Hashtable jiniNodeMap;

    //table used to handle virtualnode's registration when discovery event is received after the virtualnode's registration
    //this table contains a mapping virtualNode's name and an arrayList that contains all associated ServiceRegistrations
    protected java.util.Hashtable jiniVirtualNodeMap;
    private boolean isRuntimeRegistered = false;

    // this object is not serializable
    protected transient net.jini.lease.LeaseRenewalManager leaseManager = new net.jini.lease.LeaseRenewalManager();

    //
    // -- Constructors -----------------------------------------------
    //
    public JiniRuntimeImpl() throws java.rmi.RemoteException {
        super(true);
        this.proActiveRuntime =  ProActiveRuntimeImpl.getProActiveRuntime();
        this.proActiveRuntimeURL = buildRuntimeURL();
        this.jiniRuntimeMap = new java.util.Hashtable();
        jiniRuntimeMap.put(proActiveRuntimeURL, new java.util.Vector());
        this.jiniNodeMap = new java.util.Hashtable();
        this.jiniVirtualNodeMap = new java.util.Hashtable();
        this.registrarsTable = new java.util.Vector();
        net.jini.discovery.LookupDiscovery discover = null;
        try {
            discover = new net.jini.discovery.LookupDiscovery(net.jini.discovery.LookupDiscovery.ALL_GROUPS);
            // stay around long enough to receice replies
            //Thread.currentThread().sleep(10000L);
        } catch (Exception e) {
            runtimeLogger.error(e.toString());
        }

        discover.addDiscoveryListener(this);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- Implements JiniRuntime -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vnname,
        String jobId) throws RemoteException, NodeException {
        //counter used to check that the node has been registered at 
        //least once as jini service
        //int counter = 0;
        //wait until the discovered method is called
        //otherwise registars could be null, because it is 
        //another thread that fulfill this table
        while (!isRuntimeRegistered) {
        }
        String nodeURL = null;

        //first we build a well-formed url
        try {
            nodeURL = buildNodeURL(nodeName);
            //then take the name of the node
            String name = UrlBuilder.getNameFromUrl(nodeURL);

            //System.out.println("name is : "+ name);
            //System.out.println("url is : "+ nodeURL);
            //create the node with the name 
            proActiveRuntime.createLocalNode(name, replacePreviousBinding, ps,
                vnname, jobId);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " + nodeURL, e);
        }

        jiniNodeMap.put(nodeURL, registerService(nodeURL));

        return nodeURL;
    }

    public void killAllNodes() throws RemoteException, ProActiveException {
        for (java.util.Enumeration e = jiniNodeMap.keys(); e.hasMoreElements();) {
            String nodeURL = (String) e.nextElement();
            killNode(nodeURL);
        }
        proActiveRuntime.killAllNodes();
    }

    public void killNode(String nodeName) throws RemoteException, ProActiveException {
        String nodeUrl = null;
        String name = null;
        try {
            nodeUrl = buildNodeURL(nodeName);
            name = UrlBuilder.getNameFromUrl(nodeUrl);
            unregisterService(nodeUrl, jiniNodeMap);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " + nodeUrl, e);
        }
        proActiveRuntime.killNode(name);
    }

    public void killRT(boolean softly) throws Exception {
        killAllNodes();
        unregisterAllVirtualNodes();
        unregisterService(proActiveRuntimeURL, jiniRuntimeMap);
        proActiveRuntime.killRT(false);
    }

    public String getURL() {
        return proActiveRuntimeURL;
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws RemoteException {
        String virtualNodeURL = null;

        //first we build a well-formed url
        try {
            virtualNodeURL = buildNodeURL(virtualNodeName);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " +
                virtualNodeURL, e);
        }

        if (replacePreviousBinding) {
            if (jiniVirtualNodeMap.get(virtualNodeURL) != null) {
                jiniVirtualNodeMap.remove(virtualNodeURL);
            }
        }
        if (!replacePreviousBinding &&
                (jiniVirtualNodeMap.get(virtualNodeURL) != null)) {
            throw new java.rmi.RemoteException("VirtualNode " + virtualNodeURL +
                " already registered as Jini service");
        }

        jiniVirtualNodeMap.put(virtualNodeURL, registerService(virtualNodeURL));
    }

    public void unregisterVirtualNode(String virtualNodeName)
        throws RemoteException, ProActiveException {
        
        String virtualNodeURL = null;
        proActiveRuntime.unregisterVirtualNode(UrlBuilder.removeVnSuffix(
                virtualNodeName));
        //first we build a well-formed url
        try {
            virtualNodeURL = buildNodeURL(virtualNodeName);
            unregisterService(virtualNodeURL, jiniVirtualNodeMap);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " +
                virtualNodeURL, e);
        }
    }

    public void unregisterAllVirtualNodes() throws RemoteException, ProActiveException {
        for (java.util.Enumeration e = jiniVirtualNodeMap.keys();
                e.hasMoreElements();) {
            String vnNodeURL = (String) e.nextElement();
            unregisterVirtualNode(vnNodeURL);
        }
    }

    //
    // -- Implements  DiscoveryListener,LeaseListener-----------------------------------------------
    //
    public void discovered(DiscoveryEvent evt) {
        ServiceRegistrar[] registrars = evt.getRegistrars();

        //System.out.println("NB registrar "+registrars.length);
        // on cherche un registrar pour pouvoir s'enregistrer
        for (int n = 0; n < registrars.length; n++) {
            ServiceRegistrar registrar = registrars[n];

            ServiceRegistration reg = null;
            try {
                // construction du service
                //ServiceItem item = new ServiceItem(null, this, new Entry[] { new Name(proActiveRuntimeURL)});
                ServiceItem item = new ServiceItem(newServiceID(), this,
                        new Entry[] { new Name(proActiveRuntimeURL) });
                reg = registrar.register(item, Lease.FOREVER);
            } catch (Exception e) {
                //e.printStackTrace();
                runtimeLogger.error("register exception " + e.toString());
                continue;
            }
            ((Vector) jiniRuntimeMap.get(proActiveRuntimeURL)).add(reg);

            //add the registrar in the table for future use(node registration)
            registrarsTable.add(registrar);

            registerServiceAfterDiscovery(jiniNodeMap, registrar);
            registerServiceAfterDiscovery(jiniVirtualNodeMap, registrar);
            // on lance le lease manager pour que l'objet puisse se reenregistrer
            leaseManager.renewUntil(reg.getLease(), Lease.FOREVER, this);
            isRuntimeRegistered = true;
        }

        //we put this line in order to avoid deadlock in createLocalNode
        //Hence if a problem occurs when registering the runtime, we can still try to register
        //it in the createLocalNode method and an exception will be thrown in this method 
        isRuntimeRegistered = true;
    }

    public void discarded(DiscoveryEvent evt) {
    }

    public void notify(LeaseRenewalEvent evt) {
        runtimeLogger.info("Lease expired " + evt.toString());
        runtimeLogger.info(evt.getException().getMessage());
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected ServiceID newServiceID() {

        /** random number generator for UUID generation */
        SecureRandom secRand = new SecureRandom();

        /** 128-bit buffer for use with secRand */
        byte[] secRandBuf16 = new byte[16];

        /** 64-bit buffer for use with secRand */
        byte[] secRandBuf8 = new byte[8];

        secRand.nextBytes(secRandBuf16);
        secRandBuf16[6] &= 0x0f;
        secRandBuf16[6] |= 0x40; /* version 4 */
        secRandBuf16[8] &= 0x3f;
        secRandBuf16[8] |= 0x80; /* IETF variant */
        secRandBuf16[10] |= 0x80; /* multicast bit */
        long mostSig = 0;
        for (int i = 0; i < 8; i++) {
            mostSig = (mostSig << 8) | (secRandBuf16[i] & 0xff);
        }
        long leastSig = 0;
        for (int i = 8; i < 16; i++) {
            leastSig = (leastSig << 8) | (secRandBuf16[i] & 0xff);
        }
        return new ServiceID(mostSig, leastSig);
    }

    //
    // ---PRIVATE METHODS--------------------------------------
    //
    //This method is very useful when the JiniRuntime receives event about a new Lookup service
    //that was discovered.In such case, the runtime registers all nodes and virtualnodes previously registered
    //as Jini service with the registrar given as parameter and the corresponding hashtable
    private void registerServiceAfterDiscovery(Hashtable jiniObjectTable,
        ServiceRegistrar registrar) {
        ServiceRegistration reg = null;
        ServiceID serviceID = null;
        if (!jiniObjectTable.isEmpty()) {
            synchronized (jiniObjectTable) {
                for (java.util.Enumeration e = jiniObjectTable.keys();
                        e.hasMoreElements();) {
                    String objectURL = (String) e.nextElement();
                    Vector serviceRegistrationTable = (Vector) jiniObjectTable.get(objectURL);
                    if (!serviceRegistrationTable.isEmpty()) {
                        serviceID = ((ServiceRegistration) serviceRegistrationTable.get(0)).getServiceID();
                    } else {
                        serviceID = newServiceID();
                    }
                    ServiceItem item = new ServiceItem(serviceID, this,
                            new Entry[] { new Name(objectURL) });
                    try {
                        reg = registrar.register(item, Lease.FOREVER);
                    } catch (Exception ex) {
                        runtimeLogger.info("register exception " +
                            ex.toString());
                        continue;
                    }
                    runtimeLogger.info(" Service Registered " + objectURL);

                    // on lance le lease manager pour que l'objet puisse se reenregistrer
                    leaseManager.renewUntil(reg.getLease(), Lease.FOREVER, this);
                    ((Vector) jiniObjectTable.get(objectURL)).add(reg);
                }
            }
        }
    }

    private String buildRuntimeURL() {
        String host = UrlBuilder.getHostNameorIP(getVMInformation()
                                                     .getInetAddress());
        String name = getVMInformation().getName();
        return UrlBuilder.buildUrl(host, name, "jini:");
    }

    private String buildNodeURL(String url)
        throws java.net.UnknownHostException {
        int i = url.indexOf('/');
        if (i == -1) {
            //it is an url given by a descriptor
            String host = UrlBuilder.getHostNameorIP(getVMInformation()
                                                         .getInetAddress());
            return UrlBuilder.buildUrl(host, url, "jini:");
        } else {
            return UrlBuilder.checkUrl(url);
        }
    }

    private Vector registerService(String objectUrl)
        throws java.rmi.RemoteException {
        //counter used to check that the object has been registered at 
        //least once as jini service
        int counter = 0;
        ServiceID serviceID = newServiceID();
        Vector serviceRegistrationTable = new Vector();

        //register it as a jini service with the url
        for (int n = 0; n < registrarsTable.size(); n++) {
            ServiceRegistrar registrar = (ServiceRegistrar) registrarsTable.get(n);
            ServiceRegistration reg = null;
            try {
                ServiceItem item = new ServiceItem(serviceID, this,
                        new Entry[] { new Name(objectUrl) });
                reg = registrar.register(item, Lease.FOREVER);
                counter++;
            } catch (Exception e) {
                runtimeLogger.info("register exception " + e.toString());
                continue;
            }

            // if counter=0 no node or vn are registered as jini Service
            if (counter == 0) {
                throw new java.rmi.RemoteException("register exception ");
            }
            runtimeLogger.info("Service registered " + objectUrl);
            //System.out.println("Registrar "+registrar.getLocator().getHost());
            // on lance le lease manager pour que l'objet puisse se reenregistrer
            leaseManager.renewUntil(reg.getLease(), Lease.FOREVER, this);
            serviceRegistrationTable.add(reg);
        }
        return serviceRegistrationTable;
    }

    private void unregisterService(String objectUrl, Hashtable jiniObjectTable)
        throws java.rmi.RemoteException {
        if (!jiniObjectTable.isEmpty()) {
            synchronized (jiniObjectTable) {
                try {
                    Vector serviceRegistrationTable = (Vector) jiniObjectTable.get(objectUrl);
                    if (!serviceRegistrationTable.isEmpty()) {
                        for (int i = 0; i < serviceRegistrationTable.size();
                                i++) {
                            ServiceRegistration reg = (ServiceRegistration) serviceRegistrationTable.get(i);
                            reg.getLease().cancel();
                        }
                        if (objectUrl.indexOf("PA_JVM") < 0) {
                            runtimeLogger.info("Lease cancelled for " +
                                objectUrl);
                        }
                    }
                } catch (net.jini.core.lease.UnknownLeaseException e) {
                    throw new java.rmi.RemoteException(
                        "Unable to get the Lease for virtualNode " + objectUrl,
                        e);
                } finally {
                    jiniObjectTable.remove(objectUrl);
                }
            }
        }
    }
}
