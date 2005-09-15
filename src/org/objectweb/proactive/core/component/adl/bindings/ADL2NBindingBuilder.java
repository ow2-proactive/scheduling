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

package org.objectweb.proactive.core.component.adl.bindings;

import org.objectweb.fractal.adl.bindings.BindingBuilder;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponent;
import org.objectweb.proactive.core.component.adl.component.ADL2NComponentImpl;
import org.objectweb.proactive.core.component.adl.component.ADL2NInterface;

/**
 * A Fractal based implementation of the {@link BindingBuilder} interface. 
 * This implementation uses the Fractal API to bind components. 
 */
public class ADL2NBindingBuilder implements BindingBuilder {
  
  /**
   * Adds a binding between two components
   */
  public void bindComponent (
    final int type,
    final Object client, 
    final String clientItf, 
    final Object server, 
    final String serverItf, 
    final Object context) throws Exception
  {
    System.out.println("<LotosBindingBuilder>");
    if(client instanceof ADL2NComponent)
    	System.out.println("\tClient: "+((ADL2NComponentImpl)client).getName());
    if(server instanceof ADL2NComponent)
    	System.out.println("\tServer: "+((ADL2NComponentImpl)server).getName());
    System.out.println("\tClientItf: "+clientItf);
    System.out.println("\tServerItf: "+serverItf);
    System.out.println("<LotosBindingBuilder>\n");
    
    if(client != null && server != null && client instanceof ADL2NComponent && server instanceof ADL2NComponent){
    	//Client interface to bind
    	ADL2NInterface itfClient = ((ADL2NComponent)client).getInterfaceByName(clientItf);
    	//Server interface to bind
    	ADL2NInterface itfServer = ((ADL2NComponent)server).getInterfaceByName(serverItf);
    	//Binds the two interface
    	itfClient.addBinding(itfServer,true);
    	itfServer.addBinding(itfClient,false);
    }
  }  
}
