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

package org.objectweb.proactive.core.component.adl.implementations;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.deployment.scheduling.component.api.FactoryProviderTask;
import org.objectweb.deployment.scheduling.component.lib.AbstractInstanceProviderTask;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.TaskMap;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.PrimitiveCompiler;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.implementations.Implementation;
import org.objectweb.fractal.adl.implementations.ImplementationBuilder;
import org.objectweb.fractal.adl.implementations.ImplementationContainer;
import org.objectweb.fractal.adl.implementations.TemplateControllerContainer;
import org.objectweb.fractal.adl.nodes.VirtualNodeContainer;
import org.objectweb.fractal.api.control.BindingController;

/**
 * A {@link PrimitiveCompiler} to compile {@link Implementation} nodes in definitions.  
 */

public class ImplementationCompiler implements BindingController, PrimitiveCompiler {
//<<<<<<< ImplementationCompiler.java
//
//	/**
//	 * Name of the mandatory interface bound to the {@link ImplementationBuilder} 
//	 * used by this compiler.
//	 */
//
//	public final static String BUILDER_BINDING = "builder";
//
//	/**
//	 * The {@link ImplementationBuilder} used by this compiler.
//	 */
//
//	public ImplementationBuilder builder;
//
//	// --------------------------------------------------------------------------
//	// Implementation of the BindingController interface
//	// --------------------------------------------------------------------------
//
//	public String[] listFc() {
//		return new String[] { BUILDER_BINDING };
//	}
//
//	public Object lookupFc(final String itf) {
//		if (itf.equals(BUILDER_BINDING)) {
//			return builder;
//		}
//		return null;
//	}
//
//	public void bindFc(final String itf, final Object value) {
//		if (itf.equals(BUILDER_BINDING)) {
//			builder = (ImplementationBuilder) value;
//		}
//	}
//
//	public void unbindFc(final String itf) {
//		if (itf.equals(BUILDER_BINDING)) {
//			builder = null;
//		}
//	}
//
//	// --------------------------------------------------------------------------
//	// Implementation of the Compiler interface
//	// --------------------------------------------------------------------------
//
//	public void compile(final ComponentContainer container, final TaskMap tasks, final Map context) throws ADLException {
//		boolean template = context != null && "true".equals(context.get("template"));
//
//		String implementation = null;
//		if (container instanceof ImplementationContainer) {
//			ImplementationContainer ic = (ImplementationContainer) container;
//			Implementation i = ic.getImplementation();
//			if (i != null) {
//				implementation = i.getClassName();
//			}
//		}
//
//		String controller = null;
//		if (container instanceof ControllerContainer) {
//			ControllerContainer cc = (ControllerContainer) container;
//			if (cc.getController() != null) {
//				controller = cc.getController().getDescriptor();
//			}
//		}
//
//		String templateController = null;
//		if (container instanceof TemplateControllerContainer) {
//			TemplateControllerContainer tcc = (TemplateControllerContainer) container;
//			if (tcc.getTemplateController() != null) {
//				templateController = tcc.getTemplateController().getDescriptor();
//				template = true;
//			}
//		}
//
//		String name = null;
//		if (container instanceof Definition) {
//			name = ((Definition) container).getName();
//		} else if (container instanceof Component) {
//			name = ((Component) container).getName();
//		}
//
//		String definition = null;
//		if (container instanceof Definition) {
//			definition = name;
//		} else {
//			definition = (String) ((Node) container).astGetDecoration("definition");
//		}
//
//		Component[] comps = ((ComponentContainer) container).getComponents();
//
//		try {
//			// the task may already exist, in case of a shared component
//			tasks.getTask("create", container);
//		} catch (NoSuchElementException e) {
//			AbstractInstanceProviderTask createTask;
//			if (comps.length > 0 || implementation == null) {
//				if (implementation != null) {
//					throw new ADLException("Implementation must be empty", (Node) container);
//				}
//				if (controller == null) {
//					controller = "composite";
//				}
//				if (template) {
//					if (templateController == null) {
//						templateController = "compositeTemplate";
//					}
//					createTask = newCreateTask(container, name, definition, templateController, new Object[] { controller, null }, context);
//				} else {
////					//	PROACTIVE. This is a composite component
////					if (System.getProperty("fractal.provider").equals("org.objectweb.proactive.core.component.Fractive")) {
////						createTask = newCreateTask(container, name, definition, new ControllerDescription(name, Constants.COMPOSITE), null, context);
////					} else {
//						createTask = newCreateTask(container, name, definition, controller, null, context);
//					//}
//				}
//			} else {
//				if (controller == null) {
//					controller = "primitive";
//				}
//				if (template) {
//					if (templateController == null) {
//						templateController = "primitiveTemplate";
//					}
//					createTask = newCreateTask(container, name, definition, templateController, new Object[] { controller, implementation }, context);
//				} else {
////					//					PROACTIVE. This is a primitive component
////					if (System.getProperty("fractal.provider").equals("org.objectweb.proactive.core.component.Fractive")) {
////						createTask = newCreateTask(container, name, definition, new ControllerDescription(name, Constants.PRIMITIVE), new ContentDescription(implementation), context);
////					} else {
//						createTask = newCreateTask(container, name, definition, controller, implementation, context);
//					//}
//				}
//			}
//
//			FactoryProviderTask typeTask = (FactoryProviderTask) tasks.getTask("type", container);
//			createTask.setFactoryProviderTask(typeTask);
//
//			tasks.addTask("create", container, createTask);
//		}
//	}
//
//	public AbstractInstanceProviderTask newCreateTask(
//		final ComponentContainer container,
//		final String name,
//		final String definition,
//		final Object controller,
//		final Object implementation,
//		final Map context) {
//		return new CreateTask(builder, name, definition, controller, implementation);
//	}
//
//	// --------------------------------------------------------------------------
//	// Inner classes
//	// --------------------------------------------------------------------------
//
//	static class CreateTask extends AbstractInstanceProviderTask {
//
//		ImplementationBuilder builder;
//
//		String name;
//
//		String definition;
//
//		Object controllerDesc;
//
//		Object contentDesc;
//
//		public CreateTask(
//			final ImplementationBuilder builder,
//			final String name,
//			final String definition,
//			final Object controllerDesc,
//			final Object contentDesc) {
//			this.builder = builder;
//			this.name = name;
//			this.definition = definition;
//			this.controllerDesc = controllerDesc;
//			this.contentDesc = contentDesc;
//		}
//
//		public void execute(final Object context) throws Exception {
//			if (getInstance() != null) {
//				return;
//			}
//			Object type = getFactoryProviderTask().getFactory();
//			Object result = builder.createComponent(type, name, definition, controllerDesc, contentDesc, context);
//			setInstance(result);
//		}
//
//		public String toString() {
//			return "T" + System.identityHashCode(this) + "[CreateTask(" + name + "," + controllerDesc + "," + contentDesc + ")]";
//		}
//	}
//=======
  
