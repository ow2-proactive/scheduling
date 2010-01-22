/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.launcher.dataspace;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * Signals an error condition during a build
 */
class BuildException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = 200;
    /** Exception that might have caused this one. */
    private Throwable cause;

    /**
     * Constructs a build exception with no descriptive information.
     */
    public BuildException() {
        super();
    }

    /**
     * Constructs an exception with the given descriptive message.
     *
     * @param message A description of or information about the exception.
     *            Should not be <code>null</code>.
     */
    public BuildException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the given message and exception as
     * a root cause.
     *
     * @param message A description of or information about the exception.
     *            Should not be <code>null</code> unless a cause is specified.
     * @param cause The exception that might have caused this one.
     *              May be <code>null</code>.
     */
    public BuildException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    /**
     * Constructs an exception with the given exception as a root cause.
     *
     * @param cause The exception that might have caused this one.
     *              Should not be <code>null</code>.
     */
    public BuildException(Throwable cause) {
        super(cause.toString());
        this.cause = cause;
    }

    /**
     * Returns the nested exception, if any.
     *
     * @return the nested exception, or <code>null</code> if no
     *         exception is associated with this one
     */
    public Throwable getException() {
        return cause;
    }

    /**
     * Returns the nested exception, if any.
     *
     * @return the nested exception, or <code>null</code> if no
     *         exception is associated with this one
     */
    @Override
    public Throwable getCause() {
        return getException();
    }

    /**
     * Returns the location of the error and the error message.
     *
     * @return the location of the error and the error message
     */
    @Override
    public String toString() {
        return getMessage();
    }

    /**
     * Prints the stack trace for this exception and any
     * nested exception to <code>System.err</code>.
     */
    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Prints the stack trace of this exception and any nested
     * exception to the specified PrintStream.
     *
     * @param ps The PrintStream to print the stack trace to.
     *           Must not be <code>null</code>.
     */
    @Override
    public void printStackTrace(PrintStream ps) {
        synchronized (ps) {
            super.printStackTrace(ps);
            if (cause != null) {
                ps.println("--- Nested Exception ---");
                cause.printStackTrace(ps);
            }
        }
    }

    /**
     * Prints the stack trace of this exception and any nested
     * exception to the specified PrintWriter.
     *
     * @param pw The PrintWriter to print the stack trace to.
     *           Must not be <code>null</code>.
     */
    @Override
    public void printStackTrace(PrintWriter pw) {
        synchronized (pw) {
            super.printStackTrace(pw);
            if (cause != null) {
                pw.println("--- Nested Exception ---");
                cause.printStackTrace(pw);
            }
        }
    }
}
