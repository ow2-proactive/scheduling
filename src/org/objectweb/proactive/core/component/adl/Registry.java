/*
 * Created on Apr 22, 2004
 * author : Matthieu Morel
 */
package org.objectweb.proactive.core.component.adl;

import org.apache.log4j.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;

import java.util.Hashtable;
import java.util.Iterator;


/**
 * @author Matthieu Morel
 */
public class Registry {
    private static Logger logger = Logger.getLogger(Registry.class.getName());
    static private Registry instance = null;
    private Hashtable table;

    private Registry() {
        table = new Hashtable();
    }

    static public Registry instance() {
        if (instance == null) {
            instance = new Registry();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.adl.RegistryManager#addComponent(org.objectweb.fractal.api.Component)
     */
    public void addComponent(Component component) throws ADLException {
        if (ProActiveGroup.isGroup(component)) {
            Group group = ProActiveGroup.getGroup(component);
            Iterator it = group.iterator();
            while (it.hasNext()) {
                addComponent((Component) it.next());
            }
        } else {
            try {
                String name = Fractal.getNameController(component).getFcName();
                if (table.containsKey(name)) {
                    throw new ADLException("A component with the name " + name +
                        " is already stored in the registry", null);
                }
                table.put(name, component);
                if (logger.isDebugEnabled()) {
                    logger.debug("ADDED COMPONENT " + name +
                        " TO THE REGISTRY");
                }
            } catch (NoSuchInterfaceException e) {
                throw new ADLException("It is not possible to register a component without a NameController controller",
                    null);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.adl.RegistryManager#getComponent(java.lang.String)
     */
    public Component getComponent(String name) {
        return (Component) table.get(name);
    }
}
