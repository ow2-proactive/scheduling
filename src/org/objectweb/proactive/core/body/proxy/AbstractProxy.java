/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.body.proxy;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;


public abstract class AbstractProxy implements Proxy, java.io.Serializable {
    // Get logger
    protected static Logger logger = Logger.getLogger("NFE");

    // table of handlers associated to proxy
    private HashMap proxyLevel;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public AbstractProxy() {
    }

    //
    // -- METHODS ----------------------------------------------------
    //

    /**
     * Checks if the given <code>Call</code> object <code>c</code> can be
     * processed with a future semantics, i-e if its returned object
     * can be a future object.
     *
     * Two conditions must be met : <UL>
     * <LI> The returned object is reifiable
     * <LI> The invoked method does not throw any exceptions
     * </UL>
     * @return true if and only if the method call can be asynchronous
     */
    protected static boolean isAsynchronousCall(MethodCall mc) {
        return mc.isAsynchronousWayCall();
    }

    /**
     * Returns a boolean saying whether the methode is one-way or not.
     * Being one-way method is equivalent to <UL>
     * <LI>having <code>void</code> as return type
     * <LI>and not throwing any checked exceptions</UL>
     * @return true if and only if the method call is one way
     */
    protected static boolean isOneWayCall(MethodCall mc) {
        return mc.isOneWayCall();
    }

	/**
	 * Get information about the handlerizable object
	 * @return information about the handlerizable object
	 */
	public String getHandlerizableInfo()  throws java.io.IOException {
		return "PROXY of CLASS ["+ this.getClass()  +"]";
	}
	
    /** Give a reference to a local map of handlers
    * @return A reference to a map of handlers
    */
    public HashMap getHandlersLevel() throws java.io.IOException {
        return proxyLevel;
    }

	/** 
	 * Clear the local map of handlers
	 */
	public void clearHandlersLevel() throws java.io.IOException {
		 proxyLevel.clear();
	}

    /** Set a new handler within the table of the Handlerizable Object
     * @param handler A handler associated with a class of non functional exception.
     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
     */
    public void setExceptionHandler(Handler handler, Class exception)
        throws java.io.IOException {
        // add handler to proxy level
        if (proxyLevel == null) {
            proxyLevel = new HashMap();
        }
        proxyLevel.put(exception, handler);
    }

    /** Remove a handler from the table of the Handlerizable Object
     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
     * @return The removed handler or null
     */
    public Handler unsetExceptionHandler(Class exception)
        throws java.io.IOException {
        // remove handler from proxy level
        if (proxyLevel != null) {
            Handler handler = (Handler) proxyLevel.remove(exception);
            return handler;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("[NFE_WARNING] No handler for [" +
                    exception.getName() + "] can be removed from PROXY level");
            }
            return null;
        }
    }
}
