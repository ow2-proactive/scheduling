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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;


/**
 * Handle ProActive exceptions
 *
 * @author  ProActive Team
 * @version 1.0,  2002/07/08
 * @since   ProActive 0.9.3
 *
 */
public class HandlerNonFunctionalException implements Handler {
	
    // Logger
    protected static Logger logger = Logger.getLogger("NFE");

    /**
     * Contains a suggestion to resolve the problem
     */
    private String suggestion;

    /**
     * Construct a handler with a suggestion to handle the problem more properly
     */
    public HandlerNonFunctionalException() {
        suggestion = "=> (solution 1) UPDATE HANDLER [" +
            this.getClass().getName() + "]";
    }

    /**
     * Construct a handler with a suggestion to handle the problem more properly
     */
    public HandlerNonFunctionalException(String suggestion) {
        this.suggestion = suggestion;
    }

    /**
    * @return Suggestion to handle the problem more properly
    */
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * @param string Set suggestion to handle the problem more properly
     */
    public void setSuggestion(String string) {
        suggestion = string;
    }

    /**
     * Is the exception reliable for the handler ?
     * @param e The exception checked for handler reliability
     */
    public boolean isHandling(NonFunctionalException e) {
        return (e instanceof NonFunctionalException);
    }

    /**
     * Provide a treatment for the handled exception(s)
     * @param e The exception to be handled
     */
    public void handle(NonFunctionalException e, Object info) {
		logger.info("PROBLEM ON " + ((String) info));
        logger.info("EXCEPTION [" + e.getDescription() + "] HANDLED WITH [" +
            this.getClass().getName() + "]");
        logger.info(suggestion);
        logger.info("=> (solution 2) SET HANDLER FOR EXCEPTION [" +
            e.getClass().getName() + "]");
        logger.error("NFE", e);
    }

    /**
     * Provide a treatment for the handled exception(s)
     * @param e The exception to be handled
     */
    public void handle(NonFunctionalException nfe, Exception e, Object info)
        throws Exception {
        logger.debug("EXCEPTION [" + nfe.getDescription() + "] HANDLED WITH [" +
            this.getClass().getName() + "]");
        logger.debug(suggestion);
        logger.debug("=> (solution 2) SET HANDLER FOR EXCEPTION [" +
            nfe.getClass().getName() + "]");
        throw e;
    }
}
