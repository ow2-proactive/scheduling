package modelisation.simulator.mixed.mixedwithcalendar;

import org.apache.log4j.Logger;

import modelisation.simulator.common.Averagator;


public class MultiqueueServer extends Server {
    static Logger logger = Logger.getLogger(MultiqueueServer.class.getName());
    protected RequestQueue[] requestQueueArray;
    protected int[] currentLocation;
    protected Source[] sourceArray;
    protected Averagator averagatorActiveQueue;
    protected Averagator[] averagatorMuForAgent;
    protected Averagator[] averagatorArrivalRate;
    protected int currentRequestQueueNumber = 0;

    public MultiqueueServer(Simulator s, int size) {
        super(s);
        this.requestQueueArray = new RequestQueue[size];
        this.currentLocation = new int[size];
        for (int i = 0; i < size; i++) {
            this.requestQueueArray[i] = new RequestQueue();
        }
        this.averagatorActiveQueue = new Averagator();
        this.averagatorMuForAgent = new Averagator[size];
        this.averagatorArrivalRate = new Averagator[size];
        for (int i = 0; i < size; i++) {
            this.averagatorMuForAgent[i] = new Averagator();
            this.averagatorArrivalRate[i] = new Averagator();
        }
    }

    public void setSourceArray(Source[] sa) {
        this.sourceArray = sa;
    }

    public void notifyEvent(String description) {
        this.timeNextEvent = this.remainingTime +
            this.simulator.getCurrentTime();
        this.simulator.addEvent(new Event(this.timeNextEvent, this, description));
    }

    /**
     * @see modelisation.simulator.mixed.Server#receiveRequestFromAgent(int, int)
     */
    public void receiveRequestFromAgent(int number, int id) {
        // super.receiveRequestFromAgent(number, id);
        if (logger.isDebugEnabled()) {
            logger.debug("Server.receiveRequestFromAgent " + id);
        }

        Request r = new Request(Request.AGENT, number, id,
                this.simulator.getCurrentTime());

        //        if (id == 0 ) {
        r.setCreationTime(this.simulator.getCurrentTime());
        //             if (log) {
        //        this.simulator.log("Server.receiveRequestFromAgent queue =" +
        //        this.requestQueueArray[id].length());
        //        }
        //
        //            System.out.println(" -> request created at  " + this.simulator.getCurrentTime());
        //        } else {
        //            r.setCreationTime(0);
        //        }
        this.requestQueueArray[id].addRequest(r);
        if (id == 0) {
            this.averagatorActiveQueue.add(this.getNonEmptyQueueNumber());
        }
        this.requestReceived(id);
    }

    /**
     * @see modelisation.simulator.mixed.Server#receiveRequestFromForwarder(int)
     */
    public void receiveRequestFromForwarder(int number, int id) {
        //        super.receiveRequestFromForwarder(number, id);
        //        System.out.println("XXXXX");
        if (logger.isDebugEnabled()) {
            logger.debug("Server.receiveRequestFromForwarder " + id);
        }
        this.requestQueueArray[id].addRequest(new Request(Request.AGENT,
                number, id, this.simulator.getCurrentTime()));
        this.requestReceived(id);
    }

    /**
     * @see modelisation.simulator.mixed.Server#receiveRequestFromSource()
     */
    public void receiveRequestFromSource(int id) {
        //        super.receiveRequestFromSource(id);
        if (logger.isDebugEnabled()) {
            logger.debug("Server.receiveRequestFromSource " + id);
        }
        this.requestQueueArray[id].addRequest(new Request(Request.SOURCE, 0,
                id, this.simulator.getCurrentTime()));
        this.requestReceived(id);
    }

    public void requestReceived(int id) {
        this.averagatorArrivalRate[id].add(1);
        this.requestReceived();
    }

    public void requestReceived() {
        this.averagatorActiveQueue.add(this.getNonEmptyQueueNumber());
        //        this.simulator.log("AQ " + this.getNonEmptyQueueNumber());
        if (logger.isDebugEnabled()) {
            logger.debug("Server.receiveRequest");
        }
        if (this.state == IDLE) {
            //            this.state = IDL_REQUEST;
            this.remainingTime = 0;
            this.notifyEvent("Request Received");
        }
    }

    public void serveNextRequest(double startTime) {
        this.requestQueue = this.getNextRequestQueueCyclic();
        this.startTime = startTime;
        this.currentRequest = this.requestQueue.removeRequestFromAgent();
        if (this.currentRequest != null) {
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
            this.notifyEvent("Serving Source");
            this.averagatorMu2.add(this.remainingTime);
            if (currentRequest.getSenderID() == 0) {
                this.averagatorWaitTimeSource0.add(startTime -
                    this.currentRequest.getCreationTime());
            }
        }
        this.averagatorUtilisation.add(this.remainingTime);
    }

    protected RequestQueue getNextRequestQueueCyclicEmptyFirst() {
        if (this.requestQueueArray[currentRequestQueueNumber].isEmpty()) {
            return this.getNextRequestQueueCyclic();
        } else {
            return this.requestQueueArray[currentRequestQueueNumber];
        }
    }

    protected RequestQueue getNextRequestQueueCyclic() {
        int index = 0;
        for (int i = 0; i < this.requestQueueArray.length; i++) {
            index = (i + currentRequestQueueNumber) % this.requestQueueArray.length;
            if (!this.requestQueueArray[index].isEmpty()) {
                this.currentRequestQueueNumber = index;
                return this.requestQueueArray[index];
            }
        }
        return null;
    }

