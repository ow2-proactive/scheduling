package org.objectweb.proactive.core.component.controller.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.body.ComponentBodyImpl;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.component.request.ComponentRequestImpl;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.SerializableMethod;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * This class orders requests arriving to gathercast interfaces into queues.
 * 
 * When a request on a gathercast interface arrives, it is put into a dedicated queue.
 * 
 * There is one list of queues (lazily created) for each method of each gathercast interface.
 * 
 *  Two requests originating from the same interface and addressed to the same method on the same gathercast interface
 *  are put into separate queues.
 *  
 *  Once all clients of a gathercast interface have sent a request, and if the timeout is not reached, a new request is created, which
 *  gathers the invocation parameters from the individual requests, and it is served on the 
 *   
 * @author Matthieu Morel
 *
 */
public class GatherRequestsQueues implements Serializable {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_GATHERCAST);
    // Map <serverItfName, map<signatureOfInvokedMethod, list<queuedRequests>>>
    Map<String, Map<SerializableMethod, List<GatherRequestsQueue>>> queues = new HashMap<String, Map<SerializableMethod, List<GatherRequestsQueue>>>();
    ProActiveComponent owner;
//    List<GatherFuturesHandler> futuresHandlers = new ArrayList<GatherFuturesHandler>();
    List<ItfID> gatherItfs = new ArrayList<ItfID>();
    ProActiveInterfaceType[] itfTypes;
    
    GatherFuturesHandlerPool gatherFuturesHandlerPool;
    
    /**
     * migrates all existing futures handlers
     * @param node destination node
     * @throws MigrationException if migration failed
     */
    public void migrateFuturesHandlersTo(Node node) throws MigrationException {
    	Set<String> itfNames = queues.keySet();
    	for (Iterator iter = itfNames.iterator(); iter.hasNext();) {
			String itfName = (String) iter.next();
			Map<SerializableMethod, List<GatherRequestsQueue>> queuesPerNamedItf = queues.get(itfName);
			Set<SerializableMethod> invokedMethods = queuesPerNamedItf.keySet();
			for (Iterator iterator = invokedMethods.iterator(); iterator
					.hasNext();) {
				SerializableMethod method = (SerializableMethod) iterator.next();
				List<GatherRequestsQueue> listOfQueues = queuesPerNamedItf.get(method);
				for (Iterator iterator2 = listOfQueues.iterator(); iterator
						.hasNext();) {
					GatherRequestsQueue queue = (GatherRequestsQueue) iterator.next();
					queue.migrateFuturesHandlerTo(node);
				}
				
			}
		}
    }

    public GatherRequestsQueues(ProActiveComponent owner) {
        this.owner = owner;
        InterfaceType[] untypedItfs = ((ComponentType)owner.getFcType()).getFcInterfaceTypes();
        itfTypes = new ProActiveInterfaceType[untypedItfs.length];
        for (int i = 0; i < itfTypes.length; i++) {
            itfTypes[i] = (ProActiveInterfaceType)untypedItfs[i];
        }

        for (int i = 0; i < itfTypes.length; i++) {
            if (ProActiveTypeFactory.GATHER_CARDINALITY.equals(
                        itfTypes[i].getFcCardinality())) {
                // add a queue for each gather itf
                Map<SerializableMethod, List<GatherRequestsQueue>> map = new HashMap<SerializableMethod, List<GatherRequestsQueue>>();
                queues.put(itfTypes[i].getFcItfName(), map);
                gatherItfs.add(new ItfID(itfTypes[i].getFcItfName(),
                        owner.getID()));
            }
        }
    }

    /**
     * Adds a request into the corresponding queue 
     */
    public Object addRequest(ComponentRequest r) throws ServeException {
        Object result = null;
        String serverItfName = r.getMethodCall().getComponentMetadata()
                                .getComponentInterfaceName();
        ItfID senderItfID = r.getMethodCall().getComponentMetadata()
                             .getSenderItfID();

        Method reifiedMethod = r.getMethodCall().getReifiedMethod();
        Method itfTypeMethod;
        try {
            itfTypeMethod = GatherBindingChecker.searchMatchingMethod(reifiedMethod,
                    Class.forName(getItfType(serverItfName).getFcItfSignature())
                         .getMethods(), false);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new ServeException("problem when analysing gather request", e1);
        }

        List<ItfID> connectedClientItfs;
        try {
            connectedClientItfs = Fractive.getGathercastController(owner)
                                          .getConnectedClientItfs(serverItfName);
        } catch (NoSuchInterfaceException e) {
            throw new ServeException("this component has no binding controller");
        }
        if (!connectedClientItfs.contains(senderItfID)) {
            throw new ServeException(
                "cannot handle gather invocation : this invocation orginates from a client interface which is not bound ");
        }

        if (!queues.containsKey(serverItfName)) {
            throw new ProActiveRuntimeException(
                "there is no gathercast interface named " + serverItfName);
        }

        Map<SerializableMethod, List<GatherRequestsQueue>> map = queues.get(serverItfName);

        // SerializableMethod objects are used as keys
        List<GatherRequestsQueue> list = map.get(new SerializableMethod(itfTypeMethod));

        if (list == null) {
            list = new ArrayList<GatherRequestsQueue>();
            // new queue, and add current request
            GatherRequestsQueue queue = new GatherRequestsQueue(owner,
                    serverItfName, itfTypeMethod, connectedClientItfs, gatherFuturesHandlerPool);
            list.add(queue);
            map.put(new SerializableMethod(itfTypeMethod), list);
        }

        if (list.isEmpty()) {
            GatherRequestsQueue queue = new GatherRequestsQueue(owner,
                    serverItfName, itfTypeMethod, connectedClientItfs, gatherFuturesHandlerPool);
            map.get(new SerializableMethod(itfTypeMethod)).add(queue);
        } 
        

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            GatherRequestsQueue queue = (GatherRequestsQueue) iter.next();
            if (queue.containsRequestFrom(senderItfID)) {
                // there is already a request from this comp/itf
                if (!iter.hasNext()) {
                    // no other queue to receive this request. create one
                    // concurrent access exception?
                    queue = new GatherRequestsQueue(owner, serverItfName,
                            itfTypeMethod, connectedClientItfs, gatherFuturesHandlerPool);
                    // add the request
                    result = queue.put(senderItfID, r);
                    list.add(queue);
                    break;
                }
                continue;
            }
            // TODO if request is synchronous : put in threaded queue, notify, then put the thread on sleep and serve next request 

            // add this request
            result = queue.put(senderItfID, r);
            break;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("added request [" + r.getMethodName() +
                "] in gather queue");
        }
        // check if needs to do something!
        notifyUpdate(serverItfName, list);

        return result;
    }

    private void notifyUpdate(String serverItfName,
        List<GatherRequestsQueue> requestQueues) throws ServeException {

        // default: if all connected itfs have sent a request, then process it
        try {
            List<ItfID> connectedClientItfs = Fractive.getGathercastController(owner)
                                                      .getConnectedClientItfs(serverItfName);
            GatherRequestsQueue firstRequestsInLine = requestQueues.get(0); // need to ensure this
            if (firstRequestsInLine.isFull()) {
                // ok, condition met, proceed with request

                // create a new gather request by gathering parameters
                Method clientMethod = firstRequestsInLine.getInvokedMethod();
                String methodName = clientMethod.getName();
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "conditions reached, processing gather request [" +
                        methodName + "]");
                }

                Class[] clientMethodParamTypes = clientMethod.getParameterTypes();
                Class[] gatherMethodParamTypes = new Class[clientMethodParamTypes.length];

                for (int i = 0; i < clientMethodParamTypes.length; i++) {
                    gatherMethodParamTypes[i] = List.class;
                }

                Class gatherItfClass = Class.forName(((ProActiveInterfaceType) ((ProActiveInterface) owner.getFcInterface(
                            serverItfName)).getFcItfType()).getFcItfSignature());

                Method gatherMethod = gatherItfClass.getMethod(clientMethod.getName(),
                        gatherMethodParamTypes);
                Object[] gatherEffectiveArguments = new Object[gatherMethodParamTypes.length];

                // build the list of parameters
                for (int i = 0; i < gatherEffectiveArguments.length; i++) {
                    List<Object> l = new ArrayList<Object>(connectedClientItfs.size());
                    for (Iterator iter = connectedClientItfs.iterator();
                            iter.hasNext();) {
                        ItfID id = (ItfID) iter.next();
                        // keep same ordering as connected client itfs
                        l.add(firstRequestsInLine.get(id).getMethodCall()
                                                 .getEffectiveArguments()[i]);
                    }
                    // parameters from a given client have the same order than this client in the list of connected clients 
                    gatherEffectiveArguments[i] = l;
                }

                // create the request
                MethodCall gatherMC = MethodCall.getComponentMethodCall(gatherMethod,
                        gatherEffectiveArguments, serverItfName,
                        new ItfID(serverItfName, owner.getID()));
                long sequenceID = ((ComponentBodyImpl) ProActive.getBodyOnThis()).getNextSequenceID();
                
                ComponentRequest gatherRequest = new ComponentRequestImpl(gatherMC,
                        ProActive.getBodyOnThis(),
                        firstRequestsInLine.oneWayMethods(),
                        sequenceID);

                // serve the request (do not reenqueue it)
                if (logger.isDebugEnabled()) {
					logger.debug("gather request queues .serving request [" + gatherRequest.getMethodName()+ "]");
				}
                Reply reply = gatherRequest.serve(ProActive.getBodyOnThis());

                // handle the future for async invocations
                if (reply != null) {
                	reply.getResult().getResult();
                    firstRequestsInLine.addFutureForGatheredRequest(reply.getResult());
                }

                // remove the list that was just used
                GatherRequestsQueue queue = requestQueues.remove(0);
                queue= null;
            }
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private ProActiveInterfaceType getItfType(String name) {
        for (int i = 0; i < itfTypes.length; i++) {
            if (name.equals(itfTypes[i].getFcItfName())) {
                return itfTypes[i];
            }
        }
        return null;
    }
    
