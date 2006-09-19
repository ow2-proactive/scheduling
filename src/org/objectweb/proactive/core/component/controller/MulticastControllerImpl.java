package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.controller.util.MulticastHelper;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.SerializableMethod;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MulticastControllerImpl
    extends AbstractCollectiveInterfaceController implements MulticastController,Serializable {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_CONTROLLERS);
    private static Logger multicastLogger = ProActiveLogger.getLogger(Loggers.COMPONENTS_MULTICAST);
    private Map<String, ProActiveInterface> multicastItfs = new HashMap<String, ProActiveInterface>();
    private Map clientSideProxies = new HashMap();
	public MulticastControllerImpl(Component owner) {
        super(owner);
    
    }

    public void init() {
        // this method is called once the component is fully instantiated with all its interfaces created
        InterfaceType[] itfTypes = ((ComponentType)owner.getFcType()).getFcInterfaceTypes();
        for (int i = 0; i < itfTypes.length; i++) {
			ProActiveInterfaceType type = (ProActiveInterfaceType)itfTypes[i];
			if (type.isFcMulticastItf()) {
                try {
					addClientSideProxy(type.getFcItfName(),(ProActiveInterface) owner.getFcInterface(type.getFcItfName()));
				} catch (NoSuchInterfaceException e) {
					throw new ProActiveRuntimeException(e);
				}
            }
        }
            List<InterfaceType> interfaceTypes = Arrays.asList(((ComponentType) owner.getFcType()).getFcInterfaceTypes());
            Iterator<InterfaceType> it = interfaceTypes.iterator();

            while (it.hasNext()) {
                // keep ref on interfaces of cardinality multicast
                addManagedInterface((ProActiveInterfaceType) it.next());
            }
    }

  /**
  * client and server interfaces must have the same methods, except that
  * the client methods always returns a java.util.List<E>, whereas
  * the server methods return E. (for multicast interfaces)
  * <br>
  *
  */
 void checkCompatibility(ProActiveInterfaceType clientSideItfType, ProActiveInterface serverSideItf) throws IllegalBindingException {
     try {
     	ProActiveInterfaceType serverSideItfType = (ProActiveInterfaceType)serverSideItf.getFcItfType();
         Class clientSideItfClass;
         clientSideItfClass = Class.forName(clientSideItfType.getFcItfSignature());
         Class serverSideItfClass = Class.forName(serverSideItfType.getFcItfSignature());
 
 
         Method[] clientSideItfMethods = clientSideItfClass.getMethods();
         Method[] serverSideItfMethods = serverSideItfClass.getMethods();
 
         if (clientSideItfMethods.length != serverSideItfMethods.length) {
             throw new IllegalBindingException("incompatible binding between client interface " + clientSideItfType.getFcItfName() + " (" + clientSideItfType.getFcItfSignature() + ")  and server interface " + serverSideItfType.getFcItfName() + " ("+serverSideItfType.getFcItfSignature()+") : there is not the same number of methods (including those inherited) in both interfaces !");
         }
 
         Map<SerializableMethod, SerializableMethod> matchingMethodsForThisItf = new HashMap<SerializableMethod, SerializableMethod>(clientSideItfMethods.length);
 
         for (Method method : clientSideItfMethods) {
                 Method serverSideMatchingMethod = searchMatchingMethod(method, serverSideItfMethods, clientSideItfType.isFcMulticastItf(), serverSideItfType.isFcGathercastItf(), serverSideItf);
                 if (serverSideMatchingMethod == null) {
                     throw new IllegalBindingException("binding incompatibility between " + clientSideItfType.getFcItfName() + " and " + serverSideItfType.getFcItfName() + " : cannot find matching method");
                 }
                 matchingMethodsForThisItf.put(new SerializableMethod(method), new SerializableMethod(serverSideMatchingMethod));
         }

         if (!MulticastHelper.matchingMethods.containsKey(owner.getID())) {
        	 MulticastHelper.matchingMethods.put(owner.getID(), new HashMap<String, Map<SerializableMethod, SerializableMethod>>());
         }
         
         MulticastHelper.matchingMethods.get(owner.getID()).put(clientSideItfType.getFcItfName(), matchingMethodsForThisItf);
     } catch (ClassNotFoundException e) {
         throw new IllegalBindingException("cannot find class corresponding to given signature " +
             e.getMessage());
     }
 }
 
 public void checkCompatibility(String itfName, ProActiveInterface itf) throws IllegalBindingException {
   try {
       
   	ProActiveInterfaceType clientItfType = (ProActiveInterfaceType)Utils.getItfType(itfName, owner);
       
       checkCompatibility(clientItfType,
           itf);
   } catch (NoSuchInterfaceException e) {
       throw new IllegalBindingException("cannot find client interface " + itfName);
   }
}
    /*
     * @see org.objectweb.proactive.core.component.controller.AbstractCollectiveInterfaceController#searchMatchingMethod(java.lang.reflect.Method, java.lang.reflect.Method[])
     */
    @Override
    protected Method searchMatchingMethod(Method clientSideMethod,
        Method[] serverSideMethods, boolean clientItfIsMulticast, boolean serverItfIsGathercast, ProActiveInterface serverSideItf) {
        try {
            return MulticastBindingChecker.searchMatchingMethod(clientSideMethod,
                serverSideMethods, serverItfIsGathercast, serverSideItf);
        } catch (ParameterDispatchException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance()
                                               .createFcItfType(Constants.MULTICAST_CONTROLLER,
                    MulticastController.class.getName(), TypeFactory.SERVER,
                    TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException(
                "cannot create controller type for controller " +
                this.getClass().getName());
        }
    }

    private boolean addManagedInterface(ProActiveInterfaceType itfType) {
        if (!itfType.isFcMulticastItf()) {
            return false;
        }
        if (multicastItfs.containsKey(itfType.getFcItfName())) {
//            logger.error("the interface named " + itfType.getFcItfName() +
//                " is already managed by the collective interfaces controller");
            return false;
        }

        try {
            ProActiveInterface multicastItf = (ProActiveInterface) owner.getFcInterface(itfType.getFcItfName());

            if (itfType.isFcMulticastItf()) {
                multicastItfs.put(itfType.getFcItfName(), multicastItf);
            } else {
//                logger.error("the interface named " + itfType.getFcItfName() +
//                    " cannot be managed by this collective interfaces controller");
                return false;
            }
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.MulticastController#bindFc(java.lang.String, org.objectweb.proactive.core.component.ProActiveInterface)
     */
    public void bindFcMulticast(String clientItfName,
        ProActiveInterface serverItf) {
        init();
        if (logger.isDebugEnabled()) {
            try {
            	if (!ProActiveGroup.isGroup(serverItf.getFcItfOwner())) {
                logger.debug("multicast binding : " + clientItfName + " to : " +
                    Fractal.getNameController(serverItf.getFcItfOwner())
                           .getFcName() + "." + serverItf.getFcItfName());
            	}
            } catch (NoSuchInterfaceException e) {
                e.printStackTrace();
            }
        }
        if (multicastItfs.containsKey(clientItfName)) {
            bindFc(clientItfName, serverItf);
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.MulticastController#unbindFc(java.lang.String, org.objectweb.proactive.core.component.ProActiveInterface)
     */
    public void unbindFcMulticast(String clientItfName,
        ProActiveInterface serverItf) {
        if (multicastItfs.containsKey(clientItfName)) {
            if (ProActiveGroup.getGroup(multicastItfs.get(clientItfName))
                                  .remove(serverItf)) {
                logger.debug(
                    "removed connected interface from multicast interface : " +
                    clientItfName);
            } else {
                logger.error(
                    "cannot remove connected interface from multicast interface : " +
                    clientItfName);
            }
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.MulticastController#lookupFc(java.lang.String)
     */
    public ProxyForComponentInterfaceGroup lookupFcMulticast(
        String clientItfName) {
        if (multicastItfs.containsKey(clientItfName)) {
            return (ProxyForComponentInterfaceGroup) ((ProActiveInterface) multicastItfs.get(clientItfName)
                                                                                        .getFcItfImpl()).getProxy();
        } else {
            return null;
        }
    }
    
    
    private void bindFc(String clientItfName, ProActiveInterface serverItf) {

        try {
            ProxyForComponentInterfaceGroup clientSideProxy = (ProxyForComponentInterfaceGroup) clientSideProxies
                                                              .get(clientItfName);

            if (clientSideProxy.getDelegatee() == null) {
                ProActiveInterface groupItf = ProActiveComponentGroup
                                              .newComponentInterfaceGroup((ProActiveInterfaceType) serverItf
                                                                          .getFcItfType(),
                                                                          owner);
                ProxyForComponentInterfaceGroup proxy = (ProxyForComponentInterfaceGroup) ((StubObject) groupItf)
                                                        .getProxy();
                clientSideProxy.setDelegatee(proxy);
            }

            ((Group) clientSideProxy.getDelegatee()).add(serverItf);
        } catch (ClassNotReifiableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    private boolean hasClientSideProxy(String itfName) {
        return clientSideProxies.containsKey(itfName);
    }

    private void addClientSideProxy(String itfName, ProActiveInterface itf) {
        Proxy proxy = ((ProActiveInterface)itf.getFcItfImpl()).getProxy();
        
        if (!(proxy instanceof Group)) {
            throw new ProActiveRuntimeException("client side proxies for multicast interfaces must be Group instances");
        }

        clientSideProxies.put(itfName, proxy);
    }
}
