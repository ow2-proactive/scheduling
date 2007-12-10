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
package org.objectweb.proactive.extensions.scheduler.common.scripting;

import java.io.Serializable;


public class ScriptResult<E> implements Serializable {

    /**  */
    private static final long serialVersionUID = 2665277848502662458L;
    private E result = null;
    private Throwable exception = null;

    public ScriptResult() {
    }

    public ScriptResult(E result, Throwable exception) {
        this.result = result;
        this.exception = exception;
    }

    public ScriptResult(E result) {
        this(result, null);
    }

    public ScriptResult(Throwable exception) {
        this(null, exception);
    }

    public boolean errorOccured() {
        return exception != null;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public E getResult() {
        return result;
    }

    public void setResult(E result) {
        this.result = result;
    }
}
