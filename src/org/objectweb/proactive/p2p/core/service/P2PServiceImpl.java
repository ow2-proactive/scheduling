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
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.p2p.core.info.Info;
import org.objectweb.proactive.p2p.core.service.KnownTable.KnownTableElement;

import java.io.IOException;
import java.io.Serializable;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;


/**
 * Implemantation of P2P Service.
 *
 * @author Alexandre di Costanzo
 *
 */
public class P2PServiceImpl implements P2PService, InitActive, Serializable {
    protected static Logger logger = Logger.getLogger(P2PServiceImpl.class.getName());
    private ProActiveRuntime runtime = null;
    private String acquisitionMethod = "rmi:";
    private String portNumber = System.getProperty("proactive.rmi.port");
    private KnownTable knownProActiveJVM = null;
    private String runtimeName = null;
    private int load = 0;
    private String peerHostname;
    private String peerUrl = null;
    private Info serviceInfo;
    private FakeProActiveRuntime fakeProActiveRuntime;

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
    public P2PServiceImpl(String acquisitionMethod, String portNumber) {
        try {
            this.acquisitionMethod = acquisitionMethod;
            this.portNumber = portNumber;

            // URL
            String url = InetAddress.getLocalHost().getCanonicalHostName();
            this.peerHostname = url;

            if (url.endsWith("/")) {
                url.replace('/', ' ');
                url.trim();
            }

            url += (":" + this.portNumber);

            this.peerUrl = "//" + url;

            this.runtime = RuntimeFactory.getProtocolSpecificRuntime(this.acquisitionMethod);
            this.runtimeName = this.runtime.getURL();
            this.serviceInfo = new Info(0, 0, this.getServiceName());

            this.fakeProActiveRuntime = new FakeProActiveRuntime();
        } catch (UnknownHostException e) {
            logger.error("Could't return the URL of this P2P Service ");
        } catch (ProActiveException e) {
            logger.error(e);
        }
    }

    // -------------------------------------------------------------------------
    // Privates and Protected Methods
    // -------------------------------------------------------------------------

    /**
     * Add the default name of the P2P Node to a specified <code>url</code>.
     * @param url  the url.
     * @return the <code>url</code> with the name of the P2P Node.
     */
    private static String urlAdderP2PNodeName(String url) {
        if (url.endsWith("/")) {
            url += P2PServiceImpl.P2P_NODE_NAME;
        } else {
            url += ("/" + P2PServiceImpl.P2P_NODE_NAME);
        }

        return url;
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

        try {
            String runtimeUrl = node.getProActiveRuntime().getURL();
        } catch (ProActiveException e) {
            logger.error("Can't get remote ProActiveRuntime URL of " + url);
        }

        P2PService p2p = (P2PService) node.getActiveObjects(P2PServiceImpl.class.getName())[0];

        return p2p;
    }

    /**
     *
     * @param remoteUrl URL of a remote node.
     * @return The node pointed by the remoteUrl.
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */
    protected static Node getRemoteNode(String remoteUrl)
        throws NodeException, ActiveObjectCreationException {
        return NodeFactory.getNode(urlAdderP2PNodeName(remoteUrl));
    }

    /**
     *
     * @param distNode Node upon we can find a remote p2pService
     * @return The runtime url where the node is located.
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */
    protected static String getRemoteProActiveRuntimeURL(Node distNode)
        throws NodeException, ActiveObjectCreationException {
        String runtimeUrl = "";

        try {
            runtimeUrl = distNode.getProActiveRuntime().getURL();
        } catch (ProActiveException e) {
            logger.error("Can't get remote ProActiveRuntime URL");
        }

        return runtimeUrl;
    }

    /**
     *
     * @param distNode Node upon we can find a remote p2pService
     * @return The remote P2PService.
     * @throws NodeException If node is unavailable
     * @throws ActiveObjectCreationException
     */
    protected static P2PService getRemoteP2PService(Node distNode)
        throws NodeException, ActiveObjectCreationException {
        return (P2PService) distNode.getActiveObjects(P2PServiceImpl.class.getName())[0];
    }

