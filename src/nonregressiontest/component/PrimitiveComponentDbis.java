/*
 * Created on Apr 30, 2004
 * author : Matthieu Morel
  */
package nonregressiontest.component;

import java.util.Set;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

/**
 * @author Matthieu Morel
 */
public class PrimitiveComponentDbis extends PrimitiveComponentD {

	public void bindFc(String clientItfName, Object serverItf) {
		if (clientItfName.startsWith(I2_ITF_NAME)) {
			i2Group.addNamedElement(clientItfName, serverItf);
		} else {
			logger.error("Binding impossible : wrong client interface name");
		}
	}
	
	public String[] listFc() {
		Set itf_names = i2Group.keySet();
		return (String[])itf_names.toArray(new String[itf_names.size()]);
	}
	
	public void unbindFc(String clientItf)
		throws NoSuchInterfaceException, IllegalBindingException, 
			IllegalLifeCycleException {
		if (i2Group.containsKey(clientItf)) {
			i2Group.removeNamedElement(clientItf);
		} else {
			logger.error("client interface not found");
		}
	}
	
	public Object lookupFc(String clientItf) throws NoSuchInterfaceException {
		if (i2Group.containsKey(clientItf)) {
			return i2Group.getNamedElement(clientItf);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("cannot find " + clientItf + " interface");
			}
			return null;
		}
	}



}
