/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Proxy;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.JavaExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.util.ByteArrayWrapper;
import org.ow2.proactive.scheduler.core.db.schedulerType.BinaryLargeOBject;
import org.ow2.proactive.scheduler.util.classloading.TaskClassLoader;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;


/**
 * This class is a container for Java executable. The actual executable is instantiated on the worker node
 * in a dedicated classloader, which can download classes from the associated classServer.
 *
 * @see TaskClassServer
 * @author The ProActive Team
 */
@Entity
@Table(name = "JAVA_EXEC_CONTAINER")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "EXEC_CONTAINER_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("JEC")
@AccessType("field")
@Proxy(lazy = true)
public class JavaExecutableContainer extends ExecutableContainer {

    @Id
    @GeneratedValue
    protected long hId;

    @Column(name = "EXECUTABLE_CLASS")
    protected String userExecutableClassName;

    /** Arguments of the task as a map */
    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinTable(name = "JAVA_EXECCONTAINER_ARGUMENTS", joinColumns = @JoinColumn(name = "J_EXEC_CONTAINER_ID"))
    protected final Map<String, ByteArrayWrapper> serializedArguments = new HashMap<String, ByteArrayWrapper>();

    // instanciated on demand : not DB managed
    @Transient
    protected JavaExecutable userExecutable;

    // can be null : not DB managed
    @Transient
    protected TaskClassServer classServer;

    /** Hibernate default constructor */
    public JavaExecutableContainer() {
    }

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
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#getExecutable()
     */
    public Executable getExecutable() throws ExecutableCreationException {
        if (this.userExecutable == null) {
            // Instanciate the actual executable
            try {
                TaskClassLoader tcl = new TaskClassLoader(this.getClass().getClassLoader(), this.classServer);
                // the tcl becomes the context classloader
                Thread.currentThread().setContextClassLoader(tcl);
                Class<?> userExecutableClass = tcl.loadClass(this.userExecutableClassName);
                userExecutable = (JavaExecutable) userExecutableClass.newInstance();
            } catch (Throwable e) {
                throw new ExecutableCreationException("Unable to instanciate JavaExecutable : " + e);
            }
        }
        return userExecutable;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#init(org.ow2.proactive.scheduler.task.ExecutableContainerInitializer)
     */
    public void init(ExecutableContainerInitializer initializer) {
        // get the classserver if any (can be null)
        this.classServer = initializer.getClassServer();
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#createExecutableInitializer()
     */
    public JavaExecutableInitializer createExecutableInitializer() {
        JavaExecutableInitializer jei = new JavaExecutableInitializer();
        Map<String, byte[]> tmp = new HashMap<String, byte[]>();
        for (Entry<String, ByteArrayWrapper> e : this.serializedArguments.entrySet()) {
            tmp.put(e.getKey(), e.getValue().byteArrayValue());
        }
        jei.setSerializedArguments(tmp);
        jei.setNodes(nodes);
        return jei;
    }

}
