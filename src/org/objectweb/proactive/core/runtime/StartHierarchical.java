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
package org.objectweb.proactive.core.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.util.UrlBuilder;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only**</font></i><br>
 * <p>
 * This class is a utility class allowing to start a ProActiveRuntimeForwarder
 * with a JVM.
 * </p>

 * @author  ProActive Team
 */
public class StartHierarchical {
    static Logger logger = Logger.getLogger(StartHierarchical.class.getName());
    protected String nodeURL;
    protected String creatorID;
    protected String defaultRuntimeURL;
    protected String sNodeNumber;
    protected int nodeNumber;
    protected String protocolId;
    protected String vmName;
    protected String padURL;
    protected ProActiveRuntime proActiveRuntime;

    protected StartHierarchical() {
    }

    private StartHierarchical(String[] args) {
        if (args.length != 0) {
            this.nodeURL = args[0];
            this.creatorID = args[0].trim();

            //System.out.println(creatorID);
            this.defaultRuntimeURL = args[1];

            //this.acquisitionMethod = args[2];
            this.sNodeNumber = args[2];

            //   this.portNumber = Integer.parseInt(args[4]);
            this.nodeNumber = (new Integer(sNodeNumber)).intValue();
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

        new StartHierarchical(args).run();
    }

    private void run() {
        try {
            System.setProperty("proactive.hierarchicalRuntime", "true");
            padURL = System.getProperty("proactive.pad");
            proActiveRuntime = RuntimeFactory.getProtocolSpecificRuntime(System.getProperty(
                        "proactive.communication.protocol") + ":");
            proActiveRuntime.getVMInformation().setCreationProtocolID(protocolId);

            LocalProActiveRuntime localPart = (LocalProActiveRuntime) ProActiveRuntimeImpl.getProActiveRuntime();
            localPart.setParent(defaultRuntimeURL);
        } catch (ProActiveException e) {
            e.printStackTrace();

            //we can still try to register if for instance an Alreadybound exception occurs
        }

        try {
            ProActiveRuntime PART = RuntimeFactory.getRuntime(defaultRuntimeURL,
                    UrlBuilder.getProtocol(defaultRuntimeURL));

            ExternalProcess process = PART.getProcessToDeploy(proActiveRuntime,
                    creatorID, vmName, padURL);

            if (process == null) {
                logger.info("getProcessToDeploy failed. Aborting");
                System.exit(0);
            }

            ((ProActiveRuntimeForwarderImpl) ProActiveRuntimeImpl.getProActiveRuntime()).setProcessesToDeploy(padURL,
                vmName, process);

            try {
                setParameters(process);
                process.startProcess();
            } catch (IOException e) {
                logger.info("process starting failed: " + e.getMessage());
                System.exit(0);
            }
        } catch (ProActiveException e) {
            e.printStackTrace();

            // if we cannot get runtimes to deploy this JVM is useless
            System.exit(0);
        }
    }

    private void setParameters(ExternalProcess process) {
        String localruntimeURL = null;

        try {
            localruntimeURL = RuntimeFactory.getDefaultRuntime().getURL();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        JVMProcess jvmProcess = (JVMProcess) process.getFinalProcess();

        jvmProcess.setJvmOptions("-Dproactive.jobid=" + ProActive.getJobId());
        jvmProcess.setParameters(creatorID + " " + localruntimeURL + " " +
            nodeNumber + " " + protocolId + " " + vmName);
    }
}
