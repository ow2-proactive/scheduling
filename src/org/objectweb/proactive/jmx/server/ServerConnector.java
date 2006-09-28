/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.jmx.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.osgi.connector.Activator;


/**
 *
 * @author ProActive Team
 *
 */
public class ServerConnector {
    private MBeanServer mbs;

    /**
     *
     *
     */
    public ServerConnector() {
        this.mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            useProActiveConnector("service:jmx:proactive:///jndi/proactive://localhost/server",
                this.mbs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param url
     * @param mbs
     * @throws IOException
     */
    private void useProActiveConnector(String url, MBeanServer mbs)
        throws IOException {
        JMXServiceURL url2 = new JMXServiceURL(url);
        Map env = new HashMap();

        //Utile ???
        Thread.currentThread()
              .setContextClassLoader(Activator.class.getClassLoader());
        env.put("jmx.remote.protocol.provider.pkgs",
            "org.objectweb.proactive.jmx.provider");
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url2,
                env, this.mbs);
        cs.start();

        /***    ****/
    }
    //    public void bindProActiveService(ProActiveService service) {
    //    	System.out.println("bindProActiveSErvice connector");
    //        try {
    //            useProActiveConnector("service:jmx:proactive:///jndi/proactive://localhost:8080/server",
    //                this.mbs);
    //        } catch (NullPointerException e) {
    //            e.printStackTrace();
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //
    //    public void unbindProActiveService(ProActiveService service) {
    //    }
}
