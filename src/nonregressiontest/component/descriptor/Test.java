/*
 * Created on Oct 23, 2003
 * author : Matthieu Morel
  */
package nonregressiontest.component.descriptor;

import nonregressiontest.component.I1;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;
import nonregressiontest.component.PrimitiveComponentC;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.core.component.xml.Loader;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.group.ProActiveGroup;

import testsuite.test.FunctionalTest;

/**
 * This is a test for the former components descriptor, not used anymore : it has been
 * replaced by the Fractal ADL
 * @author Matthieu Morel
 */
public class Test extends FunctionalTest {
	private static String COMPONENTS_DESCRIPTOR_LOCATION =
		Test.class.getResource("/nonregressiontest/component/descriptor/componentsDescriptor.xml").getPath();
	private static String DEPLOYMENT_DESCRIPTOR_LOCATION =
		Test.class.getResource("/nonregressiontest/component/descriptor/deploymentDescriptor.xml").getPath();
	public static String MESSAGE = "-->Main";
	private Message message;

	//ComponentsCache componentsCache;
	ProActiveDescriptor deploymentDescriptor;

	public Test() {
		super("Components descriptor (obsolete)", "Test instantiation of a component system based on a components descriptor");
	}

	/* (non-Javadoc)
	 * @see testsuite.test.FunctionalTest#action()
	 */
	public void action() throws Exception {
		System.setProperty("proactive.future.ac", "enable");
		// start a new thread so that automatic continuations are enabled for components
		ACThread acthread = new ACThread();
		acthread.start();
		acthread.join();
		System.setProperty("proactive.future.ac", "disable");
	}

	private class ACThread extends Thread {
		public void run() {
			try {
				// instantiate and deploy components
				Loader component_loader = new Loader();
				component_loader.loadComponentsConfiguration(
					"file:" + COMPONENTS_DESCRIPTOR_LOCATION,
					"file:" + DEPLOYMENT_DESCRIPTOR_LOCATION);
				// start components
				Component c = component_loader.getComponent("c");

				//System.out.println("name of c is : " + ((ComponentParametersController)c.getFcInterface(ComponentParametersController.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters().getName());
				Component p2 = component_loader.getComponent("p2");
				Component parallel = component_loader.getComponent("parallel");

				//System.out.println("name of p2 is : " + ((ComponentParametersController)p2.getFcInterface(ComponentParametersController.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters().getName());
				Fractal.getLifeCycleController(c).startFc();
				Fractal.getLifeCycleController(p2).startFc();
				Fractal.getLifeCycleController(parallel).startFc();

				// invoke method on composite
				I1 i1 = (I1) parallel.getFcInterface("i1-server");

				//I1 i1= (I1)p1.getFcInterface("i1");
				message = i1.processInputMessage(new Message(MESSAGE)).append(MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see testsuite.test.AbstractTest#initTest()
	 */
	public void initTest() throws Exception {
	}

	/* (non-Javadoc)
	 * @see testsuite.test.AbstractTest#endTest()
	 */
	public void endTest() throws Exception {
		//deploymentDescriptor.killall(false);
	}

	public boolean postConditions() throws Exception {
		StringBuffer resulting_msg = new StringBuffer();
		int message_size = ProActiveGroup.size(message);
		for (int i = 0; i < message_size; i++) {
			resulting_msg.append(((Message) ProActiveGroup.get(message, i)).toString());
		}

		// this --> primitiveC --> primitiveA --> primitiveB--> primitiveA --> primitiveC --> this  (message goes through parallel and composite components)
		String single_message =
			Test.MESSAGE
				+ PrimitiveComponentC.MESSAGE
				+ PrimitiveComponentA.MESSAGE
				+ PrimitiveComponentB.MESSAGE
				+ PrimitiveComponentA.MESSAGE
				+ PrimitiveComponentC.MESSAGE
				+ Test.MESSAGE;

		return resulting_msg.toString().equals(single_message + single_message);
	}

	public static void main(String[] args) {
		Test test = new Test();
		try {
			test.action();
			test.postConditions();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				test.endTest();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
