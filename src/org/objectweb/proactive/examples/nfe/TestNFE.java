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

package org.objectweb.proactive.examples.nfe;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.communication.*;
import org.objectweb.proactive.core.exceptions.handler.HandlerNonFunctionalException;
import org.objectweb.proactive.core.exceptions.handler.IHandler;


/**
 * Class to test non functional exception
 */
public class TestNFE {
	
	/**
 	* Raise a non functional exception
 	*/
	public static void raiseException(Class classOfException) throws NonFunctionalException {

		System.out.println("*** RAISE " + classOfException.getName());

		// We create a non functional exception of the desired class
		NonFunctionalException ex = null;
		try {
			ex = (NonFunctionalException) classOfException.getConstructor(new Class[] {String.class, Throwable.class}).newInstance(new Object[] {classOfException.getName(), null});
		} catch (Exception e) {
			if (e instanceof ClassNotFoundException)
				System.out.println("*** ERROR : cannot find class " + ex);
			else
				e.printStackTrace();
		}

		// We throw the exception
		throw ex;
	}


	// Main program
	public static void main(String[] args) {
	
		// We need one arg : the name of a class of non functional exception
		if (args.length != 1) {
			System.out.println("Usage : java org.objectweb.proactive.examples.nfeCreation NFE");
			System.exit(0);
		}
	
		// A non functional Exception
		NonFunctionalException ex = null;
	
		// Then we try to create this exception
		try {
			ex = (NonFunctionalException) Class.forName(args[0]).getConstructor(new Class[] {String.class, Throwable.class}).newInstance(new Object[] {args[0], null});
		} catch (ClassNotFoundException e) {
			System.out.println("Class " + args[0] + " is not a valid NFE");
			System.exit(0);
		} catch (InstantiationException e) {
			System.out.println("Problems occurs when instantiating " + args[0]);
			e.printStackTrace();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	
		// We then show information about the exception
		System.out.println("PARAMETER : " + ex.getDescription() + ex.getMessage());
		
		// We try some basic stuff (set and remove handlers)
		System.out.println();
		ProActive.setExceptionHandler(IHandler.ID_defaultLevel, null, HandlerNonFunctionalException.class, NonFunctionalException.class);
		ProActive.unsetExceptionHandler(IHandler.ID_defaultLevel, null, HandlerNonFunctionalException.class);
		ProActive.setExceptionHandler(IHandler.ID_VMLevel, null, HandlerNonFunctionalException.class, NonFunctionalException.class);
		ProActive.unsetExceptionHandler(IHandler.ID_VMLevel, null, NonFunctionalException.class);
		
		// Raise an exception
		System.out.println();
		try {
			raiseException(ProActiveCommunicationException.class);
		} catch (NonFunctionalException nfe) {
			IHandler handler = ProActive.searchExceptionHandler(nfe); 
			if (handler != null) handler.handle(nfe);
		}

		// Raise an exception
		System.out.println();
		try {
			raiseException(SendRequestCommunicationException.class);
		} catch (NonFunctionalException nfe) {
			IHandler handler = ProActive.searchExceptionHandler(nfe);
			if (handler != null) handler.handle(nfe);
		}
		
		// Exit program
		System.exit(0);
	}
}