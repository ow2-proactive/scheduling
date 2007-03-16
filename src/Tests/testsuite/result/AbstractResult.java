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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package testsuite.result;

import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import testsuite.exception.BadTypeException;


/**
 * @author Alexandre di Costanzo
 *
 */
public abstract class AbstractResult {
    public static final int ERROR = 2;
    public static final int MSG = -2;
    public static final int INFO = -3;
    public static final int RESULT = -1;
    public static final int GLOBAL_RESULT = 1;
    public static final int IMP_MSG = 0;
    private int type = -2;
    private String message = "No message";
    private Throwable exception = null;
    private Calendar time = null;
    private boolean shortDateFormat = true;

    public AbstractResult(int type, String message) throws BadTypeException {
        if (!isValidType(type)) {
            throw new BadTypeException();
        }
        this.type = type;
        this.message = message;
        time = Calendar.getInstance();
    }

    public AbstractResult(int type, String message, Throwable e)
        throws BadTypeException {
        if (!isValidType(type)) {
            throw new BadTypeException();
        }
        this.type = type;
        this.message = message;
        this.exception = e;
        time = Calendar.getInstance();
    }

    private boolean isValidType(int type) {
        if ((type < 3) && (type > -4)) {
            return true;
        } else {
            return false;
        }
    }

    public abstract Node toXMLNode(Document document);

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public abstract String toString();

    /**
     * @return
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * @return
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return
     */
    public boolean isShortDateFormat() {
        return shortDateFormat;
    }

    /**
     * @param shortDateFormat
     */
    public void setShortDateFormat(boolean shortDateFormat) {
        this.shortDateFormat = shortDateFormat;
    }

    /**
     * @param i
     */
    public void setType(int i) {
        type = i;
    }
}
