/*
 * Created on 12 janv. 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.objectweb.proactive.ext.security;

import java.io.Serializable;

/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DefaultEntity extends Entity implements Serializable {

	/**
	 * 
	 */
	public DefaultEntity() {
		super();
	}

	public String getName() {
		return "defaultEntity";
	}

	public boolean equals(Entity e) {
		return true;
	}

}
