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


import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.InvalidProxyClassException;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.GenerationOfStubClassFailedException;
import org.objectweb.proactive.core.mop.InvalidProxyClassException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;



/**
 * This class provides static methods to manage object representing a Group.
 *
 * @author Laurent Baduel - INRIA
 *
 */
public class ProActiveGroup {


    public static final String DEFAULT_PROXYFORGROUP_CLASS_NAME = "org.objectweb.proactive.core.group.ProxyForGroup";



    /** Return a Group corresponding to the Object o representing a group. Return null if o does not representing a group. */
    public static Group getGroup(Object o) {
        /* check o is an Reified Object representing a group */
	if ( (MOP.isReifiedObject(o)) && (((StubObject)o).getProxy() instanceof org.objectweb.proactive.core.group.ProxyForGroup) )
	    return ((ProxyForGroup)((StubObject)o).getProxy());
	else
	    return null;
    }
    



    /** Create an object representing an empty group specifying the upper class of member. */
    public static Object newActiveGroup(String className) throws ClassNotFoundException, ClassNotReifiableException {
	
	MOP.checkClassIsReifiable(MOP.forName(className));
	
	Object result = null;
	
	try {
	    result = MOP.newInstance (className, null , DEFAULT_PROXYFORGROUP_CLASS_NAME, null);
 	}
 	catch (ClassNotReifiableException e) { System.err.println("**** ClassNotReifiableException ****"); }
 	catch (InvalidProxyClassException e) { System.err.println("**** InvalidProxyClassException ****"); }
 	catch (ConstructionOfProxyObjectFailedException e) { System.err.println("**** ConstructionOfProxyObjectFailedException ****"); }
 	catch (ConstructionOfReifiedObjectFailedException e) { System.err.println("**** ConstructionOfReifiedObjectFailedException ****"); }

	((org.objectweb.proactive.core.group.ProxyForGroup)(((StubObject)result).getProxy())).className = className;
	
	return result;
    }



    /** Create an object representing a group and create members with params cycling on nodeList. */
    public static Object newActiveGroup(String className, Object[][] params, Node[] nodeList)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {

	Object result = newActiveGroup(className);
	Group g = ProActiveGroup.getGroup(result);

	for (int i=0 ; i < params.length ; i++)
	    g.add(ProActive.newActive(className, params[i], nodeList[i % nodeList.length]));
	
	return result;
    }

   /** Create an object representing a group and create members with params cycling on nodeList. */
    public static Object newActiveGroupTHREAD(String className, Object[][] params, String[] nodeList)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {

	Object result = newActiveGroup(className);
	Group g = ProActiveGroup.getGroup(result);


	for (int i = 0 ; i < params.length ; i++)
	    ((org.objectweb.proactive.core.group.ProxyForGroup)g).createThreadCreation(className, params[i], nodeList[i % nodeList.length]);

	((org.objectweb.proactive.core.group.ProxyForGroup)g).waitForTheWaitedCreation();

	return result;
    }
    
    
    /**
     * Wait for all the futures are arrived.
     */
    public static void waitAll(Object o) {
	if (MOP.isReifiedObject (o)) {
	    org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    // If the object represents a group, we use the proxyForGroup's method
 	    if (theProxy != null)
		((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).futureList.waitAll();
 	    // Else the "standard waitFor" method has been used in the findProxyForGroup method
	}
    }
    
    
    
    /**
     * Wait for one future is arrived.
     */
    public static void waitOne(Object o) {
	if (MOP.isReifiedObject (o)) {
	    org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    // If the object represents a group, we use the proxyForGroup's method
 	    if (theProxy != null)
		((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).futureList.waitOne();
 	    // Else the "standard waitFor" method has been used in the findProxyForGroup method
	}
    }


    
    /**
     * Wait n futures are arrived.
     */
    public static void waitN(Object o, int n) {
	if (MOP.isReifiedObject (o)) {
	    org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    // If the object represents a group, we use the proxyForGroup's method
 	    if (theProxy != null)
		((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).futureList.waitN(n);
 	    // Else the "standard waitFor" method has been used in the findProxyForGroup method
	}
    }
    
			

    /**
     * Tests if all the member of the object <code>o</code> representing a group are awaited or not.
     * Always returns <code>false</code> if <code>o</code> is not a reified object (future or group).
     */
    public static boolean allAwaited (Object o) {
	// If the object is not reified, it cannot be a future (or a group of future)
	if (!(MOP.isReifiedObject (o)))
	    return false;
	else {
	    org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    // If the object represents a group, we use the proxyForGroup's method
 	    if (theProxy != null)
		return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).futureList.allAwaited();
 	    // Else the "standard waitFor" method has been used in the findProxyForGroup method so the future is arrived
	    else
		return false;
	}
    }
	    

    /**
     * Tests if all the member of the object <code>o</code> representing a group are arrived or not.
     * Always returns <code>true</code> if <code>o</code> is not a reified object (future or group).
     */
    public static boolean allArrived (Object o) {
    	// If the object is not reified, it cannot be a future (or a group of future)
	if (!(MOP.isReifiedObject (o)))
	    return true;
	else {
	    org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    // If the object represents a group, we use the proxyForGroup's method
 	    if (theProxy != null)
		return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).futureList.allArrived();
 	    // Else the "standard waitFor" method has been used in the findProxyForGroup method so the future is arrived
	    else
		return true;
	}
    }


    /**
     * Wait one future is arrived and get it.
     */
    public static Object waitAndGetOne (Object o) {
	if (MOP.isReifiedObject (o)) {
	    org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    // If the object represents a group, we use the proxyForGroup's method
	    if (theProxy != null)
		return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).futureList.waitAndGetOne();
	    // Else the "standard waitFor" method has been used in the findProxyForGroup method so the future is arrived, just return it
	    else
		return o;
	}
	// if o is not a reified object just return it
	else
	    return o;
    }


