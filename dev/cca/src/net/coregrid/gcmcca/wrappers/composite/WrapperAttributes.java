package net.coregrid.gcmcca.wrappers.composite;

import org.objectweb.fractal.api.control.AttributeController;

public interface WrapperAttributes extends AttributeController {
	
	public void setComponentID(String componentID);
	
	public String getComponentID();

}
