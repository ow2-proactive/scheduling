/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;


/**
 * JavaExecutableInitializer is used to initialize the java executable.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public interface JavaExecutableInitializer extends StandaloneExecutableInitializer {

    /**
     * @throws java.io.IOException if the deserialization of the value cannot be performed.
     * @throws ClassNotFoundException if the value's class cannot be loaded.
     */
    Map<String, Serializable> getArguments(ClassLoader cl) throws IOException, ClassNotFoundException;

    /**
     * Return a map containing all the task arguments serialized as byte[].
     *
     * @return the serialized arguments map.
     */
    Map<String, byte[]> getSerializedArguments();

    /**
     * Set an argument
     *
     * @param key key of the argument to set
     * @param arg de-serialized version of the argument
     */
    void setArgument(String key, Serializable arg);

    /**
     * Set the arguments value to the given arguments value
     *
     * @param serializedArguments the arguments to set
     */
    void setSerializedArguments(Map<String, byte[]> serializedArguments);

    /**
     * Sets the propagated variable map for the current Java task.
     *
     * @param propagatedVariables
     *            a map of propagated variables
     */
    void setPropagatedVariables(Map<String, byte[]> propagatedVariables);

    /**
     * Returns the propagated variables map of the current Java task.
     *
     * @return a map of variables
     */
    Map<String, byte[]> getPropagatedVariables();
}