//    protected Object reifyAsAsynchronous(MethodCall methodCall)
//    throws Exception, RenegotiateSessionException {
//    StubObject futureobject = null;
//
//    // Creates a stub + FutureProxy for representing the result
//    try {
//        Class returnType = methodCall.getReifiedMethod().getReturnType();
//
//        if (returnType.equals(java.lang.Void.TYPE)) {
//
//            /* A future for a void call is used to put the potential exception inside */
//            futureobject = (StubObject) MOP.newInstance(VoidFuture.class,
//                    null, Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
//        } else {
//            futureobject = (StubObject) MOP.newInstance(returnType, null,
//                    Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
//        }
//    } catch (MOPException e) {
//        // Create a non functional exception encapsulating the network exception
//        ProxyNonFunctionalException nfe = new FutureCreationException(
//                "Exception occured in reifyAsAsynchronous while creating future for methodcall = " +
//                methodCall.getName(), e);
//        e.printStackTrace();
////        NFEManager.fireNFE(nfe, ProActive.getBodyOnThis().getRemoteAdapter());
//    } catch (ClassNotFoundException e) {
//        // Create a non functional exception encapsulating the network exception
//        ProxyNonFunctionalException nfe = new FutureCreationException(
//                "Exception occured in reifyAsAsynchronous while creating future for methodcall = " +
//                methodCall.getName(), e);
//        e.printStackTrace();
////        NFEManager.fireNFE(nfe, this);
//    }
//
//    // Set the id of the body creator in the created future
//    FutureProxy fp = (FutureProxy) (futureobject.getProxy());
//    fp.setCreatorID(owner.getID());
//    fp.setOriginatingProxy((AbstractProxy)ProActive.getStubOnThis().getProxy());
//
//    try {
//        ProActive.getBodyOnThis().sendRequest(methodCall, fp, );
//    } catch (java.io.IOException e) {
//        // old stuff
//        // throw new MethodCallExecutionFailedException("Exception occured in reifyAsAsynchronous while sending request for methodcall ="+methodCall.getName(), e);
//        // Create a non functional exception encapsulating the network exception
//        ProxyNonFunctionalException nfe = new SendRequestCommunicationException(
//                "Exception occured in reifyAsAsynchronous while sending request for methodcall = " +
//                methodCall.getName(), e);
//
//        NFEManager.fireNFE(nfe, this);
//    }
//
//    // And return the future object
//    return futureobject;
//}


    
 
}
