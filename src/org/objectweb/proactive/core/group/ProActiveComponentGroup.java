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
        // MOP.checkClassIsReifiable(MOP.forName(Component.class.getName()));
        // instantiate the component metaobject factory with parameters of the component
        // first create a hashtable with the parameters
//        Hashtable factory_params = new Hashtable(1);
//        factory_params.put(ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY,
//            componentParameters);
//        MetaObjectFactory factory = new ProActiveMetaObjectFactory(factory_params);

        // ComponentBody parameters part of general config or what?
        Object result = null;

        try {
            result = MOP.newInstance(ProActiveInterface.class.getName(), null,
                    ProActiveGroup.DEFAULT_PROXYFORGROUP_CLASS_NAME,
                    new Object[] { null, null, null });
        } catch (ClassNotReifiableException e) {
            logger.error("**** ClassNotReifiableException ****");
        } catch (InvalidProxyClassException e) {
            logger.error("**** InvalidProxyClassException ****");
        } catch (ConstructionOfProxyObjectFailedException e) {
			logger.error(
                "**** ConstructionOfProxyObjectFailedException ****");
        } catch (ConstructionOfReifiedObjectFailedException e) {
			logger.error(
                "**** ConstructionOfReifiedObjectFailedException ****");
        }

        ((org.objectweb.proactive.core.group.ProxyForGroup) (((StubObject) result).getProxy())).className = Interface.class.getName();

        return ProActiveComponentRepresentativeFactory.instance()
                                                      .createComponentRepresentative(componentParameters,
            ((StubObject) result).getProxy());

        //return result;
    }

    /**
     * creates a group proxy on a set of generated Interface objects.
     * These objects are generated according to the InterfaceType they get as a parameter
     * @param interfaceType the type of interface we need a group of Interface objects on
     * @return
     * @throws ClassNotFoundException
     * @throws ClassNotReifiableException
     */
    public static ProActiveInterface newActiveComponentGroup(
        InterfaceType interfaceType)
        throws ClassNotFoundException, ClassNotReifiableException {
        	try {
        // MOP.checkClassIsReifiable(MOP.forName(Component.class.getName()));
        // PRIMITIVE parameter is given so that the calls are di
        ComponentParameters component_parameters = new ComponentParameters(interfaceType.getFcItfName(),
                null,
                ProActiveTypeFactory.instance().createFcType(new InterfaceType[] {
                        interfaceType
                    }));

        // instantiate the component metaobject factory with parameters of the component
        // first create a hashtable with the parameters
//        Hashtable factory_params = new Hashtable(1);
//        factory_params.put(ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY,
//            component_parameters);
//        MetaObjectFactory factory = new ProActiveMetaObjectFactory(factory_params);

        // ComponentBody parameters part of general config or what?
        Object result = null;

            result = MOP.newInstance(ProActiveInterface.class.getName(), null,
                    ProActiveGroup.DEFAULT_PROXYFORGROUP_CLASS_NAME, null);
                    //new Object[] { null, null, factory });
			//new Object[] { null, null, null });
			ProxyForGroup proxy = (org.objectweb.proactive.core.group.ProxyForGroup)((StubObject)result).getProxy();
            proxy.className = ProActiveInterface.class.getName();
			//proxy.stub = (StubObject)result;
			//return (ProActiveInterface)result;
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

        //return result;
    }

    /** Create an object representing a group and create members with params cycling on nodeList. */

    // ComponentBody Parameters is unique for all the group members (notably the name is the same)...
    public static Object newActiveComponentGroup(String className,
        Object[][] componentParams, Node[] nodeList, Active[] activity,
        MetaObjectFactory[] factory, ComponentParameters componentParameters)
        throws ClassNotFoundException, ClassNotReifiableException, 
            ActiveObjectCreationException, NodeException, MOPException {
        // create a container using generic component parameters
        Object result = newActiveComponentGroup(componentParameters);
        Group g = ProActiveGroup.getGroup(result);

        // add all the elements inside the group
        for (int i = 0; i < componentParams.length; i++) {
            g.add(ProActive.newActiveComponent(className, componentParams[i],
                    nodeList[i % nodeList.length], activity[i], factory[i],
                    componentParameters));
        }
        return result;
    }
}
