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

package org.objectweb.proactive.core.component.adl.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.deployment.scheduling.component.lib.AbstractInstanceProviderTask;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.ImplementationBuilder;
import org.objectweb.fractal.adl.implementations.ImplementationCompiler;

public class VirtualNodeImplementationCompiler extends ImplementationCompiler {
  
  public AbstractInstanceProviderTask newCreateTask (
    final List path,
    final ComponentContainer container,
    final String name,
    final String definition,
    final Object controller, 
    final Object implementation,
    final Map context)
  {
    VirtualNode n = null;
    if (container instanceof VirtualNodeContainer) {
      n = ((VirtualNodeContainer)container).getVirtualNode();
    }
    if (n == null) {
      for (int i = path.size() - 1; i >= 0; --i) {
        if (path.get(i) instanceof VirtualNodeContainer) {
          n = ((VirtualNodeContainer)path.get(i)).getVirtualNode();
          if (n != null) {
            break;
          }
        }
      }
    }
    if (n != null) {
    	context.put("virtualNode", n.getName());
      return new RemoteCreateTask(
        builder, name, definition, controller, implementation, context.get(n.getName()));
    }
    return super.newCreateTask(
      path, container, name, definition, controller, implementation, context);
  }

  static class RemoteCreateTask extends AbstractInstanceProviderTask {

    ImplementationBuilder builder;
    
    String name;
    
    String definition;
    
    Object controllerDesc;
    
    Object contentDesc;
    
    Object node;
    
    public RemoteCreateTask (
      final ImplementationBuilder builder,
      final String name,
      final String definition,
      final Object controllerDesc,
      final Object contentDesc,
      final Object node) 
    {
      this.builder = builder;
      this.name = name;
      this.definition = definition;
      this.controllerDesc = controllerDesc;
      this.contentDesc = contentDesc;
      this.node = node;
    }
    
    public void execute (Object context) throws Exception {
      if (getInstance() != null) {
        return;
      }
      if (node != null && context instanceof Map) {
        context = new HashMap((Map)context);
        ((Map)context).put("bootstrap", node);
      }
      Object type = getFactoryProviderTask().getFactory();
      Object result = builder.createComponent(
        type, name, definition, controllerDesc, contentDesc, context);
      setInstance(result);
    }
    
    public String toString () {
      return "T" + System.identityHashCode(this) + 
        "[CreateTask(" + name + "," + controllerDesc + "," + contentDesc + ")]";
    }
  }
}
