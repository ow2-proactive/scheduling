package org.objectweb.proactive.examples.components.helloworld;

/***
*
* Author: Eric Bruneton
* Modified by: Matthieu Morel
*/
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;


import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;


import org.objectweb.proactive.core.component.xml.Loader;

/**
 * This example is taken from the examples in the Fractal distribution. It normally works with Julia, the reference 
 * implementation, but it can also work with ProActive. <br>
 * Sections involving templates have been removed, because this implementation does not provide templates. <br>
 * Currently, the ADL is not the standard FractalADL, but this will be changed soon, so that this implementation can
 * also use the common tools of the Fractal community.<br>
 * However, a functionality offered by ProActive is the automatic deployment of components onto remote locations.<br>
 * When using the "distributed" option with the "parser" option, the ADL loader will load the "helloworld-distributed.xml" ADL, 
 * which affects virtual nodes to components, and the "deployment.xml" file, which maps the virtual nodes to real nodes.<br> 
 * If other cases, all components are instantiated locally, in the current virtual machine. <br>
 * 
 * 
 */
public class HelloWorld {
	private static String LOCAL_COMPONENTS_DESCRIPTOR =
		HelloWorld
			.class
			.getResource("/org/objectweb/proactive/examples/components/helloworld/helloworld-local.xml")
			.getPath();
	private static String DISTRIBUTED_COMPONENTS_DESCRIPTOR =
			HelloWorld
				.class
				.getResource("/org/objectweb/proactive/examples/components/helloworld/helloworld-distributed.xml")
				.getPath();
	private static String DEPLOYMENT_DESCRIPTOR =
		HelloWorld
			.class
			.getResource("/org/objectweb/proactive/examples/components/helloworld/deployment.xml")
			.getPath();

	public static void main(final String[] args) throws Exception {
		boolean useParser = false;
		boolean useTemplates = false;
		boolean useWrapper = false;
		boolean distributed = false; 
		
		for (int i = 0; i < args.length; ++i) {
			useParser |= args[i].equals("parser");
			useTemplates |= args[i].equals("templates");
			useWrapper |= args[i].equals("wrapper");
			distributed |= args[i].equals("distributed");
		}

		useParser = true;
		useWrapper = true;
		distributed=false;

		Component rComp = null;

		if (useParser) {
			//      // -------------------------------------------------------------------
			//      // OPTION 1 : USE THE (custom) FRACTAL ADL
			//      // -------------------------------------------------------------------
			Loader loader = new Loader();

			// first step : parse the configuration files and load the components system
			if (distributed) {
				loader.loadComponentsConfiguration(DISTRIBUTED_COMPONENTS_DESCRIPTOR, DEPLOYMENT_DESCRIPTOR);
			} else {
				loader.loadComponentsConfiguration(LOCAL_COMPONENTS_DESCRIPTOR, DEPLOYMENT_DESCRIPTOR);
			}
			// second step : load the desired component
			if (useWrapper) {
				rComp = loader.getComponent("ClientServerWrapper");
			} else {
				rComp = loader.getComponent("ClientServer");
			}
			System.out.println();
		} else {
			// -------------------------------------------------------------------
			// OPTION 2 : DO NOT USE THE FRACTAL ADL
			// -------------------------------------------------------------------
			Component boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();
			TypeFactory tf = Fractal.getTypeFactory(boot);

			// type of root component
			ComponentType rType =
				tf.createFcType(
					new InterfaceType[] { tf.createFcItfType("m", Main.class.getName(), false, false, false)});

			// type of client component
			ComponentType cType =
				tf.createFcType(
					new InterfaceType[] {
						tf.createFcItfType("m", Main.class.getName(), false, false, false),
						tf.createFcItfType("s", Service.class.getName(), true, false, false)});

			// type of server component
			ComponentType sType =
				tf.createFcType(
					new InterfaceType[] {
						tf.createFcItfType("s", Service.class.getName(), false, false, false),
						tf.createFcItfType(
							"attribute-controller",
							ServiceAttributes.class.getName(),
							false,
							false,
							false)});

			// #Julia specific code# GenericFactory cf = Fractal.getGenericFactory(boot);
			GenericFactory cf = Fractal.getGenericFactory(boot);

			if (!useTemplates) {
				// -------------------------------------------------------------------
				// OPTION 2.1 : CREATE COMPONENTS DIRECTLY
				// -------------------------------------------------------------------
				// create root component
				rComp =
					cf.newFcInstance(rType, new ControllerDescription("root", Constants.COMPOSITE), null);
				// create client component
				Component cComp =
					cf.newFcInstance(
						cType,
						new ControllerDescription("client", Constants.PRIMITIVE),
						new ContentDescription(ClientImpl.class.getName()));  // other properties could be added (activity for example)

				// create server component
				Component sComp =
					cf.newFcInstance(
						sType,
						new ControllerDescription("server", Constants.PRIMITIVE),
						new ContentDescription(ServerImpl.class.getName()));
				((ServiceAttributes) Fractal.getAttributeController(sComp)).setHeader("--------> ");
				((ServiceAttributes) Fractal.getAttributeController(sComp)).setCount(1);

				if (useWrapper) {
					sType =
						tf.createFcType(
							new InterfaceType[] {
								 tf.createFcItfType("s", Service.class.getName(), false, false, false)});
					// create client component "wrapper" component
					Component CComp =
						cf.newFcInstance(
							cType,
							new ControllerDescription("client-wrapper", Constants.COMPOSITE),
							null);

					// create server component "wrapper" component
					Component SComp =
						cf.newFcInstance(
							sType,
							new ControllerDescription("server-wrapper", Constants.COMPOSITE),
							null);

					// component assembly
					Fractal.getContentController(CComp).addFcSubComponent(cComp);
					Fractal.getContentController(SComp).addFcSubComponent(sComp);
					Fractal.getBindingController(CComp).bindFc("m", cComp.getFcInterface("m"));
					Fractal.getBindingController(cComp).bindFc(
						"s",
						Fractal.getContentController(CComp).getFcInternalInterface("s"));
					//Fractal.getBindingController(cComp).bindFc("s", CComp.getFcInterface("s"));
					Fractal.getBindingController(SComp).bindFc("s", sComp.getFcInterface("s"));
					// replaces client and server components by "wrapper" components
					// THIS CHANGES REFERENCES (STUBS)
					cComp = CComp;
					sComp = SComp;
				}

				// component assembly
				Fractal.getContentController(rComp).addFcSubComponent(cComp);
				Fractal.getContentController(rComp).addFcSubComponent(sComp);
				Fractal.getBindingController(rComp).bindFc("m", cComp.getFcInterface("m"));
				Fractal.getBindingController(cComp).bindFc("s", sComp.getFcInterface("s"));
			}
		}

		// -----------------------------------------------------------------------
		// COMMON PART
		// -----------------------------------------------------------------------
		// start root component
		Fractal.getLifeCycleController(rComp).startFc();

		// call main method
		 ((Main) rComp.getFcInterface("m")).main(null);
	}

}
