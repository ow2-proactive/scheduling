package org.objectweb.proactive.core.component.xml;


/**
 * @author Matthieu Morel
 */
public class ComponentResultObject {
    private String name;
    private String[] names;

    public ComponentResultObject(String[] names) {
        this.names = names;
    }

    public ComponentResultObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String[] getNames() {
        return names;
    }

    public boolean componentsAreParallelized() {
        return ((name == null) && (names != null));
    }
}
