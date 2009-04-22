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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scripting.GenerationScript;


/**
 * This class is a container for Native executable. The actual executable is instanciated on the worker node.
 * @author The ProActive Team
 */
@Entity
@Table(name = "NATIVE_EXECUTABLE_CONTAINER")
@AccessType("field")
@Proxy(lazy = false)
public class NativeExecutableContainer implements ExecutableContainer {
    /**
     *
     */
    private static final long serialVersionUID = 10L;

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    // actual executable data
    @Column(name = "CLASSPATH", columnDefinition = "BLOB")
    @Type(type = "org.ow2.proactive.scheduler.core.db.schedulerType.CharacterLargeOBject")
    private String[] command;

    // actual generation script
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = GenerationScript.class)
    private GenerationScript generated;

    /** Hibernate default constructor */
    @SuppressWarnings("unused")
    private NativeExecutableContainer() {
    }

    /**
     * Create a new container for a native executable.
     * 
     * @param command the command to be executed.
     * @param generated the script that generates the command (can be null).
     */
    public NativeExecutableContainer(String[] command, GenerationScript generated) {
        this.command = command;
        this.generated = generated;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#getExecutable()
     */
    public Executable getExecutable() throws ExecutableCreationException {
        return new NativeExecutable(command, generated);
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#init(org.ow2.proactive.scheduler.task.ExecutableContainerInitializer)
     */
    public void init(ExecutableContainerInitializer initializer) {
        // Nothing to do for now...
    }

}
