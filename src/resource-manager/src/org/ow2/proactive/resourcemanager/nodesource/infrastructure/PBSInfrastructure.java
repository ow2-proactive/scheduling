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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * Acquires nodes provided by an existing PBS cluster
 * <p>
 * The point of this Infrastructure is to interface with
 * an existing installation of a PBS (ie Torque) Scheduler:
 * node acquisition will be achieved by running
 * runtimes as Torque jobs: submitting a job acquires a node,
 * killing it stops it.
 * <p>
 * PBS jobs will be submitted through SSH from the RM to the PBS server;
 * make sure the RM and the nodes will be able to communicate once
 * the node is up.
 * <p>
 * If you need more control over you deployment, you may consider
 * using {@link GCMInfrastructure} instead, which contains the 
 * functionalities of this Infrastructure, but requires more configuration.
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 * 
 */
public class PBSInfrastructure extends BatchJobInfrastructure {

    public PBSInfrastructure() {
        this.submitJobOpt = "-l \"nodes=1:ppn=1\"";
    }

    @Override
    protected String getBatchinJobSystemName() {
        return "PBS";
    }

    @Override
    protected String getDeleteJobCommand() {
        return "qdel";
    }

    @Override
    protected String getSubmitJobCommand() {
        return "qsub";
    }

    @Override
    protected String extractSubmitOutput(String output) {
        Pattern pattern = Pattern.compile("(\\d+)[.]*.*");
        Matcher matcher = pattern.matcher(output);
        try {
            while (matcher.find()) {
                String result = matcher.group(1);
                logger.debug("jobID retrieved from submit command output: " + result);
                return result;
            }
        } catch (IndexOutOfBoundsException e) {
            logger.trace("cannot retrieve jobID from output: " + output, e);
        }
        logger.debug("no jobID retrieved from submit command output: " + output);
        return null;
    }
}
