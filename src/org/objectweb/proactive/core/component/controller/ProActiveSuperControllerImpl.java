/*
 * Created on May 4, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

/**
 * @author Matthieu Morel
 */
public class ProActiveSuperControllerImpl
	extends ProActiveController
	implements Serializable, ProActiveSuperController {

	public ProActiveSuperControllerImpl(Component owner) {
		super(owner);
		try {
			setItfType(ProActiveTypeFactory.instance().createFcItfType(Constants.SUPER_CONTROLLER,
										ProActiveContentController.class.getName(),
								TypeFactory.SERVER, TypeFactory.MANDATORY,
								TypeFactory.SINGLE));
		} catch (InstantiationException e) {
			throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
		}

	}

	// the following is borrowed from the Julia implementation
	public Component[] fcParents;

	public Component[] getFcSuperComponents() {
		if (fcParents == null) {
			return new Component[0];
		} else {
			return fcParents;
		}
	}

	public void addParent(final Component parent) {
		int length = fcParents == null ? 1 : fcParents.length + 1;
		Component[] parents = new Component[length];
		if (fcParents != null) {
			System.arraycopy(fcParents, 0, parents, 1, fcParents.length);
		}
		parents[0] = parent;
		fcParents = parents;
	}

	public void removeParent(final Component parent) {
		int length = fcParents.length - 1;
		if (length == 0) {
			fcParents = null;
		} else {
			Component[] parents = new Component[length];
			int i = 0;
			for (int j = 0; j < fcParents.length; ++j) {
				if (!fcParents[j].equals(parent)) {
					parents[i++] = fcParents[j];
				}
			}
			fcParents = parents;
		}
	}
}
