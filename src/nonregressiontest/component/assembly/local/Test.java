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
package nonregressiontest.component.assembly.local;

import java.util.Arrays;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.Fractal;

import testsuite.test.FunctionalTest;

/**
 * @author Matthieu Morel
 *
 * Creates the following component system :
 * 
 * 		___________________________
 * 		|		__________________		|
 * 		|		|			______			|		|	
 * 		|		|			|			|			|		|	
 * 	i1	|	i1	|		i1	|(p1)	|i2		|i2 	| i2	
 * 		|		|			|_____|			|		|	
 * 		|		|									|		|			
 * 		|		|_(c1)_____________|		|
 * 		|													|
 * 		|__(c2)____________________|
 * 	where :
 * 		(c2) and (c1) are composite components, (p1) is a primitive component
 * 		i1 represents an interface of type I1
 * 		i2 represents an interface of type I2
 * 
  */
public class Test extends FunctionalTest {
	public static String MESSAGE = "-->Main";
	Component p1;
	Component p2;
	Component c1;
	Component c2;
	String name;
	Component[] c2SubComponents;
	Component[] c1SubComponents;

	public Test() {
		super(
			"Assembly of components on the local default node",
			"Test creation of a composite system on the local default node");

	}

	/**
	 * @see testsuite.test.FunctionalTest#action()
	 */
	public void action() throws Exception {
		throw new testsuite.exception.NotStandAloneException();
	}

	public Component[] action(Component[] components) throws Exception {
		p1 = components[0];
		p2 = components[1];
		c1 = components[2];
		c2 = components[3];
		System.setProperty("proactive.future.ac", "enable");
		// start a new thread so that automatic continuations are enabled for components
		ACThread acthread = new ACThread();
		acthread.start();
		acthread.join();
		System.setProperty("proactive.future.ac", "disable");
		return (new Component[] { p1, p2, c1, c2 });
	}

	/**
	 * @see testsuite.test.AbstractTest#initTest()
	 */
	public void initTest() throws Exception {
	}

	private class ACThread extends Thread {

		public void run() {
			try {
				// ASSEMBLY

				Fractal.getContentController(c1).addFcSubComponent(p1);
				Fractal.getContentController(c2).addFcSubComponent(c1);

				c2SubComponents = Fractal.getContentController(c2).getFcSubComponents();
				c1SubComponents = Fractal.getContentController(c1).getFcSubComponents();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

		/**
		 * @see testsuite.test.AbstractTest#endTest()
		 */
		public void endTest() throws Exception {
		}

		public boolean postConditions() throws Exception {
			Component[] c2_sub_components = { c1 };
			Component[] c1_sub_components = { p1 };

			return (
				Arrays.equals(c2SubComponents, c2_sub_components) && Arrays.equals(c1SubComponents, c1_sub_components));
		}

		public static void main(String[] args) {
//			TestOld test = new TestOld();
//			try {
//				test.action();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}
