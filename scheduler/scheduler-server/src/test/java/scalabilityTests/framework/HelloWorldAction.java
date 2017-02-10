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
package scalabilityTests.framework;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;


/**
 * Test(hello world) action. 
 * @author fabratu
 *
 */
public class HelloWorldAction implements Action<String, Void>, Serializable {

    private final static Logger logger = Logger.getLogger(HelloWorldAction.class);

    public HelloWorldAction() {
    }

    public Void execute(String message) {
        logger.info(message + " from " + ProActiveInet.getInstance().getHostname());
        return null;
    }

    @Override
    public String toString() {
        return "Trivial Action";
    }

}
