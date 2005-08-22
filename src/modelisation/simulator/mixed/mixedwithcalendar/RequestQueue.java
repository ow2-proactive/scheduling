package modelisation.simulator.mixed.mixedwithcalendar;

import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import modelisation.simulator.common.SimulatorElement;


public class RequestQueue extends SimulatorElement {
    protected static Logger logger = Logger.getLogger(Request.class.getName());
    protected ArrayList list;
    protected double youngestRequestCreationTime;

    public RequestQueue() {
        this.list = new ArrayList(2);
    }

    public void addRequest(Request request) {
        if (request.isFromAgent()) {
            this.addRequestFromAgent(request);
        } else {
            this.list.add(request);
        }
        this.updateYoungestCreationTime(request);
    }

    protected void addRequestFromAgent(Request request) {
        int number = request.getNumber();
        ListIterator it = this.list.listIterator();
        Request r = null;
        boolean shouldAdd = true;
        while (it.hasNext()) {
            r = (Request) it.next();
            if (r.isFromAgent()) {
                if (r.getNumber() < number) {
                    it.remove();
                    shouldAdd = true;
                } else {
                    shouldAdd = false;
                }
            }
        }
        if (shouldAdd) {
            this.list.add(request);
        }
    }

    public void updateYoungestCreationTime(Request request) {
        //        double tmp = r.getCreationTime();
        //        if (this.youngestRequestCreationTime == 0) {
        //            this.youngestRequestCreationTime = tmp;
        //        } else if (tmp < this.youngestRequestCreationTime) {
        //            this.youngestRequestCreationTime = tmp;
        //        }
        Request r = null;
        double tmp = -1;
        ListIterator it = this.list.listIterator();
        while (it.hasNext()) {
            r = (Request) it.next();
            if (tmp == -1) {
                tmp = r.getCreationTime();
            } else if (r.getCreationTime() < tmp) {
                tmp = r.getCreationTime();
            }
        }
        if (tmp == -1) {
            this.youngestRequestCreationTime = 0;
        } else {
            this.youngestRequestCreationTime = tmp;
        }
    }

    /**
     * returns a request from the agent
     */
    public Request removeRequestFromAgent() {
        ListIterator it = this.list.listIterator();
        Request r = null;
        while (it.hasNext()) {
            r = (Request) it.next();
            if (r.isFromAgent()) {
                it.remove();
                this.updateYoungestCreationTime(r);
                return r;
            }
        }
        return null;
    }

    /**
     * returns a request from the agent
     */
    public Request removeRequestFromSource() {
        ListIterator it = this.list.listIterator();
        Request r = null;
        while (it.hasNext()) {
            r = (Request) it.next();
            if (!r.isFromAgent()) {
                it.remove();
                this.updateYoungestCreationTime(r);
                return r;
            }
        }
        return null;
    }

    public int length() {
        return this.list.size();
    }

    public boolean isEmpty() {
        return (this.list.size() == 0);
    }

    public boolean hasRequestFromAgent() {
        ListIterator it = this.list.listIterator();
        Request r = null;
        while (it.hasNext()) {
            r = (Request) it.next();
            if (r.isFromAgent()) {
                return true;
            }
        }
        return false;
    }

    public Request getRequestFromAgent() {
        ListIterator it = this.list.listIterator();
        Request r = null;
        while (it.hasNext()) {
            r = (Request) it.next();
            if (r.isFromAgent()) {
                return r;
            }
        }
        return null;
    }

    public double getYoungestCreationTime() {
        return this.youngestRequestCreationTime;
    }

    public void update(double time) {
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer();
        ListIterator it = this.list.listIterator();
        Request r = null;
        while (it.hasNext()) {
            r = (Request) it.next();
            tmp.append(r);
            //            if (!r.isFromAgent()) {
            //                it.remove();
            //                return r;
            //            }
        }
        return tmp.toString();
    }

    public static void main(String[] args) {
        logger.info("Creating requestQueue");
        RequestQueue rq = new RequestQueue();
        logger.info("Adding a request from agent");
        rq.addRequest(new Request(Request.AGENT, 2));
        logger.info("Adding a request from source");
        rq.addRequest(new Request(Request.SOURCE, 5));
        logger.info("------");
        logger.info("Length of the list " + rq.length());
        logger.info(rq);
        logger.info("------");
        logger.info("Adding a request from agent, should not be added");
        rq.addRequest(new Request(Request.AGENT, 1));
        logger.info("------");
        logger.info("Length of the list " + rq.length());
        logger.info(rq);
        logger.info("------");
        logger.info("Adding a request from agent, should be added");
        rq.addRequest(new Request(Request.AGENT, 7));
        logger.info("------");
        logger.info("Length of the list " + rq.length());
        logger.info(rq);
        logger.info("------");
        logger.info("Removing request from agent");
        logger.info("Request is " + rq.removeRequestFromAgent());
        logger.info("Removing request fromm source");
        logger.info("Request is " + rq.removeRequestFromSource());
        logger.info("Length of the list " + rq.length());
    }
}
