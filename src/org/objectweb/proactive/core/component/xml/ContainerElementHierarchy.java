package org.objectweb.proactive.core.component.xml;

import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;

import java.util.Vector;


/**
 * @author Matthieu Morel
 */
public class ContainerElementHierarchy {
    ContainerHandlerMarker fatherHandler;
    Vector childrenContainerHandlers;

    public ContainerElementHierarchy() {
        childrenContainerHandlers = new Vector();
    }

    public void addFatherHandler(ContainerHandlerMarker fatherHandler) {
        this.fatherHandler = fatherHandler;
    }

    public void disableGrandFatherHandler() {
        if (fatherHandler != null) {
            if (fatherHandler.getContainerElementHierarchy().getFather() != null) {
                fatherHandler.getContainerElementHierarchy().getFather()
                             .disable();
            }
        }
    }

    public void addChildContainerHandler(ContainerHandlerMarker childHandler) {
        childrenContainerHandlers.add(childHandler);
    }

    public boolean containsChild(UnmarshallerHandler handler) {
        return childrenContainerHandlers.contains(handler);
    }

    public ContainerHandlerMarker getFather() {
        return fatherHandler;
    }
}
