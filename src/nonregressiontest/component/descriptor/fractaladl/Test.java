/*
 * Created on Apr 22, 2004
 * author : Matthieu Morel
  */
package nonregressiontest.component.descriptor.fractaladl;

import nonregressiontest.component.I1;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;

//import org.objectweb.fractal.adl.Registry;
import org.objectweb.proactive.core.component.adl.Registry;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.group.ProActiveGroup;

import testsuite.test.FunctionalTest;

/**
 * For a graphical representation, open the MessagePassingExample.fractal with the fractal gui
 * 
 * parralel-2 and sub-components are located on VN3, which is a "multiple" virtual node :
 * it is mapped onto 2 nodes.  
 * Therefore, each of the primitive component inside parallel-2 will have 2 instances, one on
 * each of the underlying nodes.
 * 
 * @author Matthieu Morel
 */
public class Test extends FunctionalTest {
	public static String MESSAGE = "-->Main";
	private Message message;

	//ComponentsCache componentsCache;
	ProActiveDescriptor deploymentDescriptor;

	public Test() {
		super("Deployment of components using the Fractal ADL", "deployment of components using the Fractal ADL");
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
				org.objectweb.proactive.core.component.adl.Launcher.main(
					new String[] {
						"-fractal",
						"nonregressiontest.component.descriptor.fractaladl.MessagePassingExample",
						"",
						Test.class.getResource("/nonregressiontest/component/descriptor/deploymentDescriptor.xml").getPath() });

				Component c = Registry.instance().getComponent("parallel");

				// invoke method on composite
				I1 i1 = (I1) c.getFcInterface("i1");

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
		
//		System.out.println("\nMESSAGE IS : ");
//		System.out.println("-------------------------------------------------");
//		message.printToStream(System.out);
//		System.out.println("-------------------------------------------------");
		
		
		StringBuffer resulting_msg = new StringBuffer();
		append(resulting_msg, message);
		
		//System.out.println("***" + resulting_msg.toString());
		

		// this --> primitiveC --> primitiveA --> primitiveB--> primitiveA --> primitiveC --> this  (message goes through parallel and composite components)
		String single_message =
			Test.MESSAGE
				+ PrimitiveComponentA.MESSAGE
				+ PrimitiveComponentB.MESSAGE
				+ PrimitiveComponentA.MESSAGE
				+ Test.MESSAGE;

		// there should be 5 messages with the current configuration
		return resulting_msg.toString().equals(single_message + single_message + single_message + single_message + single_message);
	}
	
	private void append(StringBuffer buffer, Message message) {
		if (ProActiveGroup.isGroup(message)) {
			for (int i=0; i<ProActiveGroup.size(message); i++) {
				append(buffer, (Message)ProActiveGroup.get(message, i));
			}
		} else {
			buffer.append(message.getMessage());
		}
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
				System.exit(0);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
