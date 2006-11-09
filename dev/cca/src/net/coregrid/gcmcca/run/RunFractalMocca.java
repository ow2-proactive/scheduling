package net.coregrid.gcmcca.run;


import net.coregrid.gcmcca.example.CCAHelloComponent;
import net.coregrid.gcmcca.example.HelloPort;
import net.coregrid.gcmcca.example.StarterComponent;
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
import org.objectweb.proactive.core.util.UrlBuilder;

import mocca.cca.ports.GoPort;
import mocca.client.MoccaMainBuilder;
import glue.net.coregrid.gcmcca.example.HelloPortFractalGlueComponent;

import java.net.URL;
import java.net.URI;
import mocca.cca.TypeMap;
import mocca.srv.impl.MoccaTypeMap;
import mocca.cca.ComponentID;
import mocca.client.MoccaBuilderClient;



public class RunFractalMocca {


	public static void main(String[] args) throws Exception{
        // -------------------------------------------------------------------
        // DO NOT USE THE FRACTAL ADL
        // -------------------------------------------------------------------
        Component boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();
        TypeFactory tf = Fractal.getTypeFactory(boot);

        
        // type of client component
        ComponentType cType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("g", GoPort.class.getName(),
                        false, false, false),
                    tf.createFcItfType("h", HelloPort.class.getName(), true,
                        false, false)
                });

        // type of wrapper component
        ComponentType sType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("h", HelloPort.class.getName(), false,
                        false, false),
                        tf.createFcItfType("attribute-controller",
                            WrapperAttributes.class.getName(), false, false,
                            false)                    
                });

        GenericFactory cf = Fractal.getGenericFactory(boot);

        // -------------------------------------------------------------------
        // CREATE COMPONENTS DIRECTLY
        // -------------------------------------------------------------------

        // create client component
        Component cComp = cf.newFcInstance(cType,
                new ControllerDescription("StarterComponent", Constants.PRIMITIVE),
                new ContentDescription(StarterComponent.class.getName(), new Object[] {StarterComponent.class.getName()})); // other properties could be added (activity for example)

        //create MOCCA server component
        URI kernelUri = URI.create("http://zabawka.home:7799");
        MoccaMainBuilder builder = new MoccaMainBuilder();
        ComponentID builderID = builder.addNewBuilder(kernelUri, "builder");
        TypeMap properties = new MoccaTypeMap();        
        properties.putString("mocca.plugletclasspath", "/home/malawski/workspace/hello-component-proactive/hello-component.jar");
        properties.putString("mocca.builderID", builderID.getSerialization());

        ComponentID serverID =  builder.createInstance("HelloComponent",
                                CCAHelloComponent.class.getName(),
                                properties);

        
        // create Fractal -> CCA glue component
        Component sComp = cf.newFcInstance(sType,
                new ControllerDescription("HelloWrapperComponent", Constants.PRIMITIVE),
                new ContentDescription(HelloPortFractalGlueComponent.class.getName()));

        
        //pass a CCA component ID to glue component
        ((WrapperAttributes) Fractal.getAttributeController(sComp)).setComponentID(serverID.getSerialization());
        
        //connect client to the glue
        Fractal.getBindingController(cComp).bindFc("h",
               sComp.getFcInterface("h"));
        
        // start starter component
        Fractal.getLifeCycleController(cComp).startFc();
        
        // start glue component
        Fractal.getLifeCycleController(sComp).startFc();
        
        // call go() method        
        ((GoPort) cComp.getFcInterface("g")).go();
        
 
	}

}
