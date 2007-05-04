package functionalTests;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.util.OperatingSystem;


public class Helper {
	
	static public void killJVMs() {
		File dir = new File(System.getProperty("proactive.dir"));
		File cmd = new File(dir + "/dev/scripts/killTests");
		if (cmd.exists()) {
			try {
				switch (OperatingSystem.getOperatingSystem()) {
				case unix:
					Runtime.getRuntime().exec(cmd.getAbsolutePath().replaceAll(" ", "\\ "), null, dir);
					break;

				default:
					System.err.println("TODO: Kill JVMs on Windows also !");
					break;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println(cmd + "does not exist");
		}
	}
}
