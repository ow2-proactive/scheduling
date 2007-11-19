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
package org.objectweb.proactive.examples.masterworker;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;


public abstract class AbstractExample {
    protected Options command_options;
    protected URL descriptor_url;
    protected String vn_name;
    protected String usage_message = "Usage: <java_command> descriptor_path virtual_node_name";
    protected ProActiveMaster<?extends Task<?extends Serializable>, ?extends Serializable> abstract_master;
    protected CommandLine cmd = null;
    public static final String DEFAULT_DESCRIPTOR = "/org/objectweb/proactive/examples/masterworker/WorkersLocal.xml";

    public AbstractExample() {
        command_options = new Options();
        command_options.addOption("d", true, "descriptor in use");
        command_options.addOption("n", true, "virtual node name");
    }

    /**
     * Returns the url of the descriptor which defines the workers
     * @return descriptor url
     */
    public URL getDescriptor_url() {
        return descriptor_url;
    }

    /**
     * Sets the url of the descriptors which defines the workers
     * @param descriptor_url
     */
    public void setDescriptor_url(URL descriptor_url) {
        this.descriptor_url = descriptor_url;
    }

    /**
     * Returns the virtual node name of the workers
     * @return virtual node name
     */
    public String getVn_name() {
        return vn_name;
    }

    /**
     * Sets the virtual node name of the workers
     * @param vn_name virtual node name
     */
    public void setVn_name(String vn_name) {
        this.vn_name = vn_name;
    }

    /**
     * Initializing the example with command line arguments
     * @param args command line arguments
     * @throws MalformedURLException
     */
    protected void init(String[] args) throws MalformedURLException {
        before_init();

        CommandLineParser parser = new PosixParser();

        try {
            cmd = parser.parse(command_options, args);
        } catch (ParseException e) {
            System.err.println("Parsing failed, reason, " + e.getMessage());
            System.exit(1);
        }

        // get descriptor option value
        String descPath = cmd.getOptionValue("d");

        if (descPath == null) {
            descriptor_url = AbstractExample.class.getResource(DEFAULT_DESCRIPTOR);
            if (descriptor_url == null) {
                System.err.println("Couldn't find internal ressource: " +
                    DEFAULT_DESCRIPTOR);
                System.exit(1);
            }
        } else {
            // check provided descriptor
            File descriptorFile = new File(descPath);
            if (!descriptorFile.exists()) {
                System.err.println("" + descriptorFile + " does not exist");
                System.exit(1);
            } else if (!descriptorFile.canRead()) {
                System.err.println("" + descriptorFile + " can't be read");
                System.exit(1);
            } else if (!descriptorFile.isFile()) {
                System.err.println("" + descriptorFile +
                    " is not a regular file");
                System.exit(1);
            }
            descriptor_url = descriptorFile.toURI().toURL();
        }

        // get vn option value
        String vn_name = cmd.getOptionValue("n");

        //      Creating the Master
        abstract_master = creation();

        registerShutdownHook();

        if (vn_name == null) {
            abstract_master.addResources(descriptor_url);
        } else {
            abstract_master.addResources(descriptor_url, vn_name);
        }

        after_init();
    }

    /**
     * Register a shutdown hook on this example which will terminate the master
     */
    protected void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownThread()));
    }

    /**
     * A method to be launched before the init method
     */
    protected abstract void before_init();

    /**
     * A method to be launched to create the master
     */
    protected abstract ProActiveMaster<?extends Task<?extends Serializable>, ?extends Serializable> creation();

    /**
     * A method to be launched after the init method
     */
    protected abstract void after_init();

    /**
     * Internal class which handles shutdown of Master/Worker applications
     * @author fviale
     *
     */
    protected class ShutdownThread implements Runnable {
        public ShutdownThread() {
        }

        public void run() {
            abstract_master.terminate(true);
        }
    }
}
