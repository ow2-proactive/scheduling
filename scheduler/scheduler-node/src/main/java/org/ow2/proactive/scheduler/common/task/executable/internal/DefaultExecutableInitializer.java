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

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.OneShotDecrypter;
import org.ow2.proactive.utils.NodeSet;


/**
 * DefaultExecutableInitializer
 *
 * @author The ProActive Team
 **/
public class DefaultExecutableInitializer extends DefaultStandaloneExecutableInitializer implements ExecutableInitializer {

    private DataSpacesFileObject localSpaceFO;
    private DataSpacesFileObject inputSpaceFO;
    private DataSpacesFileObject outputSpaceFO;
    private DataSpacesFileObject globalSpaceFO;
    private DataSpacesFileObject userSpaceFO;

    private NodeSet nodes;

    /** Decrypter from launcher */
    private OneShotDecrypter decrypter = null;

    @Override
    public NodeSet getNodes() {
        return nodes;
    }

    @Override
    public void setNodes(NodeSet nodes) {
        this.nodes = nodes;
    }

    @Override
    public void setDecrypter(OneShotDecrypter decrypter) {
        this.decrypter = decrypter;
    }

    @Override
    public OneShotDecrypter getDecrypter() {
        return decrypter;
    }

    @Override
    public DataSpacesFileObject getLocalSpaceFileObject() {
        return localSpaceFO;
    }

    @Override
    public void setLocalSpaceFileObject(DataSpacesFileObject localSpaceFO) {
        this.localSpaceFO = localSpaceFO;
    }

    @Override
    public DataSpacesFileObject getInputSpaceFileObject() {
        return inputSpaceFO;
    }

    @Override
    public void setInputSpaceFileObject(DataSpacesFileObject inputSpaceFO) {
        this.inputSpaceFO = inputSpaceFO;
    }

    @Override
    public DataSpacesFileObject getOutputSpaceFileObject() {
        return outputSpaceFO;
    }

    @Override
    public void setOutputSpaceFileObject(DataSpacesFileObject outputSpaceFO) {
        this.outputSpaceFO = outputSpaceFO;
    }

    @Override
    public DataSpacesFileObject getGlobalSpaceFileObject() {
        return globalSpaceFO;
    }

    @Override
    public void setGlobalSpaceFileObject(DataSpacesFileObject globalSpaceFO) {
        this.globalSpaceFO = globalSpaceFO;
    }

    @Override
    public DataSpacesFileObject getUserSpaceFileObject() {
        return userSpaceFO;
    }

    @Override
    public void setUserSpaceFileObject(DataSpacesFileObject userSpaceFO) {
        this.userSpaceFO = userSpaceFO;
    }

}