    protected RequestQueue getNextRequestQueueOlderFirst() {
        double tmp = this.requestQueueArray[0].getYoungestCreationTime();
        int index = 0;
        for (int i = 1; i < this.requestQueueArray.length; i++) {
            if (!this.requestQueueArray[i].isEmpty()) {
                if ((tmp == 0) ||
                        (this.requestQueueArray[i].getYoungestCreationTime() < tmp)) {
                    tmp = this.requestQueueArray[i].getYoungestCreationTime();
                    index = i;
                }

                //            this.requestQueueArray[i];
            }
        }
        return this.requestQueueArray[index];
        //       return null;
    }

    public int getNonEmptyQueueNumber() {
        int tmp = 0;
        for (int i = 0; i < this.requestQueueArray.length; i++) {
            if (!this.requestQueueArray[i].isEmpty()) {
                tmp++;
            }
        }
        return tmp;
    }

    public void endOfService(double endTime) {
        //        this.log("Server.endOfService");
        if (this.state == SERVING_AGENT) {
            if (this.currentLocation[this.currentRequest.getSenderID()] < this.currentRequest.getNumber()) {
                this.currentLocation[this.currentRequest.getSenderID()] = this.currentRequest.getNumber();
                if (logger.isDebugEnabled()) {
                    logger.debug("MultiqueueServer: end of service for agent ");
                }
                if (this.currentRequest.getCreationTime() > 0) {
                    this.averagatorMuForAgent[this.currentRequest.getSenderID()].add(endTime -
                        this.currentRequest.getCreationTime() -
                        this.currentRequest.timeToSubstract);
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "MultiqueueServer: end of service for agent " +
                            this.currentRequest.getSenderID() + " lasted " +
                            (endTime - this.currentRequest.getCreationTime() -
                            this.currentRequest.timeToSubstract));
                        logger.debug("MultiqueueServer: known location " +
                            this.currentLocation[this.currentRequest.getSenderID()]);
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Server: ignoring request from agent");
                    logger.debug("CurrentLocation " + currentLocation +
                        " new " + this.currentRequest.getNumber());
                }
            }
            this.stateAfterService();
        } else {
            if (this.requestQueueArray[this.currentRequest.getSenderID()].hasRequestFromAgent()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Server.endOfService puting request in the queue");
                }
                this.requestQueueArray[this.currentRequest.getSenderID()].addRequest(this.currentRequest);
                this.stateAfterService();
            } else {
                this.sendReply();
            }
        }

        //               this.averagatorActiveQueue.add(this.getNonEmptyQueueNumber());
    }

    public void sendReply() {
        this.state = SENDING_REPLY;
        this.remainingTime = simulator.generateCommunicationTimeServer();
        this.notifyEvent("Send Reply");
        this.averagatorGamma2.add(this.remainingTime);
        this.averagatorUtilisation.add(this.remainingTime);

        Request r = this.requestQueueArray[0].getRequestFromAgent();
        if (r != null) {
            r.timeToSubstract += this.remainingTime;
        }
    }

    public void endOfSendReply(double endTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("SelectiveServer: time microtimer = " +
                ((endTime - startTime) * 1000) + " for method " +
                "searchObject");
            logger.debug("MultiqueueServer: last known position " +
                this.currentLocation[this.currentRequest.getSenderID()]);
        }

        //        this.state = stateAfterService();
        //        if (this.state == IDL_REQUEST) {
        //            this.serveNextRequest(endTime);
        //        }
        this.sourceArray[this.currentRequest.getSenderID()].receiveReplyFromServer(this.currentLocation[this.currentRequest.getSenderID()]);
        this.stateAfterService();
    }

    public void stateAfterService() {
        //   super.stateAfterService();
        this.state = Server.IDLE;
        //        System.out.println(this.getNonEmptyQueueNumber());
        if (this.getNonEmptyQueueNumber() != 0) {
            this.remainingTime = 0;
            this.notifyEvent("IDLE");
        }

        //        this.simulator.log("AQ " + this.getNonEmptyQueueNumber());
        //        this.averagatorActiveQueue.add(this.getNonEmptyQueueNumber());
    }

    public void end() {
        super.end();
        if (logger.isInfoEnabled()) {
            logger.info("* active queues " +
                this.averagatorActiveQueue.average());
            for (int i = 0; i < this.averagatorMuForAgent.length; i++) {
                logger.info("* mu for agent " + i + "  = " +
                    (1000 / this.averagatorMuForAgent[i].average()) + " " +
                    this.averagatorMuForAgent[i].getCount());
            }
            for (int i = 0; i < this.averagatorMuForAgent.length; i++) {
                logger.info("* arrival rate " + i + " " +
                    (this.averagatorArrivalRate[i].getCount() / this.simulator.getCurrentTime() * 1000));
            }
        }
    }

    public void update(double time) {
        //        if (this.remainingTime == 0) {
        //            //                    System.out.println("Server non empty requestQueue = " + this.getNonEmptyQueueNumber());
        //            this.requestQueue = this.getNextRequestQueue();
        //            super.update(time);
        //        }
        // if (this.remainingTime == 0) {
        switch (this.state) {
        case IDLE:
            if (getNonEmptyQueueNumber() == 0) {
                this.remainingTime = Double.MAX_VALUE;
                //   this.notifyEvent();
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

        // }
    }
}
