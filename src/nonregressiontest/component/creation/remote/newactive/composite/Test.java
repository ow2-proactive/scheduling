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
package nonregressiontest.component.creation.remote.newactive.composite;

import nonregressiontest.component.I1;
import nonregressiontest.component.I2;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;
import nonregressiontest.descriptor.defaultnodes.TestNodes;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Fractal;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

import testsuite.test.FunctionalTest;

/**
 * @author Matthieu Morel
 * 
 * Step 1. Creation of the components
 *
 * Creates the following components :
 * 
 * 		__________________
 * 		|									|					________
 * 		|									|					|				|
 * 	i1	|									|i2 			i2	|	(p2)		|
 * 		|									|					|_______|
 * 		|									|					
 * 		|_(c1)_____________|					
 * 																
 * 		__________________
 * 		|									|					________
 * 		|									|					|				|
 * 	i1	|									|i2 			i1	|	(p1)		|i2
 * 		|									|					|_______|
 * 		|									|					
 * 		|_(c2)_____________|						
 *
 * 	where :
 * 		(c1) and (c2) are composites, (p1) and (p2) are primitive components
 * 		i1 represents an interface of type I1
 * 		i2 represents an interface of type I2
 * 		c1 and p2 are on a remote JVM
 * 
  */
public class Test extends FunctionalTest {
	private static final String P1_NAME = "primitive-component-1";
	private static final String P2_NAME = "primitive-component-2";
	private static final String C1_NAME = "composite-component1";
	private static final String C2_NAME = "composite-component2";
	public static final String MESSAGE = "-->Main";
	Component primitiveComponentA;
	String name;
	String nodeUrl;
	Message message;
	Component p1;
	Component p2;
	Component c1;
	Component c2;

	public Test() {
		super(
			"Creation of a composite system on remote nodes",
			"Test creation of a composite system on remote nodes");

	}

	/**
	 * @see testsuite.test.FunctionalTest#action()
	 */
	public void action() throws Exception {
		throw new testsuite.exception.NotStandAloneException();
	}

	/**
	 * first of interlinked tests
	 * @param obj
	 */
	public Component[] action(Object obj) throws Exception {
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
				ProActiveTypeFactory type_factory = ProActiveTypeFactory.instance();
				ComponentType i1_i2_type =
					type_factory.createFcType(
						new InterfaceType[] {
							type_factory.createFcItfType(
								"i1",
								I1.class.getName(),
								TypeFactory.SERVER,
								TypeFactory.MANDATORY,
								TypeFactory.SINGLE),
							type_factory.createFcItfType(
								"i2",
								I2.class.getName(),
								TypeFactory.CLIENT,
								TypeFactory.MANDATORY,
								TypeFactory.SINGLE)});

				ComponentParameters p1_parameters =
					new ComponentParameters(P1_NAME, ComponentParameters.PRIMITIVE, i1_i2_type);

				ComponentParameters p2_parameters =
					new ComponentParameters(
						P2_NAME,
						ComponentParameters.PRIMITIVE,
						type_factory.createFcType(
							new InterfaceType[] {
								type_factory.createFcItfType(
									"i2",
									I2.class.getName(),
									TypeFactory.SERVER,
									TypeFactory.MANDATORY,
									TypeFactory.SINGLE),
								}));
				ComponentParameters c1_parameters =
					new ComponentParameters(C1_NAME, ComponentParameters.COMPOSITE, i1_i2_type);
				ComponentParameters c2_parameters =
					new ComponentParameters(C2_NAME, ComponentParameters.COMPOSITE, i1_i2_type);
				p1 = ProActive.newActiveComponent(PrimitiveComponentA.class.getName(), new Object[] {
				}, null, null, null, p1_parameters);
				p2 = ProActive.newActiveComponent(PrimitiveComponentB.class.getName(), new Object[] {
				}, TestNodes.getRemoteACVMNode(), null, null, p2_parameters);
				c1 = ProActive.newActiveComponent(Composite.class.getName(), new Object[] {
				}, TestNodes.getRemoteACVMNode(), null, null, c1_parameters);
//				p2 = ProActive.newActiveComponent(PrimitiveComponentB.class.getName(), new Object[] {
//								}, null, null, null, p2_parameters);
//								c1 = ProActive.newActiveComponent(Composite.class.getName(), new Object[] {
//								}, null, null, null, c1_parameters);
				c2= ProActive.newActiveComponent(Composite.class.getName(), new Object[] {
				}, null, null, null, c2_parameters);

			} catch (Exception e) {
				logger.error("cannot create component : " + e.getMessage());
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
			String p1_name = Fractal.getComponentParametersController(p1)
					.getComponentParameters()
					.getName();
			String p2_name =
			Fractal.getComponentParametersController(p2)
					.getComponentParameters()
					.getName();
			String c1_name =
			Fractal.getComponentParametersController(c1)
					.getComponentParameters()
					.getName();
			String c2_name =
			Fractal.getComponentParametersController(c2)
					.getComponentParameters()
					.getName();
			return (p1_name.equals(P1_NAME) && p2_name.equals(P2_NAME) && c1_name.equals(C1_NAME) && c2_name.equals(C2_NAME));

	}

	public static void main(String[] args) {
		Test test = new Test();
		try {
			test.action();
			test.postConditions();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

