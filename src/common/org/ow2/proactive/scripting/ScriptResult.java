/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scripting;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 *
 * The class implements a script result container.
 * The script result is an object typed with the template class, or
 * an exception raised by the script execution.
 * @author ProActive team
 *
 * @param <E> template class for the result.
 */
@PublicAPI
public class ScriptResult<E> implements Serializable {

    /**  */
    private E result = null;
    private Throwable exception = null;

    /**
     * ProActive empty constructor
     */
    public ScriptResult() {
    }

    /** Constructor
     * @param result result to store
     * @param exception eventual exception representing the result
     */
    public ScriptResult(E result, Throwable exception) {
        this.result = result;
        this.exception = exception;
    }

    /**
     * @param result result to store
     */
    public ScriptResult(E result) {
        this(result, null);
    }

    /** Constructor
     * @param exception to store as a script result
     */
    public ScriptResult(Throwable exception) {
        this(null, exception);
    }

    /** tell if an exception has been raised during
     * script execution.
     * @return true if an exception occured, false otherwise.
     */
    public boolean errorOccured() {
        return exception != null;
    }

    /**
     * Return the eventual exception of the script's execution.
     * @return Throwable representing the exception.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Set an exception qs result.
     * @param exception exception to set.
     */
    public void setException(Throwable exception) {
        this.exception = exception;
    }

    /**
     * Return the result's object
     * @return result object.
     */
    public E getResult() {
        return result;
    }

    /**
     * Set the result
     * @param result result to set
     */
    public void setResult(E result) {
        this.result = result;
    }
}
