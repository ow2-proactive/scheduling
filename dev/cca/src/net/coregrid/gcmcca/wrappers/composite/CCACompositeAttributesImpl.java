package net.coregrid.gcmcca.wrappers.composite;

import mocca.cca.CCAException;
import mocca.cca.ComponentID;
import mocca.cca.TypeMap;
import mocca.client.MoccaBuilderClient;
import mocca.client.MoccaMainBuilder;
import mocca.srv.impl.MoccaComponentID;
import mocca.srv.impl.MoccaTypeMap;
import net.coregrid.gcmcca.example.HelloPort;

import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.util.Fractal;


//now works only for a "composite system" made of single component
public class CCACompositeAttributesImpl extends AbstractProActiveController implements CCACompositeAttributes  {

	private String componentID;	
	private CCACompositeDescriptor compositeDescriptor = new CCACompositeDescriptor();
	
	private static Logger logger = Logger.getLogger(CCACompositeAttributesImpl.class.getName());
	  

	//maps names to client interfaces for BindingController
	protected Map clientInterfaces = new HashMap();
	//maps names to ComponentID of glue components
	protected Map namedGlueIDs = new HashMap();
	protected MoccaMainBuilder builder; 


	public CCACompositeAttributesImpl(Component owner) {
		super(owner);
	}

	protected void setControllerItfType() {
		 try {
		   setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(
		     CCACompositeAttributes.NAME, CCACompositeAttributes.class.getName(), TypeFactory.SERVER,
             TypeFactory.MANDATORY, TypeFactory.SINGLE));
		   } catch (InstantiationException e) {
		     throw new ProActiveRuntimeException("cannot create controller type: " +
		       this.getClass().getName());
		   }
		 }	
	
