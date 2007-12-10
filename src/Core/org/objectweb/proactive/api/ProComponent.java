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
package org.objectweb.proactive.api;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


@PublicAPI
public class ProComponent {

    /**
     * Creates a new ProActive component over the specified base class, according to the
     * given component parameters, and returns a reference on the component of type Component.
     * A reference on the active object base class can be retrieved through the component parameters controller's
     * method "getStubOnReifiedObject".
     *
     * @param classname the name of the base class. "Composite" if the component is a composite,
     * "ParallelComposite" if the component is a parallel composite component
     * @param genericParameters genericParameters parameterizing types
     * @param constructorParameters the parameters of the constructor of the object
     *    to instantiate as active. If some parameters are primitive types, the wrapper
     *    class types should be given here. null can be used to specify that no parameter
     *    are passed to the constructor.
     * @param node the possibly null node where to create the active object. If null, the active object
     *       is created locally on a default node
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this class for more information.
     * @param factory should be null for components (automatically created)
     * @param componentParameters the parameters of the component
     * @return a component representative of type Component
     * @exception ActiveObjectCreationException if a problem occurs while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Component newActiveComponent(String classname,
        Class<?>[] genericParameters, Object[] constructorParameters,
        Node node, Active activity, MetaObjectFactory factory,
        ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException {
        try {
            Component boot = Fractal.getBootstrapComponent();
            GenericFactory cf = Fractal.getGenericFactory(boot);
            return cf.newFcInstance(componentParameters.getComponentType(),
                new ControllerDescription(componentParameters.getName(),
                    componentParameters.getHierarchicalType()),
                new ContentDescription(classname, constructorParameters,
                    activity, factory));
        } catch (NoSuchInterfaceException e) {
            throw new ActiveObjectCreationException(e);
        } catch (InstantiationException e) {
            if (e.getCause() instanceof NodeException) {
                throw new NodeException(e);
            } else {
                throw new ActiveObjectCreationException(e);
            }
        }
    }

    /**
     * Creates a new ProActive component over the specified base class, according to the
     * given component parameters, and returns a reference on the component of type Component.
     *
     * This method allows automatic of primitive components on Virtual Nodes. In that case, the appendix
     * -cyclicInstanceNumber-<b><i>number</i></b> is added to the name of each of these components.
     * If the component is not a primitive, only one instance of the component is created, on the first node
     * Retrieved from the specified virtual node.
     *
     * A reference on the active object base class can be retrieved through the component parameters controller's
     * method "getStubOnReifiedObject".
     *
     * @param className the name of the base class. "Composite" if the component is a composite,
     * "ParallelComposite" if the component is a parallel composite component
     * @param genericParameters genericParameters parameterizing types
     * @param constructorParameters the parameters of the constructor of the object
     *    to instantiate as active. If some parameters are primitive types, the wrapper
     *    class types should be given here. null can be used to specify that no parameter
     *    are passed to the constructor.
     * @param vn the possibly null node where to create the active object. If null, the active object
     *       is created locally on a default node
     * @param componentParameters the parameters of the component
     * @return a typed group of component representative elements, of type Component
     * @exception ActiveObjectCreationException if a problem occurs while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Component newActiveComponent(String className,
        Class<?>[] genericParameters, Object[] constructorParameters,
        VirtualNode vn, ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException {
        try {
            Component boot = Fractal.getBootstrapComponent();
            ProActiveGenericFactory cf = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);
            return cf.newFcInstance(componentParameters.getComponentType(),
                new ControllerDescription(componentParameters.getName(),
                    componentParameters.getHierarchicalType()),
                new ContentDescription(className, constructorParameters));
        } catch (NoSuchInterfaceException e) {
            throw new ActiveObjectCreationException(e);
        } catch (InstantiationException e) {
            if (e.getCause() instanceof NodeException) {
                throw new NodeException(e);
            } else {
                throw new ActiveObjectCreationException(e);
            }
        }
    }

    /**
     * Creates a new ProActive component over the specified base class, according to the
     * given component parameters, and returns a reference on the component of type Component.
     * A reference on the active object base class can be retrieved through the component parameters controller's
     * method "getStubOnReifiedObject".
     *
     * @param className the name of the base class. "Composite" if the component is a composite,
     * "ParallelComposite" if the component is a parallel composite component
     * @param constructorParameters the parameters of the constructor of the object
     *    to instantiate as active. If some parameters are primitive types, the wrapper
     *    class types should be given here. null can be used to specify that no parameter
     *    are passed to the constructor.
     * @param node the possibly null node where to create the active object. If null, the active object
     *       is created locally on a default node
     * @param activity the possibly null activity object defining the different step in the activity of the object.
     *               see the definition of the activity in the javadoc of this class for more information.
     * @param factory should be null for components (automatically created)
     * @param componentParameters the parameters of the component
     * @return a component representative of type Component
     * @exception ActiveObjectCreationException if a problem occurs while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Component newActiveComponent(String className,
        Object[] constructorParameters, Node node, Active activity,
        MetaObjectFactory factory, ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException {
        return newActiveComponent(className, null, constructorParameters, node,
            activity, factory, componentParameters);
    }

    /**
     * Creates a new ProActive component over the specified base class, according to the
     * given component parameters, and returns a reference on the component of type Component.
     *
     * This method allows automatic of primitive components on Virtual Nodes. In that case, the appendix
     * -cyclicInstanceNumber-<b><i>number</i></b> is added to the name of each of these components.
     * If the component is not a primitive, only one instance of the component is created, on the first node
     * retrieved from the specified virtual node.
     *
     * A reference on the active object base class can be retrieved through the component parameters controller's
     * method "getStubOnReifiedObject".
     *
     * @param className the name of the base class. "Composite" if the component is a composite,
     * "ParallelComposite" if the component is a parallel composite component
     * @param constructorParameters the parameters of the constructor of the object
     *    to instantiate as active. If some parameters are primitive types, the wrapper
     *    class types should be given here. null can be used to specify that no parameter
     *    are passed to the constructor.
     * @param vn the possibly null node where to create the active object. If null, the active object
     *       is created locally on a default node
     * @param componentParameters the parameters of the component
     * @return a typed group of component representative elements, of type Component
     * @exception ActiveObjectCreationException if a problem occurs while creating the stub or the body
     * @exception NodeException if the node was null and that the DefaultNode cannot be created
     */
    public static Component newActiveComponent(String className,
        Object[] constructorParameters, VirtualNode vn,
        ComponentParameters componentParameters)
        throws ActiveObjectCreationException, NodeException {
        return newActiveComponent(className, null, constructorParameters, vn,
            componentParameters);
    }
}
