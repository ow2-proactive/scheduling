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
	 * see {@link Interface#getFcItfOwner()}
	 */
	public Component getFcItfOwner() {
		return owner;
	}

	/**
	 * see {@link Interface#getFcItfName()}
	 */
	public String getFcItfName() {
		return name;
	}

	/**
	 * see {@link Interface#getFcItfType()}
	 */
	public Type getFcItfType() {
		return type;
	}

	/**
	 * see {@link org.objectweb.fractal.api.Interface#isFcInternalItf()}
	 */
	public boolean isFcInternalItf() {
		return isInternal;
	}

	/**
	 * Sets the isInternal.
	 * @param isInternal The isInternal to set
	 */
	public void setFcIsInternal(boolean isInternal) {
		this.isInternal = isInternal;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setFcItfName(String name) {
		this.name = name;
	}

	/**
	 * Sets the owner.
	 * @param owner The owner to set
	 */
	public void setFcOwner(Component owner) {
		this.owner = owner;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setFcType(Type type) {
		this.type = type;
	}

	/**
	 * getter
	 * @return the delegatee
	 */
	public abstract Object getFcItfImpl();

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