package org.objectweb.proactive.core.component.controller.util;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.exceptions.manager.ExceptionThrower;
import org.objectweb.proactive.core.node.Node;


public class GatherFuturesHandler implements RunActive, Serializable {
    List<ItfID> senders;
    List<?> resultOfGatheredInvocation;
    Throwable exceptionToRaise;
    String methodName = null;
    int step = 0;
    
    public GatherFuturesHandler() {
    }
  
    public void setFutureOfGatheredInvocation(FutureResult future) {
//            	System.out.println("[gather futures handler] setFutureOfGatheredInvocation");
        if (future.getExceptionToRaise() != null) {
            exceptionToRaise = future.getExceptionToRaise();
        } else {
            // no cast for futures ==> need to get the result before casting
            resultOfGatheredInvocation = (List<?>) future.getResult();
            ProActive.waitFor(resultOfGatheredInvocation);
        }
    }

    // returns
    public Object distribute(ItfID sender) {
        //    	System.out.println(" distribute to " + sender.getComponentBodyID());
        if (exceptionToRaise != null) {
            ExceptionThrower.throwException(exceptionToRaise); // guillaume's exception thrower
        }

        // APPLY REDISTRIBUTION POLICY HERE !
        return resultOfGatheredInvocation.get(senders.indexOf(sender));
    }

    public void migrateTo(Node node) throws MigrationException {
        //        System.out.println("gather futures handler migrating to " + node);
        ProActive.migrateTo(node);
    }

    public void setConnectedClientItfs(List<ItfID> connectedClientItfs) {
        senders = connectedClientItfs;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public void passivate() {
        senders = null;
        methodName = null;
    }
    
    
    

    // TODO a migration-compatible activity (see below)
    public void runActivity(Body body) {
        //	System.out.println("\nFUTURES HANDLER ID IS " + ProActive.getBodyOnThis().getID().getUID());
        int incarnation = 0;
        Service service = new Service(body);
        while (ProActive.getBodyOnThis().isActive()) {
            service.blockingServeOldest("setConnectedClientItfs");
//        	if (incarnation>0) {
//        		System.out.println("future handler pooled for the " + incarnation + "th time");
//        	}
            
            service.blockingServeOldest("setFutureOfGatheredInvocation");
            int i = 1;
            for (ItfID senderID : senders) {
                service.blockingServeOldest("distribute");
                i++;
            }
            
            
            service.blockingServeOldest("passivate");
            
//            incarnation++;
            
//        }
        }
    }

    public static class MigrateOrSetFuturesRequestFilterImpl
        implements RequestFilter, java.io.Serializable {
        public MigrateOrSetFuturesRequestFilterImpl() {
        }

        public boolean acceptRequest(Request request) {
            return ("migrateTo".equals(request.getMethodName()) ||
            "setFutureOfGatheredInvocation".equals(request.getMethodName()));
        }
    }

    public static class MigrateOrDistributeRequestFilterImpl
        implements RequestFilter, java.io.Serializable {
        public MigrateOrDistributeRequestFilterImpl() {
        }

        public boolean acceptRequest(Request request) {
            return ("distribute".equals(request.getMethodName()) ||
            "setFutureOfGatheredInvocation".equals(request.getMethodName()));
        }
    }
}
