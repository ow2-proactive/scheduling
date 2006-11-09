package net.coregrid.gcmcca.wrappers.composite;

public interface CCACompositeAttributes  {
    public static final String NAME = "cca-controller";

//	public void setComponentID(String componentID);
//	
//	public String getComponentID();
	
	public CCACompositeDescriptor getDescriptor();
	
	public void setDescriptor (CCACompositeDescriptor descriptor);

}
