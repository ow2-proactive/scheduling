package org.objectweb.proactive.examples.eratosthenes;

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

/**
* @author Jonathan Streit
* Serves to print newly found prime numbers to the console.
*/
public class ConsolePrimeOutputListener implements PrimeOutputListener, java.io.Serializable {

  private long startTime;

  /**
   * Constructor for ConsolePrimeOutputListener.
   */
  public ConsolePrimeOutputListener() {
    super();
  }

  public void newPrimeNumberFound(long n) {
  	if (startTime == 0) startTime = System.currentTimeMillis();
  	String time = Long.toString((System.currentTimeMillis() - startTime) / 1000);
  	StringBuffer line = new StringBuffer(50);
  	line.append("    ");
  	for (int i = time.length(); i < 6; i ++) line.append('0');
  	line.append(time);
  	line.append(":\tNew prime number found: ");
  	line.append(n);
  	line.append('\n');
  	System.out.print(line);
  }

}
