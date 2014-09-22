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
package org.ow2.proactive.scheduler.task.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.executable.internal.JavaExecutableInitializerImpl;
import org.ow2.proactive.scheduler.common.task.util.ByteArrayWrapper;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ExecutableContainerInitializer;
import org.ow2.proactive.scheduler.util.classloading.TaskClassLoaderImpl;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;


/**
 * This class is a container for Java executable. The actual executable is instantiated on the worker node
 * in a dedicated classloader, which can download classes from the associated classServer.
 *
 * @see TaskClassServer
 * @author The ProActive Team
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JavaExecutableContainer extends ExecutableContainer {

    private static final long serialVersionUID = 60L;

    protected String userExecutableClassName;

    /** Arguments of the task as a map */
    protected final Map<String, ByteArrayWrapper> serializedArguments = new HashMap<String, ByteArrayWrapper>();

    // instanciated on demand : not DB managed
    protected Executable userExecutable;

    // can be null : not DB managed
    protected TaskClassServer classServer;

    /**
     * Create a new container for JavaExecutable
     * @param userExecutableClassName the classname of the user defined executable
     * @param args the serialized arguments for Executable.init() method.
     */
    public JavaExecutableContainer(String userExecutableClassName, Map<String, byte[]> args) {
        this.userExecutableClassName = userExecutableClassName;
        for (Entry<String, byte[]> e : args.entrySet()) {
            this.serializedArguments.put(e.getKey(), new ByteArrayWrapper(e.getValue()));
        }
    }

    /**
     * Copy constructor
     * 
     * @param cont original object to copy
     */
    public JavaExecutableContainer(JavaExecutableContainer cont) {
        this.userExecutableClassName = cont.userExecutableClassName;
        for (Entry<String, ByteArrayWrapper> e : cont.serializedArguments.entrySet()) {
            this.serializedArguments.put(new String(e.getKey()), new ByteArrayWrapper(e.getValue()
                    .getByteArray()));
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#getExecutable()
     */
    @Override
    public Executable getExecutable() throws ExecutableCreationException {
        if (this.userExecutable == null) {
            // Instanciate the actual executable
            try {
                TaskClassLoaderImpl tcl = new TaskClassLoaderImpl(this.getClass().getClassLoader(),
                    this.classServer);
                // the tcl becomes the context classloader
                Thread.currentThread().setContextClassLoader(tcl);
                Class<?> userExecutableClass = tcl.loadClass(this.userExecutableClassName);
                userExecutable = (Executable) userExecutableClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new ExecutableCreationException("Unable to instanciate JavaExecutable. " +
                    this.userExecutableClassName + " class cannot be found", e);
            } catch (InstantiationException e) {
                throw new ExecutableCreationException("Unable to instanciate JavaExecutable. " +
                    this.userExecutableClassName + " might not define no-args constructor", e);
            } catch (ClassCastException e) {
                throw new ExecutableCreationException(
                    "Unable to instanciate JavaExecutable. " + this.userExecutableClassName +
                        " might not inherit from org.ow2.proactive.scheduler.common.task.executable.JavaExecutable",
                    e);
            } catch (Throwable e) {
                throw new ExecutableCreationException("Unable to instanciate JavaExecutable", e);
            }
        }
        return userExecutable;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#init(org.ow2.proactive.scheduler.task.ExecutableContainerInitializer)
     */
    @Override
    public void init(ExecutableContainerInitializer initializer) {
        // get the classserver if any (can be null)
        this.classServer = initializer.getClassServer();
    }

    /**
     * Get the classServer
     *
     * @return the classServer
     */
    public TaskClassServer getClassServer() {
        return classServer;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#createExecutableInitializer()
     */
    @Override
    public JavaExecutableInitializerImpl createExecutableInitializer() {
        JavaExecutableInitializerImpl jei = new JavaExecutableInitializerImpl();
        Map<String, byte[]> tmp = new HashMap<String, byte[]>();
        for (Entry<String, ByteArrayWrapper> e : this.serializedArguments.entrySet()) {
            tmp.put(e.getKey(), e.getValue().getByteArray());
        }
        jei.setSerializedArguments(tmp);
        jei.setNodes(nodes);
        ArrayList<String> nodeUrl = new ArrayList<String>();
        if (nodes != null) {
            for (Node n : nodes) {
                nodeUrl.add(n.getNodeInformation().getURL());
            }
        }
        jei.setNodesURL(nodeUrl);
        return jei;
    }

    public String getUserExecutableClassName() {
        return userExecutableClassName;
    }

    public Map<String, ByteArrayWrapper> getSerializedArguments() {
        return serializedArguments;
    }

}
