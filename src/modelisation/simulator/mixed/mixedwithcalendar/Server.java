package modelisation.simulator.mixed.mixedwithcalendar;

import org.apache.log4j.Logger;

import modelisation.simulator.common.Averagator;
import modelisation.simulator.common.SimulatorElement;


public class Server extends SimulatorElement {
    static Logger logger = Logger.getLogger(Server.class.getName());
    public final static int IDLE = 1;
    public final static int IDL_REQUEST = 2;
    public final static int SENDING_REPLY = 4;
    public final static int SERVING_SOURCE = 5;
    public final static int SERVING_AGENT = 6;
    protected Simulator simulator;
    protected RequestQueue requestQueue;
    protected Source source;
    protected int currentLocation;
    protected Request currentRequest;
    protected double startTime;
    protected Averagator averagatorMu1;
    protected Averagator averagatorMu2;
    protected Averagator averagatorUtilisation;
    protected Averagator averagatorWaitTimeSource0;
    protected Averagator averagatorGamma2;

    public Server(Simulator s) {
        this.state = IDLE;
        this.simulator = s;
        this.requestQueue = new RequestQueue();
        this.averagatorMu1 = new Averagator();
        this.averagatorMu2 = new Averagator();
        this.averagatorUtilisation = new Averagator();
        this.averagatorWaitTimeSource0 = new Averagator();
        this.averagatorGamma2 = new Averagator();
    }

    public void setSource(Source s) {
        this.source = s;
    }

    public void notifyEvent(String description) {
        this.timeNextEvent = this.remainingTime +
            this.simulator.getCurrentTime();
        this.simulator.addEvent(new Event(this.timeNextEvent, this, description));
    }

    public void update(double time) {
        //  if (this.remainingTime == 0) {
        switch (this.state) {
        case IDLE:
            if (this.requestQueue.isEmpty()) {
                // this.remainingTime = 500000;
            } else {
                this.serveNextRequest(time);
            }
            break;
        case SERVING_SOURCE:
        case SERVING_AGENT:
            this.endOfService(time);
            break;
        case SENDING_REPLY:
            this.endOfSendReply(time);
            break;
        }

        //  }
    }

    protected void requestReceived() {
        if (logger.isDebugEnabled()) {
            logger.debug("Server.receiveRequest");
        }
        if (this.state == IDLE) {
            //            this.state = IDL_REQUEST;
            this.remainingTime = 0;
            this.notifyEvent("Receive Request");
        }
    }

    public synchronized void receiveRequestFromForwarder(int number, int id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Server.receiveRequestFromForwarder");
        }

