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
package org.objectweb.proactive.extensions.calcium.system;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.extensions.calcium.muscle.Muscle;


/**
 * This class provides system dependent functionalities which can be accessed from
 * inside {@link Muscle} function implementations.
 *
 * @author The ProActive Team (mleyton)
 *
 */
@PublicAPI
public interface SkeletonSystem {

    /**
     * The working space is a directory to which files can be written and read.
     *
     * The files stored in this directory are destroyed after the execution
     * of the muscle function. To keep files persistent use the method {@link #newPersistent(File) registerAsPersistent}.
     *
     * @return A File object representing the location of the working space.
     * @throws EnvironmentException
     */
    public WSpace getWorkingSpace() throws EnvironmentException;

    /**
     * This method executes the specified command, with the specified arguments,
     * and blocks until the command is finished.
     *
     * Before invoking the command, the current working directory is the once represented by the {@link WSpace}.
     *
     * @param command The command to execute.
     * @param arguments The arguments that must be given to the command.
     * @return The exit value of the command, where <code>0</code> indicates normal termination.
     * @throws IOException If an exception takes place.
     * @throws InterruptedException If the thread was interrupted while waiting for the command's execution.
     */
    public int execCommand(File command, String arguments) throws IOException, InterruptedException;
}
