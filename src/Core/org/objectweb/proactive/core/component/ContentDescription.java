/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component;

import java.io.Serializable;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.MetaObjectFactory;


/**
 * <p>Fractal implementation-specific description of  the content of components.</p>
 * <p>With ProActive, features such as activity, factory, virtual node or constructor parameters
 * can be specified.</p>
 *
 * @author Matthieu Morel
 */
@PublicAPI
public class ContentDescription implements Serializable {
    private String className;
    private Object[] constructorParameters;
    private Active activity;
    private MetaObjectFactory factory;
    private boolean uniqueInstance = false;

    /**
     * constructor
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     * @param constructorParameters parameters of the constructor of the base class
     * @param activity the activity as defined in the ProActive model
     * @param factory overriden meta-object factory for the component. Can be null.
     */
    public ContentDescription(String className, Object[] constructorParameters, Active activity,
            MetaObjectFactory factory) {
        this.className = className;
        this.constructorParameters = constructorParameters;
        this.activity = activity;
        this.factory = factory;
    }

    /**
     * constructor
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     * @param constructorParameters parameters of the constructor of the base class
     */
    public ContentDescription(String className, Object[] constructorParameters) {
        this(className, constructorParameters, null, null);
    }

    /**
     * constructor
     * @param className the name of the base class of the component
     * If the component is a composite component, this class is by default {@link org.objectweb.proactive.core.component.type.Composite}
     */
    public ContentDescription(String className) {
        this(className, null, null, null);
    }

    /**
     * getter for the activity
     * @return the activity of the active object
     */
    public Active getActivity() {
        return activity;
    }

    /**
     * getter for the classname
     * @return the name of the class
     */
    public String getClassName() {
        return className;
    }

    /**
     * setter for the classname
     * @param className the name of the class
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * getter for the constructor parameters
     * @return constructor parameters
     */
    public Object[] getConstructorParameters() {
        return constructorParameters;
    }

    /**
     * getter for the metaobjects factory
     * @return metaobjects factory
     */
    public MetaObjectFactory getFactory() {
        return factory;
    }

    /**
     * Indicates that there should only be one instance of this component
     * when instantiating the component on a given multiple virtual node
     *
     */
    public void forceSingleInstance() {
        uniqueInstance = true;
    }

    /**
     * Returns whether there should only be one instance of this component
     * when instantiating the component on a given multiple virtual node
     * @return boolean
     */
    public boolean uniqueInstance() {
        return uniqueInstance;
    }

    /**
     * setter
     * @param factory MetaObjectFactory
     */
    public void setFactory(MetaObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * setter
     * @param activity Active
     */
    void setActivity(Active activity) {
        this.activity = activity;
    }
}
