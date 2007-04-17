/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.monitoring.finder;

import org.objectweb.proactive.core.Constants;

/**
 * Factory design pattern.
 */
public class HostRTFinderFactory {

	//
	// -- PUBLICS METHODS -----------------------------------------------
	//

	/**
	 * Create a HostRTFinder corresponding to the protocol
	 * @param protocol The protocol to use
	 * @return A HostRTFinder
	 */
	public static HostRTFinder createHostRTFinder(String protocol) {
		if(protocol.equals(Constants.RMI_PROTOCOL_IDENTIFIER))
			return new RMIHostRTFinder();
		else if(protocol.equals(Constants.RMISSH_PROTOCOL_IDENTIFIER))
			return new RMIHostRTFinder();
		else if(protocol.equals(Constants.XMLHTTP_PROTOCOL_IDENTIFIER))
			return new HttpHostRTFinder();
		else if(protocol.equals(Constants.IBIS_PROTOCOL_IDENTIFIER))
			return new IbisHostRTFinder();
		else if(protocol.equals(Constants.JINI_PROTOCOL_IDENTIFIER))
			return new JiniHostRTFinder();
		else
			return null;
	}
}
