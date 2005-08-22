package modelisation.simulator.server;

import modelisation.simulator.common.SimulatorElement;
import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;


public class Server extends SimulatorElement {
    public final static int IDL_EMPTY = 1;
    public final static int IDL_REQUEST = 2;
    public final static int REPLY_NEEDED = 3;
    public final static int SENDING_REPLY = 4;
    public final static int SERVING_SOURCE = 5;
    public final static int SERVING_AGENT = 6;
    protected RandomNumberGenerator expoMu1;
    protected RandomNumberGenerator expoMu2;

    //    protected RandomNumberGenerator expoGamma2;
    protected boolean messageFromAgent;
    protected boolean messageFromSource;
    protected boolean sendingReply;

    // protected boolean reply;
    protected double mu1;
    protected double mu2;

    //    protected double gamma2;
    protected double startTime;

    public Server(double mu1, double mu2) {
        this.mu1 = mu1;
        this.mu2 = mu2;
        //        this.gamma2 = gamma2;
        this.state = IDL_EMPTY;
    }

    public double getNextMu1Int() {
        if (this.expoMu1 == null) {
            this.expoMu1 = RandomNumberFactory.getGenerator("mu1");
            //            this.expoMu1.initialize(mu1, System.currentTimeMillis() + 120457);
            this.expoMu1.initialize(mu1, 120457);
        }
        double tmp = this.expoMu1.next() * 1000;
        return tmp;
    }

    public double getNextMu2Int() {
        if (this.expoMu2 == null) {
            this.expoMu2 = RandomNumberFactory.getGenerator("mu2");
            //            this.expoMu2.initialize(mu1, System.currentTimeMillis() + 120457);
            this.expoMu2.initialize(mu1, 67457);
        }
        double tmp = this.expoMu2.next() * 1000;
        return tmp;
    }

    //    public double getNextGamma2Int() {
    //        if (this.expoGamma2 == null)
    //            this.expoGamma2 = new ExponentialLaw(gamma2, System.currentTimeMillis() + 2017465);
    //
    //
    //
    //        double tmp = this.expoGamma2.rand() * 1000;
    //        return tmp;
    //    }
    protected void receiveRequest() {
        System.out.println("Server.receiveRequest");
        if (this.state == IDL_EMPTY) {
            this.state = IDL_REQUEST;
            this.remainingTime = 0;
        }
    }

    public void receiveRequestFromAgent() {
        this.receiveRequest();
        this.messageFromAgent = true;
    }

    public void receiveRequestFromSource() {
        this.receiveRequest();
        this.messageFromSource = true;
    }

    public void serveNextRequest(double startTime) {
        //   this.state = SERVING;
        this.startTime = startTime;
        //we have to choose the request to serve
        //priority goes to agent
        if (this.messageFromAgent) {
            this.state = SERVING_AGENT;
            this.messageFromAgent = false;
            //    this.reply = false;
            this.remainingTime = getNextMu1Int();
            return;
        }
        if (this.messageFromSource) {
            this.state = SERVING_SOURCE;
            this.messageFromSource = false;
            //   this.reply = true;
            this.remainingTime = getNextMu2Int();
            return;
        }
    }

    public void endOfService(double endTime, double lengthCommunication) {
        //        boolean serviceOk = true;
        if (this.state == SERVING_SOURCE) {
            if (this.messageFromAgent) {
                System.out.println(
                    "SelectiveServer: puting the request back in the line");
                this.messageFromSource = true;
                this.state = stateAfterService();
                //                serviceOk = false;
            } else {
                //                this.state=SENDING_REPLY;
                this.sendReply(lengthCommunication);
                //                serviceOk=false;
                //                if (sendingReply) {
                //                    System.out.println("SelectiveServer: time microtimer = " +
                //                                       (endTime - startTime) * 1000 + " for method "
                //                                       + "searchObject");
                //
                //                    this.state = stateAfterService();
                //                    sendingReply = false;
                //                } else {
                //                    sendingReply = true;
                //                    this.state = SENDING_REPLY;
                //                    this.setRemainingTime(this.getNextGamma2Int());
                //                    serviceOk = false;
                //                }
            }
        } else {
            System.out.println("SelectiveServer: time microtimer = " +
                ((endTime - startTime) * 1000) + " for method " +
                "updateLocation");
            this.state = stateAfterService();
        }
        if (this.state == IDL_REQUEST) {
            this.serveNextRequest(endTime);
        }

        //        return serviceOk;
    }

    //            } else {
    //                this.state = REPLY_NEEDED;
    //            }
    //        } else {
    //            System.out.println("SelectiveServer: time microtimer = " +
    //                               (endTime - startTime)*1000 + " for method "
    //                               + "updateLocation");
    //            this.state = stateAfterService();
    //        }
    //   }
    public void sendReply(double length) {
        this.state = SENDING_REPLY;
        this.remainingTime = length;
    }

    public void endOfSendReply(double endTime) {
        System.out.println("SelectiveServer: time microtimer = " +
            ((endTime - startTime) * 1000) + " for method " + "searchObject");

        this.state = stateAfterService();
        if (this.state == IDL_REQUEST) {
            this.serveNextRequest(endTime);
        }
    }

    protected int stateAfterService() {
        if (messageFromSource || messageFromAgent) {
            //            this.setRemainingTime(0);
            return IDL_REQUEST;
        } else {
            this.setRemainingTime(50000000);
            return IDL_EMPTY;
        }
    }

    public boolean hasRequestFromAgent() {
        return this.messageFromAgent;
    }

    public String toString() {
        switch (this.state) {
        case IDL_EMPTY:return "IDL_EMPTY ";
        case IDL_REQUEST: {
            StringBuffer tmp = new StringBuffer("IDL_REQUEST ");
            if (this.messageFromSource) {
                tmp.append("messageFromSource ");
            }
            if (this.messageFromAgent) {
                tmp.append("messageFromAgent ");
            }
            return tmp.toString();
        }
        case SERVING_SOURCE: {
            if (this.messageFromAgent) {
                return "SERVING_SOURCE messageFromAgent ";
            } else {
                return "SERVING_SOURCE ";
            }
        }
        case SERVING_AGENT: {
            StringBuffer tmp = new StringBuffer("SERVING_AGENT ");
            if (this.messageFromSource) {
                tmp.append("messageFromSource ");
            }
            if (this.messageFromAgent) {
                tmp.append("messageFromAgent ");
            }
            return tmp.toString();
        }
        case SENDING_REPLY: {
            StringBuffer tmp = new StringBuffer("SENDING_REPLY ");
            if (this.messageFromSource) {
                tmp.append("messageFromSource ");
            }
            if (this.messageFromAgent) {
                tmp.append("messageFromAgent ");
            }
            return tmp.toString();
        }
        }
        return null;
    }
}
