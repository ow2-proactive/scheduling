package test.guidedtour;

public class Main {

	/** entry point for the program
	* @param args destination nodes
	* for example :
	* rmi://localhost/node1 jini://localhost/node2
	*/
	public static void main(String[] args) {

		//		// instanciation-based creation of the active object
		//		MigratableHello active_agent = MigratableHello.create("agent1");
		//		// check if the active_agent has been created
		//		if (active_agent != null) {
		//			// say hello
		//			System.out.println(active_agent.sayHello());
		//			// start moving the object around
		//			for (int i = 0; i < args.length; i++) {
		//				active_agent.moveTo(args[i]);
		//				active_agent.sayHello();
		//				// wait for a little while before next migration
		//				try {
		//					Thread.sleep(5000);
		//				} catch (InterruptedException ie) {
		//					System.out.println("problem while pausing : " + ie.toString());
		//					ie.printStackTrace();
		//				}
		//			}
		//		} else {
		//			System.out.println("creation of the active object failed");
		//		}

		HelloFrameController gui = HelloFrameController.createHelloFrameController("agent2");
		gui.sayHello();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			System.out.println("problem while pausing : " + ie.toString());
			ie.printStackTrace();
		}
		// start moving the object around
		for (int i = 0; i < args.length; i++) {
			gui.moveTo(args[i]);
			gui.sayHello();
			// wait for a little while before next migration
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) {
				System.out.println("problem while pausing : " + ie.toString());
				ie.printStackTrace();
			}
		}
		
		gui.terminate();

	}
}