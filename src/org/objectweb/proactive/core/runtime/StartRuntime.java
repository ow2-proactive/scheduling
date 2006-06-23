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
package org.objectweb.proactive.core.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class is a utility class allowing to start a ProActiveRuntime with a JVM.
 * </p><p>
 * This class is mainly used with ProActiveDescriptor to start a ProActiveRuntime
 * on a local or remote JVM.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/08/29
 * @since   ProActive 0.9
 *
 */
public class StartRuntime {
    //Name of the runtime that launched this class reading the ProActiveDescriptor
    //private static final String DefaultRuntimeName = "PART_DEFAULT";
    //Name of the runtime's host that launched this class reading the ProActiveDescriptor
    static Logger logger = ProActiveLogger.getLogger(Loggers.RUNTIME);
    protected String defaultRuntimeURL;
    protected String nodeURL;
    protected String creatorID;
    protected ProActiveRuntime proActiveRuntime;

    //protected String acquisitionMethod;
    protected String nodeNumber;
    protected String vmName;
    protected int nodenumber; //it is only the int value of nodeNumber
    protected String protocolId;

    protected StartRuntime() {
    }

    private StartRuntime(String[] args) {
        if (args.length != 0) {
            this.nodeURL = args[0];
            this.creatorID = args[0].trim();

            //System.out.println(creatorID);
            this.defaultRuntimeURL = UrlBuilder.removeUsername(args[1]);

            //this.acquisitionMethod = args[2];
            this.nodeNumber = args[2];

            //   this.portNumber = Integer.parseInt(args[4]);
            this.nodenumber = (new Integer(nodeNumber)).intValue();
            this.protocolId = args[3];
            this.vmName = args[4];
        }
    }

    public static void main(String[] args) {
        if ("true".equals(System.getProperty("log4j.defaultInitOverride")) &&
                (System.getProperty("log4j.configuration") != null)) {
            // configure log4j here to avoid classloading problems with log4j classes
            try {
                String log4jConfiguration = System.getProperty(
                        "log4j.configuration");
                File f = new File(log4jConfiguration);
                PropertyConfigurator.configure(new URL(f.getPath()));
            } catch (IOException e) {
                System.out.println(
                    "Error : incorrect path for log4j configuration : " +
                    System.getProperty("log4j.configuration"));
            }
        }

        ProActiveConfiguration.load();

        try {
            logger.info("**** Starting jvm on " +
                UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost()));

            if (logger.isDebugEnabled()) {
                logger.debug("**** Starting jvm with classpath " +
                    System.getProperty("java.class.path"));
                logger.debug("****              with bootclasspath " +
                    System.getProperty("sun.boot.class.path"));
            }
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
        }

        new StartRuntime(args).run();
    }

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Runs the complete creation and registration of a ProActiveRuntime and creates a
     * node once the creation is completed.
     */
    private void run() {
        ProActiveRuntimeImpl impl = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
        impl.getVMInformation().setCreationProtocolID(protocolId);

        if (defaultRuntimeURL != null) {
            ProActiveRuntime PART;
            try {
                PART = RuntimeFactory.getRuntime(defaultRuntimeURL,
                        UrlBuilder.getProtocol(defaultRuntimeURL));
                register(PART);
                impl.setParent(PART);
            } catch (ProActiveException e) {
                e.printStackTrace();
                //				 if we cannot get the parent, this jvm is useless
                System.exit(0);
            }
        }
    }

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Performs the registration of a ProActiveRuntime on the runtime that initiated the creation
     * of ProActiveDescriptor.
     */
    private void register(ProActiveRuntime PART) {
        try {
            proActiveRuntime = RuntimeFactory.getProtocolSpecificRuntime(System.getProperty(
                        "proactive.communication.protocol") + ":");

            PART.register(proActiveRuntime, proActiveRuntime.getURL(),
                creatorID,
                System.getProperty("proactive.communication.protocol") + ":",
                vmName);
        } catch (ProActiveException e) {
            e.printStackTrace();

            // if we cannot register, this jvm is useless
            System.exit(0);
        }
    }
}
