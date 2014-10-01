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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;


/**
 * JavaStandaloneExecutableInitializer is the class used to store context of java standalone executable initialization
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JavaStandaloneExecutableInitializer extends DefaultStandaloneExecutableInitializer {

    /** Arguments of the java task */
    protected Map<String, byte[]> serializedArguments;

    /** Propagated variables from parent tasks */
    protected Map<String, byte[]> propagatedVariables;

    private Map<String, String> thirdPartyCredentials;

    /**
     * @throws java.io.IOException if the deserialization of the value cannot be performed.
     * @throws ClassNotFoundException if the value's class cannot be loaded.
     */
    public Map<String, Serializable> getArguments(ClassLoader cl) throws IOException, ClassNotFoundException {
        return SerializationUtil.deserializeVariableMap(this.serializedArguments, cl);
    }

    /**
     * Return a map containing all the task arguments serialized as byte[].
     *
     * @return the serialized arguments map.
     */
    public Map<String, byte[]> getSerializedArguments() {
        return serializedArguments;
    }

    /**
     * Set an argument
     *
     * @param key key of the argument to set
     * @param arg de-serialized version of the argument
     */
    public void setArgument(String key, Serializable arg) {
        SerializationUtil.serializeAndSetVariable(key, arg, this.serializedArguments);
    }

    /**
     * Set the arguments value to the given arguments value
     *
     * @param serializedArguments the arguments to set
     */
    public void setSerializedArguments(Map<String, byte[]> serializedArguments) {
        this.serializedArguments = serializedArguments;
    }

    /**
     * Sets the propagated variable map for the current Java task.
     *
     * @param propagatedVariables
     *            a map of propagated variables
     */
    public void setPropagatedVariables(Map<String, byte[]> propagatedVariables) {
        this.propagatedVariables = propagatedVariables;
    }

    /**
     * Returns the propagated variables map of the current Java task.
     *g 
     * @return a map of variables
     */
    public Map<String, byte[]> getPropagatedVariables() {
        return propagatedVariables;
    }

    public Map<String, String> getThirdPartyCredentials() {
        return thirdPartyCredentials;
    }

    public void setThirdPartyCredentials(Map<String, String> thirdPartyCredentials) {
        this.thirdPartyCredentials = thirdPartyCredentials;
    }

}
