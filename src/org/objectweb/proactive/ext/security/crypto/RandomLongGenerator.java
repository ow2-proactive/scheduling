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
package org.objectweb.proactive.ext.security.crypto;
import java.security.*;

/**
 * This class is a proper random generator.
 *
 * @author     Vincent RIBAILLIER
 * @created    July 19, 2001
 */
public class RandomLongGenerator {
	private byte[] seed;
	private SecureRandom secureRandom;


	/**
	 *  Constructor for the RandomLongGenerator object
	 *
	 * @since
	 */
	public RandomLongGenerator() {
		secureRandom = new SecureRandom();
	}

	/**
	 * Generates a long
	 *
	 * @param  nbBytes number of bytes (8 bytes maximum)
	 * @return         The random long
	 * @since
	 */
	public long generateLong(int nbBytes) {
		if (nbBytes > 8) {
			nbBytes = 8;
		}

		seed = new byte[nbBytes];
		seed = secureRandom.generateSeed(nbBytes);

		long ra2 = 0;

		for (int i = 0; i < 4; i++) {
			ra2 = ra2
					 + (Math.abs(new Byte(seed[i]).longValue()))
					 * new Double(Math.pow(10,
					(-3 + 3 * (i + 1)))).longValue();
		}

		return ra2;
	}

}

