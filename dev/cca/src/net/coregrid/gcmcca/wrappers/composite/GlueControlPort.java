package net.coregrid.gcmcca.wrappers.composite;

import org.objectweb.fractal.api.control.AttributeController;
import mocca.cca.Port;
import org.objectweb.fractal.api.Component;

public interface GlueControlPort extends Port {
	
	public void setComponent(Component fractalComponent);
	
	public String getComponentURL();

}
