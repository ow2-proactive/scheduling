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
package org.objectweb.proactive.p2p.core.service;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.p2p.core.service.KnownTable.KnownTableElement;

/**
 * Implemantation of P2P Service.
 * 
 * @author Alexandre di Costanzo
 *  
 */
public class P2PServiceImpl implements P2PService, InitActive, Serializable {

    private ProActiveRuntime runtime = null;

    private String acquisitionMethod = "rmi:";

    private String portNumber = System.getProperty("proactive.rmi.port");

    protected static Logger logger = Logger.getLogger(P2PServiceImpl.class
            .getName());

    private KnownTable knownProActiveJVM = null;

    private String runtimeName = null;

    private int load = 0;

    private String url = null;

    /**
     * <p>
     * Create a ProActiveRuntime with defaults params.
     * </p>
     * <p>
     * Use RMI with default port number.
     * </p>
     */
    public P2PServiceImpl() {
        // Empty Cronstructor
    }

    /**
     * <p>
     * Create a ProActiveRuntime
     * </p>
     * 
     * @param acquisitionMethod
     *            like "rmi:"
     * @param portNumber
     *            like 2410
     * @param createNode
     */
    public P2PServiceImpl(String acquisitionMethod, String portNumber,
            ProActiveRuntime paRuntime) {
        try {
            this.acquisitionMethod = acquisitionMethod;
            this.portNumber = portNumber;

            // URL
            String url = InetAddress.getLocalHost().getCanonicalHostName();
            if (url.endsWith("/")) {
                url.replace('/', ' ');
                url.trim();
            }
            url += (":" + this.portNumber);
            this.url = "//" + url;

            this.runtime = paRuntime;
            this.runtimeName = this.url + "/"
                    + this.runtime.getVMInformation().getName();
        } catch (UnknownHostException e) {
            logger.error("Could't return the URL of this P2P Service", e);
        }
    }

    // -------------------------------------------------------------------------
    // Privates and Protected Methods
    // -------------------------------------------------------------------------

    /**
     * Add the default name of the P2P Node to a specified <code>url</code>.
     * 
     * @param url
     *            the url.
     * @return the <code>url</code> with the name of the P2P Node.
     */
    private static String urlAdderP2PNodeName(String url) {
        if (url.endsWith("/")) {
            return url += P2PServiceImpl.P2P_NODE_NAME;
        } else {
            return url += ("/" + P2PServiceImpl.P2P_NODE_NAME);
        }
    }

    /**
     * Get the specified remote P2P Service.
     * 
     * @param url
     *            the url of the remote P2P Service.
     * @return the specified P2P Service.
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */
    protected static P2PService getRemoteP2PService(String url)
            throws NodeException, ActiveObjectCreationException {
        url = urlAdderP2PNodeName(url);
        Node node = NodeFactory.getNode(url);
        return (P2PService) node.getActiveObjects(P2PServiceImpl.class
                .getName())[0];
    }

    private ProActiveRuntime getRemoteProActiveRuntime(String url)
            throws ProActiveException {
        return RuntimeFactory.getRuntime(url, this.acquisitionMethod);
    }

