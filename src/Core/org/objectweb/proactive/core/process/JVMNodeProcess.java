/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.core.process;

import java.io.Serializable;

import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


/**
 * <p>
 * This class has the same functionalities than JVMProcess, except that the class associated with this process
 * ie the class that this process will start when the <code>startProcess()</code> is called, is set automatically to
 * <code>org.objectweb.proactive.core.runtime.startRuntime</code>.This class is mainly used with XML deployment descriptor.
 * </p>
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class JVMNodeProcess extends JVMProcessImpl implements Serializable {

    /**
     * Creates a new instance of JVMNodeProcess.
     */
    public JVMNodeProcess() {
        this(new StandardOutputMessageLogger());
        setClassname("org.objectweb.proactive.core.runtime.StartRuntime");
    }

    /**
     * Creates a new instance of JVMNodeProcess
     * @param messageLogger The logger that handles input and error stream of this process
     */
    public JVMNodeProcess(RemoteProcessMessageLogger messageLogger) {
        super(messageLogger);
        setClassname("org.objectweb.proactive.core.runtime.StartRuntime");
    }

    /**
     * Creates a new instance of JVMNodeProcess
     * @param inputMessageLogger The logger that handles input stream of this process
     * @param errorMessageLogger The logger that handles error stream of this process
     */
    public JVMNodeProcess(RemoteProcessMessageLogger inputMessageLogger,
        RemoteProcessMessageLogger errorMessageLogger) {
        super(inputMessageLogger, errorMessageLogger);
        setClassname("org.objectweb.proactive.core.runtime.StartRuntime");
    }
}
