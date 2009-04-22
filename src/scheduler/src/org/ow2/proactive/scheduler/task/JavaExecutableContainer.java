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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.util.BigString;
import org.ow2.proactive.scheduler.util.classloading.TaskClassLoader;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;


/**
 * This class is a container for Java executable. The actual executable is instanciated on the worker node
 * in a dedicated classloader, which can download classes from the associated classServer.
 * @see TaskClassServer
 * @author The ProActive Team
 */
@Entity
@Table(name = "JAVA_EXECUTABLE_CONTAINER")
@AccessType("field")
@Proxy(lazy = false)
public class JavaExecutableContainer implements ExecutableContainer {

    /**
     *
     */
    private static final long serialVersionUID = 10L;

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    @Column(name = "EXECUTABLE_CLASS")
    private String userExecutableClassName;

    /** Arguments of the task as a map */
    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JoinTable(name = "JAVA_EXECCONTAINER_ARGUMENTS")
    private Map<String, BigString> args = new HashMap<String, BigString>();

    // instanciated on demand : not DB managed
    @Transient
    private JavaExecutable userExecutable;

    // can be null : not DB managed
    @Transient
    private TaskClassServer classServer;

    /** Hibernate default constructor */
    @SuppressWarnings("unused")
    private JavaExecutableContainer() {
    }

    /**
     * Create a new container for JavaExecutable
     * @param userExecutableClassName the classname of the user defined executable
     * @param args the arguments for Executable.init() method.
     */
    public JavaExecutableContainer(String userExecutableClassName, Map<String, BigString> args) {
        this.userExecutableClassName = userExecutableClassName;
        this.args = args;
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
                Map<String, String> tmp = new HashMap<String, String>();
                for (Entry<String, BigString> e : this.args.entrySet()) {
                    tmp.put(e.getKey(), e.getValue().getValue());
                }
                userExecutable.setArgs(tmp);
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

}
