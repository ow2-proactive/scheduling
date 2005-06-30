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
package org.objectweb.proactive.core.component;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;


/**
 * Fractal implementation-specific description of  the content of components.
 * With ProActive, features such as activity, factory, virtual node or constructor parameters
 * can be specified.
 *
 * @author Matthieu Morel
 */
public class ContentDescription {
    private String className;
    private Object[] constructorParameters;
    private Active activity;
    private MetaObjectFactory factory;
    private VirtualNode virtualNode = null;
    private Node node = null;
    private boolean uniqueInstance = false;

    /**
     * constructor
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     * If the component is a parallel component, this class is by default {@link org.objectweb.proactive.core.component.type.ParallelComposite}
     * @param constructorParameters parameters of the constructor of the base class
     * @param virtualNode virtual node where this component will be instantiated
     * @param activity the activity as defined in the ProActive model
     * @param factory overriden meta-object factory for the component. Can be null.
     */
    public ContentDescription(String className, Object[] constructorParameters,
        VirtualNode virtualNode, Active activity, MetaObjectFactory factory) {
        this.className = className;
        this.constructorParameters = constructorParameters;
        this.virtualNode = virtualNode;
        this.activity = activity;
        this.factory = factory;
    }

    /**
     * constructor
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     * If the component is a parallel component, this class is by default {@link org.objectweb.proactive.core.component.type.ParallelComposite}
     * @param constructorParameters parameters of the constructor of the base class
     * @param node node where this component will be instantiated
     * @param activity the activity as defined in the ProActive model
     * @param factory overriden meta-object factory for the component. Can be null.
     */
    public ContentDescription(String className, Object[] constructorParameters,
        Node node, Active activity, MetaObjectFactory factory) {
        this.className = className;
        this.constructorParameters = constructorParameters;
        this.node = node;
        this.activity = activity;
        this.factory = factory;
    }

    /**
     * constructor
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     * If the component is a parallel component, this class is by default {@link org.objectweb.proactive.core.component.type.ParallelComposite}
     * @param constructorParameters parameters of the constructor of the base class
     */
    public ContentDescription(String className, Object[] constructorParameters) {
        this(className, constructorParameters, (Node) null, null, null);
    }

    /**
     * constructor
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     * If the component is a parallel component, this class is by default {@link org.objectweb.proactive.core.component.type.ParallelComposite}
     * @param constructorParameters parameters of the constructor of the base class
     * @param virtualNode virtual node where this component will be instantiated
     */
    public ContentDescription(String className, Object[] constructorParameters,
        VirtualNode virtualNode) {
        this(className, constructorParameters, virtualNode, null, null);
    }

    /**
     * constructor
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     * If the component is a parallel component, this class is by default {@link org.objectweb.proactive.core.component.type.ParallelComposite}
     * @param constructorParameters parameters of the constructor of the base class
     * @param node node where this component will be instantiated
     */
    public ContentDescription(String className, Object[] constructorParameters,
        Node node) {
        this(className, constructorParameters, node, null, null);
    }

    /**
     * constructor. As no node nor virtual node is specified, the component will be instantiated in the
     * current virtual machine
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     * If the component is a parallel component, this class is by default {@link org.objectweb.proactive.core.component.type.ParallelComposite}
     */
    public ContentDescription(String className) {
        this(className, null, (Node) null, null, null);
    }

    /**
     * getter for the activity
     * @return the activity of the active object
     */
    public Active getActivity() {
        return activity;
    }

    /**
     * getter for the classname
     * @return the name of the class
     */
    public String getClassName() {
        return className;
    }

    /**
     * getter for the constructor parameters
     * @return constructor parameters
     */
    public Object[] getConstructorParameters() {
        return constructorParameters;
    }

    /**
     * getter for the metaobjects factory
     * @return metaobjects factory
     */
    public MetaObjectFactory getFactory() {
        return factory;
    }

    /**
     * gives deployment information
     * @return true if the component is to be deployed on a virtual node
     */
    public boolean isLocalizedOnAVirtualNode() {
        return ((virtualNode != null) && (node == null));
    }

    /**
     * getter for the node
     * @return the node where the component is to be deployed
     */
    public Node getNode() {
        return node;
    }

    /**
     * getter for the virtual node
     * @return the virtual node where the component is to be deployed
     */
    public VirtualNode getVirtualNode() {
        return virtualNode;
    }

    /**
     * setter (one can only change the virtual node BEFORE instantiating the component)
     * @param virtualNode the new virtual node
     */
    public void setVirtualNode(VirtualNode virtualNode) {
        this.virtualNode = virtualNode;
        node = null;
    }

    /**
     * setter (one can only change the node BEFORE instantiating the component)
     * @param node the new node
     */
    public void setNode(Node node) {
        this.node = node;
        virtualNode = null;
    }

    public void forceSingleInstance() {
        uniqueInstance = true;
    }

    public boolean uniqueInstance() {
        return uniqueInstance;
    }

    /**
     * setter (visibility is reduced)
     */
    void setFactory(MetaObjectFactory factory) {
        this.factory = factory;
    }
    
    void setActivity(Active activity) {
        this.activity = activity;
    }
}