   /**
     * Wait the N-th future in the list is arrived.
     */
    public static void waitTheNth (Object o, int n) {
	if (MOP.isReifiedObject (o)) {
	    org.objectweb.proactive.core.mop.Proxy theProxy  = findProxyForGroup(o);
	    // If the object represents a group, we use the proxyForGroup's method
 	    if (theProxy != null)
		((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).futureList.waitTheNth(n);
 	    // Else the "standard waitFor" method has been used in the findProxyForGroup method
	}
    }



    /**
     * Wait the N-th future is arrived and get it.
     */
    public static Object waitAndGetTheNth (Object o, int n) {
	if (MOP.isReifiedObject (o)) {
	    org.objectweb.proactive.core.mop.Proxy theProxy = findProxyForGroup(o);
	    // If the object represents a group, we use the proxyForGroup's method
	    if (theProxy != null)
		return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).futureList.waitAndGetTheNth(n);
	    // Else the "standard waitFor" method has been used in the findProxyForGroup method so the future is arrived, just return it
	    else
		return o;
	}
	// if o is not a reified object just return it
	else
	    return o;
    }

		
    /**
     * Returns the number of members of the object representing a Group.
     * Throws an IllegalArgumentException if <code>obj</code> doesn't represent a Group.
     */
    public static int size (Object obj) {
	if (!(MOP.isReifiedObject (obj)))
	    throw new java.lang.IllegalArgumentException("Parameter doesn't represent a group");
	else {
	    org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject)obj).getProxy();
	    /* If the object does not represent a group */
	    if (!(theProxy instanceof org.objectweb.proactive.core.group.ProxyForGroup))
		throw new java.lang.IllegalArgumentException("Parameter doesn't represent a group");
	    else
		return ((org.objectweb.proactive.core.group.ProxyForGroup)theProxy).size();
	}
    }
    

    /**
     * Returns the member at the specified index of the object representing a Group.
     * Returns <code>null</code> if <code>obj</code> doesn't represent a Group.
     */
    public static Object get (Object obj, int index) {
	if (!(MOP.isReifiedObject (obj)))
	    return null;
	else 
	    return findProxyForGroup(obj).get(index);
    }


    /**
     * Returns <code>true</code> if <code>obj</code> is an object representing a Group (future or not)
     */  
    public static boolean isGroup (Object obj) {	
	if (!(MOP.isReifiedObject(obj)))
	    return false;
	else {
	    return (findProxyForGroup(obj) != null);
	}
    }
    

    /**
     * Returns the ProxyForGroup of the object <code>obj</code>.
     * Returns <code>null</code> if <code>obj</code> does not represent a Group.
     */
    private static ProxyForGroup findProxyForGroup(Object obj) {
	if (!(MOP.isReifiedObject(obj)))
	    return null;
	else {
	    org.objectweb.proactive.core.mop.Proxy tmp = ((StubObject)obj).getProxy();

	    // obj is an object representing a Group (and not a future)
	    if (tmp instanceof org.objectweb.proactive.core.group.ProxyForGroup)
		return (org.objectweb.proactive.core.group.ProxyForGroup) tmp;
	    
	    // obj is a future ... but may be a future-Group
	    while (tmp instanceof org.objectweb.proactive.core.body.future.FutureProxy)
		// future of future ...
		if (MOP.isReifiedObject(((FutureProxy)tmp).getResult()))
		    tmp = ((StubObject)((FutureProxy)tmp).getResult()).getProxy();
	        // future of standard objet
		else
		    return null;
	    
	    // future-Group
	    if (tmp instanceof org.objectweb.proactive.core.group.ProxyForGroup)
		return (org.objectweb.proactive.core.group.ProxyForGroup) tmp;
	    // future of an active object
	    else
		return null;
	}
	
    }




}
