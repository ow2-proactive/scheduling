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
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is a utility class allowing to start a ProActiveRuntime with a
 * JVM.
 *
 */
public class StartRuntime {
    static Logger logger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    /** The URL of the parent ProActive Runtime */
    protected String parentURL;

    /** Command Line arguments */
    private String[] args;

    public static void main(String[] args) {
        try {
            initLog4j();
            ProActiveConfiguration.load();
            StartRuntime startRuntime = new StartRuntime(args);
            startRuntime.start();
        } catch (Exception e) {
            e.printStackTrace();
            abort();
        }
    }

    static private void abort(String str) {
        if ((str != null) && (str.length() > 0)) {
            System.err.println(str);
        }
        System.out.flush();
        System.err.flush();
        System.exit(1);
    }

    static private void abort() {
        abort("");
    }

    static private boolean initLog4j() {
        boolean ret = true;

        if ("true".equals(System.getProperty("log4j.defaultInitOverride"))) {
            // configure log4j here to avoid classloading problems with
            // log4j classes
            String log4jConfiguration = System.getProperty(
                    "log4j.configuration");
            if (log4jConfiguration != null) {
                try {
                    File f = new File(log4jConfiguration);
                    PropertyConfigurator.configure(new URL(f.getPath()));
                } catch (IOException e) {
                    System.err.println(
                        "Error : incorrect path for log4j configuration : " +
                        System.getProperty("log4j.configuration"));
                    ret &= false;
                }
            } else {
                System.err.println(
                    "-Dlog4.defaultInitOverride is specified but -Dlog4j.configuration property is missing");
                ret &= false;
            }
        }
        return ret;
    }

    protected StartRuntime(String[] args) {
        this.args = args;
        parseOptions();
    }

    private void parseOptions() {
        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption(Params.parent.toString(), Params.parent.sOpt, true,
            Params.parent.desc);

        CommandLine line;

        try {
            line = parser.parse(options, args);
            parentURL = line.getOptionValue(Params.parent.toString());
        } catch (ParseException e) {
            logger.warn("Cannot parse command line arguments", e);
            abort();
        }
    }

    private void start() {
        // Print some information message
        try {
            logger.info("Starting a ProActiveRuntime on " +
                UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost()));
        } catch (UnknownHostException e1) {
            logger.error("Hostname cannot be determined", new Exception());
            abort();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("**** Starting jvm with classpath " +
                System.getProperty("java.class.path"));
            logger.debug("****              with bootclasspath " +
                System.getProperty("sun.boot.class.path"));
        }

        // Creation & Setup of the local ProActive Runtime
        ProActiveRuntimeImpl localRuntimeImpl = ProActiveRuntimeImpl.getProActiveRuntime();
        ProActiveRuntime localRuntime;
        try {
            localRuntime = RuntimeFactory.getProtocolSpecificRuntime(ProActiveConfiguration.getInstance()
                                                                                           .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL));
        } catch (ProActiveException e1) {
            logger.warn("Cannot get the local ProActive Runtime", e1);
            abort();
        }

        // Say hello to our parent if needed
        if (parentURL != null) {
            ProActiveRuntime parentRuntime;
            try {
                parentRuntime = RuntimeFactory.getRuntime(parentURL,
                        UrlBuilder.getProtocol(parentURL));

                /*
                parentRuntime.register(localRuntime, localRuntime.getURL(),
                    "",
                    ProActiveConfiguration.getInstance()
                                          .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL),
                    "MKrisIlEstSympa");
                                */
                ProActiveRuntimeImpl.getProActiveRuntime()
                                    .setParent(parentRuntime);
                waitUntilInterupted();
            } catch (ProActiveException e) {
                logger.warn("Cannot register to my parent", e);
                abort();
            }
        }

        if (System.getProperty("proactive.runtime.stayalive") != null) {
            Object o = new Object();
            synchronized (o) {
                try {
                    o.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void waitUntilInterupted() {
        Object o = new Object();
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                logger.info(e);
            }
        }
    }
    private enum Params {parent("p", "URL of the parent ProActive Runtime");
        protected String sOpt;
        protected String desc;

        private Params(String sOpt, String desc) {
            this.sOpt = sOpt;
            this.desc = desc;
        }
    }
}
