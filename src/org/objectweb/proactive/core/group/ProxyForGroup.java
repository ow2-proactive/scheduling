package org.objectweb.proactive.core.group;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;

/**
 * This proxy class manages the semantic of group communication and implements the Group Interface.
 *
 * @author Laurent Baduel - INRIA
 * @see org.objectweb.proactive.core.mop.Proxy
 *
 */
public class ProxyForGroup extends AbstractProxy implements org.objectweb.proactive.core.mop.Proxy, Group, java.io.Serializable {


	/** The name of the Class : all members of the group are "className" assignable */
	protected String className;
	/** The list of member : it contains exclusively StubObjects connected to Proxies */
	protected Vector memberList;
	/** Unique identificator for body (avoid infinite loop in some hierarchicals groups) */ // NOT FULLY IMPLEMENTED !!!
	transient private UniqueID proxyForGroupID;
	/** Number of awaited call of method on the group's member : The Semantic is that we wait all call are done before continuing */
	protected int waited = 0;
	/** Flag to deternime the semantic of communication (broadcast or dispatching) */
	protected boolean dispatching = false;

	/* ----------------------- CONSTRUCTORS ----------------------- */
	public ProxyForGroup(String nameOfClass) throws ConstructionOfReifiedObjectFailedException {
		this.className = nameOfClass;
		this.memberList = new Vector();
		this.proxyForGroupID = new UniqueID();
	}

	public ProxyForGroup(String nameOfClass, Integer size) throws ConstructionOfReifiedObjectFailedException {
		this.className = nameOfClass;
		this.memberList = new Vector(size.intValue());
		this.proxyForGroupID = new UniqueID();
	}

	public ProxyForGroup() throws ConstructionOfReifiedObjectFailedException {
		this.memberList = new Vector();
		this.proxyForGroupID = new UniqueID();
	}

	public ProxyForGroup(ConstructorCall c, Object[] p) throws ConstructionOfReifiedObjectFailedException {
		this.memberList = new Vector();
		this.proxyForGroupID = new UniqueID();
	}


	/* ----------------------------- GENERAL ---------------------------------- */

	protected void setDispatchingOn() {
		this.dispatching = true;
	}

	protected void setDispatchingOff() {
		this.dispatching = false;
	}

	protected boolean isDispatchingOn () {
		return this.dispatching;
	}
	
