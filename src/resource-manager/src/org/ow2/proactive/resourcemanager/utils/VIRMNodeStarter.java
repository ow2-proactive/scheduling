/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;


public class VIRMNodeStarter extends RMNodeStarter {

    public static final char OPTION_TEMPLATE_NAME = 't';
    public static final String HOLDING_VM_KEY = "holdingVM";

    private String holdingVM = null;

    protected void fillParameters(final CommandLine cl, final Options options) {
        try {
            // Holding vm's name
            if (cl.hasOption(OPTION_TEMPLATE_NAME)) {
                this.holdingVM = cl.getOptionValue(OPTION_TEMPLATE_NAME);
            } else {
                System.out.println(VIRMNodeStarter.ExitStatus.HOLDING_VM_NOT_SET.description);
                System.err.println(VIRMNodeStarter.ExitStatus.HOLDING_VM_NOT_SET.description);
                System.exit(VIRMNodeStarter.ExitStatus.HOLDING_VM_NOT_SET.exitCode);
            }
            super.fillParameters(cl, options);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(RMNodeStarter.ExitStatus.FAILED_TO_LAUNCH.exitCode);
        }
    }

    /**
     * Fills the command line options.
     * @param options the options to fill
     */
    protected void fillOptions(final Options options) {
        // The name of the holding virtual machine
        final Option vmName = new Option(new Character(OPTION_TEMPLATE_NAME).toString(), "holdingVMName",
            true, "The name of the virtual machine within which one this RMNode is to be started.");
        vmName.setRequired(true);
        vmName.setArgName("name");
        options.addOption(vmName);
        super.fillOptions(options);
    }

    public static void main(String[] args) {
        VIRMNodeStarter starter = new VIRMNodeStarter();
        //we don't want to ping the RM to be able to cache nodes
        //when removing them from the RM
        RMNodeStarter.PING_DELAY_IN_MS = -1;
        starter.doMain(args);
    }

    @Override
    protected Node createLocalNode(String nodeName) {
        Node localNode = null;
        try {
            localNode = NodeFactory.createLocalNode(nodeName, false, null, null);
            if (localNode == null) {
                System.out.println("The node returned by the NodeFactory is null");
                System.err.println(RMNodeStarter.ExitStatus.RMNODE_NULL.description);
                System.exit(RMNodeStarter.ExitStatus.RMNODE_NULL.exitCode);
            }
            //this property is set to be able to which node source this node must
            //be added once added to the RM
            localNode.setProperty(VIRMNodeStarter.HOLDING_VM_KEY, this.holdingVM);
        } catch (Throwable t) {
            System.out.println("Unable to create the local node " + nodeName);
            t.printStackTrace();
            System.exit(RMNodeStarter.ExitStatus.RMNODE_ADD_ERROR.exitCode);
        }
        return localNode;
    }

    public enum ExitStatus {
        HOLDING_VM_NOT_SET(2, "Cannot determine the holding virtual machine");
        public final int exitCode;
        public final String description;

        private ExitStatus(int exitCode, String description) {
            this.exitCode = exitCode;
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }

        public int getExitCode() {
            return this.exitCode;
        }
    }
}
