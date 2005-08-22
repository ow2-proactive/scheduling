package org.objectweb.proactive.core.component.representative;

import org.objectweb.proactive.core.UniqueID;


/**
 * Identifies the functional interface of a component by its name and the id of the body of the component
 * it belongs to.
 *
 * @author Matthieu Morel
 */
public class FunctionalInterfaceID {
    private String functionalInterfaceName;
    private UniqueID componentBodyID;

    public FunctionalInterfaceID(String functionalInterfaceName,
        UniqueID componentBodyID) {
        this.functionalInterfaceName = functionalInterfaceName;
        this.componentBodyID = componentBodyID;
    }

    public String getFunctionalInterfaceName() {
        return functionalInterfaceName;
    }

    public UniqueID getComponentBodyID() {
        return componentBodyID;
    }

    public int hashCode() {
        return componentBodyID.hashCode() + functionalInterfaceName.hashCode();
    }

    public boolean equals(Object o) {
        //System.out.println("Now checking for equality");
        if (o instanceof FunctionalInterfaceID) {
            return (functionalInterfaceName.equals(((FunctionalInterfaceID) o).functionalInterfaceName) &&
            (componentBodyID.equals(((FunctionalInterfaceID) o).componentBodyID)));
        } else {
            return false;
        }
    }
}