    private ProActiveRuntime getRemoteProActiveRuntime()
        throws ProActiveException {
        return RuntimeFactory.getDefaultRuntime();
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
     *
     * @param myHostname Current hostname.
     * @param remoteUrl A remote url.
     */
    public void registerP2PService(String remoteUrl) {
        // 	we should find an hostname matching this kind of pattern : hostname.inria.fr:PORT
        if (this.peerHostname.compareTo(UrlBuilder.getNameFromUrlWithoutPort(
                        remoteUrl)) != 0) {
            try {
                P2PService dist;
                Node remoteP2PNode = P2PServiceImpl.getRemoteNode(remoteUrl);

                if ((dist = P2PServiceImpl.getRemoteP2PService(remoteP2PNode)) != null) {
                    this.registerP2PService(P2PServiceImpl.getRemoteProActiveRuntimeURL(
                            remoteP2PNode), dist.getInfo(), true);

                    if (logger.isInfoEnabled()) {
                        logger.info(this.getServiceName() + " has register " +
                            remoteUrl);
                    }
                }
            } catch (NodeException e) {
                logger.error("Could't register the P2P Service in: " +
                    remoteUrl);
            } catch (ActiveObjectCreationException e) {
                logger.error("No P2P Service was found in: " + remoteUrl);
            }
        }
    }

    public void registerP2PService(String remoteProActiveRuntimeURL,
        Info distInfo, boolean remoteRecord) {
        KnownTableElement exist = (KnownTableElement) knownProActiveJVM.get(remoteProActiveRuntimeURL);

        if (exist != null) {
            exist.setLastUpdate(System.currentTimeMillis());

            if (logger.isInfoEnabled()) {
                logger.info("Update ProActive JVM: " +
                    remoteProActiveRuntimeURL);
            }
        } else {
            KnownTableElement element = this.knownProActiveJVM.new KnownTableElement(remoteProActiveRuntimeURL,
                    distInfo);

            knownProActiveJVM.put(remoteProActiveRuntimeURL, element);

            if (logger.isInfoEnabled()) {
                logger.info("Add ProActive JVM: " + remoteProActiveRuntimeURL);
            }
        }

        if (remoteRecord) {
            P2PService distService;

            try {
                // we get remote service directly to avoid deadlock instead of
                // get it from the distInfo
                if ((distService = P2PServiceImpl.getRemoteP2PService(this.acquisitionMethod +
                                "//" +
                                UrlBuilder.getNameFromPARUrl(
                                    remoteProActiveRuntimeURL))) != null) {
                    distService.registerP2PService(this.getServiceName(),
                        this.serviceInfo, false);

                    if (logger.isInfoEnabled()) {
                        logger.info(this.getServiceName() + " remote record" +
                            remoteProActiveRuntimeURL);
                    }
                }
            } catch (NodeException e) {
                logger.error("Could't register the P2P Service in: " + peerUrl);
            } catch (ActiveObjectCreationException e) {
                logger.error("No P2P Service was found in: " + peerUrl);
            }
        }
    }

    /**
     * returninformation available for this peer such as name, load, stub
     */
    public Info getInfo() {
        return this.serviceInfo;
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
        return this.getProActiveJVMs(n, TTL, new LinkedList());
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProActiveJVMs(int,
     *      int, java.lang.String)
     */
    public ProActiveRuntime[] getProActiveJVMs(int n, int TTL,
        LinkedList parentList) throws ProActiveException {
        return this.getProActiveJVMs(n, TTL, parentList, false);
    }

    public ProActiveRuntime[] getProActiveJVMs(int n, int TTL,
        LinkedList parentList, boolean internalUSe) throws ProActiveException {
        Hashtable res = new Hashtable();

        KnownTableElement[] tmp = this.knownProActiveJVM.toArray();

        LinkedList peerToContact = new LinkedList();

        // adding "this" reference in the parent search path
        parentList.add(this.getServiceName());

        int i = 0;

        while (i < tmp.length) {
            if (!parentList.contains(tmp[i].getKey())) {
                peerToContact.add(tmp[i]);
            }

            i++;
        }

        if (logger.isInfoEnabled()) {
            logger.info(this.getServiceName() + " knows " + tmp.length +
                " P2PNode, where " + peerToContact.size() +
                " are not parents, TTL is " + TTL);
        }

        KnownTableElement peerElement;
        Iterator it = peerToContact.iterator();

        while (it.hasNext()) {
            peerElement = (KnownTableElement) it.next();

            if (peerElement.getLoad() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info(this.getServiceName() + " has selected " +
                        peerElement.getKey() + " has a possible giveable JVM ");
                }

                Object tmpPar = peerElement.getP2PService().getProActiveRuntime();
                Object par = ProActive.getFutureValue(tmpPar);

                if (par instanceof ProActiveRuntime) {
                    VMInformation vmi = ((ProActiveRuntime) par).getVMInformation();
                    res.put(vmi.getName(), par);

                    if (logger.isInfoEnabled()) {
                        logger.info(this.getServiceName() + " has added " +
                            peerElement.getKey() + " JVM to result list");
                    }
                }
            }
        }

        if ((res.size() >= n) || (TTL == 0)) {
            Vector resFinal = new Vector(res.values());

            if (resFinal.size() >= n) {
                if (logger.isInfoEnabled()) {
                    logger.info(this.getServiceName() + " has send back " + n +
                        " JVM");
                }

                return (ProActiveRuntime[]) resFinal.subList(0, n).toArray(new ProActiveRuntime[n]);
            } else if (TTL == 0) {
                if (logger.isInfoEnabled()) {
                    logger.info(this.getServiceName() + " has send back " +
                        resFinal.size() + " JVM");
                }

                return (ProActiveRuntime[]) resFinal.toArray(new ProActiveRuntime[resFinal.size()]);
            }
        }

        it = peerToContact.iterator();

        // asking to contact list peers for new JVM ....
        while ((res.size() < n) && (it.hasNext())) {
            P2PService current = ((KnownTableElement) it.next()).getP2PService();

            ProActiveRuntime[] currentRes;

            try {
                String peerName = null;

                if (logger.isInfoEnabled()) {
                    peerName = current.getServiceName();

                    logger.info(this.getServiceName() + " asking " + peerName +
                        " for " + (n - res.size()) + " new source .... ");
                }

                currentRes = current.getProActiveJVMs(n - res.size(), TTL - 1,
                        parentList);

                if (logger.isInfoEnabled()) {
                    if (currentRes != null) {
                        logger.info("Finnally " + peerName + " has return " +
                            currentRes.length + " JVM");
                    } else {
                        logger.info("Finnally " + peerName +
                            " has return 0 JVM");
                    }
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could't get JVMS", e);
                }

                continue;
            }

            if (currentRes != null) {
                for (int j = 0; j < currentRes.length; j++) {
                    res.put(currentRes[j].getVMInformation().getName(),
                        currentRes[j]);
                }
            }
        }

        // res.remove(this.runtimeName);
        Vector resFinal = new Vector(res.values());

        if (resFinal.size() > n) {
            return (ProActiveRuntime[]) resFinal.subList(0, n).toArray(new ProActiveRuntime[n]);
        } else {
            return (ProActiveRuntime[]) resFinal.toArray(new ProActiveRuntime[resFinal.size()]);
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
                Object par = elem.getP2PService().getProActiveRuntime();

                if (par instanceof ProActiveRuntime) {
                    res.add(par);
                }
            } catch (Exception e) {
                // The remote is dead => remove from known table
                this.unregisterP2PService((String) elem.getKey());
            }
        }

        return (ProActiveRuntime[]) res.toArray(new ProActiveRuntime[res.size()]);
    }

