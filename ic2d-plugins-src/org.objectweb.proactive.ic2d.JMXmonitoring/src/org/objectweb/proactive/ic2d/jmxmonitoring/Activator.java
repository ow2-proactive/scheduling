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
package org.objectweb.proactive.ic2d.jmxmonitoring;

import java.util.Iterator;


//import javassist.ClassClassPath;
//import javassist.ClassPool;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "org.objectweb.proactive.ic2d.JMXmonitoring";

    // The shared instance
    private static Activator plugin;

    // The console name
    public static String CONSOLE_NAME = "Monitoring";

    /**
     * The constructor
     */
    public Activator() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);

        RuntimeFactory.getDefaultRuntime();
        ProActiveRuntimeImpl.getProActiveRuntime();

        //RuntimeFactory.getDefaultRuntime().getURL();

        // add current classpath for javassist class pool
        //       ClassPool pool = ClassPool.getDefault();
        //     pool.insertClassPath(new ClassClassPath(this.getClass()));

        //        URL u = PAProperties.class.getResource("proactive-log4j");
        //        Properties p = new Properties();
        //        p.load(u.openStream());
        //        PropertyConfigurator.configure(p);
    }

    /**
     *  Performs several clean operations before stopping this plugin:
     *    unsubscribes the  MXNotificationListener from all remote MBeans
     *    kill all nodes of this Runtime
     *    unregister this runtime
     *    unregister all HalfBodies of this Runtime
     *  Stop this plugin after the operations above are performed
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        JMXNotificationManager nm = JMXNotificationManager.getInstance();

        // Unsubscribe the JMXNotificationListener from all remote MBeans
        nm.kill();

        //Kill all nodes deployed by IC2D runtime. 
        ProActiveRuntimeImpl.getProActiveRuntime().killAllNodes();

        //Unregister the IC2d runtime from all registries
        ProActiveRuntimeImpl.getProActiveRuntime().getRemoteObjectExposer()
                            .unregisterAll();

        //
        unregisterAllHalfBodiesFromRegistry();
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Unregisters from all registries the HalfBodies of this JVM
     */
    private void unregisterAllHalfBodiesFromRegistry() {
        BodyMap bm = LocalBodyStore.getInstance().getLocalHalfBodies();

        System.out.println(bm.size() + " half bodies found in the registry. ");
        Iterator<UniversalBody> halfBodies = bm.bodiesIterator();
        while (halfBodies.hasNext()) {
            UniversalBody universalBody = halfBodies.next();

            if (universalBody instanceof AbstractBody) {
                try {
                    RemoteObjectExposer roe = ((AbstractBody) universalBody).getRemoteObjectExposer();

                    roe.unregisterAll();
                    System.out.println("Unregistered Half Body: " +
                        universalBody.toString() + " ");
                } catch (Exception e) {
                    System.out.println("Could not unregister HalfBody " +
                        universalBody + " from registry." + e.getMessage());
                }
            }
        }
    }
}
