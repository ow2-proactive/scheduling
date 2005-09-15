package org.objectweb.proactive.core.component.adl.behaviour;

/**
 * Used by the parser to affect the behaviour filename to the current component
 * @author Nicolas Dalmasso
 *
 */
public interface BehaviourBuilder{
	/**
	 * Changes the behaviour filenme of the component
	 * @param component Current component
	 * @param lotos New filename
	 * @return Component changed
	 * @throws Exception
	 */
	Object setLotos(Object component,String lotos) throws Exception;
}
