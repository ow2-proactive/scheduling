/*
 * Created on Jan 17, 2005
 */
package org.objectweb.proactive.examples.nbody.common;

/**
 * @author irosenbe
 * An Exception which is thrown when the force between a Planet and a Set of Planet is tryed to be computed,
 * but the Planet is too close to the Set, and the formula used would create a way too big approximation.
 */
public class TooCloseBodiesException extends Exception{}
