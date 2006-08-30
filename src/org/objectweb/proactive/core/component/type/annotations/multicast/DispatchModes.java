package org.objectweb.proactive.core.component.type.annotations.multicast;

/**
 * Enumeration of the available dispatch modes.
 * 
 * The CUSTOM mode indicates that the dispatch mode is given as a parameter, as a class signature
 * 
 * @author Matthieu Morel
 *
 */
public enum DispatchModes {
    BROADCAST, ONE_TO_ONE, ROUND_ROBIN, CUSTOM;

}
