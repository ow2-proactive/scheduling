package modelisation.simulator.mixed;

import modelisation.simulator.common.SimulatorElement;

import java.util.ArrayList;
import java.util.ListIterator;

public class RequestList extends SimulatorElement {

    ArrayList list;

    public RequestList() {
        this.list = new ArrayList(2);
    }

    public void addRequest(Request request) {
        if (request.isFromAgent()) {
            this.addRequestFromAgent(request);
        } else {
            this.list.add(request);
        }
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
                } else
                    shouldAdd = false;
            }
        }
        if (shouldAdd)
            this.list.add(request);
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
        System.out.println("Creating requestQueue");
        RequestList rq = new RequestList();
        System.out.println("Adding a request from agent");
        rq.addRequest(new Request(Request.AGENT, 2));
        System.out.println("Adding a request from source");
        rq.addRequest(new Request(Request.SOURCE, 5));
        System.out.println("------");
        System.out.println("Length of the list " + rq.length());
        System.out.println(rq);
        System.out.println("------");
        System.out.println("Adding a request from agent, should not be added");
        rq.addRequest(new Request(Request.AGENT, 1));
        System.out.println("------");
        System.out.println("Length of the list " + rq.length());
        System.out.println(rq);
        System.out.println("------");
        System.out.println("Adding a request from agent, should be added");
        rq.addRequest(new Request(Request.AGENT, 7));
        System.out.println("------");
        System.out.println("Length of the list " + rq.length());
        System.out.println(rq);
        System.out.println("------");
        System.out.println("Removing request from agent");
        System.out.println("Request is " + rq.removeRequestFromAgent());
        System.out.println("Removing request fromm source");
        System.out.println("Request is " + rq.removeRequestFromSource());
        System.out.println("Length of the list " + rq.length());
    }
}
