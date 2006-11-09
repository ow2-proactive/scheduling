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

import mocca.cca.ports.GoPort;




public class RunWrapper {


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

        // create client component
        Component cComp = cf.newFcInstance(cType,
                new ControllerDescription("StarterComponent", Constants.PRIMITIVE),
                new ContentDescription("net.coregrid.gcmcca.example.CCAStarterComponentWrapper", new Object[] {CCAStarterComponent.class.getName()})); // other properties could be added (activity for example)

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
        
        // call go method
        ((GoPort) cComp.getFcInterface("MyGoPort")).go();
 
	}

}
