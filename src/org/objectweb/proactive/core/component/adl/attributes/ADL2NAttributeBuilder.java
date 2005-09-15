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

package org.objectweb.proactive.core.component.adl.attributes;

import org.objectweb.fractal.adl.attributes.AttributeBuilder;
import org.objectweb.fractal.adl.attributes.JavaAttributeBuilder;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponent;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponentImpl;

/**
 * A Fractal based implementation of the {@link AttributeBuilder} interface. 
 * This implementation uses the Fractal API to set component attributes. 
 */
public class ADL2NAttributeBuilder extends JavaAttributeBuilder {

  /**
   * Changes the attributes of the component
   */
  public void setAttribute (
    final Object component, 
    final String attributeController, 
    final String name, 
    final String value,
    final Object context) throws Exception 
  {    
    System.out.println("<LotosAttributeBuilder>");
    if(component instanceof ADL2NComponent)
    	System.out.println("\tName: "+((ADL2NComponentImpl)component).getName());
    System.out.println("\tAttrCtrl: "+attributeController);
    System.out.println("\tName: "+name);
    System.out.println("\tValue: "+value);
    System.out.println("<LotosAttributeBuilder>\n");
  }
}
