package nonregressiontest.component.migration;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;

public class Z implements D, Serializable {
	
	int barCounter = 0;

	public StringWrapper bar(StringWrapper s) {
//		System.out.println("z processing bar method " + barCounter);
		barCounter++;
		return s;
	}

}
