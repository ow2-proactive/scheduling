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
package org.objectweb.proactive.core.exceptions.handler;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.communication.ProActiveCommunicationException;

import java.util.HashMap;


/**
 * Handle all communication exceptions
 *
 * @author  ProActive Team
 * @version 1.0,  2002/07/08
 * @since   ProActive 0.9.3
 *
 */
public class HandlerCommunicationException extends HandlerNonFunctionalException {

    /**
     *  This list keeps a trace of the different distant machines used by the application
     */
    static public HashMap machineList;

    /**
     * Is the exception reliable for the handler ?
     * @param e The exception checked for handler reliability
     */
    public boolean isHandling(NonFunctionalException e) {
        return (e instanceof ProActiveCommunicationException);
    }

    /**
     * Provide a treatment for the handled exception(s)
     * @param e The exception to be handled
     */
    public void handle(NonFunctionalException e, Object info) {
        // System.out.println("PROBLEM ON " + ((String) info));
        super.handle(e, info);
    }

    /**
     * Provide a treatment for the handled exception(s)
     * @param e The exception to be handled
     */
    public void handle(NonFunctionalException nfe, Object info, Exception e)
        throws Exception {
        super.handle(nfe, info, e);
    }
}
