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


/**
 * Interface for handlers of Non Functional Exceptions
 *
 * @author  ProActive Team
 * @version 1.0,  2002/07/08
 * @since   ProActive 0.9.3
 *
 */
public interface Handler extends java.io.Serializable {
    // Definition of different level ID used to classify handler

    /**
     * Default level is static and initialized in core of applications. This level
     * provide a basic handling strategy for every non-functional exception.
     */
    static public int ID_Default = 0;

    /**
     * Virtual Machine level is the first level created dynamcally. It offers the
     * possibility to define a general handling behavior for every virtual machine
     * environment. In the scope of distributed application, it's pretty useful with
     * client/server application which required different recovery mechanisms.
     */
    static public int ID_VM = 1;

    /**
     * Remote and Mobile Object level gives the opportunity to associated more
     * specific handlers to remote objects. Nevertheless, we have to take into
     * account the mobility of such objects. Handlers should migrate along with
     * their associated entity.
     */
    static public int ID_Body = 2;

    /**
     * Proxy level is used to define reliable strategies for references to active objects.
     */
    static public int ID_Proxy = 3;

    /**
     * Future level is highly used with asynchronous remote method calls. It appears
     * indeed that most of the failure occur during such calls.
     */
    static public int ID_Future = 4;

    /**
     * Code level allows temporary handlers in the code. We keep such a level to let
     * some functional treatments of non functional exceptions possible.
     */
    static public int ID_Code = 5;

    /**
    * Is the exception reliable for the handler ?
    * @param e The exception checked for handler reliability
    */
    public boolean isHandling(NonFunctionalException e);

    /**
      * Provide a treatment for the handled exception(s)
      * @param e The exception to be handled
      */
    public void handle(NonFunctionalException e, Object info);

    /**
     * Provide a treatment for the handled exception(s)
    * @param e The exception to be handled
     */
    public void handle(NonFunctionalException nfe, Exception e, Object info)
        throws Exception;
}
