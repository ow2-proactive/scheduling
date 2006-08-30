package org.objectweb.proactive.core.component.gen;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

public class GatherItfAdapterProxy implements InvocationHandler, Serializable {
    
        private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_GATHERCAST);
    
        Object delegatee = null;
        
        
        public GatherItfAdapterProxy(Object delegatee) {
            this.delegatee = delegatee;
        }
        
    
        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("gather proxy trying to invoke method : " + method.toGenericString());
                }
                
//                Method serverMethod = GatherBindingChecker.searchMatchingMethod(method, delegatee.getClass().getMethods());
                // need to change invocation parameters
            Object result = delegatee.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(delegatee, args);
            return result;
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            }
        
        }

        
}
