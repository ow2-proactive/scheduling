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
package org.objectweb.proactive;

import org.objectweb.proactive.core.ProActiveException;


/**
 *
 * An exception thrown when a problem occurs during the creation of an ActiveObject
 * </p><p>
 * <b>see <a href="../../../../html/ActiveObjectCreation.html">active object creation documentation</a></b>
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 */
public class ActiveObjectCreationException extends ProActiveException {

    /**
     * Constructs a <code>ProActiveException</code> with no specified
     * detail message.
     */
    public ActiveObjectCreationException() {
    }

    /**
     * Constructs a <code>ActiveObjectCreationException</code> with the specified detail message.
     * @param s the detail message
     */
    public ActiveObjectCreationException(String s) {
        super(s);
    }

    /**
     * Constructs a <code>ActiveObjectCreationException</code> with the specified
     * detail message and nested exception.
     *
     * @param s the detail message
     * @param detail the nested exception
     */
    public ActiveObjectCreationException(String s, Throwable detail) {
        super(s, detail);
    }

    /**
     * Constructs a <code>ActiveObjectCreationException</code> with the specified
     * detail message and nested exception.
     * @param detail the nested exception
     */
    public ActiveObjectCreationException(Throwable detail) {
        super(detail);
    }
}
