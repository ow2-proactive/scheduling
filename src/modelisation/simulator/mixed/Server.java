package modelisation.simulator.mixed;

import modelisation.simulator.common.SimulatorElement;

public class Server extends SimulatorElement {

    public final static int IDLE = 1;
    public final static int IDL_REQUEST = 2;
//    public final static int REPLY_NEEDED = 3;
    public final static int SENDING_REPLY = 4;
    public final static int SERVING_SOURCE = 5;
    public final static int SERVING_AGENT = 6;

    protected Simulator simulator;
    protected RequestList requestQueue;
    protected Source source;

//    protected double mu1;
//    protected double mu2;

    protected int currentLocation;
    protected Request currentRequest;


    protected double startTime;


    public Server(Simulator s) {
//        this.mu1 = mu1;
//        this.mu2 = mu2;
        this.state = IDLE;
        this.simulator = s;
        this.requestQueue = new RequestList();
    }

    public void setSource(Source s) {
        this.source = s;
    }

    public void update(double time) {
        if (this.remainingTime == 0) {
            switch (this.state) {
                case IDLE:
                    if (this.requestQueue.isEmpty()) {
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

    protected void receiveRequest() {
//        System.out.println("Server.receiveRequest");
        if (this.state == IDLE) {
//            this.state = IDL_REQUEST;
            this.remainingTime = 0;
        }
    }


    public void receiveRequestFromForwarder(int number) {
        System.out.println("Server.receiveRequestFromForwarder");
        this.requestQueue.addRequest(new Request(Request.AGENT, number));
        this.receiveRequest();
//        this.messageFromAgent = true;
    }

    public void receiveRequestFromAgent(int number) {
        System.out.println("Server.receiveRequestFromAgent");
        this.requestQueue.addRequest(new Request(Request.AGENT, number));
        this.receiveRequest();
//        this.messageFromAgent = true;
    }

    public void receiveRequestFromSource() {
        System.out.println("Server.receiveRequestFromSource");
        this.requestQueue.addRequest(new Request(Request.SOURCE, 0));
        this.receiveRequest();
//        this.messageFromSource = true;
    }

    public void serveNextRequest(double startTime) {
        this.startTime = startTime;
        this.currentRequest = this.requestQueue.removeRequestFromAgent();
        if (this.currentRequest != null) {
            //serving request from agent
            this.state = SERVING_AGENT;
            this.remainingTime = simulator.getServiceTimeMu1();
        } else {
            this.currentRequest = this.requestQueue.removeRequestFromSource();
            this.state = SERVING_SOURCE;
            this.remainingTime = simulator.getServiceTimeMu2();
        }
    }


    public void endOfService(double endTime) {
//        System.out.println("Server.endOfService");
        if (this.state == SERVING_AGENT) {
        	if (this.currentLocation<this.currentRequest.getNumber()) {
               this.currentLocation=this.currentRequest.getNumber();
        	} else {
        	  System.out.println("Server: ignoring request from agent");
        	  System.out.println("CurrentLocation " + currentLocation + " new " 
        	  + this.currentRequest.getNumber());
        	}
            this.stateAfterService();
        } else {
            if (this.requestQueue.hasRequestFromAgent()) {
                //we put the request back in the queue
//                System.out.println("Server.endOfService puting request in the queue");
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
    }

    public void sendReply() {
        this.state = SENDING_REPLY;
        this.remainingTime = simulator.getCommunicationTimeServer();
    }

    public void endOfSendReply(double endTime) {
        System.out.println("SelectiveServer: time microtimer = " +
                           (endTime - startTime) * 1000 + " for method "
                           + "searchObject");

//        this.state = stateAfterService();
//        if (this.state == IDL_REQUEST) {
//            this.serveNextRequest(endTime);
//        }
        this.source.receiveReplyFromServer(this.currentLocation);
        this.stateAfterService();
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

    public static void main(String[] args) {
        Simulator s = new Simulator(1, 1, 1, 1, 1, 1, 1, 1,1,1);
        Server server = new Server(s);
        System.out.println("State of server: " + server);
        server.receiveRequestFromSource();
        server.receiveRequestFromAgent(1);
        System.out.println("State of server: " + server);
        server.setRemainingTime(0);
        server.update(100);
        System.out.println("State of server: " + server);
        server.setRemainingTime(0);
        server.update(100);
        System.out.println("State of server: " + server);
        server.setRemainingTime(0);
        server.update(100);
        System.out.println("State of server: " + server);
    }

}
