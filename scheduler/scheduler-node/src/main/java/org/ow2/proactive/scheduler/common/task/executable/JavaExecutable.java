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
package org.ow2.proactive.scheduler.common.task.executable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.task.executable.internal.JavaExecutableInitializerImpl;
import org.ow2.proactive.utils.NodeSet;


/**
 * Extends this abstract class if you want to create your own java task.<br>
 * A java task is a task representing a java process as a java class.<br>
 * This class provides an {@link #init(Map)} that will get your parameters back for this task.
 * By default, this method does nothing.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class JavaExecutable extends AbstractJavaExecutable {

    // this value is set only on worker node side !!
    // see JavaTaskLauncher
    protected JavaExecutableInitializerImpl execInitializer;
    
    /**
     * Initialize the executable using the given executable Initializer.
     *
     * @param execInitializer the executable Initializer used to init the executable itself
     *
     * @throws Exception an exception if something goes wrong during executable initialization.
     */
    // WARNING WHEN REMOVE OR RENAME, called by task launcher by introspection
    protected void internalInit(JavaExecutableInitializerImpl execInitializer) throws Exception {
        super.internalInit(execInitializer);
        this.execInitializer = execInitializer;

    }

    /**
     * Use this method for a multi-node task. It returns the list of nodes demanded by the user
     * while describing the task.<br>
     * In a task, one node is used to start the task itself, the other are returned by this method.<br>
     * If user describe the task using the "numberOfNodes" property set to 5, then this method
     * returns a list containing 4 nodes. The first one being used by the task itself.
     *
     * @return the list of nodes demanded by the user.
     */
    public final NodeSet getNodes() {
        return execInitializer.getNodes();
    }

    /**
     * Retrieve the root of the INPUT space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The INPUT space is Read-Only, so you can get files but not put files inside.</br>
     * It's real path is defined by the INPUT space specified in your Job or the Scheduler default one
     * specified by the administrator.<br />
     * INPUT space can be a local or a distant space.
     *
     * @return the root of the INPUT space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     * 							   if the node is not properly configured,
     * 							   or if the INPUT space cannot be reached or has not be found.
     */
    public final DataSpacesFileObject getInputSpace() throws FileSystemException {
        return execInitializer.getInputSpaceFileObject();
    }

    /**
     * Retrieve the root of the OUTPUT space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The OUTPUT space is a full access space, so you can get files, create, move, copy, etc...</br>
     * It's real path is defined by the OUTPUT space specified in your Job or the Scheduler default one
     * specified by the administrator.<br />
     * OUTPUT space can be a local or a distant space.
     *
     * @return the root of the OUTPUT space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     * 							   if the node is not properly configured,
     * 							   or if the OUTPUT space cannot be reached or has not be found.
     */
    public final DataSpacesFileObject getOutputSpace() throws FileSystemException {
        return execInitializer.getOutputSpaceFileObject();
    }

    /**
     * Retrieve the root of the GLOBAL space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The GLOBAL space is a full access space, so you can get files, create, move, copy, etc...</br>
     * It's real path is defined by the GLOBAL space specified by the Scheduler administrator.<br />
     * GLOBAL space is shared among all nodes and all users.
     * GLOBAL space can be a local or a distant space.
     *
     * @return the root of the GLOBAL space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     *                             if the node is not properly configured,
     *                             or if the GLOBAL space cannot be reached or has not be found.
     */
    public final DataSpacesFileObject getGlobalSpace() throws FileSystemException {
        return execInitializer.getGlobalSpaceFileObject();
    }

    /**
     * Retrieve the root of the USER space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The USER space is a full access space reserved to the current user, so you can get files, create, move, copy, etc...</br>
     * It's real path is defined by the USER space specified by the Scheduler administrator.<br />
     * USER space is shared among all nodes but specific to one user
     * USER space can be a local or a distant space.
     *
     * @return the root of the OUTPUT space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     *                             if the node is not properly configured,
     *                             or if the USER space cannot be reached or has not be found.
     */
    public final DataSpacesFileObject getUserSpace() throws FileSystemException {
        return execInitializer.getUserSpaceFileObject();
    }

    /**
     * Retrieve the root of the LOCAL space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The LOCAL space is a full local access space, so you can get files, create, move, copy, etc...</br>
     * It's real path is located in the temporary directory of the host on which the task is executed.<br />
     * As it is a local path, accessing data for computing remains faster.
     *
     * @return the root of the LOCAL space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     * 							   or if the node is not properly configured.
     */
    public final DataSpacesFileObject getLocalSpace() throws FileSystemException {
        return execInitializer.getLocalSpaceFileObject();
    }

    /**
     * Retrieve the given file resolved relative to the INPUT space.<br />
     * The file path denoted by the given path argument must exist in the INPUT space.
     *
     * @param path the file path to be resolve relative to the INPUT space.
     * @return the given file resolved in the INPUT space
     * @throws FileNotFoundException if the file cannot be found in the INPUT space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     * 							   if the node is not properly configured,
     * 							   or if the OUTPUT space cannot be reached or has not be found.
     * @see #getInputSpace() for details
     */
    public final DataSpacesFileObject getInputFile(String path) throws FileNotFoundException,
            FileSystemException {
        try {
            DataSpacesFileObject dsfo = getInputSpace().resolveFile(path);
            if (!dsfo.exists()) {
                throw new FileNotFoundException("File '" + path + "' has not be found in the INPUT space.");
            }
            return dsfo;
        } catch (FileSystemException e) {
            throw e;
        }
    }

    /**
     * Retrieve the given file resolved relative to the OUTPUT space.<br />
     *
     * @param path the file path to be resolve relative to the OUTPUT space.
     * @return the given file resolved in the OUTPUT space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     * 							   if the node is not properly configured,
     * 							   or if the OUTPUT space cannot be reached or has not be found.
     * @see #getOutputSpace() for details
     */
    public final DataSpacesFileObject getOutputFile(String path) throws FileSystemException {
        return getOutputSpace().resolveFile(path);
    }

    /**
     * Retrieve the given file resolved relative to the GLOBAL space.<br />
     *
     * @param path the file path to be resolve relative to the GLOBAL space.
     * @return the given file resolved in the GLOBAL space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     *                             if the node is not properly configured,
     *                             or if the GLOBAL space cannot be reached or has not be found.
     * @see #getGlobalSpace() for details
     */
    public final DataSpacesFileObject getGlobalFile(String path) throws FileSystemException {
        return getGlobalSpace().resolveFile(path);
    }

    /**
     * Retrieve the given file resolved relative to the USER space.<br />
     *
     * @param path the file path to be resolve relative to the USER space.
     * @return the given file resolved in the USER space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     *                             if the node is not properly configured,
     *                             or if the USER space cannot be reached or has not be found.
     * @see #getUserSpace() for details
     */
    public final DataSpacesFileObject getUserFile(String path) throws FileSystemException {
        return getUserSpace().resolveFile(path);
    }

    /**
     * Retrieve the given file resolved relative to the LOCAL space.<br />
     *
     * @param path the file path to be resolve relative to the LOCAL space.
     * @return the given file resolved in the LOCAL space
     * @throws FileSystemException if the node is not configured for DATASPACE,
     * 							   or if the node is not properly configured.
     * @see #getLocalSpace() for details
     */
    public final DataSpacesFileObject getLocalFile(String path) throws FileSystemException {
        return getLocalSpace().resolveFile(path);
    }

}
