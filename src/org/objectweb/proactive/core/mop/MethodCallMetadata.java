/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.mop;

import java.io.Serializable;


/* This class is optimized so that its instances are immutable */
public class MethodCallMetadata implements Serializable {

    /**
     * If the caller catches some RuntimeException, we have to wait for all calls
     * generated in the block at its end because any of these calls could throw one.
     */
    private boolean runtimeExceptionHandled;

    /**
     * If the caller told ProActive to catch all the thrown exceptions, we can
     * make the call asynchronous.
     */
    private boolean exceptionAsynchronously;

    /**
     * The default parameters, when the exception mechanism is not used.
     */
    public static final MethodCallMetadata DEFAULT = new MethodCallMetadata(false,
            false);

    /**
     * @param runtimeExceptionHandled
     * @param exceptionAsynchronously
     */
    public MethodCallMetadata(boolean runtimeExceptionHandled,
        boolean exceptionAsynchronously) {
        this.runtimeExceptionHandled = runtimeExceptionHandled;
        this.exceptionAsynchronously = exceptionAsynchronously;
    }

    /**
     * @return Returns the exceptionAsynchronously.
     */
    public boolean isExceptionAsynchronously() {
        return exceptionAsynchronously;
    }

    /**
     * @return Returns the runtimeExceptionHandled.
     */
    public boolean isRuntimeExceptionHandled() {
        return runtimeExceptionHandled;
    }

    public static MethodCallMetadata optimize(MethodCallMetadata metadata) {
        if (DEFAULT.equals(metadata)) {
            metadata = null;
        }

        return metadata;
    }

    public String toString() {
        return "[rt:" + runtimeExceptionHandled + ", async:" +
        exceptionAsynchronously + "]";
    }
}
