/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 * 
 * ################################################################
 */
package org.objectweb.proactive.calcium.examples.findprimes;

import java.util.Collections;
import java.util.Vector;

import org.objectweb.proactive.calcium.interfaces.Conquer;

public class ConquerChallenge implements Conquer<Challenge>{
	
	public Challenge conquer(Challenge parent, Vector<Challenge> p) {
			
		for(Challenge param:p){
			parent.max=Math.max(parent.max, param.max);
			parent.min=Math.min(parent.min, param.min);
			parent.primes.addAll(param.primes);
		}

		Collections.sort(parent.primes);
		return parent;
	}
}