    // -------------------------------------------------------------------------
    // Implements P2P Service
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#registerP2PService(org.objectweb.proactive.p2p.core.service.P2PService,
     *      boolean)
     */
    public void registerP2PService(String serviceName, P2PService service,
            int serviceLoad, boolean remoteRecord) {
        KnownTableElement exist = (KnownTableElement) knownProActiveJVM
                .get(serviceName);
        if (exist != null) {
            exist.setLastUpdate(System.currentTimeMillis());
            if (logger.isInfoEnabled()) {
                logger.info("Update ProActive JVM: " + serviceName);
            }
        } else {
            KnownTableElement element = this.knownProActiveJVM.new KnownTableElement(
                    serviceName, service, serviceLoad, System
                            .currentTimeMillis());
            knownProActiveJVM.put(serviceName, element);
            if (logger.isInfoEnabled()) {
                logger.info("Add ProActive JVM: " + serviceName);
            }
        }

        if (remoteRecord) {
            service.registerP2PService(this.runtimeName, (P2PService) ProActive
                    .getStubOnThis(), this.load, false);
        }
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#registerP2PService(java.lang.String)
     */
    public void registerP2PService(String url) {
        try {
            // Get the remote service
            P2PService dist = P2PServiceImpl.getRemoteP2PService(url);
            String name = dist.getServiceName();
            int load = dist.getLoad();
            this.registerP2PService(name, dist, load, true);
        } catch (NodeException e) {
            logger.error("Could't register the P2P Service in: " + url, e);
        } catch (ActiveObjectCreationException e) {
            logger.error("No P2P Service was found in: " + url, e);
        }
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#registerP2PServices(java.util.Collection)
     */
    public void registerP2PServices(Collection servers) {
        Iterator it = servers.iterator();
        while (it.hasNext()) {
            String currentServer = (String) it.next();

            //  Record the current server in this PAR
            this.registerP2PService(currentServer);
        }
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#unregisterP2PService(java.lang.String)
     */
    public void unregisterP2PService(String service) {
        this.knownProActiveJVM.remove(service);
        if (logger.isInfoEnabled()) {
            logger.info("Remove ProActive JVM: " + service);
        }
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#unregisterP2PService(org.objectweb.proactive.p2p.core.service.P2PService)
     */
    public void unregisterP2PService(P2PService service) {
        this.unregisterP2PService(service.getServiceName());
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProActiveJVMs(int)
     */
    public ProActiveRuntime[] getProActiveJVMs(int n) throws ProActiveException {
        return this.getProActiveJVMs(n, P2PService.TTL);
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProActiveJVMs(int,
     *      int)
     */
    public ProActiveRuntime[] getProActiveJVMs(int n, int TTL)
            throws ProActiveException {
        return this.getProActiveJVMs(n, TTL, this.runtimeName);
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProActiveJVMs(int,
     *      int, java.lang.String)
     */
    public ProActiveRuntime[] getProActiveJVMs(int n, int TTL, String parent)
            throws ProActiveException {
        return this.getProActiveJVMs(n, TTL, parent, false);
    }
    
    public ProActiveRuntime[] getProActiveJVMs(int n, int TTL, String parent,
            boolean internalUSe) throws ProActiveException {
        Hashtable allServices = new Hashtable();
        Hashtable res = new Hashtable();

        Object[] iterator = this.knownProActiveJVM.toArray();
        for (int i = 0; i < iterator.length; i++) {
            KnownTableElement element = (KnownTableElement) iterator[i];
            if (((String) element.getKey()).compareTo(parent) != 0) {
                allServices.put(element.getKey(), element.getP2PService());
                if (element.getLoad() == 0) {
                    try {
                        res.put(this.getRemoteProActiveRuntime(
                                (String) element.getKey()).getVMInformation()
                                .getName(), this
                                .getRemoteProActiveRuntime((String) element
                                        .getKey()));
                    } catch (ProActiveException e) {
                        allServices.remove(element.getKey());
                        continue;
                    }
                }
            }
        }

        if ((res.size() >= n) || (TTL == 0)) {
            Vector resFinal = new Vector(res.values());
            if (resFinal.size() >= n) {
                return (ProActiveRuntime[]) resFinal.subList(0, n).toArray(
                        new ProActiveRuntime[n]);
            } else if (TTL == 0) { return (ProActiveRuntime[]) resFinal
                    .toArray(new ProActiveRuntime[resFinal.size()]); }
        }

        Enumeration enum = allServices.elements();
        while ((res.size() != n) && (enum.hasMoreElements())) {
            P2PService current = (P2PService) enum.nextElement();

            ProActiveRuntime[] currentRes;
            try {
                currentRes = current.getProActiveJVMs(n - res.size(), TTL - 1,
                        this.runtimeName);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                        logger.debug("Could't get JVMS", e);
                }
                continue;
            }
            if (currentRes != null) {
                for (int i = 0; i < currentRes.length; i++) {
                    res.put(currentRes[i].getVMInformation().getName(),
                            currentRes[i]);
                }
            }
        }

        res.remove(this.runtimeName);
        Vector resFinal = new Vector(res.values());

        if (resFinal.size() > n) {
            return (ProActiveRuntime[]) resFinal.subList(0, n).toArray(
                    new ProActiveRuntime[n]);
        } else {
            return (ProActiveRuntime[]) resFinal
                    .toArray(new ProActiveRuntime[resFinal.size()]);
        }
    }
    
    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProActiveJVMs()
     */
    public ProActiveRuntime[] getProActiveJVMs() throws ProActiveException {
        Vector res = new Vector(this.knownProActiveJVM.size());
        Object[] table = this.knownProActiveJVM.toArray();
        for (int i = 0; i < table.length; i++) {
            KnownTableElement elem = (KnownTableElement) table[i];

            try {
                res.add(this.getRemoteProActiveRuntime((String) elem.getKey()));
            } catch (Exception e) {
                // The remote is dead => remove from known table
                this.unregisterP2PService((String) elem.getKey());
            }
        }
        return (ProActiveRuntime[]) res
                .toArray(new ProActiveRuntime[res.size()]);
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProActiveRuntime()
     */
    public ProActiveRuntime getProActiveRuntime() {
        return this.runtime;
    }

    /**
     * @return Returns the name of theis P2P Service.
     */
    public String getServiceName() {
        return this.runtimeName;
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProtocol()
     */
    public String getProtocol() {
        return this.acquisitionMethod;
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getURL()
     */
    public String getURL() {
        return this.url;
    }

    // -------------------------------------------------------------------------
    // Implements Load
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.p2p.core.load.Load#setLoad(int)
     */
    public void setLoad(int load) {
        this.load = load;
        Object[] values = this.knownProActiveJVM.toArray();
        for (int i = 0; i < values.length; i++) {
            P2PService service = ((KnownTableElement) values[i])
                    .getP2PService();
            service.registerP2PService(this.runtimeName, (P2PService) ProActive
                    .getStubOnThis(), this.load, false);
        }
    }

    /**
     * @see org.objectweb.proactive.p2p.core.load.Load#getLoad()
     */
    public int getLoad() {
        return P2PService.MAX_LOAD - this.load;
    }

    // -------------------------------------------------------------------------
    // Init Activity
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            // Create the known table
            this.knownProActiveJVM = (KnownTable) ProActive.newActive(
                    KnownTable.class.getName(), null, P2PServiceImpl
                            .urlAdderP2PNodeName(this.url));
            ProActive.enableAC(this.knownProActiveJVM);
            // Launch a thread to update the known table.
            Object[] params = { this.knownProActiveJVM,
                    ProActive.getStubOnThis(), this.runtimeName };
            ProActive.newActive(Updater.class.getName(), params, P2PServiceImpl
                    .urlAdderP2PNodeName(this.url));
        } catch (ActiveObjectCreationException e) {
            logger.error("Could't create the Updater", e);
        } catch (NodeException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    // -------------------------------------------------------------------------
    // Overrides Methods
    // -------------------------------------------------------------------------

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return (this.runtimeName.compareTo(((P2PService) obj).getServiceName()) == 0) ? true
                : false;
    }
}