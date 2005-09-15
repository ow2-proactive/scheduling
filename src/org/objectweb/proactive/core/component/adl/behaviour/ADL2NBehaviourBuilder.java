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

package org.objectweb.proactive.core.component.adl.behaviour;

import org.objectweb.fractal.adl.attributes.AttributeBuilder;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponent;

/**
 * A Java based implementation of the {@link AttributeBuilder} interface. 
 */
public class ADL2NBehaviourBuilder implements BehaviourBuilder {
	
	/**
	 * Changes the behaviour file (in Lotos or FC2) of the current component
	 */
	public Object setLotos(Object component,String lotos) {	
		System.out.println("<LotosBehaviourBuilder>");
		if(component instanceof ADL2NComponent){
			((ADL2NComponent)component).setLotosFile(lotos);
			System.out.println("\tLotosFile set : "+lotos);
		}
		System.out.println("</LotosBehaviourBuilder>\n");
		return component;
	}
	
}
