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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.group.spmd.MethodCallSetSPMDGroup;
import org.objectweb.proactive.core.group.threadpool.ThreadPool;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;


public class ProxyForGroup extends AbstractProxy
    implements org.objectweb.proactive.core.mop.Proxy, Group,
        java.io.Serializable {

    /** The logger for the Class */
    protected static Logger logger = Logger.getLogger(ProxyForGroup.class.getName());

    /** The name of the Class : all members of the group are "className" assignable */
    protected String className;

	/** The list of member : it contains exclusively, StubObjects connected to Proxies, or Java Objects */
	protected Vector memberList;

	/** The hashmap : to associate a name to the members */
	protected HashMap hashList;

    /** Unique identificator for body (avoid infinite loop in some hierarchicals groups) */

    // NOT FULLY IMPLEMENTED !!!
    transient private UniqueID proxyForGroupID;

    /** Number of awaited methodcall on the group's member. The Semantic is : we wait all call are done before continuing */
    protected int waited = 0;

    /** Flag to deternime the semantic of communication (broadcast or dispatching) */
    protected boolean dispatching = false;

    /** Flag to deternime the semantic of communication (unique serialization of parameters or not) */
    protected boolean uniqueSerialization = false;

    /** The stub of the typed group */
    protected StubObject stub;

    /** A pool of thread to serve the request */
    transient private ThreadPool threadpool;

    /* ----------------------- CONSTRUCTORS ----------------------- */
    public ProxyForGroup(String nameOfClass)
        throws ConstructionOfReifiedObjectFailedException {
        this.className = nameOfClass;
        this.memberList = new Vector();
        this.proxyForGroupID = new UniqueID();
        this.threadpool = new ThreadPool();
    }

    public ProxyForGroup(String nameOfClass, Integer size)
        throws ConstructionOfReifiedObjectFailedException {
        this.className = nameOfClass;
        this.memberList = new Vector(size.intValue());
        this.proxyForGroupID = new UniqueID();
        this.threadpool = new ThreadPool();
    }

    public ProxyForGroup() throws ConstructionOfReifiedObjectFailedException {
        this.memberList = new Vector();
        this.proxyForGroupID = new UniqueID();
        this.threadpool = new ThreadPool();
    }

    public ProxyForGroup(ConstructorCall c, Object[] p)
        throws ConstructionOfReifiedObjectFailedException {
        this.memberList = new Vector();
        this.proxyForGroupID = new UniqueID();
        this.threadpool = new ThreadPool();
    }

    /* ----------------------------- GENERAL ---------------------------------- */

    /**
     * Allows the Group to dispatch parameters.
     */
    protected void setDispatchingOn() {
        this.dispatching = true;
    }

    /**
     * Allows the Group to broadcast parameters.
     */
    protected void setDispatchingOff() {
        this.dispatching = false;
    }

    /**
     * Allows the Group to make an unique serialization of parameters.
     */
    protected void setUniqueSerializationOn() {
        this.uniqueSerialization = true;
    }

    /**
     * Removes the ability of the Group to make an unique serialization of parameters..
     */
    protected void setUniqueSerializationOff() {
        this.uniqueSerialization = false;
    }

    /**
     * Checks the semantic of communication of the Group.
     * @return <code>true</code> if the "scatter option" is enabled.
     */
    protected boolean isDispatchingOn() {
        return this.dispatching;
    }

    private boolean isDispatchingCall(MethodCall mc) {
        for (int i = 0; i < mc.getNumberOfParameter(); i++)
            if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i))) {
                return true;
            }
        return false;
    }

    /* ------------------------ THE PROXY'S METHOD ------------------------ */

    /**
     *  The proxy's method : implements the semantic of communication. This method invokes the
     * method call <code>mc</code> on each members of the Group.
     * @param <code>mc</code> the MethodCall to apply on each member of the Group.
     * @return the result of the call : <b> the result of a method call on a typed group is a
     * typed group</b>.
     * @throws InvocationTargetException if a problem occurs when invoking the method on the members of the Group
     */
    public Object reify(MethodCall mc) throws InvocationTargetException {

		//System.out.println("A method is called : \"" + mc.getName() + "\" on " + this.memberList.size() + " membres.");

        /* if the method called is toString, apply it to the proxy, not to the members */
        if ("toString".equals(mc.getName())) {
            return this.toString();
        }

        /* if the method called is hashCode, apply it to the proxy, not to the members */
        if ("hashCode".equals(mc.getName())) {
            return new Integer(this.hashCode());
        }

        /* result will be a stub on a proxy for group representing the group of results */
        Object result = null;

        /* check if the threadpool is big enough to make the call (is there is not enough thread, create new ones) */
        this.threadpool.checkNumberOfThreads(this.memberList.size());

        /* if OneWay : do not construct result */
        if (AbstractProxy.isOneWayCall(mc)) {
            this.oneWayCallOnGroup(mc);
        }
        /* Special case : the method returns void but is Synchronous because it throws Exception */
        else if (mc.getReifiedMethod().getReturnType() == Void.TYPE) {
            this.oneWayCallOnGroup(mc);
        }
        /* if the call is asynchronous the group of result will be a group a future */
        else { // with group in general case : SYNC == ASYNC !!!!
            result = this.asynchronousCallOnGroup(mc);
        }

        /* A barrier of synchronisation to be sur that all calls are done before continuing the execution */
        this.threadpool.complete();

        return result;
    }

    /* ------------ FOR ASYNCHRONOUS CALL ------------ */

    /**
     * Creates and initializes (and returns) the group of result, then launch threads for asynchronous call of each member.
     * @param <code>mc</code> the MethodCall to be applied on each member of the Group.
     * @return the result of the call.
     */
    protected synchronized Object asynchronousCallOnGroup(MethodCall mc) {
        Object result;
        Body body = ProActive.getBodyOnThis();

        // Creates a stub + ProxyForGroup for representing the result
        try {
            Object[] paramProxy = new Object[0];
            result = MOP.newInstance(mc.getReifiedMethod().getReturnType()
                                       .getName(), null,
                    ProxyForGroup.class.getName(), paramProxy);
            ((ProxyForGroup) ((StubObject) result).getProxy()).className = mc.getReifiedMethod()
                                                                             .getReturnType()
                                                                             .getName();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        int size = this.memberList.size();

        // Init the lists of result with null value to permit the "set(index)" operation
        Vector memberListOfResultGroup = ((ProxyForGroup) ((StubObject) result).getProxy()).memberList;
        for (int i = 0; i < size; i++) {
            memberListOfResultGroup.add(null);
        }

        // Creating Threads
        if (isDispatchingCall(mc) == false) {
            if (uniqueSerialization) {
                mc.transformEffectiveArgumentsIntoByteArray();
            }
            for (int index = 0; index < this.memberList.size(); index++)
                this.threadpool.addAJob(new ProcessForAsyncCall(this,
                        this.memberList, memberListOfResultGroup, index, mc,
                        body));
        } else { // isDispatchingCall == true
            for (int index = 0; index < memberList.size(); index++) {
                Object[] individualEffectiveArguments = new Object[mc.getNumberOfParameter()];
                for (int i = 0; i < mc.getNumberOfParameter(); i++)
                    if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i))) {
                        individualEffectiveArguments[i] = ProActiveGroup.get(mc.getParameter(
                                    i),
                                index % ProActiveGroup.size(mc.getParameter(i)));
                    } else {
                        individualEffectiveArguments[i] = mc.getParameter(i);
                    }
                this.threadpool.addAJob(new ProcessForAsyncCall(this,
                        this.memberList, memberListOfResultGroup, index,
                        new MethodCall(mc.getReifiedMethod(),
                            individualEffectiveArguments), body));
            }
        }

        LocalBodyStore.getInstance().setCurrentThreadBody(body);

        return result;
    }

    /**
     * Add the results (Future) into the typed group result at the correct poisition.
     * @param <code>memberListOfResultGroup</code> the list of the typed group result.
     * @param <code>result</code> the result of a call on member of a Group.
     * @param <code>index</code> the rank of the result.
     */
    protected synchronized void addToListOfResult(
        Vector memberListOfResultGroup, Object result, int index) {
        memberListOfResultGroup.set(index, result);
    }

    /* -------------------- FOR ONEWAY CALL ---------------------- */

    /**
     * Launchs the threads for OneWay call of each member of the Group.
     * @param <code>mc</code> the MethodCall to be applied on each member of the Group.
     */
    protected synchronized void oneWayCallOnGroup(MethodCall mc) {
        Body body = ProActive.getBodyOnThis();
        ExceptionList exceptionList = new ExceptionList();

        // Creating Threads
        if (isDispatchingCall(mc) == false) {
            if (uniqueSerialization) {
                mc.transformEffectiveArgumentsIntoByteArray();
            }
            for (int index = 0; index < this.memberList.size(); index++) {
                this.threadpool.addAJob(new ProcessForOneWayCall(this,
                        this.memberList, index, mc, body, exceptionList));
	        }
        } else { // isDispatchingCall == true
            for (int index = 0; index < memberList.size(); index++) {
                Object[] individualEffectiveArguments = new Object[mc.getNumberOfParameter()];
                for (int i = 0; i < mc.getNumberOfParameter(); i++)
                    if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i))) {
                        individualEffectiveArguments[i] = ProActiveGroup.get(mc.getParameter(
                                    i),
                                index % ProActiveGroup.size(mc.getParameter(i)));
                    } else {
                        individualEffectiveArguments[i] = mc.getParameter(i);
                    }
                this.threadpool.addAJob(new ProcessForOneWayCall(this,
                        this.memberList, index,
                        new MethodCall(mc.getReifiedMethod(),
                            individualEffectiveArguments), body, exceptionList));
            }
        }

        LocalBodyStore.getInstance().setCurrentThreadBody(body);

        if (exceptionList.size() != 0) {
            throw exceptionList;
        }
    }

    /* ------------------- THE COLLECTION'S METHOD ------------------ */

    /**
     * If o is a reified object and if it is "assignableFrom" the class of the group, add it into the group<br>
     *  - if o is a group merge it into the group<br>
     *  - if o is not a reified object nor a group : do nothing<br>
     * @param o - element whose presence in this group is to be ensured
     * @return <code>true</code> if this collection changed as a result of the call
     */
    public boolean add(Object o) {
        try {
            if ((MOP.forName(this.className)).isAssignableFrom(o.getClass())) {

                /* if o is an reified object and if it is "assignableFrom" the class of the group, ... add it into the group */
                if (MOP.isReifiedObject(o)) {
                    boolean result = this.memberList.add(o);

                    /* in the particular case that o extends GroupMember, there are few more operations */
                    if (o instanceof org.objectweb.proactive.core.group.GroupMember) {
                        ((org.objectweb.proactive.core.group.GroupMember) o).setMyGroup(stub);
                        ((org.objectweb.proactive.core.group.GroupMember) o).setMyRank(this.memberList.indexOf(
                                o));
                    }
                    return result;
                }
                // COMPONENTS

                /* if o is a reference on a component interface*/
                else if (o instanceof ProActiveInterface) {
                    return this.memberList.add(o);
                } /* if o is a Group */ else if (o instanceof org.objectweb.proactive.core.group.ProxyForGroup) {

                    /* like an addMerge call */
                    return this.memberList.addAll(((org.objectweb.proactive.core.group.ProxyForGroup) o).memberList);
                } /* o is a standard Java object */ else {
                    return this.memberList.add(o);
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("uncompatible Object");
                }
                return false;
            }
        } catch (java.lang.ClassNotFoundException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Unknown class : " + this.className);
            }
        }
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
            if (!contained) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the specified object with this group for equality.
     * @param <o> the Object for wich we test the equality.
     * @return <code>true</code> if <code>o</code> is the same Group as <code>this</code>.
     */
    public boolean equals(Object o) {
        if (o instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
            return this.proxyForGroupID.equals(((org.objectweb.proactive.core.group.ProxyForGroup) o).proxyForGroupID);
        } else {
            return false;
        }
    }

    /**
     * Returns the hash code value for this Group.
     * @return the hash code value for this Group.
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
     * Returns an Iterator of the member in the Group.
     * @return an Iterator of the member in the Group.
     */
    public Iterator iterator() {
        return this.memberList.iterator();
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
            if (this.contains(tmp)) {
                modified |= this.remove(tmp);
            }
        }
        return modified;
    }

    /**
     * Returns the number of member in this Group.
     * @return the number of member in this Group.
     */
    public int size() {
        return this.memberList.size();
    }

    /**
     * Returns an array containing all of the elements in this Group in the correct order.
     * @return an array containing all of the elements in this Group in the correct order.
     */
    public Object[] toArray() {
        return this.memberList.toArray();
    }

    /**
     * Returns an array containing all of the elements in this collection;
     * the runtime type of the returned array is that of the specified array.
     * @param <code>a</code> - the array into which the elements of this collection are to be stored, if it is big enough;
     * otherwise, a new array of the same runtime type is allocated for this purpose.
     * @return an array containing the elements of this collection.
     */
    public Object[] toArray(Object[] a) {
        return this.memberList.toArray(a);
    }

    /* ---------------------- THE GROUP'S METHOD ------------------- */

    /**
     *  Add all member of the group <code>ogroup</code> into the Group. <code>ogroup</code> can be :<br>
     * - a typed group<br>
     * - a Group<br>
     * - a standard Object<br>
     * but it have to be (or to extend) the Class of the Group.
     * @param <code>ogroup</code> the object(s) to merge into the Group.
     */
    public void addMerge(Object oGroup) {
        try {

            /* check oGroup is an Reified Object and if it is "assignableFrom" the class of the group */
            if ((MOP.isReifiedObject(oGroup)) &&
                    ((MOP.forName(this.className)).isAssignableFrom(
                        oGroup.getClass()))) {

                /* check oGroup is an object representing a group */
                if (((StubObject) oGroup).getProxy() instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
                    memberList.addAll(((ProxyForGroup) ((StubObject) oGroup).getProxy()).memberList);
                } /* if oGroup is a Standard Active Object (but not a group), just add it */ else {
                    this.add(oGroup);
                }
            } /* if oGroup is a Group */ else if (oGroup instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
                memberList.addAll(((org.objectweb.proactive.core.group.ProxyForGroup) oGroup).memberList);
            }
        } catch (java.lang.ClassNotFoundException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Unknown class : " + this.className);
            }
        }
    }

    /**
     * Returns the index of the first occurence of the specified Object <code>obj</code>.
     * @param <code>obj</code> the obj tahat is searched in the Group.
     * @return the rank of <code>obj</code> in the Group.
     * -1 if the list does not contain this object.
     */
    public int indexOf(Object obj) {
        return this.memberList.indexOf(obj);
    }

    /**
     * Returns a list iterator of the members in this Group (in proper sequence).
     * @return a list iterator of the members in this Group.
     */
    public ListIterator listIterator() {
        return this.memberList.listIterator();
    }

    /**
     * Removes the element at the specified position.
     * @param <code>index</code> the rank of the object to remove in the Group.
     */
    public void remove(int index) {
        this.memberList.remove(index);
    }

    /**
     * Returns the i-th member of the group.
     * @param <code>index</code> the rank of the object to return.
     * @return the member of the Group at the specified rank.
     */
    public Object get(int i) {
        return this.memberList.get(i);
    }

    /**
     * Returns the ("higher") Class of group's member.
     * @return the Class that all Group's members are (or extend).
     * @throws java.lang.ClassNotFoundException if the class name of the Group is not known.
     */
    public Class getType() throws java.lang.ClassNotFoundException {
        return MOP.forName(this.className);
    }

    /**
     * Returns the full name of ("higher") Class of group's member
     * @return the name of the Class that all Group's members are (or extend).
     */
    public String getTypeName() {
        return this.className;
    }

    /**
     * Returns an Object (a <b>typed group</b> Object) representing the Group
     * @return a typed group corresponding to the Group.
     */
    public Object getGroupByType() {
        Object result;
        try { // a new proxy is generated
            result = MOP.newInstance(this.className, null,
                    ProxyForGroup.class.getName(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        ProxyForGroup proxy = (ProxyForGroup) ((StubObject) result).getProxy();
        proxy.memberList = this.memberList;
        proxy.className = this.className;
        proxy.proxyForGroupID = this.proxyForGroupID;
        proxy.waited = this.waited;
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

	/**
	 * Creates a new group with all members of the group and all the members of the group <code>g</code>
	 * @param g - a group
	 * @return a group that contain all the members of the group and <code>g</code>. <code>null<code> if the class of the group is incompatible.
	 */
	public Group union (Group g) {
		try {
			if ((MOP.forName(this.getTypeName())).isAssignableFrom(MOP.forName(g.getTypeName()))) {
				ProxyForGroup result = new ProxyForGroup(this.getTypeName());
				// add the members of this
				Iterator it = this.iterator();
				while (it.hasNext()) {
					result.add(it.next()); 
				}
				// add the members of g
				it = g.iterator();
				while (it.hasNext()) {
					result.add(it.next()); 
				}
				return result; 
			} }
		catch (ClassNotFoundException e) { e.printStackTrace();	}
		catch (ConstructionOfReifiedObjectFailedException e) { e.printStackTrace(); }
		// the group are incompatible (i.e. they have not members of the the same class)
		return null;
	}

	/**
	 * Creates a new group with all members that belong to the group and to the group <code>g</code>.
	 * @param g - a group
	 * @return a group that contain the common members of the group and <code>g</code>. <code>null<code> if the class of the group is incompatible.
	 */
	public Group intersection (Group g) {
		try {
			if ((MOP.forName(this.getTypeName())).isAssignableFrom(MOP.forName(g.getTypeName()))) {
				ProxyForGroup result = new ProxyForGroup(this.getTypeName());
				Object member;
				Iterator it = this.iterator();
				// add the members of the group that belong to g
				while (it.hasNext()) {
					member = it.next();
					if (g.indexOf(member) > -1) {
						result.add(member);
					} 
				}
				return result; 
			} }
		catch (ClassNotFoundException e) { e.printStackTrace();	}
		catch (ConstructionOfReifiedObjectFailedException e) { e.printStackTrace(); }
		// the group are incompatible (i.e. they have not members of the the same class)
		return null;
	}

	/**
	 * Creates a new group with the members that belong to the group, but not to the group <code>g</code>.
	 * @param g - a group
	 * @return a group that contain the members of the group without the member <code>g</code>. <code>null<code> if the class of the group is incompatible.
	 */
	public Group exclude (Group g) {
		try {
			if ((MOP.forName(this.getTypeName())).isAssignableFrom(MOP.forName(g.getTypeName()))) {
				ProxyForGroup result = new ProxyForGroup(this.getTypeName());
				Object member;
				Iterator it = this.iterator();
				while (it.hasNext()) {
					member = it.next();
					if (g.indexOf(member) < 0) {
						result.add(member);
					} 
				}
				return result; 
			} }
		catch (ClassNotFoundException e) { e.printStackTrace();	}
		catch (ConstructionOfReifiedObjectFailedException e) { e.printStackTrace(); }
		// the group are incompatible (i.e. they have not members of the the same class)
		return null;
	}

	/**
	 * Creates a new group with all members that belong to the group or to the group <code>g</code>, but not to both.
	 * @param g - a group
	 * @return a group that contain the non-common members of the group and <code>g</code>. <code>null<code> if the class of the group is incompatible.
	 */
	public Group difference (Group g) {
		try {
			if ((MOP.forName(this.getTypeName())).isAssignableFrom(MOP.forName(g.getTypeName()))) {
				ProxyForGroup result = new ProxyForGroup(this.getTypeName());
				Object member;
				Iterator it = this.iterator();
				// add the members of the group that do not belong to g
				while (it.hasNext()) {
					member = it.next();
					if (g.indexOf(member) < 0) {
						result.add(member);
					} 
				}
				it = g.iterator();
				// add the members of g that do not belong to the group
				while (it.hasNext()) {
					member = it.next();
					if (this.indexOf(member) < 0) {
						result.add(member);
					} 
				}
				return result; 
			} }
		catch (ClassNotFoundException e) { e.printStackTrace();	}
		catch (ConstructionOfReifiedObjectFailedException e) { e.printStackTrace(); }
		// the group are incompatible (i.e. they have not members of the the same class)
		return null;
	}

	/**
	 * Creates a new group with the members of the group begining at the index <code>begin</code> and ending at the index <code>end</code>.
	 * @param begin - the begining index
	 * @param end - the ending index
	 * @return a group that contain the members of the group from <code>begin</code> to <code>end</code>. <code>null</code> if <code>begin > end</code>.
	 */
	public Group range (int begin, int end) {
		// bag arguments => return null
		if (begin > end) {
			return null;
		}
		if (begin < 0) {
			begin = 0;
		}
		if (end > this.size()) {
			end = this.size();
		}
		try {
			ProxyForGroup result = new ProxyForGroup(this.getTypeName());
			for (int i = begin ; i <= end ; i++) {
				result.add(this.get(i));
			}
			return result;
		}
		catch (ConstructionOfReifiedObjectFailedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Communicates the SPMD Group to members
	 * @param spmdGroup - the SPMD group
	 */
	public void setSPMDGroup (Object spmdGroup) {
		try {
			this.reify(new MethodCallSetSPMDGroup(spmdGroup)); }
		catch (InvocationTargetException e) {
			logger.info("Unable to set the SPMD group");
			e.printStackTrace();
		}
	}

    /**
     * To debug, display the size of the Group and all its members with there position
     */
    public void display() {
        logger.info("Number of member : " + memberList.size());
        for (int i = 0; i < memberList.size(); i++)
            logger.info("  " + i + " : " +
                memberList.get(i).getClass().getName());
    }

    /* ------------------- SYNCHRONIZATION -------------------- */

    /**
     * Waits that all the members are arrived.
     */
    public void waitAll() {
        ProActive.waitForAll(this.memberList);
    }

    /**
     * Waits that at least one member is arrived.
     */
    public void waitOne() {
        ProActive.waitForAny(this.memberList);
    }

    /**
     * Waits that the member at the specified rank is arrived.
     * @param <code>index</code> the rank of the awaited member.
     */
    public void waitTheNth(int n) {
        ProActive.waitFor(this.memberList.get(n));
    }

    /**
     * Waits that at least <code>n</code> members are arrived.
     * @param <code>n</code> the number of awaited members.
     */
    public void waitN(int n) {
        for (int i = 0; i < n; i++) {
            this.waitTheNth(i);
        }
    }

    /**
     * Waits that at least one member is arrived and returns it.
     * @return a non-awaited member of the Group.
     */
    public Object waitAndGetOne() {
        return this.memberList.get(ProActive.waitForAny(this.memberList));
    }

	/**
	 * Waits one future is arrived and returns it (removes it from the group).
	 * @return a member of <code>o</code>. (<code>o</code> is removed from the group)
	 */
	public Object waitAndGetOneThenRemoveIt() {
		return this.memberList.remove(ProActive.waitForAny(this.memberList));
	}


    /**
     * Waits that the member at the specified rank is arrived and returns it.
     * @param <code>n</code> the rank of the wanted member.
     * @return the member (non-awaited) at the rank <code>n</code> in the Group.
     */
    public Object waitAndGetTheNth(int n) {
        ProActive.waitForTheNth(this.memberList, n);
        return this.memberList.get(n);
    }

    /**
     * Waits that at least one member is arrived and returns its index.
     * @return the index of a non-awaited member of the Group.
     */
    public int waitOneAndGetIndex() {
        int index = 0;
        this.memberList.get(ProActive.waitForAny(this.memberList));
        while (ProActive.isAwaited(this.memberList.get(index))) {
            index++;
        }
        return index;
    }

    /**
     * Checks if all the members of the Group are awaited.
     * @return <code>true</code> if all the members of the Group are awaited.
     */
    public boolean allAwaited() {
        for (int i = 0; i < this.memberList.size(); i++)
            if (!(ProActive.isAwaited(this.memberList.get(i)))) {
                return false;
            }
        return true;
    }

    /**
     * Checks if all the members of the Group are arrived.
     * @return <code>true</code> if all the members of the Group are arrived.
     */
    public boolean allArrived() {
        for (int i = 0; i < this.memberList.size(); i++)
            if (ProActive.isAwaited(this.memberList.get(i))) {
                return false;
            }
        return true;
    }

    /**
     * Returns an ExceptionList containing all the throwables (exceptions and errors) occured
     * when this group was built
     * @return an ExceptionList
     */
    public ExceptionList getExceptionList() {
        ExceptionList exceptionList = new ExceptionList();
        for (int i = 0; i < this.memberList.size(); i++) {
            if (this.memberList.get(i) instanceof Throwable) {
                exceptionList.add(new ExceptionInGroup(null,
                        (Throwable) this.memberList.get(i)));
            }
        }
        return exceptionList;
    }

    /**
     * Removes all exceptions and null references contained in the Group.
     * Exceptions (and null references) appears with communication/program-level/runtime errors
     * and are stored in the Group.
     * (After this operation the size of the Group decreases)
     */
    public void purgeExceptionAndNull() {
        Iterator it = this.memberList.iterator();
        while (it.hasNext()) {
            Object element = (Object) it.next();
            if ((element instanceof Throwable) || (element == null)) {
                it.remove();
            }
        }
    }

    /**
     * Modifies the number of members served by one thread
     * @param i - the new ratio
     */
    public void setRatioNemberToThread(int i) {
        this.threadpool.ratio(i);
    }

	/**
	 * Modifies the number of additional threads to serve members
	 * @param i - the new number
	 */
	public void setAdditionalThread(int i) {
		this.threadpool.thread(i);
	}

    /* ---------------------- METHOD FOR SYNCHRONOUS CREATION OF A TYPED GROUP ---------------------- */

    /**
     * Builds the members using the threads (of the threadpool).
     * @param <code>className</code> the name of the Class of the members.
     * @param <code>param</code> an array that contains the parameters for the constructor of member.
     * @param <code>node</code> the node where the member will be created.
     */
    protected void createMemberWithMultithread(String className,
        Object[][] params, String[] nodeList) {
        // Initializes the Group to the correct size
        for (int i = 0; i < params.length; i++) {
            this.memberList.add(null);
        }
        for (int i = 0; i < params.length; i++) {
            this.threadpool.addAJob(new ProcessForGroupCreation(this,
                    className, params[i], nodeList[i % nodeList.length], i));
        }
        this.threadpool.complete();
    }

    /**
     * Sets an object to the specified position in the Group
     * @param index - the position
     * @param o - the object to add
     */
    protected void set(int index, Object o) {
        this.memberList.set(index, o);
    }

    /* ------------------------ PRIVATE METHODS FOR SERIALIZATION --------------------- */
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        // this.threadpool.finalize();
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.proxyForGroupID = new UniqueID();
        this.threadpool = new ThreadPool();
    }


	//Map class style method
	/**
	 * Returns <code>true</code> if this Group contains a mapping for the specified key.
	 * More formally, returns <code>true</code> if and only if this Group contains at
	 * a mapping for a key <code>k</code> such that <code>(key==null ? k==null : key.equals(k))</code>.
	 * (There can be at most one such mapping.)
	 * @param key - key whose presence in this Group is to be tested.
	 * @return <code>true</code> if this Group contains a mapping for the specified key.
	 * @throws ClassCastException - if the key is of an inappropriate type for this Group (optional).
	 * @throws NullPointerException - if the key is null and this Group does not not permit null keys (optional).
	 */
	public boolean containsKey(String key) {
		return this.hashList.containsKey(key);
	}

	/**
	 * Returns <code>true</code> if this Group maps one or more keys to the specified value.
	 * More formally, returns <code>true</code> if and only if this Group contains at least
	 * one mapping to a value <code>v</code> such that <code>(value==null ? v==null : value.equals(v))</code>.
	 * @param value - value whose presence in this map is to be tested.
	 * @return <code>true</code> if this Group maps one or more keys to the specified value.
	 * @throws ClassCastException - if the value is of an inappropriate type for this Collection (optional).
	 * @throws NullPointerException - if the value is null and this Group does not not permit null values (optional).  
	 */
	public boolean containsValue(Object value) {
		return this.memberList.contains(value);
	}


	/**
 	 * Returns the Object to which this Group maps the specified key.
 	 * Returns <code>null</code> if the Collection contains no mapping for this key.
 	 * A return value of <code>null</code> does not necessarily indicate that the Collection
 	 * contains no mapping for the key; it's also possible that the Group explicitly maps the key to null.
 	 * The containsKey operation may be used to distinguish these two cases.
 	 * More formally, if this Group contains a mapping from a key <code>k</code> to a value
 	 * <code>v</code> such that <code>(key==null ? k==null : key.equals(k))</code>,
 	 * then this method returns <code>v</code>; otherwise it returns <code>null</code>.
 	 * (There can be at most one such mapping.)
 	 * @param key - key whose associated value is to be returned.
 	 * @return the value to which this map maps the specified key, or <code>null</code> if the map contains no mapping for this key.
 	 * @throws ClassCastException - if the key is of an inappropriate type for this Group (optional).
 	 * @throws NullPointerException - key is <code>null</code> and this Group does not not permit null keys (optional).
	 */
	public Object get(String key) {
		return this.hashList.get(key);
	}

	/**
	 * Associates the specified value with the specified key in this Group (optional operation).
	 * If the Group previously contained a mapping for this key, the old value is replaced by
	 * the specified value. (A map <code>m</code> is said to contain a mapping for a key
	 * <code>k</code> if and only if <code>m.containsKey(k)</code> would return <code>true</code>.))
	 * @param key - key with which the specified value is to be associated.
	 * @param value - value to be associated with the specified key.
	 * @throws UnsupportedOperationException - if the put operation is not supported by this Group.
	 * @throws ClassCastException - if the class of the specified key or value prevents it from being stored in this Group.
	 * @throws IllegalArgumentException - if some aspect of this key or value prevents it from being stored in this Group.
	 * @throws NullPointerException - this map does not permit null keys or values, and the specified key or value is <code>null</code>.
	 */
	public synchronized void put(String key, Object value) {
		this.add(value);
		this.hashList.put(key,new Integer(this.size()-1));
	}


	/**
	 *	Returns a set view of the keys contained in this Group.
	 * The set is backed by the Group, so changes to the Group are reflected in the set,
	 * and vice-versa. If the Group is modified while an iteration over the set is in progress,
	 * the results of the iteration are undefined. The set supports element removal,
	 * which removes the corresponding mapping from the Group, via the Iterator.remove,
	 * Set.remove, removeAll retainAll, and clear operations.
	 * It does not support the add or addAll operations.
	 * @return a set view of the keys contained in this Group.
	 */
	public Set keySet() {
		return this.hashList.keySet();
	}


}
