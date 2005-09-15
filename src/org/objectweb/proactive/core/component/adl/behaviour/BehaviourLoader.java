package org.objectweb.proactive.core.component.adl.behaviour;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;

/**
 * Loader used by the parser
 * @author Nicolas Dalmasso
 *
 */
public class BehaviourLoader extends AbstractLoader{

	/**
	 * Loads a definition
	 */
	public Definition load(String name, Map context) throws ADLException {
		Definition d = clientLoader.load(name,context);
		return d;
	}

}
