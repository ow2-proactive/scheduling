/*
 * Created on Oct 14, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractal;
import org.objectweb.proactive.core.component.type.ParallelComposite;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Morel
 */
public class ParallelCompositeComponentHandler extends AbstractContainerComponentHandler {

	/**
	 * @param deploymentDescriptor
	 * @param componentsCache
	 */
	public ParallelCompositeComponentHandler(
		ProActiveDescriptor deploymentDescriptor,
		ComponentsCache componentsCache,
		HashMap componentTypes,
		ComponentsHandler fatherHandler) {
		super(deploymentDescriptor, componentsCache, componentTypes, fatherHandler);
		componentParameters.setHierarchicalType(ComponentParameters.PARALLEL);
		//System.out.println("PARALLEL HANDLER");
		addHandler(ComponentsDescriptorConstants.BINDINGS_TAG, new BindingsHandler(componentsCache));
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
	 */
	public Object getResultObject() throws SAXException {
		return new ComponentResultObject(componentParameters.getName());
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
	 */
	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
		if (getContainerElementHierarchy().containsChild(activeHandler)) {
			enable();
		}
		if (isEnabled()) {
			Component parallel = null;
			if (name.equals(ComponentsDescriptorConstants.COMPONENTS_TAG)) {
				try {
					if (virtualNode.equals(ComponentsDescriptorConstants.NULL)) {
						//System.out.println("INSTANTIATING COMPONENT ON CURRENT VM");
						//					componentsCache.addComponent(componentParameters.getName(),
						//					//PrimitiveComponentB.class.getName(),
						parallel = ProActive.newActiveComponent(ParallelComposite.class.getName(), new Object[] {
						}, null, null, null, componentParameters);
						componentsCache.addComponent(componentParameters.getName(), parallel);
						//PrimitiveComponentB.class.getName(),
					} else {
						// we have to instantiate the component, and perform internal bindings (not explicitely specified)
						// then instantiate the component and add a stub on it to the cache
						VirtualNode vn = deploymentDescriptor.getVirtualNode(virtualNode);

						if (vn.getNodeCount() != 1) {
							throw new NodeException("can only create an enclosing parallel composite on a single node \n" +
								" (internal primitive components can be deployed on multiple nodes)");
						}
						// get corresponding node
						Node targeted_node = vn.getNode();
						parallel = ProActive.newActiveComponent(ParallelComposite.class.getName(), new Object[] {
						}, targeted_node, null, null, componentParameters);
						componentsCache.addComponent(componentParameters.getName(), parallel);
						if (logger.isDebugEnabled()) {
							logger.debug("created parallel component : " + componentParameters.getName());
						}
					}
					// add sub components
					List sub_components_names = (List) getHandler(name).getResultObject();
					Component[] sub_components = new Component[sub_components_names.size()];
					Iterator iterator = sub_components_names.iterator();
					int n = 0;
					while (iterator.hasNext()) {
						String sub_component_name = (String) iterator.next();
						if (logger.isDebugEnabled()) {
							logger.debug("adding sub component : " + sub_component_name);
						}
						Fractal.getContentController(parallel).addFcSubComponent(
							componentsCache.getComponent(sub_component_name));
						sub_components[n] = componentsCache.getComponent(sub_component_name);
						n++;
					}

					// perform automatic bindings 
					// for a parallel component : 
					// - when a server interface of the parallel component
					// 	and a server interface of an internal component have the same name,
					// OR
					// - when a client interface of an internal component
					// 	and a client interface of the parallel component have the same name,
					// they are automatically bound together

					//ProActiveInterface[] parallel_component_interfaces = (ProActiveInterface[])component.getFcInterfaces();
					//Vector functional_interfaces_names_vector = new Vector();
					InterfaceType[] current_component_interfaces = componentParameters.getInterfaceTypes();
					for (int i = 0; i < sub_components.length; i++) {
						// get the interfaces
						//ProActiveInterface[] interfaces = (ProActiveInterface[])sub_components[i].getFcInterfaces();
						InterfaceType[] sub_component_interfaces =
							Fractal
								.getComponentParametersController(sub_components[i])
								.getComponentParameters()
								.getInterfaceTypes();
						// loop on the interfaces
						for (int j = 0; j < sub_component_interfaces.length; j++) {
							// perform a binding when names match
							//	if (!(interfaces[i].isControlInterface()) {
							// we have a functional interface
							// check name
							//if (interfaces[j].getFcItfName().equals(anObject))
							for (int k = 0; k < current_component_interfaces.length; k++) {
								if (sub_component_interfaces[j]
									.getFcItfName()
									.equals(current_component_interfaces[k].getFcItfName())) {
									// names match
									String itf_name = current_component_interfaces[k].getFcItfName();
									if ((sub_component_interfaces[j].isFcClientItf()
										&& current_component_interfaces[k].isFcClientItf())) {
										if (logger.isDebugEnabled()) {
											logger.debug("BINDING INTERFACES OF SAME NAME " + itf_name);
										}
										// roles match ==> we have a candidate
										// perform binding sub_component.client --> parallel.client
										(
											(BindingController) sub_components[i].getFcInterface(
												Constants.BINDING_CONTROLLER)).bindFc(
											itf_name,
											parallel.getFcInterface(itf_name));
									} else if (
										(!sub_component_interfaces[j].isFcClientItf()
											&& !current_component_interfaces[k].isFcClientItf())) {
										if (logger.isDebugEnabled()) {
											logger.debug("BINDING INTERFACES OF SAME NAME " + itf_name);
										}
										// roles match ==> we have a candidate
										// perform binding parallel.server --> subcomponent.server
										(
											(BindingController) parallel.getFcInterface(
												Constants.BINDING_CONTROLLER)).bindFc(
											itf_name,
											sub_components[i].getFcInterface(itf_name));
									}
								}
							}
						}
					}
				} catch (NodeException ne) {
					logger.error("cannot create active component: node exception");
					ne.printStackTrace();
				} catch (ActiveObjectCreationException aoce) {
					logger.error("cannot create active component : active object creation exception");
					aoce.printStackTrace();
				} catch (IllegalContentException e) {
					logger.error("cannot assemble active component : illegal content");
					e.printStackTrace();
				} catch (NoSuchInterfaceException nsie) {
					logger.error("interface not found");
					nsie.printStackTrace();
				} catch (IllegalLifeCycleException ilce) {
					logger.error("cannot bind active component : illegal life cycle operation");
					ilce.printStackTrace();
				} catch (IllegalBindingException ice) {
					logger.error("cannot bind active component : illegal binding operation");
					ice.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
	 */
	public void startContextElement(String name, Attributes attributes) throws SAXException {
		if (isEnabled()) {
			super.startContextElement(name, attributes);
		}
	}

}
