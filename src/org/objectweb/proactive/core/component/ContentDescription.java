/*
 * Created on Feb 3, 2004
 * author : Matthieu Morel
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
	public ContentDescription(String className, Object[] constructorParameters, VirtualNode virtualNode, Active activity, MetaObjectFactory factory) {
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
	public ContentDescription(String className, Object[] constructorParameters, Node node, Active activity, MetaObjectFactory factory) {
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
		this(className, constructorParameters, (Node)null, null, null);
	}
	
	/**
	 * constructor
	 * @param className the name of the base class of the component 
	 * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
	 * If the component is a parallel component, this class is by default {@link org.objectweb.proactive.core.component.type.ParallelComposite}
	 * @param constructorParameters parameters of the constructor of the base class
	 * @param virtualNode virtual node where this component will be instantiated
	 */
	public ContentDescription(String className, Object[] constructorParameters, VirtualNode virtualNode) {
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
	public ContentDescription(String className, Object[] constructorParameters, Node node) {
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
		this(className, null, (Node)null, null, null);
	}

	/**
	 * getter for the activity
	 * @return 
	 */
	public Active getActivity() {
		return activity;
	}

	/**
	 * getter for the classname
	 * @return
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
	 * setter (visibility is reduced) 
	 */
	void setFactory(MetaObjectFactory factory) {
		this.factory = factory;
	}


}
