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
package org.objectweb.proactive.extensions.calcium.exceptions;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A <code>MuscleException</code> wraps exceptions that where generated during the execution of a {@link org.objectweb.proactive.extensions.calcium.muscle.Muscle} function,
 * which caused the program to halt its execution.
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class MuscleException extends Exception {
    public MuscleException(String msg) {
        super(msg);
    }

    public MuscleException(String smg, Throwable e) {
        super(e);
    }
}