//	public void setComponentID(String componentID) {
//		this.componentID = componentID;
//		createGlue();
//
//	}
//
//	public String getComponentID() {
//		return componentID;
//	}

	public void setDescriptor(CCACompositeDescriptor descriptor) {
		this.compositeDescriptor = descriptor;
		createGlue();
	}
	
	public CCACompositeDescriptor getDescriptor() {
		
//		//TODO this works now only for one glue component
//		compositeDescriptor.addUsesPort("h", componentID);
//		compositeDescriptor
//				.setClassPath("h",
//						"gcmcca/glue-component.jar");
//		compositeDescriptor.setType("h", HelloPort.class.getName()); 
//		// this can be taken from cType
//		compositeDescriptor.addProvidesPort("MyGoPort", componentID);
		return compositeDescriptor;

		
	}

	
	
	public void createGlue() {
		
		builder = new MoccaMainBuilder();

		try {
			compositeDescriptor = getDescriptor();

			// create glue components for server interfaces
			String[] providesPortNames = compositeDescriptor.listProvidesPortNames();
			for (int i = 0; i < providesPortNames.length; i++) {
				String portName = providesPortNames[i];
				createServerGlue(portName);
			}
			
			// create glue components for client interfaces
			String[] usesPortNames = compositeDescriptor.listUsesPortNames();
			for (int i = 0; i < usesPortNames.length; i++) {
				String portName = usesPortNames[i];
				createClientGlue(portName);				
			}
			
		} catch (CCAException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchInterfaceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalBindingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalLifeCycleException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * Create CCA -> Fractal glue component for a portName uses port of glued CCA component 
	 * @param portName Name of the glued port
	 * @param gluedComponentID ID of glued component
	 * @throws Exception
	 */
	public void createClientGlue(String portName) throws Exception {

		ComponentID gluedComponentID = builder.getDeserialization(compositeDescriptor
				.getClientComponentID(portName));

		//get the builder ID from the component ID
        MoccaComponentID moccaGluedID = (MoccaComponentID) gluedComponentID;
        String serializedBuilderID = moccaGluedID.getParentURI().toString();
        TypeMap properties = new MoccaTypeMap();        
        properties.putString("mocca.builderID", serializedBuilderID);
        
        logger.fine("mocca.builderID: " + serializedBuilderID);

        String glueName = gluedComponentID.getInstanceName() + "-" + portName + "-Glue";
        properties.putString("mocca.plugletclasspath", compositeDescriptor.getClassPath(portName));
        String glueClass = compositeDescriptor.getClientGlueClassName(portName);

        logger.fine("Creating client glue for port name [" + portName + "] of type [" + glueClass + "]");
        
        ComponentID glueID = builder.createInstance(glueName, glueClass, properties);
        
        logger.fine("Created glue with GlueID :[" + glueID.getSerialization() +"]");

		namedGlueIDs.put(portName, glueID);
		// connect CCA client component to Glue component
		builder.connect(gluedComponentID, portName, glueID, portName);
		
		//get a reference to the glue component as fractal component
		
		String glueURL = (String) MoccaBuilderClient.invokeMethodOnComponent(
				glueID, 
				GlueControlPort.class.getName(), 
				"glue-control", 
				"getComponentURL", 
				new Object[] {}
		);
		Component glue = Fractive.lookup(glueURL);

		
		//bind the glue to the membrane
		Fractal.getBindingController(glue).bindFc(portName, 
				Fractive.getComponentRepresentativeOnThis().getFcInterface(portName));

		// start the glue
		Fractal.getLifeCycleController(glue).startFc();


//		// pass a reference of Fractal Server component to glue component
//		Component fractalComponent =  Fractive.getComponentRepresentativeOnThis();
//		String URL = UrlBuilder.buildUrlFromProperties("localhost", "hello");
//		// get localhost address
//		try {
//			Fractive.register(fractalComponent, URL);
//			System.out.println("Bind succeeded for URL: " + URL);
//			TypeMap properties = new MoccaTypeMap();
//			properties.putString("URL", URL);
//
//			MoccaBuilderClient.writeConfigurationMap(glueID, "config",
//					properties);
////			MoccaBuilderClient.invokeMethodOnComponent(
////					glueID, 
////					GlueControlPort.class.getName(), 
////					"glue-control", 
////					"setComponent", 
////					new Object[] {fractalComponent}
////			);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		        
    }
	
	
	/**
	 * Create Fractal -> CCA glue component
	 * @param portName Name of glued port
	 * @param gluedComponentID ID of glued CCA component
	 * @return Fractal reference to created component
	 * @throws Exception
	 */
	public void createServerGlue (String portName) throws Exception {
		
		ComponentID gluedComponentID = builder.getDeserialization(
				compositeDescriptor.getServerComponentID(portName));
	
		
        Component boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();
        TypeFactory tf = Fractal.getTypeFactory(boot);

        // type of wrapper component
        String signature = ((InterfaceType)((Interface)owner.getFcInterface(portName)).getFcItfType()).getFcItfSignature();
        logger.fine("Creating server glue for port name [" + portName + "] of type [" + signature + "]");
        
        ComponentType glueType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType(portName, signature, TypeFactory.SERVER ,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    tf.createFcItfType("attribute-controller",
                            WrapperAttributes.class.getName(), TypeFactory.SERVER ,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE)                    
                });

        GenericFactory cf = Fractal.getGenericFactory(boot);
		
        String glueName = gluedComponentID.getInstanceName() + "-" + portName + "-Glue";
        
        String glueClass = compositeDescriptor.getServerGlueClassName(signature);

        logger.fine("Creating server glue component with name [" + glueName + "] and content [" + glueClass + "]");

        Component glue = cf.newFcInstance(glueType,
                new ControllerDescription(glueName, Constants.PRIMITIVE),
                new ContentDescription(glueClass));

		// pass a CCA component ID to glue component
		((WrapperAttributes) Fractal.getAttributeController(glue))
				.setComponentID(gluedComponentID.getSerialization());
		// bind membrane to the glue
		Fractal.getBindingController(owner).bindFc(portName,
				glue.getFcInterface(portName));
		Fractal.getLifeCycleController(glue).startFc();

	}


	
	
}
