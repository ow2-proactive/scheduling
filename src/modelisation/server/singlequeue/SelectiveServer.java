package modelisation.server.singlequeue;


import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestQueue;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ext.util.SimpleLocationServer;
import util.timer.MicroTimer;

public class SelectiveServer extends SimpleLocationServer implements org.objectweb.proactive.RunActive {


    protected MicroTimer microtimer;
    //the request of the source to put back in the request line
    //we need to do this because of the request processor and
    //a concurrent access exception
    protected Request sourceRequest;

    public SelectiveServer() {
    }

    public SelectiveServer(String url) {
        super(url);
        this.microtimer = new MicroTimer();
    }

    /**
     * First register with the specified url
     * Then wait for request
     */
    public void runActivity(Body body) {
        Request request = null;
        System.out.println("SelectiveServer.live");
        this.register();
        BlockingRequestQueue queue = body.getRequestQueue();

        //  SelectiveRequestProcessor selectiveProcessor =
        //              new SelectiveRequestProcessor(queue, body);
        while (body.isActive()) {
            try {
                queue.waitForRequest();
//                System.out.println("XXXX SelectiveServer.live requests available" );
//                 System.out.println(queue);
                //   queue.processRequests(selectiveProcessor);
                microtimer.start();
                this.serve(body, this.getNexRequestAndFlush(queue));
//                if (this.sourceRequest != null) {
//                    queue.add(sourceRequest);
//                    this.sourceRequest = null;
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void serve(Body body, Request request) {
        Reply reply = null;

        try {
            reply = request.serve(body);
            this.processReply(body, request, reply);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void processReply(Body body, Request request, Reply reply) {
        long startTime;
        long endTime;
        if ("searchObject".equals(request.getMethodName())) {
            if (sendAnswer(body, "updateLocation", request)) {
                //sender.receiveReply(reply);
            } else {
                try {
                    System.out.println("SelectiveServer: puting the request back in the line");
                    body.getRequestQueue().add(request);
                    //this.sourceRequest = request;
                    reply = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        microtimer.stop();
        System.out.println("SelectiveServer: time microtimer = " +
                           microtimer.getCumulatedTime() + " for method "
                           + request.getMethodName());
        if (reply != null) {
            startTime = System.currentTimeMillis();
            try {
                reply.send(request.getSender());
            } catch (Exception e) {
                e.printStackTrace();
            }
            endTime = System.currentTimeMillis();
            System.out.println("SelectiveServer:  .............. done  = "
                               + (endTime - startTime) + " for method "
                               + request.getMethodName());

        }

        //   System.out.println("SelectiveServer: there were " + body.getRequestQueue().size() +
        //                      "  methods in " + ((CompositeRequestQueue) body.getRequestQueue()).getQueueCount() + " queue ");
    }


    protected boolean sendAnswer(Body body, String name, Request r) {
        return (body.getRequestQueue().getOldest(name) == null);
    }

    protected Request getNexRequestAndFlush(RequestQueue queue) {
        System.out.println("SelectiveServer.getNexRequestAndFlush");
        Request requestToServe;
        synchronized (queue) {
            requestToServe = queue.removeOldest("updateLocation");
            if (requestToServe != null) {
                //remove all other requests
                while (queue.removeOldest("updateLocation") != null) ;
            } else {
                requestToServe = queue.removeOldest("searchObject");
            }
            return requestToServe;
        }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("usage: modelisation.SelectiveServer <server url> [node]");
            System.exit(-1);
        }
        Object arg[] = new Object[1];
        arg[0] = args[0];
        SelectiveServer server = null;
        
        
    

        
        try {
            if (args.length == 2){
            System.out.println("Creating server on node " + args[1]);
                server = (SelectiveServer) ProActive.newActive(SelectiveServer.class.getName(), arg, NodeFactory.getNode(args[1]));
            }
            else
                server = (SelectiveServer) ProActive.newActive(SelectiveServer.class.getName(), arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    protected class SelectiveRequestProcessor implements RequestProcessor {
//        int counter, numberOfRequests;
//        RequestQueue requestQueue;
//        Request requestSource, requestAgent;
//        Body body;
//
//        public SelectiveRequestProcessor(RequestQueue requestQueue, Body body) {
//            this.requestQueue = requestQueue;
//            this.body = body;
//        }
//
//        public boolean processRequest(Request request) {
//            if (counter == 0) {
//                // first call
//                numberOfRequests = requestQueue.size();
//                requestSource = null;
//                requestAgent = null;
//            }
//            counter++;
//            if (request.getMethodName().equals("updateLocation")) {
//                requestAgent = request;
//            }
//            if (request.getMethodName().equals("searchObject")) {
//                requestSource = request;
//            }
//            if (counter == numberOfRequests) {
////                System.out.println("SelectiveRequestProcessor.processRequest " +
////                                   " requestAgent = " + requestAgent + " requestSource = " +
////                                   requestSource);
//                if (requestAgent != null) {
//                    serve(body, requestAgent);
//                }
//                if (requestSource != null) {
//                    serve(body, requestSource);
//                }
//                this.counter = 0;
//            }
//            return true;
//        }
//    }
}
