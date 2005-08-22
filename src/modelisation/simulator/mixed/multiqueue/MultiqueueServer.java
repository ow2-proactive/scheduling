package modelisation.simulator.mixed.multiqueue;

import modelisation.simulator.common.Averagator;
import modelisation.simulator.mixed.Request;
import modelisation.simulator.mixed.RequestQueue;
import modelisation.simulator.mixed.Server;
import modelisation.simulator.mixed.Simulator;
import modelisation.simulator.mixed.Source;


public class MultiqueueServer extends Server {
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

    //    public void log(String s) {
    //        if (log) {
    //            this.simulator.log(s);
    //        }
    //    }
    public void setSourceArray(Source[] sa) {
        this.sourceArray = sa;
    }

    //    /**
    //     * @see modelisation.simulator.mixed.Server#receiveRequest()
    //     */
    //    protected void requestReceived() {
    //        super.requestReceived();
    //    }

    /**
     * @see modelisation.simulator.mixed.Server#receiveRequestFromAgent(int, int)
     */
    public void receiveRequestFromAgent(int number, int id) {
        // super.receiveRequestFromAgent(number, id);
        if (log) {
            this.simulator.log("Server.receiveRequestFromAgent " + id);
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
        if (log) {
            this.simulator.log("Server.receiveRequestFromForwarder " + id);
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
        if (log) {
            this.simulator.log("Server.receiveRequestFromSource " + id);
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
        super.requestReceived();
    }

    public void serveNextRequest(double startTime) {
        //        System.out.println(
        //                "Server non empty requestQueue = " +
        //                this.getNonEmptyQueueNumber() + " time " + startTime);
        //        this.requestQueue = this.getNextRequestQueueOlderFirst();
        this.requestQueue = this.getNextRequestQueueCyclic();
        //  this.requestQueue=this.getNextRequestQueueCyclicEmptyFirst();
        //        this.averagatorActiveQueue.add(this.getNonEmptyQueueNumber());
        //        this.currentRequest =
        super.serveNextRequest(startTime);
        //        this.startTime = startTime;
        //        //        this.currentRequest = this.requestQueue.removeRequestFromAgent();
        //        this.currentRequest = this.getNextRequest();
        //        this.this.currentRequest.getSenderID = this.currentRequest.getSenderID();
        //        if (this.currentRequest != null) {
        //            //serving request from agent
        //            this.state         = SERVING_AGENT;
        //            this.remainingTime = simulator.generateServiceTimeMu1();
        //        } else {
        //            this.currentRequest = this.requestQueue.removeRequestFromSource();
        //            this.state          = SERVING_SOURCE;
        //            this.remainingTime  = simulator.generateServiceTimeMu2();
        //        }
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
                //                if (log) {
                //                    this.simulator.log(
                //                            "MultiqueueServer: end of service for agent ");
                //                }
                if (this.currentRequest.getCreationTime() > 0) {
                    this.averagatorMuForAgent[this.currentRequest.getSenderID()].add(endTime -
                        this.currentRequest.getCreationTime() -
                        this.currentRequest.timeToSubstract);
                    if (log) {
                        this.simulator.log(
                            "MultiqueueServer: end of service for agent " +
                            this.currentRequest.getSenderID() + " lasted " +
                            (endTime - this.currentRequest.getCreationTime() -
                            this.currentRequest.timeToSubstract));
                    }
                }
            } else {
                if (log) {
                    this.simulator.log("Server: ignoring request from agent");
                    this.simulator.log("CurrentLocation " + currentLocation +
                        " new " + this.currentRequest.getNumber());
                }
            }
            this.stateAfterService();
        } else {
            if (this.requestQueueArray[this.currentRequest.getSenderID()].hasRequestFromAgent()) {
                //we put the request back in the queue
                //                this.log("Server.endOfService puting request in the queue");
                this.requestQueueArray[this.currentRequest.getSenderID()].addRequest(this.currentRequest);
                this.stateAfterService();
            } else {
                this.sendReply();
            }
        }

        //               this.averagatorActiveQueue.add(this.getNonEmptyQueueNumber());
    }

    public void sendReply() {
        super.sendReply();
        Request r = this.requestQueueArray[0].getRequestFromAgent();
        if (r != null) {
            r.timeToSubstract += this.remainingTime;
        }
    }

    public void endOfSendReply(double endTime) {
        if (log) {
            this.simulator.log("SelectiveServer: time microtimer = " +
                ((endTime - startTime) * 1000) + " for method " +
                "searchObject");
        }

        //        this.state = stateAfterService();
        //        if (this.state == IDL_REQUEST) {
        //            this.serveNextRequest(endTime);
        //        }
        this.sourceArray[this.currentRequest.getSenderID()].receiveReplyFromServer(this.currentLocation[this.currentRequest.getSenderID()]);
        this.stateAfterService();
    }

    public void stateAfterService() {
        super.stateAfterService();
        //        this.simulator.log("AQ " + this.getNonEmptyQueueNumber());
        //        this.averagatorActiveQueue.add(this.getNonEmptyQueueNumber());
    }

    public void end() {
        super.end();
        System.out.println("* active queues " +
            this.averagatorActiveQueue.average());
        for (int i = 0; i < this.averagatorMuForAgent.length; i++) {
            System.out.println("* mu for agent " + i + "  = " +
                (1000 / this.averagatorMuForAgent[i].average()) + " " +
                this.averagatorMuForAgent[i].getCount());
        }
        for (int i = 0; i < this.averagatorMuForAgent.length; i++) {
            System.out.println("* arrival rate " + i + " " +
                (this.averagatorArrivalRate[i].getCount() / this.simulator.getCurrentTime() * 1000));
        }
    }

    public void update(double time) {
        //        if (this.remainingTime == 0) {
        //            //                    System.out.println("Server non empty requestQueue = " + this.getNonEmptyQueueNumber());
        //            this.requestQueue = this.getNextRequestQueue();
        //            super.update(time);
        //        }
        if (this.remainingTime == 0) {
            switch (this.state) {
            case IDLE:
                if (getNonEmptyQueueNumber() == 0) {
                    this.remainingTime = 500000;
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
        }
    }
}
