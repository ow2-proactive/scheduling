/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.core.group;


import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Collections;
import java.lang.reflect.InvocationTargetException;


/**
 * This proxy class manages the semantic of group communication and implements the Group Interface.
 *
 * @author Laurent Baduel - INRIA
 * @see org.objectweb.proactive.core.mop.Proxy
 *
 */
public class ProxyForGroup extends AbstractProxy implements org.objectweb.proactive.core.mop.Proxy,
    Group,
    java.io.Serializable {
    
    /** The name of the Class : all members of the group are "className" assignable */
    protected String className;

    /** The list of member */
    protected List memberListStub;

    /** The list of the proxies of members */
    protected List memberListProxy;

    /** Unique identificator for body (avoid infinite loop in some hierarchical group) */ // NOT YET IMPLEMENTED !!!
    protected UniqueID proxyForGroupID;
    
    /** Number of awaited call of method on the group's member */
    protected int waited = 0;

    /** Use to monitor futures */
    protected FutureListGroup futureList;

    
    /*                    */
    /*    CONSTRUCTORS    */
    /*                    */

    public ProxyForGroup(String nameOfClass) throws ConstructionOfReifiedObjectFailedException {
	className = nameOfClass;
	memberListStub  = Collections.synchronizedList(new ArrayList());
	memberListProxy = Collections.synchronizedList(new ArrayList());
	futureList = new FutureListGroup(memberListStub,((AbstractBody)ProActive.getBodyOnThis()).getFuturePool());
 	proxyForGroupID = new UniqueID();
    }


    public ProxyForGroup(String nameOfClass, Integer size) throws ConstructionOfReifiedObjectFailedException {
	className = nameOfClass;
	memberListStub  = Collections.synchronizedList(new ArrayList());
	memberListProxy = Collections.synchronizedList(new ArrayList());
	for (int i = 0 ; i < size.intValue() ; i++) {
	    memberListStub.add(null);
	    memberListProxy.add(null);
	}
	futureList = new FutureListGroup(memberListStub,((AbstractBody)ProActive.getBodyOnThis()).getFuturePool());
 	proxyForGroupID = new UniqueID();
    }


    public ProxyForGroup() throws ConstructionOfReifiedObjectFailedException {
	memberListStub  = Collections.synchronizedList(new ArrayList());
	memberListProxy = Collections.synchronizedList(new ArrayList());
	futureList = new FutureListGroup(memberListStub,((AbstractBody)ProActive.getBodyOnThis()).getFuturePool());
	proxyForGroupID = new UniqueID();
    }




    public ProxyForGroup(ConstructorCall c, Object[] p) throws ConstructionOfReifiedObjectFailedException {
	memberListStub  = Collections.synchronizedList(new ArrayList());
	memberListProxy = Collections.synchronizedList(new ArrayList());
	futureList = new FutureListGroup(memberListStub,((AbstractBody)ProActive.getBodyOnThis()).getFuturePool());
 	proxyForGroupID = new UniqueID();
    }





    /*                          */
    /*    THE PROXY'S METHOD    */
    /*                          */
    
    /** The proxy's method, implements the semantic of communication */
    public Object reify (MethodCall c) throws Throwable {

	/* result will be a stub on a proxy for group representing the group of results */
	Object result = null;

	this.waitForAllCallsDone();

	/* if OneWay : do not construct result */
	if (AbstractProxy.isOneWayCall (c))
	    {
		oneWayCallOnGroup(c);
	    }

	/* if the call is asynchronous the group of result will be a group a future */
	else // if (AbstractProxy.isAsynchronousCall (c))                // SYNC == ASYNC !!!!
	    {
	int size = memberListProxy.size();
	/* Creates a stub + ProxyForGroup for representing the result */
		Object[] paramProxy = new Object[0];
		result = MOP.newInstance (c.getReifiedMethod().getReturnType().getName(),
					  null,
					  "org.objectweb.proactive.core.group.ProxyForGroup",
					  paramProxy);
		((ProxyForGroup)((StubObject)result).getProxy()).className = c.getReifiedMethod().getReturnType().getName();
	
	// Init the lists of result with null value to permit the "set(index)" operation
	List memberListStubOfResult  = ((ProxyForGroup)((StubObject)result).getProxy()).memberListStub;
	List memberListProxyOfResult = ((ProxyForGroup)((StubObject)result).getProxy()).memberListProxy;
	for (int i = 0 ; i < size ; i++) {
	    memberListStubOfResult.add(null);
	    memberListProxyOfResult.add(null);
	}
	
	// Creating Threads
	for (int i = 0 ; i < size ; i++) {




	    Object tmp = ((Proxy)(memberListProxy.get(i))).reify(c);
	    addToListOfResult(memberListProxyOfResult,memberListStubOfResult,tmp,i);

	    memberListStubOfResult.set(i,tmp);
	    memberListProxyOfResult.set(i,((StubObject)tmp).getProxy());


	    //((ProxyForGroup)((StubObject)result).getProxy()).createThreadForAsync(this.memberListProxy,memberListProxyOfResult,memberListStubOfResult,i,c);
	}

	    }

	return result;
    }

    
    /**
     * Create and initialize (and return) the group of result, then launch threads for asynchronous call of each member
     */
    protected Object asynchronousCallOnGroup(MethodCall c) throws Throwable {
	Object result;
	int size = memberListProxy.size();
	/* Creates a stub + ProxyForGroup for representing the result */
		Object[] paramProxy = new Object[0];
		result = MOP.newInstance (c.getReifiedMethod().getReturnType().getName(),
					  null,
					  "org.objectweb.proactive.core.group.ProxyForGroup",
					  paramProxy);
		((ProxyForGroup)((StubObject)result).getProxy()).className = c.getReifiedMethod().getReturnType().getName();
	
	// Init the lists of result with null value to permit the "set(index)" operation
	List memberListStubOfResult  = ((ProxyForGroup)((StubObject)result).getProxy()).memberListStub;
	List memberListProxyOfResult = ((ProxyForGroup)((StubObject)result).getProxy()).memberListProxy;
	for (int i = 0 ; i < size ; i++) {
	    memberListStubOfResult.add(null);
	    memberListProxyOfResult.add(null);
	}
	
	// Creating Threads
	for (int i = 0 ; i < size ; i++) {
	    ((ProxyForGroup)((StubObject)result).getProxy()).createThreadForAsync(this.memberListProxy,memberListProxyOfResult,memberListStubOfResult,i,c);
	}
	return result;
    }


   /**
    * Launch threads for OneWay call of each member
    */
    protected void oneWayCallOnGroup(MethodCall c) {
	int size = memberListProxy.size();
	// Creating Threads
	for (int i = 0 ; i < size ; i++)
	    this.createThreadForOneWay(this.memberListProxy,i,c);
    }




    


    /*                            */
    /*     THE GROUP'S METHOD     */
    /*                            */

    /** If o is a reified object and if it is "assignableFrom" the class of the group, add it into the group 
	if o is a group merge it into the group 
	if o is not a reified object nor a group : do nothing */
    public void add(Object o) {
	try {
	    
	    /* if o is an reified object and if it is "assignableFrom" the class of the group, ... add it into the group */
	    if ( (MOP.isReifiedObject(o)) && ((MOP.forName(this.className)).isAssignableFrom(o.getClass())) ) {
		memberListStub.add(o);
		memberListProxy.add( ((StubObject)o).getProxy() );
	    }

	    /* if o is a Group */
	    else if ( o instanceof org.objectweb.proactive.core.group.ProxyForGroup ) {
		memberListStub.addAll (((org.objectweb.proactive.core.group.ProxyForGroup)o).memberListStub );
		memberListProxy.addAll(((org.objectweb.proactive.core.group.ProxyForGroup)o).memberListProxy);  /* like an addMerge call */
	    }
	}
	catch (java.lang.ClassNotFoundException e) { System.err.println("Unknown class : " + this.className); }
    }




    /** Add all member of the group oGroup into the Group */
    public void addMerge(Object oGroup) {
	try {
	    /* check oGroup is an Reified Object and if it is "assignableFrom" the class of the group */
	    if ( (MOP.isReifiedObject(oGroup))  && ((MOP.forName(this.className)).isAssignableFrom(oGroup.getClass())) ){
		
		/* check oGroup is an object representing a group */
		if ( ((StubObject)oGroup).getProxy() instanceof org.objectweb.proactive.core.group.ProxyForGroup ) {
		    
		    memberListStub.addAll ( ((ProxyForGroup)((StubObject)oGroup).getProxy()).memberListStub  );
		    memberListProxy.addAll( ((ProxyForGroup)((StubObject)oGroup).getProxy()).memberListProxy );

		}
		
		/* if oGroup is a Standard Active Object (but not a group), just add it */
		else
		    this.add(oGroup);
		
	    }
	    /* if oGroup is a Group */
	    else if ( oGroup instanceof org.objectweb.proactive.core.group.ProxyForGroup ) {
		memberListStub.addAll (((org.objectweb.proactive.core.group.ProxyForGroup)oGroup).memberListStub );
		memberListProxy.addAll(((org.objectweb.proactive.core.group.ProxyForGroup)oGroup).memberListProxy);
	    }
	}
	catch (java.lang.ClassNotFoundException e) { System.err.println("Unknown class : " + this.className); }
    }





    /**
     * Returns the index of the first occurence of the specified Object <code>obj</code>
     * Returns -1 if the list does not contain this object.
     */
    public synchronized int indexOf(Object obj) {
	return memberListStub.indexOf(obj);
    }



    /**
     * Removes the element at the specified position.
     */
    public synchronized void remove (int index) {
	memberListStub.remove(index);
	memberListProxy.remove(index);
    }
     
    
    /**
     * Returns the i-th member of the group
     */
    public Object get (int i) {
	this.waitForAllCallsDone();
	return memberListStub.get(i);
    }
    

    /**
     * Returns the number of member in the group
     */
    public int size() {
	return memberListProxy.size();
    }


    /**
     * Returns a ListIterator of the member in the group
     */
    public ListIterator iterator() {
	return memberListStub.listIterator();
    }

    /** Return the ("higher") Class of group's member */
    public Class getType() throws java.lang.ClassNotFoundException {
	return MOP.forName(className);
    }

    /** Return the full name of ("higher") Class of group's member */
    public String getTypeName() {
	return className;
    }


    /** Return an Object representing the group */
    public Object getGrouped() {
	
	Object result;
	
	try {  // an unuseful proxy is generated
	    result = MOP.newInstance (this.className, null, "org.objectweb.proactive.group.ProxyForGroup", null);
	}
	catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	
	((ProxyForGroup)((StubObject)result).getProxy()).memberListProxy = this.memberListProxy;
	((ProxyForGroup)((StubObject)result).getProxy()).className = this.className;
	((ProxyForGroup)((StubObject)result).getProxy()).proxyForGroupID = this.proxyForGroupID;
	return result;
    }
    
    //  This is the best thing to do, but createStubObject has a private acces !!!!	
	
    // 	// Instanciates the stub object
    // 	StubObject stub = MOP.createStubObject(this.className, MOP.forName(this.className));
	
    // 	// Connects the proxy to the stub
    // 	stub.setProxy(this);
    // 	return stub;
    //  }

    //  An other way is to "store" the stub and return it when asked
    
    
    

    /** To debug, display all members and there position */
    public void testClass () {	
	for (int i=0 ; i < memberListProxy.size() ; i++) {
	    System.out.println(i + " : " + memberListProxy.get(i).getClass().getName());
	}
    }


  

    /** Wait until the method has been apply to all the member */
    private synchronized void waitForAllCallsDone() {
	while(this.waited != 0) {
	    try {
		wait();
	    } catch (Exception e) {e.printStackTrace();}
	}
    }
    



    // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
  
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
	this.waitForAllCallsDone();
	// Now that all the results are available, we can copy the group (of future)
	out.defaultWriteObject();
 
    }

    //for the moment, we set the value of migration to false here
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
	in.defaultReadObject();
    }







    private synchronized void createThreadForAsync (List memberListProxyOfThis,
						    List memberListProxyOfResult,
						    List memberListStubOfResult,
						    int index,
						    MethodCall mc) {
	new Thread (new MyProcessForGroupAsync(memberListProxyOfThis,memberListProxyOfResult,memberListStubOfResult,index,mc)).start();
	waited++;
    }


    private synchronized void createThreadForOneWay (List memberListProxyOfThis,
						     int index,
						     MethodCall mc) {
	new Thread (new MyProcessForGroupOneWay(memberListProxyOfThis,index,mc)).start();
    }
   


    private synchronized void addToListOfResult(List arrayProxy, List arrayStub, Object o, int index) {
	arrayStub.set(index, o);
	arrayProxy.set(index, ((StubObject)o).getProxy());
	waited--;
	notifyAll();
    }






    private class MyProcessForGroupAsync implements Runnable {
	List listOfFather;
	List listOfResultProxy;
	List listOfResultStub;
	int index;
	MethodCall mc;

	MyProcessForGroupAsync(List father, List resultProxy, List resultStub, int index, MethodCall mc) {
	    this.listOfFather = father;
	    this.listOfResultProxy = resultProxy;
	    this.listOfResultStub  = resultStub;
	    this.index        = index;
	    this.mc           = mc;
	}


	public void run() {
	    try {
		
		Object tmp = ((Proxy)(listOfFather.get(index))).reify(mc);
		addToListOfResult(listOfResultProxy,listOfResultStub,tmp,index);
		
	    } catch (Throwable e) {
		e.printStackTrace();
	    }
	}
    }





    private class MyProcessForGroupOneWay implements Runnable {
	List listOfFather;
	int index;
	MethodCall mc;

	MyProcessForGroupOneWay(List father, int index, MethodCall mc) {
	    this.listOfFather = father;
	    this.index        = index;
	    this.mc           = mc;
	}


	public void run() {
	    try {
		
		((Proxy)(listOfFather.get(index))).reify(mc);
		
	    } catch (Throwable e) {
		e.printStackTrace();
	    }
	}
    }



}

    
	
