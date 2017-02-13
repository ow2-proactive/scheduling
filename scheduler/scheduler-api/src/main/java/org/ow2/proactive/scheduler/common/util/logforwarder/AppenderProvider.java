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
package org.ow2.proactive.scheduler.common.util.logforwarder;

import java.io.Serializable;

import org.apache.log4j.Appender;


/**
 * An appender provider is a container that can be sent over the network and that contains a log appender.
 * Actual creation and activation of the contained appender should be performed by the getAppender() method
 * on receiver side.
 */
public interface AppenderProvider extends Serializable {

    /**
     * Create and return the contained appender. Note that several call to getAppender() return the same appender instance is not specified.
     * @return an instance of the contained log appender.
     * @throws LogForwardingException if the appender cannot be created or activated.
     */
    Appender getAppender() throws LogForwardingException;

}
