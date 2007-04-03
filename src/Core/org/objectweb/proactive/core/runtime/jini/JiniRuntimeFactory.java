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
package org.objectweb.proactive.core.runtime.jini;

import java.rmi.RemoteException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jini.ServiceLocatorHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;


public class JiniRuntimeFactory extends RuntimeFactory {
    protected static int MAX_RETRY = 3;

    //private final static long WAITFOR = 100000L;
    protected static ServiceLocatorHelper serviceLocatorHelper = new ServiceLocatorHelper();
    private static ProActiveRuntime defaultJiniRuntime = null;

    static {
        if (JiniRuntimeFactory.class.getClassLoader() != null) {
            if (runtimeLogger.isDebugEnabled()) {
                runtimeLogger.debug("JiniRuntimeFactory created with " +
                    JiniRuntimeFactory.class.getClassLoader().getClass()
                                            .getName());
            }
        }
    }

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public JiniRuntimeFactory() throws java.io.IOException {
        // Obligatoire d'avoir le security manager fixe
        if ((System.getSecurityManager() == null) &&
                !("false".equals(System.getProperty("proactive.securitymanager")))) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }

        //serviceLocatorHelper.initializeServiceLocator();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected synchronized ProActiveRuntime getProtocolSpecificRuntimeImpl()
        throws ProActiveException {
        //return createRuntimeAdapter(s,false);
        if (defaultJiniRuntime == null) {
            serviceLocatorHelper.initializeServiceLocator();
            defaultJiniRuntime = createRuntimeAdapter();
        }

        return defaultJiniRuntime;
    }

    @Override
    protected ProActiveRuntime getRemoteRuntimeImpl(String s)
        throws ProActiveException {
        runtimeLogger.info("> JiniRuntimeFactory.getJiniRuntimeImpl(" + s +
            ")");

        String host = null;
        Entry[] entries;
        RemoteProActiveRuntime jiniRuntime = null;
        LookupLocator lookup = null;
        ServiceRegistrar registrar = null;

        host = UrlBuilder.getHostNameFromUrl(s);
        runtimeLogger.info("Try to find the service lookup on host: " + host);

        //ServiceDiscoveryManager clientMgr = null;
        if (host != null) {
            // recherche unicast
            try {
                lookup = new LookupLocator("jini://" + host);
                runtimeLogger.info("Service lookup found ");
                registrar = lookup.getRegistrar();
            } catch (java.net.MalformedURLException e) {
                throw new ProActiveException("Lookup failed: " +
                    e.getMessage());
            } catch (java.io.IOException e) {
                runtimeLogger.error("Registrar search failed: " +
                    e.getMessage());

                if (MAX_RETRY-- > 0) {
                    runtimeLogger.info(
                        "failed to contact the service lookup, retrying ...");
                    getRemoteRuntimeImpl(s);
                } else {
                    throw new ProActiveException(
                        "Cannot contact the lookup service for node " + s);
                }
            } catch (java.lang.ClassNotFoundException e) {
                throw new ProActiveException("Registrar search failed: " +
                    e.toString());
            }

            Class[] classes = new Class[] { RemoteProActiveRuntime.class };

            entries = new Entry[] { new Name(s) };

            ServiceTemplate template = new ServiceTemplate(null, classes,
                    entries);

            try {
                jiniRuntime = (RemoteProActiveRuntime) registrar.lookup(template);

                if (jiniRuntime == null) {
                    throw new ProActiveException("No service found for url: " +
                        s);
                }

                return createRuntimeAdapter(jiniRuntime);
            } catch (java.rmi.RemoteException e) {
                throw new ProActiveException(e);

                //e.printStackTrace();
            }
        } else {
            throw new ProActiveException("node url should not be null");
        }

        //    try {
        //      // recherche multicast
        //      LookupDiscoveryManager mgr = new LookupDiscoveryManager(LookupDiscovery.ALL_GROUPS,
        //                    null,null);
        //
        //      clientMgr = new ServiceDiscoveryManager(mgr,new LeaseRenewalManager());
        //    } catch (Exception e) {
        //      throw new ProActiveException("Remote",e);
        //    }
        //    Class[] classes = new Class[] {JiniRuntime.class};
        // construction de la template pour la recherche
        // on peut ne pas mettre le nom de l'objet
        // ensuite on recherche une certaine classe d'objet
        //    ServiceItem item = null;
        //  	try {
        //      item = clientMgr.lookup(template, null, WAITFOR);
        //    } catch (Exception e) {
        //      throw new ProActiveException("Remote",e);
        //    }
        //
        //    if (item == null) {
        //      System.out.println("no service found");
        //      return null;
        //    } else {
        //      jiniRuntime = (JiniRuntime) item.service;
        //      if (jiniRuntime == null) {
        //  System.out.println("item null");
        //  return null;
        //      }
        //      return createRuntimeAdapter(jiniRuntime);
        //    }
    }

    @Override
    protected ProActiveRuntimeAdapterImpl createRuntimeAdapter()
        throws ProActiveException {
        JiniRuntimeImpl impl;

        try {
            impl = new JiniRuntimeImpl();
        } catch (RemoteException e) {
            throw new ProActiveException("Cannot create the JiniRuntimeImpl", e);
        }

        return new ProActiveRuntimeAdapterImpl(impl);
    }

    public static void setMulticastLocator(boolean multicastLocator) {
        serviceLocatorHelper.setMulticastLocator(multicastLocator);
    }
}