  /**
   * Name of the mandatory interface bound to the {@link ImplementationBuilder} 
   * used by this compiler.
   */
  
  public final static String BUILDER_BINDING = "builder";
  
  /**
   * The {@link ImplementationBuilder} used by this compiler.
   */
  
  public ImplementationBuilder builder;
  
  // --------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // --------------------------------------------------------------------------
  
  public String[] listFc() {
    return new String[] { BUILDER_BINDING };
  }

  public Object lookupFc (final String itf) {
    if (itf.equals(BUILDER_BINDING)) {
      return builder;
    }
    return null;
  }

  public void bindFc (final String itf, final Object value) {
    if (itf.equals(BUILDER_BINDING)) {
      builder = (ImplementationBuilder)value;
    }
  }

  public void unbindFc (final String itf) {
    if (itf.equals(BUILDER_BINDING)) {
      builder = null;
    }
  }
  
  // --------------------------------------------------------------------------
  // Implementation of the Compiler interface
  // --------------------------------------------------------------------------
  
  public void compile (
    final List path,
    final ComponentContainer container, 
    final TaskMap tasks,
    final Map context) throws ADLException 
  {
    boolean template = context != null && "true".equals(context.get("template"));
    
    String implementation = null;
    if (container instanceof ImplementationContainer) {
      ImplementationContainer ic = (ImplementationContainer)container;
      Implementation i = ic.getImplementation();
      if (i != null) { 
        implementation = i.getClassName();
      }
    }
    
    String virtualNode = null;
    if (container instanceof VirtualNodeContainer) {
        VirtualNodeContainer vnc = (VirtualNodeContainer)container;
        if (vnc.getVirtualNode() != null) {
          virtualNode = vnc.getVirtualNode().getName();
        }
    	
    }

    String controller = null;
    if (container instanceof ControllerContainer) {
      ControllerContainer cc = (ControllerContainer)container;
      if (cc.getController() != null) {
        controller = cc.getController().getDescriptor();
      }
    }
    
    String templateController = null;
    if (container instanceof TemplateControllerContainer) {
      TemplateControllerContainer tcc = (TemplateControllerContainer)container;
      if (tcc.getTemplateController() != null) {
        templateController = tcc.getTemplateController().getDescriptor();
        template = true;
      }
    }
    
    String name = null;
    if (container instanceof Definition) {
      name = ((Definition)container).getName();
    } else if (container instanceof Component) {
      name = ((Component)container).getName();
    }
    
    String definition = null;
    if (container instanceof Definition) {
      definition = name;
    } else {
      definition = (String)((Node)container).astGetDecoration("definition");
    }
    
    
    Component[] comps = ((ComponentContainer)container).getComponents();
    
    try {
      // the task may already exist, in case of a shared component
      tasks.getTask("create", container);
    } catch (NoSuchElementException e) {
      AbstractInstanceProviderTask createTask;
      if (comps.length > 0 || implementation == null) {
        if (implementation != null) {
          throw new ADLException("Implementation must be empty", (Node)container);
        }
        if (controller == null) {
          controller = "composite";
        }
        if (template) {
          if (templateController == null) {
            templateController = "compositeTemplate";
          }
          createTask = newCreateTask(path, container, name, definition, templateController, new Object[] { controller, null }, context);
        } else {
          createTask = newCreateTask(path, container, name, definition, controller, null, context);
        }
      } else {
        if (controller == null) {
          controller = "primitive";
        }
        if (template) {
          if (templateController == null) {
            templateController = "primitiveTemplate";
          }
          createTask = newCreateTask(path, container, name, definition, templateController, new Object[] { controller, implementation }, context);
        } else {
          createTask = newCreateTask(path, container, name, definition, controller, implementation, context);
        }
      }
      
      FactoryProviderTask typeTask = 
        (FactoryProviderTask)tasks.getTask("type", container);
      createTask.setFactoryProviderTask(typeTask);
      
      tasks.addTask("create", container, createTask);
    }
  }
  
  public AbstractInstanceProviderTask newCreateTask (
    final List path,
    final ComponentContainer container,
    final String name,
    final String definition,
    final Object controller, 
    final Object implementation,
    final Map context)
  {
    return new CreateTask(builder, name, definition, controller, implementation);
  }
  
  // --------------------------------------------------------------------------
  // Inner classes
  // --------------------------------------------------------------------------
  
  static class CreateTask extends AbstractInstanceProviderTask {

    ImplementationBuilder builder;
    
    String name;
    
    String definition;
    
    Object controllerDesc;
    
    Object contentDesc;
    
    public CreateTask (
      final ImplementationBuilder builder,
      final String name,
      final String definition,
      final Object controllerDesc,
      final Object contentDesc) 
    {
      this.builder = builder;
      this.name = name;
      this.definition = definition;
      this.controllerDesc = controllerDesc;
      this.contentDesc = contentDesc;
    }
    
    public void execute (final Object context) throws Exception {
      if (getInstance() != null) {
        return;
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
