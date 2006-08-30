package nonregressiontest.component.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

public class Y implements B, BindingController, Serializable {

	int fooCounter = 0;
	C c = null;
	

	public List<StringWrapper> foo(List<StringWrapper> l) {
		System.out.println("y processing foo method " + fooCounter);
		fooCounter++;
		List<StringWrapper> result = c.bar(l);
		return result;
	}
	
	public List<StringWrapper> gee() {
		List<StringWrapper>l = new ArrayList<StringWrapper>();
		l.add(new StringWrapper("hello gee"));
		return l;
	}

	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if ("c".equals(clientItfName)) {
			c = (C)serverItf;
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}
	}

	public String[] listFc() {
		return new String[] {"c"};
	}

	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if ("c".equals(clientItfName)) {
			return c;
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}
	}

	public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if ("c".equals(clientItfName)) {
			c = null;
		} else {
			throw new NoSuchInterfaceException(clientItfName);
		}

		
	}

}
