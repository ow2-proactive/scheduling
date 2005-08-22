package org.objectweb.proactive.core.exceptions.manager;

import java.lang.reflect.Method;

import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallMetadata;


public class ExceptionHandler {

    /* Called by the user */
    public static void tryWithCatch(Class[] exceptions) {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForIntersection(exceptions);
            stack.push(exceptions);
        }
    }

    public static void throwArrivedException() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.throwArrivedException();
        }
    }

    public static void waitForPotentialException() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForPotentialException(true);
        }
    }

    public static void endTryWithCatch() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForPotentialException(false);
            stack.pop();
        }
    }

    public static void removeTryWithCatch() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.fixupPop();
        }
    }

    /* Called by ProActive on the client side */
    public static void addRequest(MethodCall methodCall, FutureProxy future) {
        MethodCallMetadata metadata = methodCall.getMetadata();
        if (metadata.isExceptionAsynchronously() ||
                metadata.isRuntimeExceptionHandled()) {
            ExceptionMaskStack stack = ExceptionMaskStack.get();
            synchronized (stack) {
                Method m = methodCall.getReifiedMethod();
                ExceptionMaskLevel level = stack.findBestLevel(m.getExceptionTypes());
                level.addFuture(future);
            }
        }
    }

    public static void addResult(FutureProxy future) {
        ExceptionMaskLevel level = future.getExceptionLevel();
        if (level != null) {
            level.removeFuture(future);
        }
    }

    public static MethodCallMetadata getMetadataForCall(Method m) {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            boolean runtime = stack.isRuntimeExceptionHandled();
            boolean async = stack.isCaught(m.getExceptionTypes());
            MethodCallMetadata res = new MethodCallMetadata(runtime, async);

            //            System.out.println(m + " => " + res);
            return res;
        }
    }

    public static void throwException(Throwable exception) {
        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }

        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            if (!stack.isCaught(exception.getClass())) {
                RuntimeException re = new IllegalStateException(
                        "Invalid Future Usage");
                re.initCause(exception);
                throw re;
            }

            ExceptionThrower.throwException(exception);
        }
    }
}