    /**
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProActiveRuntime()
     */
    public Object getProActiveRuntime() {
        if (this.serviceInfo.getFreeLoad() > 0) {
            this.setLoad(this.serviceInfo.getFreeLoad() - 1);

            if (logger.isInfoEnabled()) {
                logger.info(this.getServiceName() +
                    " give is ProActiveRuntime");
            }

            return this.runtime;
        } else {
            // As it's not possible to return null when using Future we return a
            // FakeProActiveRuntime object
            if (logger.isInfoEnabled()) {
                logger.info(this.getServiceName() +
                    " doesn't give is ProActiveRuntime because load is full");
            }

            return this.fakeProActiveRuntime;
        }
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
        return this.peerUrl;
    }

    // -------------------------------------------------------------------------
    // Implements Load
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.p2p.core.load.Load#setLoad(int)
     */
    public void setLoad(int load) {
        this.serviceInfo.setFreeLoad(load);

        Object[] values = this.knownProActiveJVM.toArray();

        for (int i = 0; i < values.length; i++) {
            P2PService service = ((KnownTableElement) values[i]).getP2PService();
            service.registerP2PService(this.getServiceName(), this.serviceInfo,
                false);
        }
    }

    /**
     * @see org.objectweb.proactive.p2p.core.load.Load#getLoad()
     */
    public int getLoad() {
        // !!! return MAXLOAD - currentLoad !!!
        return this.serviceInfo.getFreeLoad();
    }

    public void printKnownPeer() {
        this.knownProActiveJVM.printKnownPeer();
    }

    // -------------------------------------------------------------------------
    // Init Activity
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            this.serviceInfo.setService((P2PService) ProActive.getStubOnThis());

            // Create the known table
            this.knownProActiveJVM = (KnownTable) ProActive.newActive(KnownTable.class.getName(),
                    null, P2PServiceImpl.urlAdderP2PNodeName(this.peerUrl));

            ProActive.enableAC(this.knownProActiveJVM);

            // Launch a thread to update the known table.
            Object[] params = {
                    this.knownProActiveJVM, ProActive.getStubOnThis(),
                    this.getServiceName()
                };

            ProActive.newActive(Updater.class.getName(), params,
                P2PServiceImpl.urlAdderP2PNodeName(this.peerUrl));
        } catch (ActiveObjectCreationException e) {
            logger.error("Could't create the Updater");
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
        return (this.getServiceName().compareTo(((P2PService) obj).getServiceName()) == 0)
        ? true : false;
    }

    /**
     * This class is use as a possible return value for the method
     * getProActiveRuntime() We need it because the call to the method lead to
     * the creation of a future, so we can't return null if the p2pservice
     * doesn't want to give is runtime. We can use this class to store some
     * information about future availability of the peer ...
     *
     * @author vcave
     *
     * TODO To change the template for this generated type comment go to Window -
     * Preferences - Java - Code Style - Code Templates
     */
    public class FakeProActiveRuntime implements Serializable {
        public FakeProActiveRuntime() {
        }
    }
}
