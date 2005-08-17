package org.objectweb.proactive.core.component.representative;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * This class acts as a proxy between a representative interface and the actual
 * destination of the invocation. It therefore allows the creation of shortcuts :
 * functional requests can cross membranes of composites and directly go to the
 * target primitive component that is able to handle the request.
 * <p>
 * This feature is especially useful in the context of distributed systems,
 * where hierarchies can be quite complex and composite components distributed
 * over many different and remote sites.
 * <p>
 * The implementation of the shorcuts is based on tensioning : the first
 * functional request on an interface goes all the way through possible
 * intermediate composite components, and when it reaches its final destination,
 * which is the primitive component that holds the business code for handling
 * this functional request, the original sender of the request receives
 * a notification of the shortcut which can be realized. The following
 * requests on the same interface will then go directly to the target primitive
 * component, without having to cross possible intermediate composite
 * components. Note that <b>the tensioning is performed during the rendez-vous
 * </b>, which guarantees causal dependency.
 * 
 * @author Matthieu Morel
 */
public class FunctionalInterfaceProxyImpl implements FunctionalInterfaceProxy, Serializable, Cloneable {

    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUEST);

    private static Field universalBodyField;
    private static Field bodyIDField;

    Proxy bodyProxyDelegatee = null;
    Proxy nonShortcutProxy = null;
    String fcItfName; // name of the functional interface

    static {
        try {
        universalBodyField = UniversalBodyProxy.class.getDeclaredField("universalBody");
        universalBodyField.setAccessible(true);
        bodyIDField = UniversalBodyProxy.class.getSuperclass().getDeclaredField("bodyID");
        bodyIDField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }


    }

    private FunctionalInterfaceProxyImpl() {}

    public FunctionalInterfaceProxyImpl(Proxy bodyProxyDelegatee, String fcItfName) {
        nonShortcutProxy = this.bodyProxyDelegatee = bodyProxyDelegatee;
        this.fcItfName = fcItfName;
    }

    private void changeRefOnBody(UniversalBody body) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("changing reference on body");
            }
            UniversalBodyProxy proxy = (UniversalBodyProxy)UniversalBodyProxy.class.newInstance();
            universalBodyField.set(proxy, body);
            bodyIDField.set(proxy, body.getID());
            bodyProxyDelegatee=proxy;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new ProActiveRuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ProActiveRuntimeException(e);
        }
    }

    public Object reify(MethodCall c) throws Throwable {
        // check shortcut by asking source body (LocalBodyStore.currentThreadBody)
        // if shortcut : change ref on Body
        UniversalBody newDestinationBody = LocalBodyStore.getInstance().getCurrentThreadBody().getShortcutTargetBody(new FunctionalInterfaceID(c.getComponentInterfaceName(), ((UniversalBodyProxy)bodyProxyDelegatee).getBody().getID()));
        if (newDestinationBody != null) {
            changeRefOnBody(newDestinationBody);
        }
        return bodyProxyDelegatee.reify(c);
    }

    public void setBodyProxy(Proxy proxy) {
        bodyProxyDelegatee = proxy;
    }

    public Proxy getBodyProxy() {
        return bodyProxyDelegatee;
    }

//    public String getHandlerizableInfo() throws IOException {
//        return null;
//    }
//
//    public HashMap getHandlersLevel() throws IOException {
//        return null;
//    }
//
//    public void clearHandlersLevel() throws IOException {
//    }
//
//    public void setExceptionHandler(Handler handler, Class exception) throws IOException {
//    }
//
//    public Handler unsetExceptionHandler(Class exception) throws IOException {
//        return null;
//    }
}
