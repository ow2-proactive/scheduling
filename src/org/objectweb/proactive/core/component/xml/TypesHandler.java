/*
 * Created on Oct 29, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;

import java.util.HashMap;


/**
 * @author Matthieu Morel
 */
public class TypesHandler extends CollectionUnmarshaller {
    HashMap componentTypes;

    public TypesHandler(HashMap componentTypes) {
        this.componentTypes = componentTypes;
        addHandler(ComponentsDescriptorConstants.COMPONENT_TYPE_TAG,
            new ComponentTypeHandler());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        if (name.equals(ComponentsDescriptorConstants.COMPONENT_TYPE_TAG)) {
            // the result object is a table with 2 elements : {name of the type, ComponentType instance} 
            Object[] component_type_info = (Object[]) getHandler(ComponentsDescriptorConstants.COMPONENT_TYPE_TAG)
                                                          .getResultObject();
            componentTypes.put(component_type_info[0], component_type_info[1]);
            ((ComponentTypeHandler) activeHandler).reset();
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
    }
}
