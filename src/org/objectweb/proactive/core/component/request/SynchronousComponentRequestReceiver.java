package org.objectweb.proactive.core.component.request;

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestReceiverImpl;
import org.objectweb.proactive.core.component.body.ComponentBody;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.IOException;


/**
 * This is an extension of the RequestReceiverImpl class, which allows the
 * shortcutting of functional requests : when crossing a composite component
 * that has such a request receiver, a shortcut notification is sent to the
 * emitter, and the request is directly transferred to the following linked
 * interface. This means that we stay in the rendez-vous until the request
 * reaches its final destination (a primitive component where the request can be
 * executed, or a component that does not have such a synchronous request
 * receiver).
 * 
 * @author Matthieu Morel
 */
public class SynchronousComponentRequestReceiver extends RequestReceiverImpl {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUEST);
    public final static int SHORTCUT = 1;

    public SynchronousComponentRequestReceiver() {
        super();
    }

    public int receiveRequest(Request r, Body bodyReceiver)
            throws IOException {
        if (r instanceof ComponentRequest) {
            if (!((ComponentRequest) r).isControllerRequest()) {

                if ("true".equals(System.getProperty("proactive.components.use_shortcuts"))) {
                    if (!((ComponentBody)bodyReceiver).getProActiveComponent().getInterceptors().isEmpty()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("shortcut is stopped in this component, because functional invocations are intercepted");
                        }
                        // no shortcut if there is an interception
                        return super.receiveRequest(r, bodyReceiver);
                    }
                    
                    ((ComponentRequest) r).shortcutNotification(r.getSender(),
                            bodyReceiver.getRemoteAdapter());

                    // TODO_M leave a ref of the shortcut
                    if (logger.isDebugEnabled()) {
                        logger.debug("directly executing request " +
                                r.getMethodCall().getName() +
                                ((r.getMethodCall().getComponentInterfaceName() != null)
                                ? (" on interface " +
                                r.getMethodCall().getComponentInterfaceName()) : ""));
                    }
                }
                bodyReceiver.serve(r);
                // TODO_M check with FT
                return SynchronousComponentRequestReceiver.SHORTCUT;
            }
        }
        // normal object invocations and controller requests are not subject to shortcuts
        return super.receiveRequest(r, bodyReceiver);
    }
}
