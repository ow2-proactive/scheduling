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
package org.objectweb.proactive.core.exceptions;

import java.util.HashMap;

import org.objectweb.proactive.core.exceptions.handler.Handler;


/**
 * An interface to add handlerizable behaviour to reified object
 * @author  ProActive Team
 * @version 1.0,  2004/07/01
 * @since   ProActive 2.0
 *
 */
public interface Handlerizable {

	/**
	 * Get information about the handlerizable object
	 */
	public String getHandlerizableInfo() throws java.io.IOException;

	/** 
	 * Give a reference to a local map of handlers
	 * @return A reference to a map of handlers
	 */
	public HashMap getHandlersLevel() throws java.io.IOException;

	/** 
	 * Clear the local map of handlers
	 */
	public void clearHandlersLevel() throws java.io.IOException;

	/** Set a new handler within the table of the Handlerizable Object
		 * @param handler A handler associated with a class of non functional exception.
		 * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
		 */
	public void setExceptionHandler(Handler handler, Class exception)
		throws java.io.IOException;

	/** Remove a handler from the table of the Handlerizable Object
		 * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
		 * @return The removed handler or null
		 */
	public Handler unsetExceptionHandler(Class exception)
		throws java.io.IOException;
}
