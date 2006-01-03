package org.objectweb.proactive.core.component.factory;

import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

/**
 * A factory for instantiating components on remote nodes.
 * 
 * @author Matthieu Morel
 *
 */
public interface ProActiveGenericFactory extends GenericFactory {
    /**
     * Creates a component.
     * 
     * @param type
     *            an arbitrary component type.
     * @param controllerDesc
     *            a description of the controller part of the component to be
     *            created. This description is implementation specific. If it is
     *            <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc
     *            a description of the content part of the component to be
     *            created. This description is implementation specific.
     * @return the {@link Component} interface of the created component.
     * @throws InstantiationException
     *             if the component cannot be created.
     */
    Component newFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc) throws InstantiationException;

    /**
     * Creates a component on a given node.
     * 
     * @param type
     *            an arbitrary component type.
     * @param controllerDesc
     *            a description of the controller part of the component to be
     *            created. This description is implementation specific. If it is
     *            <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc
     *            a description of the content part of the component to be
     *            created. This description is implementation specific.
     * @param node
     *            the node where to create the component
     * @return the {@link Component} interface of the created component.
     * @throws InstantiationException
     *             if the component cannot be created.
     */
    Component newFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node node)
            throws InstantiationException;

    /**
     * Creates a component on a given virtual node. It creates one component
     * only, even if the virtual node is multiple.<br>
     * 
     * @param type
     *            an arbitrary component type.
     * @param controllerDesc
     *            a description of the controller part of the component to be
     *            created. This description is implementation specific. If it is
     *            <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc
     *            a description of the content part of the component to be
     *            created. This description is implementation specific.
     * @param virtualNode
     *            the virtual node where to create the component. If the virtual
     *            node is multiple, then the component is instantiated on the
     *            node retreived through the {@link VirtualNode#getNode()}
     *            method
     * @return the {@link Component} interface of the created component.
     * @throws InstantiationException
     *             if the component cannot be created.
     */
    Component newFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, VirtualNode virtualNode)
            throws InstantiationException;
    
    

    /**
     * Creates a list of components on a given virtual node.<br>
     * If the component is primitive, the list is expected to contain 
     * <code>contentDesc.length<code> components.<br>
     * If the component is composite, the list is expected to contain 1 element.
     * 
     * @param type
     *            an arbitrary component type.
     * @param controllerDesc
     *            a description of the controller part of the component to be
     *            created. This description is implementation specific. If it is
     *            <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc
     *            a description of the content part of the component to be
     *            created. Each component has a specific content. 
     *            This description is implementation specific.
     * @param virtualNode
     *            the virtual node where to create the components.
     * @return an list of {@link Component} interfaces of the created
     *         components. 
     * @throws InstantiationException
     *             if the component cannot be created.
     */
    List newFcInstanceAsList(Type type, ControllerDescription controllerDesc,
            ContentDescription[] contentDesc, VirtualNode virtualNode)
            throws InstantiationException;

    /**
     * Creates a list of components on a given virtual node.<br>
     * If the component is primitive, the list is expected to contain 
     * <code>nodes.length<code> components.<br>
     * If the component is composite, the list is expected to contain 1 element.
     * 
     * @param type
     *            an arbitrary component type.
     * @param controllerDesc
     *            a description of the controller part of the component to be
     *            created. This description is implementation specific. If it is
     *            <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc
     *            a description of the content part of the component to be
     *            created. Each component has a specific content. 
     *            This description is implementation specific.
     * @param nodes
     *            the nodes where to create the components.
     * @return an list of {@link Component} interfaces of the created
     *         components. 
     * @throws InstantiationException
     *             if the component cannot be created.
     */
    List newFcInstanceAsList(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node[] nodes)
            throws InstantiationException;
    
    /**
     * Creates a list of components on a given virtual node.<br>
     * If the component is primitive, the list is expected to contain 
     * <code>virtualNodes.getNodes().length<code> components.<br>
     * If the component is composite, the list is expected to contain 1 element.
     * 
     * @param type
     *            an arbitrary component type.
     * @param controllerDesc
     *            a description of the controller part of the component to be
     *            created. This description is implementation specific. If it is
     *            <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc
     *            a description of the content part of the component to be
     *            created. All components have the same content. This description is implementation specific.
     * @param virtualNode
     *            the virtual node where to create the components.
     * @return an list of {@link Component} interfaces of the created
     *         components.
     * @throws InstantiationException
     *             if the component cannot be created.
     */
    List newFcInstanceAsList(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, VirtualNode virtualNode)
            throws InstantiationException;

    /**
     * Creates a component on a given virtual node. It creates one component
     * only, even if the virtual node is multiple.<br>
     * 
     * @param type
     *            an arbitrary component type.
     * @param controllerDesc
     *            a description of the controller part of the component to be
     *            created. This description is implementation specific. If it is
     *            <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc
     *            description of the content part of the component to be
     *            created. This description is implementation specific.
     * @param virtualNode
     *            the virtual node where to create the components.
     * @return an list of {@link Component} interfaces of the created
     *         components.
     * @throws InstantiationException
     *             if the component cannot be created.
     */
    List newFcInstanceAsList(Type type, ControllerDescription controllerDesc,
            ContentDescription[] contentDesc, Node[] nodes)
            throws InstantiationException;

}
