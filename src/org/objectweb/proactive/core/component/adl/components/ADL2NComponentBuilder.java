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
package org.objectweb.proactive.core.component.adl.components;

import org.objectweb.fractal.adl.components.ComponentBuilder;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponent;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponentImpl;


/**
 * A ProActive based implementation of the {@link ComponentBuilder} interface.
 * This implementation uses the Fractal API to add and start components.
 * It slightly differs from the FractalComponentBuilder class : the name of the component
 * is not specified in this addition operation, but when the component is instantiated.
 * 
 */
public class ADL2NComponentBuilder implements ComponentBuilder {

	/**
	 * Adds a component as a child of this one
	 */
    public void addComponent(final Object superComponent,
        final Object subComponent, final String name, final Object context)
        throws Exception {
    	System.out.println("<LotosComponentBuilder>");
    	System.out.println("\tName: "+name);
    	if(subComponent instanceof ADL2NComponent)
    		System.out.println("\tSubComponent: "+((ADL2NComponentImpl)subComponent).getName());
    	if(superComponent instanceof ADL2NComponent)
    		System.out.println("\tSuperComponent: "+((ADL2NComponentImpl)superComponent).getName());
    	System.out.println("</LotosComponentBuilder>\n");
    	
    	if(subComponent != null && superComponent != null){
    		if(subComponent instanceof ADL2NComponent &&
    				superComponent instanceof ADL2NComponent){
    			((ADL2NComponent)superComponent).addComponent((ADL2NComponent)subComponent);
    		}
    	}
    }

    /**
     * Starts the component (does nothing here)
     */
    public void startComponent(final Object component, final Object context)
        throws Exception {
    }
}
