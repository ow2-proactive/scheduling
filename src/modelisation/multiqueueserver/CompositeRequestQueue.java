package modelisation.multiqueueserver;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestQueue;
import org.objectweb.proactive.core.body.request.RequestQueueImpl;

import java.util.HashMap;
import java.util.Iterator;

public class CompositeRequestQueue extends BlockingRequestQueueImpl implements java.io.Serializable {

  protected HashMap queueList;
  protected UniqueID ownerID;
  protected int requestCount;

  public CompositeRequestQueue(UniqueID ownerID) {
    super(ownerID);
    System.out.println("CompositeRequestQueue: <init>");
    this.queueList = new HashMap();
  }

  /**
   * Add the request to a requestQueue
   * Creates a new requestQueue if none match the agentID
   * In both cases (updateLocation or searchObject), the ID of the agent is
   * the first parameter of the request
   * The queue will remove previous requests with the same method name in the
   * queue
   */
  public synchronized int add(Request r) {
    RequestQueue tmp = null;
    if ((tmp = this.getRequestQueue((UniqueID) r.getParameter(0))) == null) {
      System.out.println("CompositeRequestQueue: creating a new request queue " + "for id " + r.getParameter(0));
      tmp = this.createNewRequestQueue((UniqueID) r.getParameter(0));
    }
    removeAllRequests(tmp, r.getMethodName());
    tmp.add(r);
    System.out.println(
      "CompositeRequestQueue: There are " + this.getNonEmptyQueueCount() + " actives queues " + 
      " total requests count is " + this.size());
    requestCount++;
    if (requestCount == 1) {
      this.notifyAll();
    }
    return FTManager.NON_FT; // dummy value for fault-tolerance
  }

  protected synchronized void removeAllRequests(RequestQueue rq, String name) {
    //System.out.println("CompositeRequestQueue.RemoveAllRequests");
    while (rq.removeOldest(name) != null)
      requestCount--;
  }

  protected synchronized Request blockingRemove(boolean oldest) {
    //we check if there are requests available
    while (requestCount == 0) {
      try {
        wait();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    //then we choose the queue
    RequestQueue tmp = getOldestRequestQueue();
    requestCount--;
    return oldest ? tmp.removeOldest() : tmp.removeYoungest();
  }

  /**
   * Return the oldest request white methodname name
   * in the request queue where r belongs
   */
  public synchronized Request getOldest(String name, Request r) {
    RequestQueue rq = getRequestQueue(r);
    if (rq != null) {
      return rq.getOldest(name);
    }
    return null;
  }

  /**
   * Return the request queue where a request waits to be served
   *
   * @return returns the request whose first element is the oldest
   */
  protected synchronized RequestQueue getOldestRequestQueue() {
    RequestQueue rq = null;
    RequestQueue tmpQueue = null;
    Request tmpRequest = null;
    long t = 0;
    Iterator it = this.queueList.values().iterator();
    while (it.hasNext()) {
      tmpQueue = (RequestQueue) it.next();
      tmpRequest = tmpQueue.getYoungest();
      if (tmpRequest != null) {
        if (tmpRequest.getTimeStamp() > t) {
          t = tmpRequest.getTimeStamp();
          rq = tmpQueue;
        }
      }
    }
    return rq;
  }

  protected synchronized RequestQueue getRequestQueue(Request r) {
    if (r.getMethodName().equals("searchObject")) {
      return this.getRequestQueue((UniqueID) r.getParameter(0));
    } else {
      return getRequestQueue(r.getSourceBodyID());
    }
  }

  public synchronized int size() {
    int sizeTmp = 0;
    Iterator it = this.queueList.values().iterator();
    while (it.hasNext()) {
      sizeTmp += ((RequestQueue) it.next()).size();
    }
    return sizeTmp;
  }

  public synchronized int getQueueCount() {
    return this.queueList.size();
  }

  public synchronized int getNonEmptyQueueCount() {
    int queueCount = 0;
    RequestQueue tmp = null;
    Iterator it = this.queueList.values().iterator();
    while (it.hasNext()) {
      tmp = (RequestQueue) it.next();
      if (tmp.size() != 0) {
        queueCount++;
      }
    }
    return queueCount;
  }

  public synchronized Request removeOldest(String methodName) {
    RequestQueue rq = getOldestRequestQueue();
    //System.out.println("CompositeRequestQueue.removeOldest " + rq);
    if (rq != null) {
      Request r = rq.removeOldest(methodName);
      if (r != null) {
        requestCount--;
      }
      return r;
    }
    return null;
  }

  protected synchronized RequestQueue getRequestQueue(UniqueID id) {
    return (RequestQueue) this.queueList.get(id);
  }

  protected synchronized RequestQueue createNewRequestQueue(UniqueID id) {
    RequestQueue tmp = new RequestQueueImpl(id);
    this.registerRequestQueue(tmp, id);
    return tmp;
  }

  public synchronized void waitForRequest() {
    while (requestCount == 0) {
      try {
        this.wait();
      } catch (InterruptedException e) {}
    }
  }

  /**
   * Register an existing request queue to id
   *
   */
  protected synchronized void registerRequestQueue(RequestQueue rq, UniqueID id) {
    this.queueList.put(id, rq);
  }
}