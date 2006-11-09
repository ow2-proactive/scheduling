package net.coregrid.gcmcca.run;


import net.coregrid.gcmcca.example.CCAStarterComponent;
import net.coregrid.gcmcca.example.HelloComponent;
import net.coregrid.gcmcca.example.HelloPort;
import net.coregrid.gcmcca.wrappers.composite.FractalCompositeStarterComponent;
import net.coregrid.gcmcca.wrappers.composite.CCACompositeDescriptor;

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
import org.objectweb.proactive.core.util.UrlBuilder;

import mocca.cca.ports.GoPort;
import mocca.client.MoccaMainBuilder;
import java.net.URL;
import java.net.URI;
import mocca.cca.TypeMap;
import mocca.srv.impl.MoccaTypeMap;
import mocca.cca.ComponentID;
import mocca.client.MoccaBuilderClient;



public class RunCompositeWrapper {


	public static void main(String[] args) throws Exception{
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
                        false, false)
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

        
        //create MOCCA client component
        URI kernelUri = URI.create("http://zabawka.home:7799");
        MoccaMainBuilder builder = new MoccaMainBuilder();
        ComponentID builderID = builder.addNewBuilder(kernelUri, "builder");
        TypeMap properties = new MoccaTypeMap();        
        properties.putString("mocca.plugletclasspath", "/home/malawski/workspace/hello-component-proactive/starter-component.jar");
        properties.putString("mocca.builderID", builderID.getSerialization());

        ComponentID clientID =  builder.createInstance("MyStarterComponent",
                                CCAStarterComponent.class.getName(),
                                properties);


        // create the composite system description for the wrapper
        // here the composite system consists of a single component 
        
        CCACompositeDescriptor desc = new CCACompositeDescriptor();
        desc.addUsesPort("h", clientID.getSerialization());
        desc.setClassPath("h", "/home/malawski/workspace/hello-component-proactive/glue-component.jar");
        desc.setType("h", HelloPort.class.getName()); //this can be taken from cType
        desc.addProvidesPort("MyGoPort", clientID.getSerialization());
        
        // create wrapper component around MOCCA client, passing description to the wrapper 
        Component cComp = cf.newFcInstance(cType,
                new ControllerDescription("StarterComponent", Constants.PRIMITIVE),
                new ContentDescription(FractalCompositeStarterComponent.class.getName(), 
                		new Object[] {desc})
                ); // pass the ID to the wrapper

        
        // create server component
        Component sComp = cf.newFcInstance(sType,
                new ControllerDescription("HelloComponent", Constants.PRIMITIVE),
                new ContentDescription(HelloComponent.class.getName()));

        
       Fractal.getBindingController(cComp).bindFc("h",
               sComp.getFcInterface("h"));
        // start starter component
        Fractal.getLifeCycleController(cComp).startFc();
        
        // start hello component
        Fractal.getLifeCycleController(sComp).startFc();

                
        ((GoPort) cComp.getFcInterface("MyGoPort")).go();
        
 
	}

}
