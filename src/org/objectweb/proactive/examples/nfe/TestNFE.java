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


import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.communication.ProActiveCommunicationException;
import org.objectweb.proactive.core.exceptions.communication.SendRequestCommunicationException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.exceptions.handler.HandlerCommunicationException;
import org.objectweb.proactive.core.exceptions.handler.HandlerNonFunctionalException;
import org.objectweb.proactive.core.exceptions.migration.ProActiveMigrationException;

/**
 * Class to test non functional exception
 */
public class TestNFE {
	
	protected static Logger logger = Logger.getLogger(ProActive.class.getName());
	
	/**
	 * Constructor
	 * @param classOfException
	 * @throws NonFunctionalException
	 */
	public TestNFE() {
	}
	
	
	/**
 	* Raise a non functional exception
 	*/
	public void raiseException(Class classOfException) throws NonFunctionalException {

		System.out.println("RAISE EXCEPTION => " + classOfException.getName());

		// We create a non functional exception of the desired class
		NonFunctionalException ex = null;
		try {
			ex = (NonFunctionalException) classOfException.getConstructor(new Class[] {String.class, Throwable.class}).newInstance(new Object[] {classOfException.getName(), null});
		} catch (Exception e) {
			if (e instanceof ClassNotFoundException)
				if (logger.isDebugEnabled()) {
					logger.debug("*** CANNOT FIND CLASS " + ex);
				}
			else
				e.printStackTrace();
		}

		// We throw the exception
		throw ex;
	}

	/**
	 *  test method
	 * @param args
	 */
	public void go(String[] args) {
		
		// We need one arg : the name of a class of non functional exception
		System.out.println();
		if (args.length != 1) {
			System.out.println("USAGE =>  java org.objectweb.proactive.examples.nfeCreation NFE");
			System.exit(0);
		}
	
		// A non functional Exception
		NonFunctionalException ex = null;
	
		// Then we try to create this exception
		try {
			ex = (NonFunctionalException) Class.forName(args[0]).getConstructor(new Class[] {String.class, Throwable.class}).newInstance(new Object[] {args[0], null});
		} catch (ClassNotFoundException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("*** ERROR => Class " + args[0] + " is not a valid NFE");
			}
			System.exit(0);
		} catch (InstantiationException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("*** ERROR => A problem occurs during instantiation " + args[0]);
			}
			e.printStackTrace();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	
		// We then show information about the exception
		System.out.println("PARAMETER => " + ex.getDescription() + ex.getMessage());
		
		// We try some basic stuff (set and remove handlers)
		System.out.println();
		ProActive.setExceptionHandler(HandlerNonFunctionalException.class, NonFunctionalException.class, Handler.ID_Default, null);
		//ProActive.unsetExceptionHandler(Handler.ID_Default, null, HandlerNonFunctionalException.class);
		ProActive.setExceptionHandler(HandlerCommunicationException.class, ProActiveCommunicationException.class, Handler.ID_VM, null);
		//ProActive.unsetExceptionHandler(Handler.ID_VM, null, NonFunctionalException.class);
				
		// Raise an exception
		System.out.println();
		try {
			raiseException(ProActiveMigrationException.class);
		} catch (NonFunctionalException nfe) {
			Handler handler = ProActive.searchExceptionHandler(nfe, null); 
			if (handler != null) {
				handler.handle(nfe, null);
				System.out.println("EXCEPTION CATCHED BY => " + handler.getClass().getName());
			} else {
				System.out.println("EXCEPTION NOT CATCHED");
			}
		}

		// Raise an exception
		System.out.println();
		try {
			raiseException(SendRequestCommunicationException.class);
		} catch (NonFunctionalException nfe) {
			Handler handler = ProActive.searchExceptionHandler(nfe, null);
			if (handler != null) {
				handler.handle(nfe, null);	
				System.out.println("EXCEPTION CATCHED BY => " + handler.getClass().getName());
			} else {
				System.out.println("EXCEPTION NOT CATCHED");
			}
		}
	}
	
	/**
	 *  Main program
	 */
	public static void main(String[] args) {
	
		// Creation of an active test object
		TestNFE test = new TestNFE(); 
		test.go(args);
		
		// Exit program
		System.exit(0);
	}
}

