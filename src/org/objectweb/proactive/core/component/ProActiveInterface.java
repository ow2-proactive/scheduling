package org.objectweb.proactive.core.component;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.Type;

/** 
 * Abstract implementation of the Interface interface of the Fractal api.
 * As functional interfaces are specified for each component, they are generated at
 * instantiation time (bytecode generation), by subclassing this class.
 * 
 * @author Matthieu Morel
 */
public abstract class ProActiveInterface implements Interface, java.io.Serializable {
	private Component owner;
	private String name;
	private Type type;
	private boolean isInternal;

	public ProActiveInterface() {
	}

	/**
	 * @see org.objectweb.fractal.api.Interface#getFcItfOwner()
	 */
	public Component getFcItfOwner() {
		return owner;
	}

	/**
	 * @see org.objectweb.fractal.api.Interface#getFcItfName()
	 */
	public String getFcItfName() {
		return name;
	}

	/**
	 * @see org.objectweb.fractal.api.Interface#getFcItfType()
	 */
	public Type getFcItfType() {
		return type;
	}

	/**
	 * @see org.objectweb.fractal.api.Interface#isFcInternalItf()
	 */
	public boolean isFcInternalItf() {
		return isInternal;
	}

	/**
	 * Returns the isInternal.
	 * @return boolean
	 */
	public boolean isInternal() {
		return isInternal;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the owner.
	 * @return Component
	 */
	public Component getOwner() {
		return owner;
	}

	/**
	 * Returns the type.
	 * @return Type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Sets the isInternal.
	 * @param isInternal The isInternal to set
	 */
	public void setIsInternal(boolean isInternal) {
		this.isInternal = isInternal;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the owner.
	 * @param owner The owner to set
	 */
	public void setOwner(Component owner) {
		this.owner = owner;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return
	 */
	public abstract Object getFcItfImpl();

//	public boolean isControlInterface() {
//		return (
//			getFcItfName().equals(ContentController.CONTENT_CONTROLLER)
//				|| getFcItfName().equals(BindingController.BINDING_CONTROLLER)
//				|| getFcItfName().equals(LifeCycleController.LIFECYCLE_CONTROLLER)
//				|| getFcItfName().equals(UserBindingController.USER_BINDING_CONTROLLER)
//				|| getFcItfName().equals(ComponentParametersController.COMPONENT_PARAMETERS_CONTROLLER));
//	}

	/**
	 * Sets the object to which this interface reference object should delegate
	 * method calls.
	 *
	 * @param impl the object to which this interface reference object should
	 *      delegate method calls.
	 * @see #getFcItfImpl getFcItfImpl
	 */
	public abstract void setFcItfImpl(final Object impl);

	public String toString() {
		String string =
			"name : "
				+ getFcItfName()
				+ "\n"
				+ "componentIdentity : "
				+ getFcItfOwner()
				+ "\n"
				+ "type : "
				+ getFcItfType()
				+ "\n"
				+ "isInternal : "
				+ isFcInternalItf()
				+ "\n";
		return string;
	}
	

}