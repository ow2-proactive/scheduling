/*
 * Created on Dec 8, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.ParallelComposite;
import org.objectweb.proactive.core.node.NodeException;

/**
 * @author Matthieu Morel
 */
public class GenericFactoryImpl implements GenericFactory {

	private static GenericFactoryImpl instance = null;

	public static GenericFactoryImpl instance() {
		if (instance == null) {
			instance = new GenericFactoryImpl();
		}
		return instance;
	}

	private GenericFactoryImpl() {
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.factory.GenericFactory#newFcInstance(org.objectweb.fractal.api.Type, java.lang.Object, java.lang.Object)
	 */
	public Component newFcInstance(Type type, Object controllerDesc, Object contentDesc)
		throws InstantiationException {
			try {
			ComponentParameters component_params;
			if (controllerDesc.equals(ComponentParameters.COMPOSITE)) {
				component_params = new ComponentParameters("", ComponentParameters.COMPOSITE, (ComponentType) type);
				return ProActive.newActiveComponent(Composite.class.getName(), null, null, null, null, component_params);
			} else if (controllerDesc.equals(ComponentParameters.PARALLEL)) {
				component_params = new ComponentParameters("", ComponentParameters.PARALLEL, (ComponentType) type);
				return ProActive.newActiveComponent(ParallelComposite.class.getName(), null, null, null, null, component_params);
			} else if (controllerDesc.equals(ComponentParameters.PRIMITIVE)) {
				component_params = new ComponentParameters("", ComponentParameters.PRIMITIVE, (ComponentType) type);
				return ProActive.newActiveComponent((String)contentDesc, null, null, null, null, component_params);
			}
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
			throw new InstantiationException(e.getMessage());
		} catch (NodeException e) {
			throw new InstantiationException(e.getMessage());
		}
		return null;
	}
}


