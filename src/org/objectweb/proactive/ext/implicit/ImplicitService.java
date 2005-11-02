/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.ext.implicit;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MOP;


public class ImplicitService extends BlockingRequestQueueImpl
    implements Implicit {

    /**
     * The Hashtable for caching (shortcut/Methods) for this reified object
     */
    transient protected java.util.Hashtable shortcuts;

    /**
     * The Hashtable for caching (name of class / shortcuts table) associations
     */
    public final static java.util.Hashtable shortcutsTables = new java.util.Hashtable();

    /**
     *   A hashtable to hold 'forbid' declarations
     */
    protected java.util.Hashtable sync;

    public ImplicitService(UniqueID id) {
        super(id);
        sync = new java.util.Hashtable();
    }

    /**
     * Associates a shortcut with a blocking condition.
     *
     * The blocking condition is a method that return a boolean.
     * @throws InvalidAssociateDeclaration if one of the following is met : <UL>
     * <LI>There is more than one method with name <code>condition</code>
     * <LI>There is no method with name <code>condition</code>
     * <LI>There is a method with name condition but it does not return a boolean
     * <LI>There is a method with name condition but it takes arguments
     * </UL>
     */
    public void forbid(String shortcut, String condition)
        throws InvalidAssociateDeclaration {
        java.lang.reflect.Method cond;
        System.out.println("ImplicitBody: forbid() for " + shortcut);

        if (this.shortcuts == null) {
            //System.out.println("this.shortcuts = " + this.shortcuts);
            this.setShortcutsTable();
        }

        //	System.out.println("this.shortcuts = " + this.shortcuts);
        if (!(this.shortcuts.containsKey(shortcut))) {
            throw new InvalidAssociateDeclaration("No shortcut " + shortcut +
                " defined");
        }

        //try {
        //cond = body.getReifiedObject().getClass().getMethod(condition, argstype);
        cond = null;
        //} catch (NoSuchMethodException e) {
        //  throw new InvalidAssociateDeclaration("No method with name " + condition + " and no args");
        //}
        if (!(cond.getReturnType().equals(java.lang.Boolean.TYPE))) {
            throw new InvalidAssociateDeclaration("Method " + condition +
                " does not return a boolean as expected");
        }

        // The declaration is ok
        this.sync.put(shortcut, cond);
        //System.out.println ("New association : "+shortcut+" with condition "+cond.getName());
        return;
    }

    public void run() {

        /*
           boolean test = true; // If something fails, do not serve the request !
           int index;
           System.out.println("ImplicitBody: run()");

           //locateLiveRoutine("org.objectweb.proactive.Implicit");
           //launchLive();  // Executes the live(...) routine (reading associations)
           while (body.isActive()) {
             synchronized (this) {
               if (isEmpty()) {
                 //System.out.println("XXXXXXXXXXXXXXXXXXWaiting for request");
                 blockingRemoveOldest(); //No requests in line
                 //System.out.println("XXXXXXXXXXXXXXXXXXWaiting over");
               } else {
                 Request request = getOldestReadyRequest();
                 if (request != null) {
                   serve(request);
                 } else {
                   //System.out.println("XXXXXXXXXXXXXXXXXXWaiting for new request");
                   waitForNewRequest();
                 }
               }
             }
           }*/
    }

    public synchronized Request getOldestReadyRequest() {
        java.util.Iterator iterator = iterator();
        while (iterator.hasNext()) {
            Request r = (Request) iterator.next();
            java.lang.reflect.Method target = null; //r.getMethodCall().getReifiedMethod();

            // Find if there is a shortcut for this method
            String methodName;
            if (this.shortcuts.contains(target)) {
                methodName = this.getShortcut(target);
            } else {
                methodName = target.getName();
            }

            //Is there a blocking condition for this method ?
            java.lang.reflect.Method cond = (java.lang.reflect.Method) this.sync.get(methodName);
            if (cond != null) {
                //System.out.println("Testing condition");
                boolean test = this.testCondition(cond);

                //System.out.println("Testing condition on method " +  s + "  result is " + test);
                if (test) {
                    return r;
                }
            }
        }
        return null;
    }

    private boolean testCondition(java.lang.reflect.Method cond) {
        boolean result = true;
        Object[] args = new Object[0];

        System.out.println("ImplicitBody: testingCondition() on method" + cond);

        try {
            result = false; // ((Boolean)cond.invoke(body.getReifiedObject(), args)).booleanValue();
        } catch (Exception e) {
            System.err.println("Exception during invocation of method " +
                cond.getName());
            e.printStackTrace();
        }
        return result;
    }

    public synchronized void serveOldestThatIsNot(String s) {

        /*
           java.util.Iterator li = iterator();
           Request r;
           while (li.hasNext()) {
             r = (Request)li.next();
             if (!s.equals(r.getMethodName())) {
               serve(r);
               return;
             }
           }
         */
    }

    /**
     * Dumps the shortcut table to System.out
     */
    public void dumpShortcutsTable() {
        java.util.Enumeration en;
        String currentshc;
        java.lang.reflect.Method currentmethod;
        en = this.shortcuts.keys();
        System.out.println("--- Dump of the shortcuts table ---");
        while (en.hasMoreElements()) {
            currentshc = (String) en.nextElement();
            currentmethod = (java.lang.reflect.Method) this.shortcuts.get(currentshc);
            System.out.println(currentshc + "/" + currentmethod.toString());
        }
    }

    public String getShortcut(java.lang.reflect.Method m) {
        java.util.Enumeration en;
        String currentshc;
        java.lang.reflect.Method currentmethod;

        en = this.shortcuts.keys();

        while (en.hasMoreElements()) {
            currentshc = (String) en.nextElement();
            currentmethod = (java.lang.reflect.Method) this.shortcuts.get(currentshc);

            if (currentmethod.equals(m)) {
                return currentshc;
            }
        }
        return null;
    }

    public void addShortcut(String shortcut, String name, String[] argumentstype) {
        int index = 0;
        int size;
        Class[] args;
        java.lang.reflect.Method met = null;

        // If the shortcut is already a key, do nothing
        if (this.shortcuts.containsKey(shortcut)) {
            System.err.println("Shortcut '" + shortcut + "' already exists.");
            return;
        }

        size = argumentstype.length;
        args = new Class[size];

        // Let's find a method with the given arguments
        try {
            for (index = 0; index < size; index++) {
                // Do not deal with primitive types for now
                args[index] = MOP.forName(argumentstype[index]);
            }
            met = null; //body.getReifiedObject().getClass().getMethod(name, args);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found : " + argumentstype[index]);
            return;
        } // catch (NoSuchMethodException e) {

        //  System.err.println("No Such Method");
        //  e.printStackTrace();
        //  return;
        //}
        this.shortcuts.put(shortcut, met);
    }

    /**
     * Fills the shortcuts table with all obvious entries.
     *
     * An entry is obvious when there is only one method with the that name.
     */
    public void fillShortcutsTable() {
        java.lang.reflect.Method[] mets;
        java.lang.reflect.Method currentmethod;
        String currentshc;
        int size;
        int index;
        int index2;

        mets = null; // body.getReifiedObject().getClass().getMethods();
        size = mets.length;

        for (index = 0; index < size; index++) {
            currentmethod = mets[index];
            currentshc = currentmethod.getName();

            //if (body.isOverloaded(currentmethod)) {
            // If there is another method with the same simple name, do nothing
            //} else {
            // put it in hashtable
            // this.shortcuts.put(currentshc, currentmethod);
            //}
        }
    }

    public void setShortcutsTable() {
        // if there already exists one hashtable for this class, use it
        // else create it 
        String reifiedObjectClassName;

        //this.shortcuts = (java.util.Hashtable)shortcutsTables.get(body.getReifiedObject().getClass());
        if (this.shortcuts == null) {
            this.shortcuts = new java.util.Hashtable();
            this.fillShortcutsTable();
            // On enregistre cette nouvelle table aupres du gestionnaire des tables
            //shortcutsTables.put(body.getReifiedObject().getClass(), this.shortcuts);
        } else {
            // Nothing to do
        }
    }

    /**
     * Tests if there is another method with the same simple name in the class
     */
    public static boolean isOverloaded(java.lang.reflect.Method met,
        Class reifiedObjectClass) {
        int n = 0;
        java.lang.reflect.Method[] mets = reifiedObjectClass.getMethods();
        int size = mets.length;
        for (int index = 0; index < size; index++) {
            java.lang.reflect.Method currentmethod = mets[index];
            String currentshc = currentmethod.getName();
            if (currentshc.equals(met.getName())) {
                n++;
            }
        }
        return (n > 1);
    }
}
