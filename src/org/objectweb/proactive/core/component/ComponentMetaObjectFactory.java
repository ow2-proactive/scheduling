package org.objectweb.proactive.core.component;

import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.component.identity.ProActiveComponentFactory;


/**
 *  * Meta-object factory for the components.
 * 
 * @author Matthieu Morel
 */
public interface ComponentMetaObjectFactory extends MetaObjectFactory {
    /**
    * Creates or reuses a ProActiveComponentFactory
    * @return a new or existing ProActiveComponentFactory
    * @see ProActiveComponentFactory
    */
    public ProActiveComponentFactory newComponentIdentityFactory();
}