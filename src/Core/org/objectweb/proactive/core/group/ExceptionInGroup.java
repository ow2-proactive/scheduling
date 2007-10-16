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
package org.objectweb.proactive.core.group;


/**
 * This class represents an throwable occured in a group communication.
 *
 * @author Laurent Baduel
 */
public class ExceptionInGroup extends RuntimeException {

    /** The Object who throwns the Throwable */
    private Object object;

    /** The index of the object who throwns the Throwable in the caller group */
    private int index;

    /** The Throwable thrown */
    private Throwable throwable;

    /**
     * Built an ThrowableInGroup
     * @param object - the object who throwns the throwable (use null if the object is unknown)
     * @param index - the index of the object who throwns the throwable in the caller group
     * @param throwable - the throwable thrown
     */
    public ExceptionInGroup(Object object, int index, Throwable throwable) {
        super(throwable);
        this.object = object;
        this.index = index;
        this.throwable = throwable;
    }

    /**
     * Returns the object who throwns the Throwable
     * @return the object who throwns the Throwable
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * Returns the index of the object who throwns the Throwable
     * @return the index of the object who throwns the Throwable
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Returns the Throwable thrown
     * @return the Throwable thrown
     */
    public Throwable getThrowable() {
        return this.throwable;
    }
}
