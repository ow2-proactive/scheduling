package test.proactive;

import org.objectweb.proactive.core.body.request.RequestQueue;
import org.objectweb.proactive.core.body.request.RequestQueueImpl;
import org.objectweb.proactive.core.mop.MethodCall;
import java.lang.reflect.Method;
import java.util.Vector;

public class RequestQueueTester {

  private RequestQueue requestQueue;


  public RequestQueueTester() {
    requestQueue = new RequestQueueImpl(new org.objectweb.proactive.core.UniqueID());
  }


  public void insertMethods(Method m, int number) {
    for (int i = 0; i < 10; i++) {
      //this.requestQueue.addCall(MethodCall.getMethodCall(m, null));
    }
  }


  public void mixedInsert(Method m1, Method m2, int number) {
    for (int i = 0; i < 10; i++) {
      //this.requestQueue.addCall(MethodCall.getMethodCall(m1, null));
      //this.requestQueue.addCall(MethodCall.getMethodCall(m2, null));
    }
  }


  public boolean testInsertion(Method m1) {
    Vector vector = new Vector(10);
    boolean result = true;
    //we fill both the vector and the requestQueue
    for (int i = 0; i < 10; i++) {
      MethodCall me = MethodCall.getMethodCall(m1, null);
      vector.addElement(me);
      //this.requestQueue.addCall(me);
    }
    //now we check for the results
    for (int i = 0; i < 10; i++) {
      //result = result && (((MethodCall)vector.elementAt(i)).equals(this.requestQueue.removeOldest(i)));
      if (!result)
        return result;
    }
    return result;
  }


  public boolean buggyTestInsertion(Method m1, Method m2) {
    Vector vector = new Vector(10);
    boolean result = true;
    this.requestQueue.clear();
    //we fill both the vector and the requestQueue
    for (int i = 0; i < 9; i++) {
      MethodCall me = MethodCall.getMethodCall(m1, null);
      vector.addElement(me);
      //this.requestQueue.addCall(me);
    }

    MethodCall me = MethodCall.getMethodCall(m1, null);
    MethodCall me2 = MethodCall.getMethodCall(m2, null);
    vector.addElement(me);
   // this.requestQueue.addCall(me2);

    //now we check for the results
    for (int i = 0; i < 10; i++) {
      //result = result && (((MethodCall)vector.elementAt(i)).equals(this.requestQueue.removeOldest(i)));
      if (!result)
        return result;
    }
    return result;
  }


  public boolean testRetrieveByName(Method m1) {
    Vector vector = new Vector(10);
    boolean result = true;
    this.requestQueue.clear();
    //we fill both the vector and the requestQueue
    for (int i = 0; i < 10; i++) {
      MethodCall me = MethodCall.getMethodCall(m1, null);
      vector.addElement(me);
      //this.requestQueue.addCall(me);
    }
    //now we check for the results
    for (int i = 0; i < 10; i++) {
      result = result && (((MethodCall)vector.elementAt(i)).equals(this.requestQueue.getOldest(m1.getName())));
      if (!result)
        return result;
    }
    return result;
  }


  public void displayRequestQueue() {
    //this.requestQueue.display();
  }


  public void clear() {
    this.requestQueue.clear();
  }


  public static void main(String[] args) {

    //we use this dummy object to create Method objects
    DummyObject dummy = new DummyObject();

    //this is the RequestQueueTester 
    RequestQueueTester tester = new RequestQueueTester();

    //first we get some methods from the dummyObject
    Method[] theMethods = dummy.getClass().getMethods();

    //we create 10 methods
    tester.insertMethods(theMethods[0], 10);

    //and we display them 
    tester.displayRequestQueue();

    //we add 10 other methods
    tester.insertMethods(theMethods[1], 10);
	
    //and we display them 
    tester.displayRequestQueue();

    //we flush it
    tester.clear();

    //and we display them 
    tester.displayRequestQueue();

    //a bit of mixed methods
    tester.mixedInsert(theMethods[0], theMethods[1], 5);
    tester.displayRequestQueue();

    //we flush it
    tester.clear();

    // 	tester.testInsertion(theMethods[0], theMethods[1]);
    // 	tester.displayRequestQueue();

    System.out.println("The result of the insertion test is ... " + tester.testInsertion(theMethods[0]));
    System.out.println("The result of the buggy insertion test is ... " + tester.buggyTestInsertion(theMethods[0], theMethods[1]));
    System.out.println("The result of the retrieve by name  test is ... " + tester.testRetrieveByName(theMethods[0]));
  }
}
