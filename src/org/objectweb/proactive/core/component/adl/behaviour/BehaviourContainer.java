package org.objectweb.proactive.core.component.adl.behaviour;

/**
 * Container used by the parser
 * @author Nicolsa Dalmasso
 *
 */
public interface BehaviourContainer {
	/**
	 * Gets the behaviour used by the parser for the <bahaviour> tag
	 * @return Current behaviour
	 */
	Behaviour getBehaviour();
	
	/**
	 * Sets the behaviour used by the parser for the <bahaviour> tag
	 * @param behaviour New behaviour
	 */
	void setBehaviour(Behaviour behaviour);
}
