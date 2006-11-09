package net.coregrid.gcmcca.wrappers.primitive;

import mocca.cca.Component;
import mocca.cca.CCAException;
import mocca.cca.ports.GoPort;

import java.util.*;

import javassist.*;

public class WrapperGenerator {

	private Component component;
	private String className;
	private String wrapperClassName;
	private IntrospectionServices services = new IntrospectionServices();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		WrapperGenerator wrapperGenerator = new WrapperGenerator();
		wrapperGenerator.inspectComponent("net.coregrid.gcmcca.example.CCAStarterComponent");
		wrapperGenerator.generateWrapper();
	}

	public void inspectComponent(String className) {
		try {
			this.className = className;
			this.component = (Component) Class.forName(className).newInstance();
			component.setServices(services);
			System.out.println("Analyzing CCA component: " + className);
			System.out.println("Provides ports (Server interfaces):");		
			System.out.println(Arrays.deepToString(services.getProvidesPortTypes().keySet().toArray()));
			System.out.println(Arrays.deepToString(services.getProvidesPortTypes().values().toArray()));
			System.out.println("Uses ports (Client interfaces):");
			System.out.println(Arrays.deepToString(services.getUsesPortTypes().keySet().toArray()));
			System.out.println(Arrays.deepToString(services.getUsesPortTypes().values().toArray()));
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CCAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void generateWrapper() throws Exception {
		ClassPool pool = ClassPool.getDefault();
		wrapperClassName = className + "Wrapper";
		String wrapperShortName = wrapperClassName.substring(
				wrapperClassName.lastIndexOf(".") + 1, 
				wrapperClassName.length());
		CtClass cc = pool.makeClass(wrapperClassName);
		
		//generate supeclass
		cc.setSuperclass(pool.get(AbstractCCAComponent.class.getName()));
		
		//generate interfaces for each CCA provides port
		CtClass itf;
		Collection providesPorts = services.getProvidesPortTypes().values();
		Iterator it = providesPorts.iterator();
		while (it.hasNext()) {
			itf = pool.get((String)it.next());
			cc.addInterface(itf);			
		}
		
		//generate constructors
		String constructorSource = "public "+ wrapperShortName + "() {}";
		System.out.println(constructorSource);
		CtConstructor constructor = CtNewConstructor.make(constructorSource, cc);
		cc.addConstructor(constructor);
		constructorSource = "public "+ wrapperShortName + "(String className) throws Exception " +
		"{ super( className ); }";
		System.out.println(constructorSource);
		constructor = CtNewConstructor.make(constructorSource, cc);
		cc.addConstructor(constructor);
		
		//generate delegate methods for each method in each interface
		
		Collection providesPortNames = services.getProvidesPortTypes().keySet();
		it = providesPortNames.iterator();
		while (it.hasNext()) {
			String portName = (String) it.next();
			itf = pool.get(services.getProvidesPortType(portName));
			CtMethod[] methods = itf.getDeclaredMethods();
			for (int i=0; i<methods.length; i++) {
				CtMethod method = CtNewMethod.copy(methods[i], cc, null);
				String methodBody = "{return (("
					+ itf.getName() + ")getProvidesPort(\"" + portName+ "\"))." + method.getName() + "($$)" + ";	}";	
				System.out.println("Adding body: " + methodBody);
				method.setBody(methodBody);
				method.setModifiers(method.getModifiers() & ~Modifier.ABSTRACT);
				cc.addMethod(method);
			}
		}
		
		
//		CtMethod method = CtNewMethod.make("public int go() {return ((mocca.cca.ports.GoPort)getProvidesPort(\"s\")).go();	}", cc);
//		cc.addMethod(method);
		cc.writeFile("classes");
		
		
		
	}
}
