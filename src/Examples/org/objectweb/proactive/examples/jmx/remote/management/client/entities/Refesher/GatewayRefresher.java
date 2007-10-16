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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.jmx.remote.management.client.entities.Refesher;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.examples.jmx.remote.management.client.entities.RemoteGateway;
import org.objectweb.proactive.examples.jmx.remote.management.client.jmx.FrameworkConnection;
import org.objectweb.proactive.examples.jmx.remote.management.mbean.BundleInfo;
import org.objectweb.proactive.examples.jmx.remote.management.utils.Constants;


public class GatewayRefresher {
    private RemoteGateway gateway;
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(50, 60,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private FrameworkConnection connection;
    private boolean persistantData;

    public GatewayRefresher(RemoteGateway gw, FrameworkConnection connection) {
        this.gateway = gw;
        this.connection = connection;
    }

    public void launch() {
        executor.execute(new MyRunnable(gateway));
    }

    private class MyRunnable implements Runnable {
        private RemoteGateway gateway;
        private GenericTypeWrapper<ArrayList> future;

        public MyRunnable(RemoteGateway rg) {
            this.gateway = rg;
        }

        public void run() {
            if (connection.isConnected()) {
                ProActiveConnection paConn = connection.getConnection();

                ObjectName onUrl;
                try {
                    onUrl = new ObjectName(Constants.ON_URL);

                    String url = ((GenericTypeWrapper<String>) paConn.getAttributeAsynchronous(onUrl,
                            "Url")).getObject();
                    gateway.setUrl(url);

                    GenericTypeWrapper<Object> gtw = (GenericTypeWrapper<Object>) paConn.getAttributeAsynchronous(gateway.getOn(),
                            "Bundles");

                    ProFuture.waitFor(gtw);
                    Object o = gtw.getObject();
                    if (o instanceof ArrayList) {
                        gateway.setBundles((ArrayList<BundleInfo>) o);
                    } else if (o instanceof Exception) {
                        System.out.println("Exception recue");
                        ((Exception) o).printStackTrace();
                    }
                } catch (MalformedObjectNameException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