        //       System.out.println("XXXXX");
        this.requestQueue.addRequest(new Request(Request.AGENT, number));
        this.requestReceived();
        //        this.messageFromAgent = true;
    }

    public synchronized void receiveRequestFromAgent(int number, int id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Server.receiveRequestFromAgent");
        }

        Request r = new Request(Request.AGENT, number, id);
        this.requestQueue.addRequest(r);
        this.requestReceived();
        //        this.messageFromAgent = true;
    }

    public synchronized void receiveRequestFromSource(int id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Server.receiveRequestFromSource");
        }

        Request r = new Request(Request.SOURCE, 0);
        this.requestQueue.addRequest(r);
        this.requestReceived();
        if (id == 0) {
            r.setCreationTime(this.simulator.getCurrentTime());
            //           this.averagatorWaitTimeSource0
        }

        //        this.messageFromSource = true;
    }

    public void serveNextRequest(double startTime) {
        this.startTime = startTime;
        this.currentRequest = this.requestQueue.removeRequestFromAgent();
        if (this.currentRequest != null) {
            //serving request from agent
            //            if (this.id == 0) {
            //                //            r.setCreationTime(this.simulator.getCurrentTime());
            //                System.out.println(
            //                        " -> request served  at  " +
            //                        this.simulator.getCurrentTime());
            //            }
            this.state = SERVING_AGENT;
            this.remainingTime = simulator.generateServiceTimeMu1();
            this.notifyEvent("Serving Agent");
            if (logger.isDebugEnabled()) {
                logger.debug("Server.serveNextRequest will last " +
                    this.remainingTime);
                logger.debug("getNextRequestQueueFifo: the request waited " +
                    (this.simulator.getCurrentTime() -
                    this.currentRequest.getCreationTime()));
            }
            this.averagatorMu1.add(this.remainingTime);
        } else {
            this.currentRequest = this.requestQueue.removeRequestFromSource();
            this.state = SERVING_SOURCE;
            this.remainingTime = simulator.generateServiceTimeMu2();
            this.notifyEvent("Serving Queue");
            this.averagatorMu2.add(this.remainingTime);
            if (currentRequest.getSenderID() == 0) {
                this.averagatorWaitTimeSource0.add(startTime -
                    this.currentRequest.getCreationTime());
            }
        }
        this.averagatorUtilisation.add(this.remainingTime);
    }

    public void endOfService(double endTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("Server.endOfService");
        }
        if (this.state == SERVING_AGENT) {
            if (this.currentLocation < this.currentRequest.getNumber()) {
                this.currentLocation = this.currentRequest.getNumber();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Server: ignoring request from agent");
                    logger.debug("CurrentLocation " + currentLocation +
                        " new " + this.currentRequest.getNumber());
                }
            }
            this.stateAfterService();
        } else {
            if (this.requestQueue.hasRequestFromAgent()) {
                //we put the request back in the queue
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Server.endOfService puting request in the queue");
                }
                this.currentRequest.setCreationTime(this.simulator.getCurrentTime());
                this.requestQueue.addRequest(this.currentRequest);
                this.stateAfterService();
            } else {
                this.sendReply();
            }
        }
    }

    public void stateAfterService() {
        this.state = Server.IDLE;
        this.remainingTime = 0;
        this.notifyEvent("IDLE");
    }

    public void sendReply() {
        this.state = SENDING_REPLY;
        this.remainingTime = simulator.generateCommunicationTimeServer();
        this.notifyEvent("Send Reply");
        this.averagatorGamma2.add(this.remainingTime);
        this.averagatorUtilisation.add(this.remainingTime);
    }

    public void endOfSendReply(double endTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("SelectiveServer: time microtimer = " +
                ((endTime - startTime) * 1000) + " for method " +
                "searchObject");
        }

        //        this.state = stateAfterService();
        //        if (this.state == IDL_REQUEST) {
        //            this.serveNextRequest(endTime);
        //        }
        this.source.receiveReplyFromServer(this.currentLocation);
        this.stateAfterService();
    }

    public void end() {
        if (logger.isInfoEnabled()) {
            logger.info("########## Server ##################");
            logger.info("* mu1 = " + (1000 / this.averagatorMu1.average()) +
                " " + this.averagatorMu1.getCount());
            logger.info("* mu2  = " + (1000 / this.averagatorMu2.average()) +
                " " + this.averagatorMu2.getCount());
            logger.info(" * utilisation = " +
                (this.averagatorUtilisation.getTotal() / this.simulator.getCurrentTime()));
            logger.info(" * waittimeSource0 = " +
                this.averagatorWaitTimeSource0.average());
            logger.info(" * gamma2 server = " +
                (1000 / this.averagatorGamma2.average()));
        }
    }

    public String getName() {
        return "Server" + this.id;
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer();
        switch (this.state) {
        case IDLE:
            tmp.append("IDLE ");
            break;
        case IDL_REQUEST:
            tmp.append("IDL_REQUEST ");
            //                if (this.messageFromSource) {
            //                    tmp.append("messageFromSource ");
            //                }
            //                if (this.messageFromAgent) {
            //                    tmp.append("messageFromAgent ");
            //                }
            //                return tmp.toString();
            break;
        case SERVING_SOURCE:
            //                if (this.messageFromAgent) {
            //                    return "SERVING_SOURCE messageFromAgent ";
            //                } else {
            //                    return "SERVING_SOURCE ";
            //                }
            tmp.append("SERVING_SOURCE ");
            break;
        case SERVING_AGENT:
            tmp.append("SERVING_AGENT ");
            //                if (this.messageFromSource) {
            //                    tmp.append("messageFromSource ");
            //                }
            //                if (this.messageFromAgent) {
            //                    tmp.append("messageFromAgent ");
            //                }
            //                return tmp.toString();
            break;
        case SENDING_REPLY:
            tmp.append("SENDING_REPLY ");
            break;

            //                if (this.messageFromSource) {
            //                    tmp.append("messageFromSource ");
            //                }
            //                if (this.messageFromAgent) {
            //                    tmp.append("messageFromAgent ");
            //                }
            //                return tmp.toString();
        }
        tmp.append(" ").append(this.requestQueue);
        tmp.append(" remaining time = ").append(this.remainingTime);
        return tmp.toString();
    }
    //    public static void main(String[] args) {
    //        Simulator s = new Simulator(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    //        Server server = new Server(s);
    //        System.out.println("State of server: " + server);
    //        server.receiveRequestFromSource(0);
    //        server.receiveRequestFromAgent(1, 0);
    //        System.out.println("State of server: " + server);
    //        server.setRemainingTime(0);
    //        server.update(100);
    //        System.out.println("State of server: " + server);
    //        server.setRemainingTime(0);
    //        server.update(100);
    //        System.out.println("State of server: " + server);
    //        server.setRemainingTime(0);
    //        server.update(100);
    //        System.out.println("State of server: " + server);
    //    }
}
