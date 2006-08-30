package nonregressiontest.component.migration;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;

public class X implements E, Serializable {
	
	int i = 0;

	public StringWrapper gee(StringWrapper s) {
//		System.out.println("x processing gee method from node : " + ProActiveRuntimeImpl.getProActiveRuntime().getURL());
		return s;
	}

}
