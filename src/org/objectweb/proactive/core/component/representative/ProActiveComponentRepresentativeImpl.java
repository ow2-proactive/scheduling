package org.objectweb.proactive.core.component.representative;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.asmgen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.request.ComponentRequestQueue;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;

import java.io.Serializable;


public class ProActiveComponentRepresentativeImpl
    implements ProActiveComponentRepresentative, BindingController,
        LifeCycleController, ContentController, ComponentParametersController,
        Interface, Serializable {
    protected static Logger logger = Logger.getLogger(ProActiveComponentRepresentativeImpl.class.getName());
    private Interface[] interfaceReferences;
    private Proxy proxy;
    //private ComponentParameters componentParameters;
    private ComponentType componentType = null; // immutable
    private Object stubOnBaseObject = null; // kept for possible direct invocations on the base object

    //	public ProActiveComponentRepresentativeImpl() {
    //		// required for reification in group mechanism
    //	}
    public ProActiveComponentRepresentativeImpl(
        ComponentParameters componentParameters, Proxy proxy) {
        // set the reference to the proxy for delegating calls to the component metalevel of the active object
        this.proxy = proxy;

        componentType = componentParameters.getComponentType();
        stubOnBaseObject = componentParameters.getStubOnReifiedObject();

        // create the interface references tables
        // the size is the addition of :  
        // - 1 for the current ItfRef (that is at the same time a binding controller, lifecycle controller,
        // content controller and name controller
        // - the number of client functional interfaces
        // - the number of server functional interfaces
        interfaceReferences = new Interface[1 +
            (componentParameters.getClientInterfaceTypes().length +
            componentParameters.getServerInterfaceTypes().length)];

        int i = 0;

        // add controllers
        interfaceReferences[i] = (Interface) this;
        i++;

        // add functional interfaces
        // functional interfaces are proxies on the corresponding meta-objects
        // 3. external functional interfaces
        InterfaceType[] interface_types = componentParameters.getComponentType()
                                                             .getFcInterfaceTypes();
        try {
            for (int j = 0; j < interface_types.length; j++) {
                Interface interface_reference = RepresentativeInterfaceClassGenerator.instance()
                                                                                     .generateInterface(interface_types[j].getFcItfName(),
                        this, interface_types[j], false);

                ((StubObject) interface_reference).setProxy(proxy);

                // all calls are to be reified
                interfaceReferences[i] = interface_reference;
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("cannot create interface references : " +
                e.getMessage());
        }
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.BindingController#lookupFc(String)}
     */
    public Object lookupFc(String clientItfName) {
        return (Interface) reifyCall(BindingController.class.getName(),
            "lookupFc", new Class[] { String.class },
            new Object[] { clientItfName });
    }

    /**
     *see {@link  BindingController#bindFc(java.lang.String, java.lang.Object)}
     */
    public void bindFc(String clientItfName, Object serverItf) {
        reifyCall(BindingController.class.getName(), "bindFc",
            new Class[] { String.class, Object.class },
            new Object[] { clientItfName, serverItf });
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.BindingController#unbindFc(String)}
     */
    public void unbindFc(String clientItfName) {
        reifyCall(BindingController.class.getName(), "unbindFc",
            new Class[] { String.class }, new Object[] { clientItfName });
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.LifeCycleController#getFcState()}
     */
    public String getFcState() {
        return (String) reifyCall(LifeCycleController.class.getName(),
            "getFcState", new Class[] {  }, new Object[] {  });
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.LifeCycleController#startFc()}
     */
    public void startFc() {
        reifyCall(LifeCycleController.class.getName(), "startFc",
            new Class[] {  }, new Object[] {  });
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.LifeCycleController#stopFc()}
     */
    public void stopFc() {
        reifyCall(LifeCycleController.class.getName(), "stopFc",
            new Class[] {  }, new Object[] {  });
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.ContentController#getFcInternalInterfaces()}
     */
    public Object[] getFcInternalInterfaces() {
        return (Object[]) reifyCall(ContentController.class.getName(),
            "getFcInternalInterfaces", new Class[] {  }, new Object[] {  });
    }

    /**
     * in this implementation, internal interfaces are also external interfaces.
     *see {@link  org.objectweb.fractal.api.control.ContentController#getFcInternalInterface(String)}
     */
    public Object getFcInternalInterface(String interfaceName)
        throws NoSuchInterfaceException {
        return getFcInterface(interfaceName);
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.ContentController#getFcSubComponents()}
     */
    public Component[] getFcSubComponents() {
        return (Component[]) reifyCall(ContentController.class.getName(),
            "getFcSubComponents", new Class[] {  }, new Object[] {  });
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.ContentController#addFcSubComponent(Component)}
     */
    public void addFcSubComponent(Component subComponent) {
        reifyCall(ContentController.class.getName(), "addFcSubComponent",
            new Class[] { Component.class }, new Object[] { subComponent });
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.ContentController#removeFcSubComponent(Component)}
     */
    public void removeFcSubComponent(Component subComponent) {
        reifyCall(ContentController.class.getName(), "removeFcSubComponent",
            new Class[] { Component.class }, new Object[] { subComponent });
    }

    /**
     *see {@link  org.objectweb.fractal.api.Interface#getFcItfOwner()}
     */
    public Component getFcItfOwner() {
        return (Component) reifyCall(Interface.class.getName(),
            "getFcItfOwner", new Class[] {  }, new Object[] {  });
    }

    /**
     *see {@link  org.objectweb.fractal.api.Interface#getFcItfName()}
     */
    public String getFcItfName() {
        // PB is that the current object implements several functional interfaces.
        // Thus it has several names...
        return null;
    }

    /**
     *see {@link  org.objectweb.fractal.api.Interface#getFcItfType()}
     */
    public Type getFcItfType() {
        return getFcType();
    }

    /**
     *see {@link  org.objectweb.fractal.api.Interface#isFcInternalItf()}
     */
    public boolean isFcInternalItf() {
        return false;
    }

    private Object reifyCall(String className, String methodName,
        Class[] parameterTypes, Object[] effectiveParameters) {
        try {
            return proxy.reify((MethodCall) MethodCall.getComponentMethodCall(
                    Class.forName(className).getDeclaredMethod(methodName,
                        parameterTypes), effectiveParameters, null));

            // functional interface name is null
        } catch (NoSuchMethodException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new ProActiveRuntimeException(e.toString());
        }
    }

    /**
     *see {@link  org.objectweb.fractal.api.Component#getFcInterface(String)}
     */
    public Object getFcInterface(String interfaceName)
        throws NoSuchInterfaceException {
        if (interfaceName.equals(Constants.BINDING_CONTROLLER) ||
                interfaceName.equals(Constants.CONTENT_CONTROLLER) ||
                interfaceName.equals(Constants.LIFECYCLE_CONTROLLER) ||
                interfaceName.equals(Constants.COMPONENT_PARAMETERS_CONTROLLER)) {
            return this;
        } else {
            for (int i = 0; i < interfaceReferences.length; i++) {
                if (interfaceReferences[i].getFcItfName() != null) {
                    if (interfaceReferences[i].getFcItfName().equals(interfaceName)) {
                        return interfaceReferences[i];
                    }
                }
            }
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    /**
     *see {@link  org.objectweb.fractal.api.Component#getFcInterfaces()}
     */
    public Object[] getFcInterfaces() {
        // TODO implementation
        throw new ProActiveRuntimeException("not yet implemented");
    }

    /**
     *see {@link  org.objectweb.fractal.api.Component#getFcType()}
     */
    public Type getFcType() {
        return componentType;
    }

    /**
     *see {@link  org.objectweb.proactive.core.mop.StubObject#getProxy()}
     */
    public Proxy getProxy() {
        return proxy;
    }

    /**
     *see {@link  org.objectweb.proactive.core.mop.StubObject#setProxy(Proxy)}
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     *  The comparison of component references is actually a comparison of unique
     * identifiers accross jvms.
     */
    public boolean equals(Object component) {
        if (!(component instanceof ProActiveComponent)) {
            logger.error(
                "can only compare proactive components to proactive components ");
            return false;
        }
        return getID().equals(((ProActiveComponent) component).getID());
    }

    public int hashCode() {
        // should be cached maybe
        return ((UniversalBodyProxy) getProxy()).getBodyID().hashCode();
    }

    public UniqueID getID() {
        return ((UniversalBodyProxy) getProxy()).getBodyID();
    }

    /**
     *see {@link  ComponentParametersController#setComponentParameters(ComponentParameters)}
     */
    public void setComponentParameters(ComponentParameters componentParameters) {
    	logger.error("only available in the meta-objects");
    }

    /**
     *see {@link  ComponentParametersController#getComponentParameters()}
     */
    public ComponentParameters getComponentParameters() {
        return (ComponentParameters) reifyCall(ComponentParametersController.class.getName(),
            "getComponentParameters", new Class[] {  }, new Object[] {  });
    }

    /**
     *see {@link  org.objectweb.fractal.api.control.BindingController#listFc()}
     */
    public String[] listFc() {
        return null;
    }
    
	/**
	 * see {@link org.objectweb.proactive.core.component.controller.ComponentParametersController#setComponentName(java.lang.String)}
	 */
	public void setComponentName(String componentName) {
		reifyCall(ComponentParametersController.class.getName(),
			"setComponentName", new Class[] {String.class}, new Object[] {componentName});

	}

	/**
	 * @see org.objectweb.proactive.core.component.identity.ProActiveComponent#getReferenceOnBaseObject()
	 */
	public Object getReferenceOnBaseObject() {
		return null;
	}

	/** (non-Javadoc)
	 * only available in the meta-objects
	 */
	public ComponentRequestQueue getRequestQueue() {
		logger.error("only available in the meta-objects");
		return null;
	}

}
