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
package org.ow2.proactive.scheduler.common.task.executable;

import org.ow2.proactive.scheduler.common.task.dataspaces.LocalSpace;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;
import org.ow2.proactive.scheduler.common.task.executable.internal.JavaStandaloneExecutableInitializer;


/**
 * JavaExecutableStandalone replacement for JavaExecutable for REST API Java Clients
 *
 * Extends this abstract class if you want to create your own java task.<br>
 * A java task is a task representing a java process as a java class.<br>
 * This class provides an {@link #init(java.util.Map)} that will get your parameters back for this task.
 * By default, this method does nothing.
 *
 * This class differs from JavaExecutable through the interfaces it offers. It can be sub-classed without the need
 * to include ProActive dependencies.
 *
 * @author The ProActive Team
 **/
public abstract class JavaStandaloneExecutable extends AbstractJavaExecutable {


    protected void internalInit(JavaStandaloneExecutableInitializer execInitializer) throws Exception {
        super.internalInit(execInitializer);
    }

    /**
     * Retrieve the interface to the LOCAL space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The LOCAL space is a full local access space, so you can get files, create, move, copy, etc...</br>
     * It's real path is located in the temporary directory of the host on which the task is executed.<br />
     * As it is a local path, accessing data for computing remains faster.
     *
     * @return the LOCAL space interface
     */
    public LocalSpace getLocalSpace() {
        return execInitializer.getLocalSpace();
    }

    /**
     * Retrieve the root of the INPUT space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The INPUT space is Read-Only, so you can get files but not put files inside.</br>
     * It's real path is defined by the INPUT space specified in your Job or the Scheduler default one
     * specified by the administrator.<br />
     * INPUT space can be a local or a distant space.
     *
     * @return interface to the INPUT space
     */
    public RemoteSpace getInputSpace() {
        return execInitializer.getInputSpace();
    }

    /**
     * Retrieve the root of the OUTPUT space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The OUTPUT space is a full access space, so you can get files, create, move, copy, etc...</br>
     * It's real path is defined by the OUTPUT space specified in your Job or the Scheduler default one
     * specified by the administrator.<br />
     * OUTPUT space can be a local or a distant space.
     *
     * @return interface to the OUTPUT space
     */
    public RemoteSpace getOutputSpace() {
        return execInitializer.getOutputSpace();
    }

    /**
     * Retrieve the root of the GLOBAL space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The GLOBAL space is a full access space, so you can get files, create, move, copy, etc...</br>
     * It's real path is defined by the GLOBAL space specified by the Scheduler administrator.<br />
     * GLOBAL space is shared among all nodes and all users.
     * GLOBAL space can be a local or a distant space.
     *
     * @return interface to the GLOBAL space
     */
    public RemoteSpace getGlobalSpace() {
        return execInitializer.getGlobalSpace();
    }

    /**
     * Retrieve the root of the USER space. This allow you to resolve files, copy, move, delete, etc...
     * from and to other defined spaces.<br />
     * The USER space is a full access space reserved to the current user, so you can get files, create, move, copy, etc...</br>
     * It's real path is defined by the USER space specified by the Scheduler administrator.<br />
     * USER space is shared among all nodes but specific to one user
     * USER space can be a local or a distant space.
     *
     * @return interface to the OUTPUT space
     */
    public RemoteSpace getUserSpace() {
        return execInitializer.getUserSpace();
    }

}
