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
package org.ow2.proactive.scheduler.task.nativ;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ExecutableContainerInitializer;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.InvalidScriptException;


/**
 * This class is a container for Native executable. The actual executable is instanciated on the worker node.
 * @author The ProActive Team
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class NativeExecutableContainer extends ExecutableContainer {

    private static final long serialVersionUID = 61L;

    public static final Logger logger = Logger.getLogger(NativeExecutableContainer.class);

    // actual executable data
    private String[] command;

    // actual generation script
    private GenerationScript generated;

    /** working dir (launching dir, pwd... of the native executable) */
    private String workingDir;

    /** Hibernate default constructor */
    public NativeExecutableContainer() {
    }

    /**
     * Create a new container for a native executable.
     * 
     * @param command the command to be executed.
     * @param generated the script that generates the command (can be null).
     */
    public NativeExecutableContainer(String[] command, GenerationScript generated, String workingDir) {
        this.command = command;
        this.generated = generated;
        this.workingDir = workingDir;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#getExecutable()
     */
    @Override
    public Executable getExecutable() throws ExecutableCreationException {
        return new NativeExecutable();
    }

    /**
     * Copy constructor
     * 
     * @param cont original object to copy
     */
    public NativeExecutableContainer(NativeExecutableContainer cont) throws ExecutableCreationException {
        this.command = new String[cont.command.length];
        for (int i = 0; i < cont.command.length; i++) {
            this.command[i] = new String(cont.command[i]);
        }
        if (cont.generated == null) {
            this.generated = null;
        } else {
            try {
                this.generated = new GenerationScript(cont.generated);
            } catch (InvalidScriptException e) {
                throw new ExecutableCreationException("Could not copy generation script", e);
            }
        }
        if (cont.workingDir == null) {
            this.workingDir = null;
        } else {
            this.workingDir = new String(cont.workingDir);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#init(org.ow2.proactive.scheduler.task.ExecutableContainerInitializer)
     */
    @Override
    public void init(ExecutableContainerInitializer initializer) {
        // Nothing to do for now...
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#createExecutableInitializer()
     */
    @Override
    public NativeExecutableInitializer createExecutableInitializer() {
        NativeExecutableInitializer nei = new NativeExecutableInitializer();
        nei.setCommand(command);
        nei.setGenerationScript(generated);
        nei.setWorkingDir(workingDir);
        List<String> nodesHost = new ArrayList<String>();
        for (Node nodeHost : nodes) {
            nodesHost.add(nodeHost.getNodeInformation().getVMInformation().getHostName());
        }
        //add local node name
        try {
            nodesHost.add(PAActiveObject.getNode().getNodeInformation().getVMInformation().getHostName());
        } catch (NodeException e) {
            logger.warn("Local node could not be added to the list of host !", e);
        }
        nei.setNodesHost(nodesHost);
        return nei;
    }

    public GenerationScript getGenerationScript() {
        return generated;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public String[] getCommand() {
        return command;
    }

}
