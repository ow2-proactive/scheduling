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
package org.objectweb.proactive.core.mop;


/**
 */
public class MOPException extends Exception {
    public Throwable detail;

    /**
     * Constructs a <code>ProActiveException</code> with no specified
     * detail message.
     */
    public MOPException() {
    }

    /**
     * Constructs a <code>ProActiveException</code> with the specified detail message.
     * @param s the detail message
     */
    public MOPException(String s) {
        super(s);
    }

    /**
     * Constructs a <code>ProActiveException</code> with the specified
     * detail message and nested exception.
     *
     * @param s the detail message
     * @param ex the nested exception
     */
    public MOPException(String s, Throwable ex) {
        super(s);
        detail = ex;
    }

    /**
     * Constructs a <code>ProActiveException</code> with the specified
     * detail message and nested exception.
     *
     * @param ex the nested exception
     */
    public MOPException(Throwable ex) {
        super();
        detail = ex;
    }

    public Throwable getTargetException() {
        return detail;
    }

    /**
     * Returns the detail message, including the message from the nested
     * exception if there is one.
     */
    @Override
    public String getMessage() {
        if (detail == null) {
            return super.getMessage();
        } else {
            if (super.getMessage() == null) {
                return detail.getMessage();
            } else {
                return super.getMessage() + "; nested exception is: \n" +
                detail.toString();
            }
        }
    }

    /**
     * Prints the composite message and the embedded stack trace to
     * the specified stream <code>ps</code>.
     * @param ps the print stream
     */
    @Override
    public void printStackTrace(java.io.PrintStream ps) {
        if (detail == null) {
            super.printStackTrace(ps);
        } else {
            synchronized (ps) {
                ps.println(getMessage());
                detail.printStackTrace(ps);
            }
        }
    }

    /**
     * Prints the composite message to <code>System.err</code>.
     */
    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Prints the composite message and the embedded stack trace to
     * the specified print writer <code>pw</code>.
     * @param pw the print writer
     */
    @Override
    public void printStackTrace(java.io.PrintWriter pw) {
        if (detail == null) {
            super.printStackTrace(pw);
        } else {
            synchronized (pw) {
                pw.println(getMessage());
                detail.printStackTrace(pw);
            }
        }
    }
}
