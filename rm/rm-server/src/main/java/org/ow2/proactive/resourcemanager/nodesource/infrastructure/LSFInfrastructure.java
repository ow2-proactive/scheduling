/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LSFInfrastructure extends BatchJobInfrastructure {

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
