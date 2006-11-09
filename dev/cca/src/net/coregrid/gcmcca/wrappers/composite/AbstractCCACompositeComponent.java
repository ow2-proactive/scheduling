package net.coregrid.gcmcca.wrappers.composite;

import glue.net.coregrid.gcmcca.example.HelloPortCCAGlueComponent;

import java.io.IOException;
import java.util.*;
import java.net.URI;

import mocca.cca.CCAException;
import mocca.cca.ComponentID;
import mocca.cca.ComponentRelease;
import mocca.cca.Port;
import mocca.cca.TypeMap;
import mocca.cca.ports.BuilderService;
import mocca.client.MoccaMainBuilder;
import mocca.client.MoccaBuilderClient;
import mocca.srv.impl.MoccaTypeMap;
import mocca.srv.impl.MoccaComponentID;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.util.UrlBuilder;

//move this 


/**
 * @author malawski
 * TODO: extract MOCCA dependencies to be CCA-framework independent
 */
public abstract class AbstractCCACompositeComponent implements BindingController {


	//maps names to client interfaces for BindingController
	protected Map clientInterfaces = new HashMap();
	//maps names to ComponentID of glue components
	protected Map namedGlueIDs = new HashMap();
	protected MoccaMainBuilder builder; 
	//keeps information about wrapped system (ports and component IDs)
	protected CCACompositeDescriptor compositeDescription;
	
	public AbstractCCACompositeComponent() {
		//ProActive does not work without this empty constructor
	}
	
	
	/**
	 * this constructor is called by passing parameters to ContentDescription
	 * 	
	 * @param compositeDescriptor this should be a map of external interfaces of the system to:
	 * 	    component IDs
	 *      port names
	 * @throws Exception
	 */

	public AbstractCCACompositeComponent(CCACompositeDescriptor compositeDescription) throws Exception {
		
		this.compositeDescription = compositeDescription;
		//this should be passed by framework-specific implementation
		builder = new MoccaMainBuilder();

		String[] usesPortNames = compositeDescription.listUsesPortNames();
		//create glue components
		for (int i=0; i< usesPortNames.length; i++) {
			String portName = usesPortNames[i];
			ComponentID gluedComponentID = builder.getDeserialization(
					compositeDescription.getClientComponentID(portName));
			ComponentID glueID = createGlue(portName, gluedComponentID);
			namedGlueIDs.put(portName, glueID);
			// connect CCA client component to Glue component
	        builder.connect(gluedComponentID, portName, glueID, portName);
		}
        
	}

	
	public ComponentID createGlue(String portName, ComponentID gluedComponentID) throws CCAException {
		//get the builder ID from the component ID
        MoccaComponentID moccaGluedID = (MoccaComponentID) gluedComponentID;
        String serializedBuilderID = moccaGluedID.getParentURI().toString();
        TypeMap properties = new MoccaTypeMap();        
        properties.putString("mocca.builderID", serializedBuilderID);
		properties.putString("mocca.plugletclasspath", compositeDescription.getClassPath(portName));
        ComponentID glueID =  builder.createInstance("MyGlueComponent",
                HelloPortCCAGlueComponent.class.getName(),
                properties);
        return glueID;
	}
	
	
	public void bindFc(String name, Object sItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		//we have to get the reference to the component from this interface
		clientInterfaces.put(name, sItf);
		Interface fractalInterface = (Interface) sItf;
		Component fractalComponent = fractalInterface.getFcItfOwner();
//		pass a reference of Fractal Server component to glue component
        String URL = UrlBuilder.buildUrlFromProperties("localhost", "hello");
        //get localhost address
        try {
			Fractive.register(fractalComponent, URL);
			System.out.println("Bind succeeded for URL: " + URL);
	        TypeMap properties = new MoccaTypeMap();
	        properties.putString("URL", URL);
	        ComponentID glueID = (ComponentID) namedGlueIDs.get(name);
	        MoccaBuilderClient.writeConfigurationMap(glueID, "config", properties);

        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String[] listFc() {
	//here we list our registered ports
		return (String[]) clientInterfaces.keySet().toArray(new String[clientInterfaces.size()]);	
	}

	public Object lookupFc(String cItf) throws NoSuchInterfaceException {
		//here we return our registered ports		
		return clientInterfaces.get(cItf);
	}

	public void unbindFc(String cItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		//here we remove hashmap entry
		clientInterfaces.remove(cItf);
	}
	
}
