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
package test.ibis.serialization;

import java.io.IOException;

import org.objectweb.proactive.Service;

/**
 * @author fhuet
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Test {


public static void main(String[] arguments) {
	Object source = new Service(null); //Test.class;
	java.io.ByteArrayOutputStream tmp = new java.io.ByteArrayOutputStream();
	ibis.io.ArrayOutputStream baos = new ibis.io.BufferedArrayOutputStream(tmp);
	 ibis.io.IbisSerializationOutputStream oos;
	try {
		oos = new ibis.io.IbisSerializationOutputStream(baos);
	
	 oos.writeObject(source);
	 oos.flush();
	 oos.close();
	} catch (IOException e) {
			e.printStackTrace();
		}
		
		java.io.ByteArrayInputStream tmp2 = new java.io.ByteArrayInputStream(tmp.toByteArray());
	ibis.io.ArrayInputStream bais = new ibis.io.BufferedArrayInputStream(tmp2); //= new java.io.ByteArrayInputStream(baos.toByteArray());
	
	   try {
		ibis.io.IbisSerializationInputStream ois = new ibis.io.IbisSerializationInputStream(bais);
	   
		 Object result = ois.readObject();
		 ois.close();
		 //return result;
	   } catch (Exception e) {
		 e.printStackTrace();
	   }
		
}

}
