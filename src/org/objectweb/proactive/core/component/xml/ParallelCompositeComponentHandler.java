/*
 * Created on Oct 14, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import java.util.HashMap;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.type.ParallelComposite;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Morel
 */
public class ParallelCompositeComponentHandler extends ComponentHandler {

	/**
	 * @param deploymentDescriptor
	 * @param componentsCache
	 */
	public ParallelCompositeComponentHandler(
		ProActiveDescriptor deploymentDescriptor,
		ComponentsCache componentsCache,
		HashMap componentTypes) {
		super(deploymentDescriptor, componentsCache, componentTypes);
		componentParameters.setHierarchicalType(ComponentParameters.PARALLEL);
		addHandler(
			ComponentsDescriptorConstants.COMPONENTS_TAG,
			new ComponentsHandler(deploymentDescriptor, componentsCache, componentTypes));
		addHandler(ComponentsDescriptorConstants.BINDINGS_TAG, new BindingsHandler(componentsCache));
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
	 */
	public Object getResultObject() throws SAXException {
		return componentParameters.getName();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
	 */
	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
		// we have to instantiate the component, and perform internal bindings (not explicitely specified)
		// then instantiate the component and add a stub on it to the cache
		// TODO : cyclic case : instantiate as many components as there are effective nodes
		// But may be a non sense to consider cyclic composites
		VirtualNode vn = deploymentDescriptor.getVirtualNode(virtualNode);
		Component component = null;

		try {
			if (vn.getNodeCount() != 1) {
				throw new NodeException("can only create a parallel composite on a single node");
			}
			// get corresponding node
			Node targeted_node = vn.getNode();
			component = ProActive.newActiveComponent(ParallelComposite.class.getName(), new Object[] {
			}, targeted_node, null, null, componentParameters);
			componentsCache.addComponent(componentParameters.getName(), component);
			logger.debug("created composite component : " + componentParameters.getName());

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
			Component[] sub_components =
				((ContentController) component.getFcInterface(Constants.CONTENT_CONTROLLER)).getFcSubComponents();
			for (int i = 0; i < sub_components.length; i++) {
				// get the interfaces
				//ProActiveInterface[] interfaces = (ProActiveInterface[])sub_components[i].getFcInterfaces();
				InterfaceType[] interfaces =
					((ComponentParametersController) sub_components[i]
						.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
						.getComponentParameters()
						.getInterfaceTypes();
				// loop on the interfaces
				for (int j = 0; j < interfaces.length; i++) {
					// perform a binding when names match
					//	if (!(interfaces[i].isControlInterface()) {
					// we have a functional interface
					// check name
					//if (interfaces[j].getFcItfName().equals(anObject))
					for (int k = 0; k < current_component_interfaces.length; k++) {
						if (interfaces[j].getFcItfName().equals(current_component_interfaces[k].getFcItfName())) {
							// names match
							String itf_name = current_component_interfaces[k].getFcItfName();
							if ((interfaces[j].isFcClientItf() && current_component_interfaces[k].isFcClientItf())
								|| (!interfaces[j].isFcClientItf() && !current_component_interfaces[k].isFcClientItf())) {
								// roles match ==> we have a candidate
								// perform binding
								(
									(BindingController) component.getFcInterface(
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
		} catch (NoSuchInterfaceException nsie) {
			logger.error("cannot bind active component : interface not found");
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
