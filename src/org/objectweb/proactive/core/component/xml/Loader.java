/*
 * Created on Dec 9, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.runtime.RuntimeFactory;

/**
 * @author Matthieu Morel
 */
public class Loader {
	
	protected static Logger logger = Logger.getLogger(Loader.class.getName());
	
	private ComponentsCache cache;
	
	public Loader() {
	}
	
	public void loadComponentsConfiguration(
		String componentsDescriptorURL, ProActiveDescriptor deploymentDescriptor) throws ProActiveException {
		RuntimeFactory.getDefaultRuntime();
		try {
			cache =  (ComponentsCache)ComponentsDescriptorHandler.createComponentsDescriptorHandler(componentsDescriptorURL, deploymentDescriptor).getResultObject();
		} catch (org.xml.sax.SAXException e) {
			e.printStackTrace();
			logger.fatal("a problem occured when getting the ProActive descriptor or the ComponentsDescriptor");
			throw new ProActiveException(e);
		} catch (java.io.IOException e) {
			e.printStackTrace();
			logger.fatal("a problem occured during the ProActiveDescriptor object creation");
			throw new ProActiveException(e);
		}
	}
	
	public Component getComponent(String name) {
		return cache.getComponent(name);
	}


}
