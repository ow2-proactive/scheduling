package org.objectweb.proactive.core.group;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.InvalidProxyClassException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 *
 *  // TODO : change class name (interfaces only are grouped)
 *
 * A class for creating groups of interfaces
 * Indeed, the standard mechanism cannot be used here, as we are referencing components
 * through interfaces of component representatives.
 *
 *  It was moved to this package so it can see className attribute in ProxyForGroup
 *
 * @author Matthieu Morel
 */
public class ProActiveComponentGroup {
    protected static Logger logger = Logger.getLogger(ProActiveComponentGroup.class.getName());

    // hack for not displaying the javadoc. See mother class.
    //private ProActiveComponentGroup(){}

    /** Create an object representing an empty group of components specifying the java class of the components. */
    public static Object newActiveComponentGroup(
        ComponentParameters componentParameters)
        throws ClassNotFoundException, ClassNotReifiableException {
        Object result = null;

        try {
            result = MOP.newInstance(ProActiveInterface.class.getName(), null,
                    ProActiveGroup.DEFAULT_PROXYFORGROUP_CLASS_NAME,
                    new Object[] { null, null, null });
        } catch (ClassNotReifiableException e) {
            System.err.println("**** ClassNotReifiableException ****");
        } catch (InvalidProxyClassException e) {
            System.err.println("**** InvalidProxyClassException ****");
        } catch (ConstructionOfProxyObjectFailedException e) {
            System.err.println(
                "**** ConstructionOfProxyObjectFailedException ****");
        } catch (ConstructionOfReifiedObjectFailedException e) {
            System.err.println(
                "**** ConstructionOfReifiedObjectFailedException ****");
        }

        ((org.objectweb.proactive.core.group.ProxyForGroup) (((StubObject) result).getProxy())).className = Interface.class.getName();

        return ProActiveComponentRepresentativeFactory.instance()
                                                      .createComponentRepresentative(componentParameters,
            ((StubObject) result).getProxy());
    }

    /**
     * creates a group proxy on a set of generated Interface objects.
     * These objects are generated according to the InterfaceType they get as a parameter
     * @param interfaceType the type of interface we need a group of Interface objects on
     * @return
     * @throws ClassNotFoundException
     * @throws ClassNotReifiableException
     */
    public static ProActiveInterface newActiveComponentInterfaceGroup(
        InterfaceType interfaceType)
        throws ClassNotFoundException, ClassNotReifiableException {
        try {
            // PRIMITIVE parameter is given so that the calls are di
            ComponentParameters component_parameters = new ComponentParameters(interfaceType.getFcItfName(),
                    null,
                    ProActiveTypeFactory.instance().createFcType(new InterfaceType[] {
                            interfaceType
                        }));

            Object result = null;

            result = MOP.newInstance(ProActiveInterface.class.getName(), null,
                    ProActiveGroup.DEFAULT_PROXYFORGROUP_CLASS_NAME, null);

            ProxyForGroup proxy = (org.objectweb.proactive.core.group.ProxyForGroup) ((StubObject) result).getProxy();
            proxy.className = ProActiveInterface.class.getName();

            //return a reference on the generated interface reference corresponding to the interface type 
            return (ProActiveInterface) (ProActiveComponentRepresentativeFactory.instance()
                                                                                .createComponentRepresentative(component_parameters,
                proxy)).getFcInterface(interfaceType.getFcItfName());
        } catch (ClassNotReifiableException e) {
            logger.error("**** ClassNotReifiableException ****");
        } catch (InvalidProxyClassException e) {
            logger.error("**** InvalidProxyClassException ****");
        } catch (ConstructionOfProxyObjectFailedException e) {
            logger.error("**** ConstructionOfProxyObjectFailedException ****");
        } catch (ConstructionOfReifiedObjectFailedException e) {
            logger.error("**** ConstructionOfReifiedObjectFailedException ****");
        } catch (NoSuchInterfaceException e) {
            logger.error("**** ConstructionOfReifiedObjectFailedException ****");
        }
        return null;
    }

    /** Create an object representing a group and create members with params cycling on nodeList. */

    // ComponentBody Parameters is unique for all the group members (notably the name is the same)...
    public static Object newActiveComponentGroup(String className,
        Object[][] constructorParams, Node[] nodeList, Active[] activity,
        MetaObjectFactory[] factory, ComponentParameters componentParameters)
        throws ClassNotFoundException, ClassNotReifiableException, 
            ActiveObjectCreationException, NodeException, MOPException {
        // create a container using generic component parameters
        Object result = newActiveComponentGroup(componentParameters);
        Group g = ProActiveGroup.getGroup(result);

        // add all the elements inside the group
        for (int i = 0; i < constructorParams.length; i++) {
            g.add(ProActive.newActiveComponent(className, constructorParams[i],
                    nodeList[i % nodeList.length], activity[i], factory[i],
                    componentParameters));
        }
        return result;
    }

    //	/**
    // jem3D stuff - to be committed later.
    //	 * creates a group
    //	 * @param className
    //	 * @param constructorsParameters
    //	 * @param nodeList
    //	 * @param componentParameters
    //	 * @return
    //	 * @throws ClassNotFoundException
    //	 * @throws ClassNotReifiableException
    //	 * @throws ActiveObjectCreationException
    //	 * @throws NodeException
    //	 */
    //    public static Object newActiveComponentGroupBuildWithMultithreading(
    //        String className, Object[][] constructorsParameters, String[] nodeList,
    //        ComponentParameters componentParameters)
    //        throws ClassNotFoundException, ClassNotReifiableException, 
    //            ActiveObjectCreationException, NodeException {
    //        Object result = ProActiveGroup.newGroup(Component.class.getName());
    //        ProxyForGroup proxy = (org.objectweb.proactive.core.group.ProxyForGroup) ProActiveGroup.getGroup(result);
    //
    //        proxy.createComponentMemberWithMultithread(className,
    //            constructorsParameters, nodeList, componentParameters);
    //
    //        return result;
    //    }
}
