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
package benchmark.nfe;


import java.io.BufferedWriter;
import java.io.FileWriter;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.exceptions.handler.HandlerNonFunctionalException;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveBenchmark;


/**
 * @author Alexandre Genoud
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BenchMigrationHandlerizable extends ProActiveBenchmark {

	private A a = null;	
	private int nb_handlers, nb_handlers_tmp = 0;
	private Node node_dest = null;
	
	private String file;
	
	/**
	  *
	  */
	 public BenchMigrationHandlerizable() {
		 super(null, "Migration of a Mobile Object",
			 "Benchs migration time for a mobile object.");
	 }

	 /**
	  * @param node
	  */
	 public BenchMigrationHandlerizable(Node node_creation, Node node_dest, int nb_handlers, String class_file) {
		 super(node_creation, "Migration of a Mobile Object",
			 "Benchs migration time for a mobile object.");
		 this.nb_handlers =nb_handlers;
		 this.node_dest = node_dest;
		 this.file = class_file;
		 
		// Create an active object on the given node and get the body
		Node node = getNode();
		try {
			a = (A) org.objectweb.proactive.ProActive.newActive(A.class.getName(), null, node);
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }

	 /**
	  * @see testsuite.test.Benchmark#action()
	  */
	 public long action() throws Exception {
		
		System.out.print("Object protected before migration with " + a.getHandlerNumber() + " " + file);
		
		/*UniversalBody body = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) a).getProxy()).getBody();*/
		/*
		FileOutputStream out = new FileOutputStream(a.getTableOfHandler().getClass().getName() + " BEFORE MIG. " + a.getNodeUrl() + " - SERIALIZED with " + a.getHandlerNumber() + " " + file);
		ObjectOutputStream s = new ObjectOutputStream(out);
		s.writeObject(a.getTableOfHandler());
		s.close();
		*/
		
		this.timer.start();
		a.moveTo(node_dest);
		this.timer.stop();
		 
		/*body = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) a).getProxy()).getBody();*/
		/*
		out = new FileOutputStream(a.getTableOfHandler().getClass().getName() + " AFTER MIG. " + a.getNodeUrl() + " - SERIALIZED with " + a.getHandlerNumber() + " " + file);
		s = new ObjectOutputStream(out);
		s.writeObject(a.getTableOfHandler());
		s.close();
		*/
		
		System.out.println(" / after migration with " + a.getHandlerNumber() + " " + file);
		
		// Generate Benchmarks (GNUplot) for iteration tests  
		FileWriter fw = new FileWriter("MIGRATION to " + node_dest.getNodeInformation().getURL().replace('/',' ') + " WITH " + file + ".test", true);
		BufferedWriter bw=new BufferedWriter(fw);
		bw.write(a.getHandlerNumber() + " \t "+ this.timer.getCumulatedTime());
		bw.newLine();
		bw.close();
		
		return this.timer.getCumulatedTime();
	 }

	 /**
	  * @see testsuite.test.AbstractTest#initTest()
	  */
	 public void initTest() throws Exception {
		// Generating NFE class with different hashcode and using them as key
		nb_handlers_tmp = nb_handlers; 
		//UniversalBody body = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) a).getProxy()).getBody();
		while ((nb_handlers_tmp--) >= 1) {
			try {
				ProActive.setExceptionHandler(HandlerNonFunctionalException.class, 
																	Class.forName("benchmark.nfe." + file + nb_handlers_tmp).newInstance().getClass(),
																	Handler.ID_Body, a);
			} catch (ClassNotFoundException e) {
				System.out.println("NFE Class generNFE" + nb_handlers_tmp + " not found : " + e.toString());
			} catch (IllegalAccessException e) {
				System.out.println("NFE Class generNFE" + nb_handlers_tmp + " cannot be accessed : " + e.toString());
			} catch (InstantiationException e) {
				System.out.println("NFE Class generNFE" + nb_handlers_tmp + " cannot be instantiated : " + e.toString());
			}
		}	
	 }

	 /**
	  * @see testsuite.test.AbstractTest#endTest()
	  */
	 public void endTest() throws Exception {
		 // nothing to do
	 }

	 /**
	  * @see testsuite.test.AbstractTest#preConditions()
	  */
	 public boolean preConditions() throws Exception {
		 return (getNode() != null);
	 }
    
	 public boolean postConditions() throws Exception {
		 return true;
	 }
	 
	// Internal class of NFE modifying the hashcode method
	private class InternalNFE extends NonFunctionalException {
		int hashmod = 0;
		public InternalNFE(int hashmod) {
			super();
			this.hashmod = hashmod;
		}
		public int hashCode() {
			return (super.hashCode() + (hashmod));
		}
	}

}