/***
 * Fractal ADL Parser
 * Copyright (C) 2002-2004 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.objectweb.proactive.core.component.adl.type;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.types.TypeBuilder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.adl.ADL2NComponentFactory;
import org.objectweb.proactive.core.component.adl.component.ADL2NInterfaceImpl;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;

/**
 * A Fractal based implementation of the {@link TypeBuilder} interface. 
 * This implementation uses the Fractal API to create types.
 */
public class ADL2NTypeBuilder implements TypeBuilder {
	
	/**
	 * Adds an interface to the current component
	 */
	public Object createInterfaceType (
			final String name,
			final String signature,
			final String role,
			final String contingency,
			final String cardinality,
			final Object context) throws Exception
			{
		System.out.println("<LotosTypeBuilder (interface)>");
		System.out.println("\tName: "+name);
		System.out.println("\tSig: "+signature);
		System.out.println("\tRole: "+role);
		System.out.println("\tCont: "+contingency);
		System.out.println("\tCard: "+cardinality);
		System.out.println("</LotosTypeBuilder (interface)>\n");
		ClassLoader loader = null;
		if (context instanceof Map) {
			loader = (ClassLoader)((Map)context).get("classloader");
		}
		if (loader == null) {
			loader = getClass().getClassLoader();
		}
		
		// TODO : cache already created types ?
		Component bootstrap = null;
		if (context != null) {
			bootstrap = (Component)((Map)context).get("bootstrap");
		}
		if (bootstrap == null) {
			Map ctxt = new HashMap();
			ctxt.put("classloader", loader);
			bootstrap = Fractal.getBootstrapComponent(ctxt);
		}
		boolean client = "client".equals(role);
		boolean optional = "optional".equals(contingency);
		boolean collection = "collection".equals(cardinality);
		
		return Fractal.getTypeFactory(bootstrap).createFcItfType(
				name, signature, client, optional, collection);
			}
	
	/**
	 * Adds a sub-component to the current component
	 */
	public Object createComponentType (
			final String name,
			final Object[] interfaceTypes, 
			final Object context) throws Exception 
			{
		System.out.println("<LotosTypeBuilder (component)>");
		System.out.println("\tName: "+name);
		for(int i=0;i<interfaceTypes.length;i++){
			System.out.println("\tItf Name: "+((ProActiveInterfaceType)interfaceTypes[i]).getFcItfName());
			System.out.println("\tItf Sig: "+((ProActiveInterfaceType)interfaceTypes[i]).getFcItfSignature());
		}
		System.out.println("</LotosTypeBuilder (component)>\n");
		ClassLoader loader = null;
		if (context instanceof Map) {
			loader = (ClassLoader)((Map)context).get("classloader");
		}
		if (loader == null) {
			loader = getClass().getClassLoader();
		}
		
		Component bootstrap = null;
		if (context != null) {
			bootstrap = (Component)((Map)context).get("bootstrap");
		}
		if (bootstrap == null) {
			Map ctxt = new HashMap();
			ctxt.put("classloader", loader);
			bootstrap = Fractal.getBootstrapComponent(ctxt);
		}
		InterfaceType[] types = new InterfaceType[interfaceTypes.length];
		for (int i = 0; i < types.length; ++i) {
			types[i] = (InterfaceType)interfaceTypes[i];
		}
	
		for(int m=0;m<interfaceTypes.length;m++){
			ADL2NComponentFactory.instance().addInterface(new ADL2NInterfaceImpl(
					((ProActiveInterfaceType)interfaceTypes[m]).getFcItfName(),
					((ProActiveInterfaceType)interfaceTypes[m]).getFcItfSignature()));
		}
		return Fractal.getTypeFactory(bootstrap).createFcType(types);
			}
}
