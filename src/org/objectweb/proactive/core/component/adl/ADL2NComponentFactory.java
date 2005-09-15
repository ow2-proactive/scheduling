/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.adl;

import java.util.Map;
import java.util.Vector;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.Factory;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponent;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponentImpl;
import org.objectweb.proactive.core.component.adl.component.ADL2NInterface;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

/**
 * This class is used for creating components.
 * It acts as : <ol>
 * <li> a bootstrap component</li>
 * <li> a GenericFactory for instantiating new components</li>
 * <li> a utility class providing static methods to create collective interfaces and retreive references to ComponentParametersController</li>
 * </ol>
 */
public class ADL2NComponentFactory implements GenericFactory, Factory {
    private static ADL2NComponentFactory instance = null;
    private TypeFactory typeFactory = (TypeFactory) ProActiveTypeFactory.instance();
    private Type type = null;
    private ADL2NComponent current;
    private Vector interfaces;
    
    /**
     * no-arg constructor (used by Fractal to get a bootstrap component)
     *
     */
    public ADL2NComponentFactory() {
    	interfaces = new Vector();
    }

    public static ADL2NComponentFactory instance() {
        if (instance == null) {
            instance = new ADL2NComponentFactory();
        }
        return instance;
    }
    
    public void addInterface(ADL2NInterface i){
    	interfaces.add(i);
    }

	public Component newFcInstance(Type type, Object controller, Object content) throws InstantiationException {
		current = new ADL2NComponentImpl("","",controller,content);
		for(int i=0;i<interfaces.size();i++)
			current.addInterface((ADL2NInterface)interfaces.get(i));
		interfaces.clear();
		return current;
	}
	
	public ADL2NComponent getCurrentComponent(){
		return current;
	}
	
	public Type getFcInstanceType() {
		System.out.println("getFcInstanceType Unimplemented !");
		return null;
	}

	public Object getFcControllerDesc() {
		System.out.println("getFcControllerDesc Unimplemented !");
		return null;
	}

	public Object getFcContentDesc() {
		System.out.println("getFcContentDesc Unimplemented !");
		return null;
	}

	public Component newFcInstance() throws InstantiationException {
		System.out.println("newFcInstance Unimplemented !");
		return null;
	}

	public Object createFcItfType(String name, String signature, boolean client, boolean optional, boolean collection) {
		System.out.println(this.getClass());
		return null;
	}

}
