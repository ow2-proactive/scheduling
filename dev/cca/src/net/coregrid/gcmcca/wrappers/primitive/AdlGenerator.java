package net.coregrid.gcmcca.wrappers.primitive;

import mocca.cca.Component;
import java.util.Arrays;
import java.util.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;




public class AdlGenerator {

	private Component component;
	private String className;
	private String wrapperClassName;
	private IntrospectionServices services = new IntrospectionServices();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		AdlGenerator adlGenerator = new AdlGenerator();
		adlGenerator.inspectComponent("net.coregrid.gcmcca.example.CCAStarterComponent");
	}

	
	private void inspectComponent(String className) throws Exception{
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
		
		Document document = DocumentHelper.createDocument();
        Element definition = document.addElement("definition");
        definition.addAttribute("name", className);

        //add server interfaces
        Iterator it = services.getProvidesPortTypes().keySet().iterator();
        while (it.hasNext()) {
        	String itfName = (String) it.next();
        	Element intf = definition.addElement("interface");
        	intf.addAttribute("role", "server");
        	intf.addAttribute("name", itfName);
        	intf.addAttribute("signature", services.getProvidesPortType(itfName));
        }
        //add client interfaces
        it = services.getUsesPortTypes().keySet().iterator();
        while (it.hasNext()) {
        	String itfName = (String) it.next();
        	Element intf = definition.addElement("interface");
        	intf.addAttribute("role", "client");
        	intf.addAttribute("name", itfName);
        	intf.addAttribute("signature", services.getUsesPortType(itfName));
        }
        
        System.out.println(document.asXML());


		
	}
}
