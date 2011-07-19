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
package org.ow2.proactive.scheduler.common.task;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher.OneShotDecrypter;
import org.ow2.proactive.utils.NodeSet;


/**
 * JavaExecutableInitializer is the class used to store context of java executable initialization
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JavaExecutableInitializer implements ExecutableInitializer {

    /**  */
    private static final long serialVersionUID = 31L;

    /** Demanded nodes */
    protected NodeSet nodes;

    /** Arguments of the java task */
    protected Map<String, byte[]> serializedArguments;

    /**
     * Get the nodes list
     *
     * @return the nodes list
     */
    public NodeSet getNodes() {
        return nodes;
    }

    /**
     * Set the nodes list value to the given nodes value
     *
     * @param nodes the nodes to set
     */
    public void setNodes(NodeSet nodes) {
        this.nodes = nodes;
    }

    /**
     * Get the arguments of the executable. Instances are created from the serialized version.
     *
     * @return the arguments of the executable
     * @throws IOException if the deserialization of the value cannot be performed.
     * @throws ClassNotFoundException if the value's class cannot be loaded.
     */
    public Map<String, Serializable> getArguments(ClassLoader cl) throws IOException, ClassNotFoundException {
        final Map<String, Serializable> deserialized = new HashMap<String, Serializable>(
            this.serializedArguments.size());
        for (Entry<String, byte[]> e : this.serializedArguments.entrySet()) {
            deserialized.put(e.getKey(), (Serializable) ByteToObjectConverter.ObjectStream.convert(e
                    .getValue(), cl));
        }
        return deserialized;
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
        if (key != null && key.length() > 255) {
            throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " + key);
        } else {
            byte[] serialized = null;
            try {
                serialized = ObjectToByteConverter.ObjectStream.convert(arg);
                this.serializedArguments.put(key, serialized);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot add argument " + key, e);
            }
            this.serializedArguments.put(key, serialized);
        }
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
     * {@inheritDoc}
     */
    public OneShotDecrypter getDecrypter() {
        throw new RuntimeException("Should not be called in this context");
    }

    /**
     * {@inheritDoc}
     */
    public void setDecrypter(OneShotDecrypter decrypter) {
        throw new RuntimeException("Should not be called in this context");
    }

}
