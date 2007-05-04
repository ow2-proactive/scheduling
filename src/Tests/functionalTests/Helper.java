package functionalTests;

import java.io.File;
import java.io.IOException;


public class Helper {
	
	static public void killJVMs() {
		File dir = new File(System.getProperty("proactive.dir"));
		File cmd = new File(dir + "/dev/scripts/killTests");
		if (cmd.exists()) {
			try {
				Runtime.getRuntime().exec(cmd.toString(), null, dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println(cmd + "does not exist");
		}
	}
}
