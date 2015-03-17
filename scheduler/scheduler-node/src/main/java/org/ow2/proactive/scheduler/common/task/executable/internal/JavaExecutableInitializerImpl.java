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

import java.util.ArrayList;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.OneShotDecrypter;
import org.ow2.proactive.utils.NodeSet;


/**
 * JavaExecutableInitializer is the class used to store context of java executable initialization
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JavaExecutableInitializerImpl extends JavaStandaloneExecutableInitializer implements
        ExecutableInitializer {

    private static final long serialVersionUID = 61L;

    private NodeSet nodes;

    private DataSpacesFileObject localSpaceFO;
    private DataSpacesFileObject inputSpaceFO;
    private DataSpacesFileObject outputSpaceFO;
    private DataSpacesFileObject globalSpaceFO;
    private DataSpacesFileObject userSpaceFO;

    /**
     * {@inheritDoc}
     */
    public OneShotDecrypter getDecrypter() {
        throw new RuntimeException("Should not be called in this context");
    }

    @Override
    public NodeSet getNodes() {
        return nodes;
    }

    @Override
    public void setNodes(NodeSet nodes) {
        this.nodes = nodes;
    }

    /**
     * {@inheritDoc}
     */
    public void setDecrypter(OneShotDecrypter decrypter) {
        throw new RuntimeException("Should not be called in this context");
    }

    public DataSpacesFileObject getLocalSpaceFileObject() {
        return localSpaceFO;
    }

    public void setLocalSpaceFileObject(DataSpacesFileObject localSpaceFO) {
        this.localSpaceFO = localSpaceFO;
    }

    public DataSpacesFileObject getInputSpaceFileObject() {
        return inputSpaceFO;
    }

    public void setInputSpaceFileObject(DataSpacesFileObject inputSpaceFO) {
        this.inputSpaceFO = inputSpaceFO;
    }

    public DataSpacesFileObject getOutputSpaceFileObject() {
        return outputSpaceFO;
    }

    public void setOutputSpaceFileObject(DataSpacesFileObject outputSpaceFO) {
        this.outputSpaceFO = outputSpaceFO;
    }

    public DataSpacesFileObject getGlobalSpaceFileObject() {
        return globalSpaceFO;
    }

    public void setGlobalSpaceFileObject(DataSpacesFileObject globalSpaceFO) {
        this.globalSpaceFO = globalSpaceFO;
    }

    public DataSpacesFileObject getUserSpaceFileObject() {
        return userSpaceFO;
    }

    public void setUserSpaceFileObject(DataSpacesFileObject userSpaceFO) {
        this.userSpaceFO = userSpaceFO;
    }
}
