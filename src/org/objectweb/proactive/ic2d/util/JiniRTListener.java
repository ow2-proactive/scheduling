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
package org.objectweb.proactive.ic2d.util;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.jini.JiniRuntime;
import org.objectweb.proactive.core.runtime.jini.JiniRuntimeAdapter;

import java.rmi.ConnectException;
import java.rmi.RMISecurityManager;


public class JiniRTListener implements DiscoveryListener {
    static Logger log4jlogger = Logger.getLogger(JiniRTListener.class.getName());
    protected java.util.ArrayList runtimes = new java.util.ArrayList();
    private String host;
    private IC2DMessageLogger logger;

    public JiniRTListener(String _host, IC2DMessageLogger logger) {
        host = _host;
        this.logger = logger;
        if ((System.getSecurityManager() == null) &&
                !("false".equals(System.getProperty("proactive.securitymanager")))) {
            System.setSecurityManager(new RMISecurityManager());
        }

        //    this.logger = logger;
        LookupDiscovery discover = null;
        LookupLocator lookup = null;
        if (host != null) {
            logger.log("Exploring " + host + " with JINI ");
            // recherche unicast
            try {
                lookup = new LookupLocator("jini://" + host);
                // System.out.println("Lookup.getRegistrar() on " + host);
                ServiceRegistrar registrar = lookup.getRegistrar();
                Class[] classes = new Class[] { JiniRuntime.class };
                JiniRuntime runtime = null;
                ServiceMatches matches = null;

                ServiceTemplate template = new ServiceTemplate(null, classes,
                        null);

                //System.out.println("JiniNodeListener:  lookup registrar");
                matches = registrar.lookup(template, Integer.MAX_VALUE);
                if (matches.totalMatches > 0) {
                    //System.out.println("matches "+matches.items.length);
                    for (int i = 0; i < matches.items.length; i++) {
                        //check if it is really a node and not a ProactiveRuntime
                        String jiniName = matches.items[i].attributeSets[0].toString();

                        //System.out.println("name of the node "+jiniName);
                        if ((jiniName.indexOf("PA_JVM") != -1)) {
                            // it is a runtime
                            int k = jiniName.indexOf("=");
                            String name = jiniName.substring(k + 1,
                                    jiniName.length() - 1);

                            //System.out.println("-----------------name "+name);
                            try {
                                if (matches.items[i].service == null) {
                                    log4jlogger.warn("Service : NULL !!!");
                                } else {
                                    runtime = (JiniRuntime) matches.items[i].service;
                                    ProActiveRuntime part = new JiniRuntimeAdapter(runtime);
                                    runtimes.add(part);
                                }
                            } catch (ProActiveException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    log4jlogger.error("JiniRTListener: No Service");
                }
            } catch (java.net.MalformedURLException e) {
                log4jlogger.error("Lookup failed: " + e.getMessage());
            } catch (java.io.IOException e) {
                log4jlogger.error("Registrar search failed: " + e.getMessage());
            } catch (java.lang.ClassNotFoundException e) {
                log4jlogger.error("Class Not Found: " + e.getMessage());
            }
        } else {
            //recherche multicast
            logger.log("Exploring the network with JINI multicast ");
            try {
                discover = new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
            } catch (Exception e) {
                log4jlogger.fatal(" JiniNodeFinder exception");
                e.printStackTrace();
            }

            discover.addDiscoveryListener(this);
        }
    }

    public void discovered(DiscoveryEvent evt) {
        ServiceRegistrar[] registrars = evt.getRegistrars();

        //System.out.println("registar lenght :"+registrars.length);
        Class[] classes = new Class[] { JiniRuntime.class };
        JiniRuntime runtime = null;
        ServiceMatches matches = null;
        ServiceTemplate template = new ServiceTemplate(null, classes, null);

        for (int n = 0; n < registrars.length; n++) {
            //System.out.println("JiniNodeListener:  Service found");
            ServiceRegistrar registrar = registrars[n];
            try {
                //System.out.println("JiniNodeListener:  lookup registrar");
                matches = registrar.lookup(template, Integer.MAX_VALUE);
                if (matches.totalMatches > 0) {
                    //System.out.println("matches "+matches.items.length);
                    for (int i = 0; i < matches.items.length; i++) {
                        //check if it is really a node and not a ProactiveRuntime
                        String jiniName = matches.items[i].attributeSets[0].toString();

                        //System.out.println("name of the node "+jiniName);
                        if ((jiniName.indexOf("PA_JVM") != -1)) {
                            // it is a runtime
                            int k = jiniName.indexOf("=");
                            String name = jiniName.substring(k + 1,
                                    jiniName.length() - 1);

                            //System.out.println("-----------------name "+name);
                            //                            try {
                            if (matches.items[i].service == null) {
                                log4jlogger.warn("Service : NULL !!!");
                            } else {
                                runtime = (JiniRuntime) matches.items[i].service;
                                ProActiveRuntime part = new JiniRuntimeAdapter(runtime);
                                runtimes.add(part);
                            }
                        }
                    }
                } else {
                    log4jlogger.error("JiniRTListener: No Service");
                }
            }catch (ConnectException e) {
              
                //System.err.println("JiniNodeListener RemoteException ");
                continue;
                //e.printStackTrace();
            }
             catch (java.rmi.RemoteException e) {
                //logger.log(e);
                //System.err.println("JiniNodeListener RemoteException ");
                //e.printStackTrace();
                continue;
                //e.printStackTrace();
            } catch (ProActiveException e) {
                //logger.log(e);
               //e.printStackTrace();
                continue;
            }
        }

        //System.out.println("JiniNodeListener: Fin recherche multicast");
    }

    public void discarded(DiscoveryEvent evt) {
    }

    public java.util.ArrayList getRuntimes() {
        return runtimes;
    }
}