	private boolean isDispatchingCall (MethodCall mc) {
		for (int i = 0 ; i < mc.getNumberOfParameter() ; i++)
			if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i)))
				return true;
		return false;
	}

	/* ------------------------ THE PROXY'S METHOD ------------------------ */

	/** The proxy's method, implements the semantic of communication */
	public Object reify(MethodCall mc) throws InvocationTargetException {
		/* result will be a stub on a proxy for group representing the group of results */
		Object result = null;

		/* if OneWay : do not construct result */
		if (AbstractProxy.isOneWayCall(mc))
			oneWayCallOnGroup(mc);

		/* if the call is asynchronous the group of result will be a group a future */
		else // with group : SYNC == ASYNC !!!!
			result = asynchronousCallOnGroup(mc);

		/* A barrier of synchronisation to be sur that all calls are done before continuing the execution */
		this.waitForAllCallsDone();

		return result;
	}

	/* -------------------------- METHOD FOR THREAD CREATION AND BARRIER OF SYNCHRONISATION -------------------------- */

	/** Wait until the method has been apply to all the member */
	protected synchronized void waitForAllCallsDone() {
		while (this.waited != 0) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected synchronized void decrementWaitedAndNotifyAll() {
		waited--;
		notifyAll();
	}

	/* ------------ FOR ASYNCHRONOUS CALL ------------ */

	/**
	 * Create and initialize (and return) the group of result, then launch threads for asynchronous call of each member
	 */
	protected synchronized Object asynchronousCallOnGroup(MethodCall mc) {
		Object result;
		Body body = ProActive.getBodyOnThis();		

		int size = this.memberList.size();
		// Creates a stub + ProxyForGroup for representing the result
		try {
			Object[] paramProxy = new Object[0];
			result = MOP.newInstance(mc.getReifiedMethod().getReturnType().getName(), null, "org.objectweb.proactive.core.group.ProxyForGroup", paramProxy);
			((ProxyForGroup) ((StubObject) result).getProxy()).className = mc.getReifiedMethod().getReturnType().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// Init the lists of result with null value to permit the "set(index)" operation
		Vector memberListOfResultGroup = ((ProxyForGroup) ((StubObject) result).getProxy()).memberList;
		for (int i = 0; i < size; i++) {
			memberListOfResultGroup.add(null);
		}

		// Creating Threads
		if (isDispatchingCall(mc) == false) {
			mc.transformEffectiveArgumentsIntoByteArray();
			for (int index = 0; index < this.memberList.size(); index++)
				this.createThreadForAsync(this.memberList, memberListOfResultGroup, index, mc,body);			
		}
		else { // isDispatchingCall == true
			for (int index = 0; index < memberList.size(); index++) {
				Object[] individualEffectiveArguments = new Object[mc.getNumberOfParameter()];
				for (int i = 0; i < mc.getNumberOfParameter(); i++)
					if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i)))
						individualEffectiveArguments[i] = ProActiveGroup.get(mc.getParameter(i), index % ProActiveGroup.size(mc.getParameter(i)));
					else
						individualEffectiveArguments[i] = mc.getParameter(i);
				this.createThreadForAsync(this.memberList, memberListOfResultGroup, index, new MethodCall(mc.getReifiedMethod(), individualEffectiveArguments), body);
			}
		}

		LocalBodyStore.getInstance().setCurrentThreadBody(body);

		return result;
	}

	private synchronized void createThreadForAsync(Vector memberList, Vector memberListOfResultGroup, int index, MethodCall mc, Body body) {
		new Thread(new ProcessForAsyncCall(this,memberList, memberListOfResultGroup, index, mc, body)).start();
		this.waited++;
	}

	protected synchronized void addToListOfResult(Vector memberListOfResultGroup, Object o, int index) {
		memberListOfResultGroup.set(index, o);
		decrementWaitedAndNotifyAll();
	}

	/* -------------------- FOR ONEWAY CALL ---------------------- */

	/**
	 * Launch threads for OneWay call of each member
	 */
	protected synchronized void oneWayCallOnGroup(MethodCall mc) {
		Body body = ProActive.getBodyOnThis();		
		// Creating Threads

		if (isDispatchingCall(mc) == false) {
			mc.transformEffectiveArgumentsIntoByteArray();
			for (int index = 0; index < this.memberList.size(); index++)
				this.createThreadForOneWay(this.memberList, index, mc, body);
		}
		else { // isDispatchingCall == true
			for (int index = 0; index < memberList.size(); index++) {
				Object[] individualEffectiveArguments = new Object[mc.getNumberOfParameter()];
				for (int i = 0; i < mc.getNumberOfParameter(); i++)
					if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i)))
						individualEffectiveArguments[i] = ProActiveGroup.get(mc.getParameter(i), index % ProActiveGroup.size(mc.getParameter(i)));
					else
						individualEffectiveArguments[i] = mc.getParameter(i);
				this.createThreadForOneWay(this.memberList, index, new MethodCall(mc.getReifiedMethod(), individualEffectiveArguments), body);
			}
		}

		LocalBodyStore.getInstance().setCurrentThreadBody(body);
	}
	

	private synchronized void createThreadForOneWay(Vector memberListStubOfThis, int index, MethodCall mc, Body body) {
		new Thread(new ProcessForOneWayCall(this,memberListStubOfThis, index, mc, body)).start();
		this.waited++;
	}


	/* ------------------- THE COLLECTION'S METHOD ------------------ */

	/** 
	 * If o is a reified object and if it is "assignableFrom" the class of the group, add it into the group
	 *  - if o is a group merge it into the group
	 *  - if o is not a reified object nor a group : do nothing
	 * @param o - element whose presence in this group is to be ensured
	 * @return <code>true</code> if this collection changed as a result of the call
	 */
	public boolean add(Object o) {
		try {
			if ((MOP.forName(this.className)).isAssignableFrom(o.getClass())) {
				/* if o is an reified object and if it is "assignableFrom" the class of the group, ... add it into the group */
				if (MOP.isReifiedObject(o)) {
					return this.memberList.add(o);
				} /* if o is a Group */
				else if (o instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
					/* like an addMerge call */
					return this.memberList.addAll(((org.objectweb.proactive.core.group.ProxyForGroup) o).memberList);
				} /* o is a standard Java object */
				else {
					Object tmp = null;
					try {
						tmp = MOP.newInstance(o.getClass().getName(), null, "org.objectweb.proactive.core.body.future.FutureProxy", null);
					} catch (Exception e) {
						System.err.println("Unable to create a stub+proxy for the new member of the group");
					}
					((org.objectweb.proactive.core.body.future.FutureProxy)((StubObject)tmp).getProxy()).setResult(o);
					return this.add(tmp);
				}
			}
			else {
				System.err.println("uncompatible Object");
				return false;
			}
		}
		catch (java.lang.ClassNotFoundException e) { System.err.println("Unknown class : " + this.className); }
		return true;
	}

	/**
	 * Adds all of the elements in the specified Collection to this Group.
	 * @param <code>c</code> - the elements to be inserted into this Group.
	 * @return <code>true</code> if this collection changed as a result of the call.
	 */
	public boolean addAll(Collection c) {
		boolean modified = false;
		Iterator iterator = c.iterator();
		while (iterator.hasNext()) {
			modified |= this.add(iterator.next());
		}
		return modified;
	}


	/**
	 * Removes all of the elements from this group.
	 * This group will be empty after this method returns.
	 */
	public void clear() {
		this.memberList.clear();
	}

	/**
	 * This method returns true if and only if this group contains at least one element e such that <code>o.equals(e)</code>
	 * @return <code>true</code> if this collection contains the specified element. 
	 */
	public boolean contains(Object o) {
		return this.memberList.contains(o);
	}

	/**
	 * Checks if this Group contains all of the elements in the specified collection.
	 * @param <code>c</code> - the collection to be checked for containment in this Group.
	 * @return <code>true</code> if this Group contains all of the elements in the specified collection
	 */
	public boolean containsAll(Collection c) {
		boolean contained;
		Iterator iterator = c.iterator();
		while (iterator.hasNext()) {
			contained = this.contains(iterator.next());
			if (!contained) return false;
		}
		return true;
	}

	/**
	 * Compares the specified object with this group for equality.
	 */
	public boolean equals(Object o) {
		if (o instanceof org.objectweb.proactive.core.group.ProxyForGroup)
			return this.proxyForGroupID.equals(((org.objectweb.proactive.core.group.ProxyForGroup)o).proxyForGroupID);
		else
			return false;
	}

	/**
	 * Returns the hash code value for this group
	 */
	public int hashCode() {
		return this.memberList.hashCode();
	}

	/**
	 * Check if the group is empty.
	 * @return <code>true</code> if this collection contains no elements.
	 */
 	public boolean isEmpty() {
 		return this.memberList.isEmpty();
 	}

	/**
	 * Returns a ListIterator of the member in the group
	 */
	public Iterator iterator() {
		return this.memberList.listIterator();
	}

	/**
	 * Removes a single instance of the specified element from this Group, if it is present.
	 * It removes the first occurence e where <code>o.equals(e)</code> returns <code>true</code>. 
	 * @param <code>o</code> the element to be removed from this Group (if present).
	 * @return <code>true> if the Group contained the specified element. 
	 */
	public boolean remove(Object o) {
		return this.memberList.remove(o);
	}

	/**
	 * Removes all this Group's elements that are also contained in the specified collection.
	 * After this call returns, this collection will contain no elements in common with the specified collection.
	 * @param <code>c</code> - elements to be removed from this Group.
	 * @return <code>true</code> if this Group changed as a result of the call 
	 */
	public boolean removeAll(Collection c) {
		boolean modified = false;
		Iterator iterator = c.iterator();
		while (iterator.hasNext()) {
			modified |= this.remove(iterator.next());
		}
		return modified;		
	}

	/**
	 * Retains only the elements in this Group that are contained in the specified collection.
	 * It removes from this Group all of its elements that are not contained in the specified collection.
	 * @param <code>c</code> - elements to be retained in this Group.
	 * @return <code>true</code> if this Group changed as a result of the call.
	 */
	public boolean retainAll(Collection c) {
		boolean modified = false;
		Iterator iterator = c.iterator();
		while (iterator.hasNext()) {
			Object tmp = iterator.next();
			if (this.contains(tmp))
				modified |= this.remove(tmp);
		}
		return modified;		

	}
 
	/**
	 * Returns the number of member in this Group.
	 */
	public int size() {
		return this.memberList.size();
	}
	
	/**
	 * Returns an array containing all of the elements in this Group in the correct order.
	 */
	public Object[] toArray() {
		return this.memberList.toArray();
	}

	/**
	 * Returns an array containing all of the elements in this collection;
	 * the runtime type of the returned array is that of the specified array. 
	 * @param <code>a</code> - the array into which the elements of this collection are to be stored, if it is big enough;
	 * otherwise, a new array of the same runtime type is allocated for this purpose.
	 * @return an array containing the elements of this collection
	 */		
	public Object[] toArray(Object[] a) {
		return this.memberList.toArray(a);
	}



	/* ---------------------- THE GROUP'S METHOD ------------------- */


	/** Add all member of the group oGroup into the Group */
	public void addMerge(Object oGroup) {
		try {
			/* check oGroup is an Reified Object and if it is "assignableFrom" the class of the group */
			if ((MOP.isReifiedObject(oGroup)) && ((MOP.forName(this.className)).isAssignableFrom(oGroup.getClass()))) {
				/* check oGroup is an object representing a group */
				if (((StubObject) oGroup).getProxy() instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
					memberList.addAll(((ProxyForGroup) ((StubObject) oGroup).getProxy()).memberList);
				} /* if oGroup is a Standard Active Object (but not a group), just add it */
				else
					this.add(oGroup);
			} /* if oGroup is a Group */
			else if (oGroup instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
				memberList.addAll(((org.objectweb.proactive.core.group.ProxyForGroup) oGroup).memberList);
			}
		} catch (java.lang.ClassNotFoundException e) {
			System.err.println("Unknown class : " + this.className);
		}
	}


	/**
	 * Returns the index of the first occurence of the specified Object <code>obj</code>
	 * @return -1 if the list does not contain this object.
	 */
	public int indexOf(Object obj) {
		return this.memberList.indexOf(obj);
	}

	/**
	 * Removes the element at the specified position.
	 */
	public void remove(int index) {
		this.memberList.remove(index);
	}

	/**
	 * Returns the i-th member of the group
	 */
	public Object get(int i) {
		this.waitForAllCallsDone();
		return this.memberList.get(i);
	}


	/** Return the ("higher") Class of group's member */
	public Class getType() throws java.lang.ClassNotFoundException {
		return MOP.forName(this.className);
	}

	/** Return the full name of ("higher") Class of group's member */
	public String getTypeName() {
		return this.className;
	}

	/** Return an Object representing the group */
	public Object getGroupByType() {
		Object result;
		try { // a new proxy is generated
			result = MOP.newInstance(this.className, null, "org.objectweb.proactive.group.ProxyForGroup", null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		((ProxyForGroup) ((StubObject) result).getProxy()).memberList = this.memberList;
		((ProxyForGroup) ((StubObject) result).getProxy()).className = this.className;
		((ProxyForGroup) ((StubObject) result).getProxy()).proxyForGroupID = this.proxyForGroupID;
		((ProxyForGroup) ((StubObject) result).getProxy()).waited = this.waited;
		return result;
	}
	//  This is the best thing to do, but createStubObject has a private acces !!!! :
	//    // Instanciates the stub object
	//    StubObject stub = MOP.createStubObject(this.className, MOP.forName(this.className));
	//    // Connects the proxy to the stub
	//    stub.setProxy(this);
	//    return stub;
	//  }
	//  An other way is to "store" the stub and return it when asked

	/** To debug, display all members and there position */
	public void testClass() {
		System.out.println(" Nombre d'elements : " + memberList.size());
		for (int i = 0; i < memberList.size(); i++) {
			System.out.println(i + " : " + memberList.get(i).getClass().getName());
		}
	}

	/* ------------------- SYNCHRONIZATION -------------------- */

	protected void waitAll() {
		ProActive.waitForAll(this.memberList);
	}
	
	protected void waitOne() {
		ProActive.waitForAny(this.memberList);
	}

	protected void waitTheNth(int n) {
		ProActive.waitFor(this.memberList.get(n));
	}

	protected void waitN(int n) {
		for (int i = 0; i < n ; i++) {
			this.waitTheNth(i);
		}
	}

	protected Object waitAndGetOne() {
		return this.memberList.get(ProActive.waitForAny(this.memberList));
	}

	protected Object waitAndGetTheNth(int n) {
		ProActive.waitForTheNth(this.memberList,n);
		return this.memberList.get(n);
	}

	protected boolean allAwaited() {
		for (int i = 0 ; i < this.memberList.size() ; i++)
			if (!(ProActive.isAwaited(this.memberList.get(i))))
				return false;
		return true;
	}

	protected boolean allArrived() {
		for (int i = 0 ; i < this.memberList.size() ; i++)
			if (ProActive.isAwaited(this.memberList.get(i)))
				return false;
		return true;
	}


	/* ---------------------- METHOD FOR SYNCHRONOUS CREATION OF A TYPED GROUP ---------------------- */

	protected synchronized void createThreadCreation(String className, Object[] param, String node) {
		new Thread(new ProcessForGroupCreation(this, className, param, node)).start();
		waited++;
	}


	/* ------------------------ PRIVATE METHODS FOR SERIALIZATION --------------------- */

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		this.waitForAllCallsDone();
		// Now that all the results are available, we can copy the group (of future)
		out.defaultWriteObject();
	}

	//for the moment, we set the value of migration to false here
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.proxyForGroupID = new UniqueID();
	}

}