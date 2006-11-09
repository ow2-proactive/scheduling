package net.coregrid.gcmcca.test;


import net.coregrid.gcmcca.example.CCAStarterComponent;
import net.coregrid.gcmcca.example.HelloComponent;
import net.coregrid.gcmcca.example.HelloPort;
import net.coregrid.gcmcca.wrappers.composite.FractalCompositeStarterComponent;
import net.coregrid.gcmcca.wrappers.composite.CCACompositeDescriptor;
import net.coregrid.gcmcca.wrappers.composite.CCACompositeAttributes;
import net.coregrid.gcmcca.wrappers.composite.WrapperAttributes;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.UrlBuilder;

import mocca.cca.ports.GoPort;
import mocca.client.MoccaMainBuilder;
import h2otest.cases.H2OTestCase;
import h2otest.suites.H2OTestSuite;

import java.net.URL;
import java.net.URI;
import mocca.cca.TypeMap;
import mocca.srv.impl.MoccaTypeMap;
import mocca.test.suites.MoccaBasicSuite;
import mocca.cca.ComponentID;
import mocca.client.MoccaBuilderClient;



public class CompositeControllerWrapperTest extends H2OTestCase{

	public H2OTestSuite getTestSuite() {
		return new MoccaProactiveTestSuite();
	}
	
	
	public void runTest() throws Throwable {
        // -------------------------------------------------------------------
        // DO NOT USE THE FRACTAL ADL
        // -------------------------------------------------------------------
        Component boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();
        TypeFactory tf = Fractal.getTypeFactory(boot);

        
        // type of client component
        ComponentType cType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("MyGoPort", GoPort.class.getName(),
                        false, false, false),
                    tf.createFcItfType("h", HelloPort.class.getName(), true,
                        false, false),
                    tf.createFcItfType("attribute-controller",
                            CCACompositeAttributes.class.getName(), false, false,
                            false)   
                });

        // type of server component
        ComponentType sType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("h", HelloPort.class.getName(), false,
                        false, false),
                    
                });

        GenericFactory cf = Fractal.getGenericFactory(boot);

        // -------------------------------------------------------------------
        // CREATE COMPONENTS DIRECTLY
        // -------------------------------------------------------------------

        ProActiveRuntimeImpl.getProActiveRuntime();

        // create server component
        Component sComp = cf.newFcInstance(sType,
                new ControllerDescription("HelloComponent", Constants.PRIMITIVE),
                new ContentDescription(HelloComponent.class.getName()));

        
        //create MOCCA client component
        URI kernelUri = getKernelURIs()[0];
        MoccaMainBuilder builder = new MoccaMainBuilder();
        ComponentID builderID = builder.addNewBuilder(kernelUri, "builder");
        TypeMap properties = new MoccaTypeMap();        
        properties.putString("mocca.plugletclasspath", "gcmcca/starter-component.jar");
        properties.putString("mocca.builderID", builderID.getSerialization());

        ComponentID clientID =  builder.createInstance("MyStarterComponent",
                                CCAStarterComponent.class.getName(),
                                properties);


        // create the composite system description for the wrapper
        // here the composite system consists of a single component 
        
        CCACompositeDescriptor desc = new CCACompositeDescriptor();
        desc.addUsesPort("h", clientID.getSerialization());
        desc.setClassPath("h", "gcmcca/glue-component.jar");
        desc.setType("h", HelloPort.class.getName()); //this can be taken from cType
        desc.addProvidesPort("MyGoPort", clientID.getSerialization());
        
        // create wrapper component around MOCCA client, passing description to the wrapper 
        Component cComp = cf.newFcInstance(cType,
                new ControllerDescription("StarterComponent", Constants.COMPOSITE, 
                		desc.getClass()
                        .getResource("/net/coregrid/gcmcca/wrappers/composite/controller-config.xml")
                        .getPath(), Constants.SYNCHRONOUS),
                null);
        
        //pass a CCA composite descriptor to the composite wrapper
        ((CCACompositeAttributes) cComp.getFcInterface(CCACompositeAttributes.NAME)).setDescriptor(desc);

        

        
       Fractal.getBindingController(cComp).bindFc("h",
               sComp.getFcInterface("h"));
        // start starter component
        Fractal.getLifeCycleController(cComp).startFc();
        
        // start hello component
        Fractal.getLifeCycleController(sComp).startFc();

                
        ((GoPort) cComp.getFcInterface("MyGoPort")).go();
        
 
	}

}
