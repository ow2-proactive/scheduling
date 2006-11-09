package net.coregrid.gcmcca.run;


import net.coregrid.gcmcca.example.CCAStarterComponent;
import net.coregrid.gcmcca.example.HelloComponent;
import net.coregrid.gcmcca.example.HelloPort;

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
import glue.net.coregrid.gcmcca.example.HelloPortCCAGlueComponent;

import java.net.URL;
import java.net.URI;
import mocca.cca.TypeMap;
import mocca.srv.impl.MoccaTypeMap;
import mocca.cca.ComponentID;
import mocca.client.MoccaBuilderClient;



public class RunMoccaFractal {


	public static void main(String[] args) throws Exception{
        // -------------------------------------------------------------------
        // DO NOT USE THE FRACTAL ADL
        // -------------------------------------------------------------------
        Component boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();
        TypeFactory tf = Fractal.getTypeFactory(boot);

        
        // type of client component
//        ComponentType cType = tf.createFcType(new InterfaceType[] {
//                    tf.createFcItfType("g", GoPort.class.getName(),
//                        false, false, false),
//                    tf.createFcItfType("h", HelloPort.class.getName(), true,
//                        false, false)
//                });

        // type of server component
        ComponentType sType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("h", HelloPort.class.getName(), false,
                        false, false),
                    
                });

        GenericFactory cf = Fractal.getGenericFactory(boot);

        // -------------------------------------------------------------------
        // CREATE COMPONENTS DIRECTLY
        // -------------------------------------------------------------------

        // create client component
//        Component cComp = cf.newFcInstance(cType,
//                new ControllerDescription("StarterComponent", Constants.PRIMITIVE),
//                new ContentDescription(CCAStarterComponentWrapper.class.getName(), new Object[] {CCAStarterComponent.class.getName()})); // other properties could be added (activity for example)

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


        
        // create server component
        Component sComp = cf.newFcInstance(sType,
                new ControllerDescription("HelloComponent", Constants.PRIMITIVE),
                new ContentDescription(HelloComponent.class.getName()));

        //create a glue component
        properties.putString("mocca.plugletclasspath", "/home/malawski/workspace/hello-component-proactive/glue-component.jar");
        ComponentID glueID =  builder.createInstance("MyGlueComponent",
                HelloPortCCAGlueComponent.class.getName(),
                properties);
        
        //pass a reference of Fractal Server component to glue component
        String URL = UrlBuilder.buildUrlFromProperties("localhost", "hello");        
        Fractive.register(sComp, URL);
        properties.putString("URL", URL);
        MoccaBuilderClient.writeConfigurationMap(glueID, "config", properties);
        
        //connect CCA client component to Glue component
        builder.connect(clientID, "h", glueID, "h");
        
//       Fractal.getBindingController(cComp).bindFc("h",
//               sComp.getFcInterface("h"));
//        // start starter component
//        Fractal.getLifeCycleController(cComp).startFc();
        
        // start hello component
        Fractal.getLifeCycleController(sComp).startFc();

        
        // call go() method
        MoccaBuilderClient.invokeGo(clientID);
        
//        ((GoPort) cComp.getFcInterface("g")).go();
        
//        //and now call the go() method by accessing a component using URL:
//        
//        Component sComp2 = Fractive.lookup(URL);
//        ((HelloPort) sComp2.getFcInterface("h")).hello("Calling via URL: " + URL);
 
	}

}
