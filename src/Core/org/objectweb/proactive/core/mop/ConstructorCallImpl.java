/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.mop;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;


/**
 * A reified constructor call.
 */
public class ConstructorCallImpl implements ConstructorCall, Serializable {

    /**
     * The array holding the arguments og the constructor
     */
    public Object[] effectiveArguments;

    /**
     * The corresponding constructor object
     */
    public Constructor reifiedConstructor;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Effective constructor
     * @param reifiedConstructor the constructor object which is called
     * @param effectiveArguments the array holding the effective args
     */
    public ConstructorCallImpl(Constructor reifiedConstructor,
        Object[] effectiveArguments) {
        this.reifiedConstructor = reifiedConstructor;
        this.effectiveArguments = effectiveArguments;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ConstructorCallImpl\n");
        sb.append("reifiedConstructor=");
        sb.append(reifiedConstructor);
        sb.append("\n");
        sb.append("effectiveArguments=");
        if (effectiveArguments == null) {
            sb.append("null\n");
        } else {
            sb.append("\n");
            for (int i = 0; i < effectiveArguments.length; i++) {
                sb.append("   effectiveArguments[");
                sb.append(i);
                sb.append("]=");
                sb.append(effectiveArguments[i]);
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //
    // -- implements ConstructorCall -----------------------------------------------
    //

    /**
     * Make a deep copy of all arguments of the constructor
     */
    public void makeDeepCopyOfArguments() throws java.io.IOException {
        effectiveArguments = (Object[]) Utils.makeDeepCopy(effectiveArguments);
    }

    /**
     * Return the name of the target class that constructor is for
     */
    public String getTargetClassName() {
        return getReifiedClass().getName();
    }

    /**
     * Performs the object construction that is reified vy this object
     * @throws InvocationTargetException
     * @throws ConstructorCallExecutionFailedException
     */
    public Object execute()
        throws InvocationTargetException,
            ConstructorCallExecutionFailedException {
        // System.out.println("ConstructorCall: The constructor is " + reifiedConstructor); 
        try {
            return reifiedConstructor.newInstance(effectiveArguments);
        } catch (IllegalAccessException e) {
            throw new ConstructorCallExecutionFailedException(
                "Access rights to the constructor denied: " + e);
        } catch (IllegalArgumentException e) {
            throw new ConstructorCallExecutionFailedException(
                "Illegal constructor arguments: " + e);
        } catch (InstantiationException e) {
            if (getReifiedClass().isInterface()) {
                throw new ConstructorCallExecutionFailedException(
                    "Cannot build an instance of an interface: " + e);
            } else if (Modifier.isAbstract(getReifiedClass().getModifiers())) {
                throw new ConstructorCallExecutionFailedException(
                    "Cannot build an instance of an abstract class: " + e);
            } else {
                throw new ConstructorCallExecutionFailedException(
                    "Instanciation problem: " + e +
                    ". Strange enough, the reified class is neither abstract nor an interface.");
            }
        } catch (ExceptionInInitializerError e) {
            throw new ConstructorCallExecutionFailedException(
                "Cannot build object because the initialization of its class failed: " +
                e);
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     *        Returns a <code>Class</code> object representing the type of
     * the object this reified constructor will build when reflected
     */
    protected Class<?> getReifiedClass() {
        return reifiedConstructor.getDeclaringClass();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        // We want to implement a workaround the Constructor
        // not being Serializable
        out.writeObject(this.effectiveArguments);

        // Constructor needs to be converted because it is not serializable
        Class<?> declaringClass;
        Class<?>[] parameters;

        declaringClass = this.reifiedConstructor.getDeclaringClass();
        out.writeObject(declaringClass);

        parameters = this.reifiedConstructor.getParameterTypes();
        out.writeObject(parameters);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        Class<?> declaringClass = null;
        Class<?>[] parameters;
        try {
            this.effectiveArguments = (Object[]) in.readObject();
        } catch (IOException e) {
            //  System.out.println("Stream is  " + in.getClass().getName());
            //    e.printStackTrace();
            throw e;
        }

        declaringClass = (Class<?>) in.readObject();
        parameters = (Class<?>[]) in.readObject();

        try {
            this.reifiedConstructor = declaringClass.getConstructor(parameters);
        } catch (NoSuchMethodException e) {
            throw new InternalException("Lookup for constructor failed: " + e +
                ". This may be caused by having different versions of the same class on different VMs. Check your CLASSPATH settings.");
        }
    }
}
