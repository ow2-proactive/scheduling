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


public class LSFInfrastructure extends BatchJobInfrastructure {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;

    public LSFInfrastructure() {
        this.submitJobOpt = "";
    }

    @Override
    protected String extractSubmitOutput(String output) {
        Pattern pattern = Pattern.compile(".*<(\\d+)>.*");
        Matcher matcher = pattern.matcher(output);
        try {
            while (matcher.find()) {
                String result = matcher.group(1);
                logger.debug("jobID retrieved from submit command output: " + result);
                return result;
            }
        } catch (IndexOutOfBoundsException e) {
            logger.trace("cannot retrieve jobID from output: " + output, e);
            //no such group
        }
        logger.debug("no jobID retrieved from submit command output: " + output);
        return null;
    }

    @Override
    protected String getBatchinJobSystemName() {
        return "LSF";
    }

    @Override
    protected String getDeleteJobCommand() {
        return "bkill";
    }

    @Override
    protected String getSubmitJobCommand() {
        return "bsub";
    }

}
