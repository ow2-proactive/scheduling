/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.component.asmgen;

import org.apache.log4j.Logger;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.mop.Utils;

import java.io.Serializable;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;


/**
 * Generates Interface implementations for the functional interfaces of the
 * component representative.
 *<br>
 * This class :<br>
 * - implements the java interface corresponding to the functional
 * interface of the component.<br>
 * - implements StubObject, like the standard ProActive stub<br>
 * - is linked to the ProActive proxy (corresponding to the actual active object)
 *<br>
 * Method calls are reified as MethodCall objects, that contain :<br>
 * - a tag signaling component requests<br>
 * - the name given to the component functional interface.<br>
 *
 * @author Matthieu Morel
 */
public class RepresentativeInterfaceClassGenerator
    extends AbstractInterfaceClassGenerator {
    protected static Logger logger = Logger.getLogger(RepresentativeInterfaceClassGenerator.class.getName());

    // A few constants that come in handy when using ASM in our case
    protected static final String PROXY_TYPE = "Lorg/objectweb/proactive/core/mop/Proxy;";
    protected static final String STUB_INTERFACE_NAME = "org/objectweb/proactive/core/mop/StubObject";
    protected static final String PROXY_FIELD_NAME = "myProxy";

    // generatedClassesCache that contains all the generated classes according to their name
    private static Hashtable generatedClassesCache = new Hashtable();
    private static RepresentativeInterfaceClassGenerator instance;

    // this boolean for deciding of a possible indirection for the functionnal calls
    protected boolean isPrimitive = false;
    private String fcInterfaceName = null;

    public RepresentativeInterfaceClassGenerator() {
        // Obtains the object that represents the type we want to create
        // a wrapper class for. This call may fail with a ClassNotFoundException
        // if the class corresponding to this type cannot be found.
        this.cl = ProActiveInterface.class;

        // Keep this info at hand for performance purpose
        this.className = cl.getName();

        //generatedClassesCache = new Hashtable();
    }

    public static RepresentativeInterfaceClassGenerator instance() {
        if (instance == null) {
            return new RepresentativeInterfaceClassGenerator();
        } else {
            return instance;
        }
    }

    /**
     * retreives the bytecode associated to the generated class of the given name
     */
    public static byte[] getClassData(String classname) {
        return (byte[]) getGeneratedClassesCache().get(classname);
    }

    /**
     * Returns the generatedClassesCache.
     * @return a Map acting as a cache for generated classes
     */
    public static Map getGeneratedClassesCache() {
        return generatedClassesCache;
    }

    public ProActiveInterface generateInterface(final String fcInterfaceName,
        Component owner, InterfaceType interfaceType, boolean isInternal)
        throws InterfaceGenerationFailedException {
        try {
            this.fcInterfaceName = fcInterfaceName;

            //isPrimitive = ((ProActiveComponentRepresentativeImpl) owner).getHierarchicalType()
            //                                                    .equals(ComponentParameters.PRIMITIVE);
            interfacesToImplement = new ArrayList();

            // add functional interface
            interfacesToImplement.add(Class.forName(
                    interfaceType.getFcItfSignature()));

            // add Serializable interface
            interfacesToImplement.add(Serializable.class);

            // add StubObject, so we can set the proxy
            interfacesToImplement.add(StubObject.class);

            this.stubClassFullName = org.objectweb.proactive.core.component.asmgen.Utils.getMetaObjectComponentRepresentativeClassName(fcInterfaceName,
                    interfaceType.getFcItfSignature());
            //}
            Class generated_class;

            // check whether class has already been generated
            try {
                generated_class = loadClass(stubClassFullName);
            } catch (ClassNotFoundException cnfe) {
                byte[] bytes;
                setInfos();
                bytes = create();
                RepresentativeInterfaceClassGenerator.generatedClassesCache.put(stubClassFullName,
                    bytes);
                if (logger.isDebugEnabled()) {
                    logger.debug("added " + stubClassFullName + " to cache");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("generated classes cache is : " +
                        generatedClassesCache.toString());
                }

                //                // Next few lines for debugging only
                //                                            try {
                //                                                java.io.File file = new java.io.File(System.getProperty("user.home") + "/ProActive/generated/" + 
                //                                                                                     stubClassFullName + ".class");
                //                                
                //                                                if (logger.isDebugEnabled()) {
                //                                                    //logger.debug("writing down the generated class : " + file.getAbsolutePath());
                //                                                }
                //                                
                //                                                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                //                                                fos.write(bytes);
                //                                                fos.close();
                //                                			} catch (Exception e) {
                //                                				// e.printStackTrace();
                //                                				logger.info("if you want a dump of the generated classes, you need to create a /generated folder at the root of you command");
                //                                			}
                // convert the bytes into a Class
                generated_class = defineClass(stubClassFullName, bytes);
            }

            ProActiveInterface reference = (ProActiveInterface) generated_class.newInstance();
            reference.setName(fcInterfaceName);
            reference.setOwner(owner);
            reference.setType(interfaceType);
            reference.setIsInternal(isInternal);

            return reference;
        } catch (ClassNotFoundException e) {
            throw new InterfaceGenerationFailedException("cannot find interface signature class",
                e);
        } catch (IllegalAccessException e) {
            throw new InterfaceGenerationFailedException("constructor not accessible",
                e);
        } catch (InstantiationException e) {
            throw new InterfaceGenerationFailedException("constructor belongs to an abstract class?",
                e);
            // TODO : check this
        }
    }

    protected CodeVisitor createMethod(int methodIndex, Method m) {
        CodeVisitor cv = createMethodGenerator(m);

        // Pushes on the stack the reference to the proxy object
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, this.stubClassFullName.replace('.', '/'),
            PROXY_FIELD_NAME, PROXY_TYPE);

        // Pushes on the stack the Method object that represents the current method
        cv.visitFieldInsn(GETSTATIC, this.stubClassFullName.replace('.', '/'),
            "methods", METHOD_ARRAY_TYPE);
        pushInt(cv, methodIndex);
        cv.visitInsn(AALOAD);

        Class[] paramTypes = m.getParameterTypes();

        // Create an array of type Object[] for holding all the parameters
        // Push on the stack the size of the array
        pushInt(cv, paramTypes.length);

        // Creates an array of class objects of that size
        cv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        // Fill in the array with the parameters
        int indexInParameterArray = 1; // Necessary because sometimes we need to jump 2 slots ahead
        for (int i = 0; i < paramTypes.length; i++) {
            // First, duplicate the reference to the array of type Object[]
            // That currently sits on top of the stack
            cv.visitInsn(DUP);

            // Load the array index for storing the result
            pushInt(cv, i);

            //Type theType = convertClassToType(paramTypes[i]);
            Class param = paramTypes[i];

            // If it is a primitive type, we need to create the wrapper here
            if (param.isPrimitive()) {
                int opcode = ILOAD;
                String type;
                String desc;
                if (param == Byte.TYPE) {
                    type = "java/lang/Byte";
                    desc = "B";
                } else if (param == Integer.TYPE) {
                    type = "java/lang/Integer";
                    desc = "I";
                } else if (param == Boolean.TYPE) {
                    type = "java/lang/Boolean";
                    desc = "Z";
                } else if (param == Double.TYPE) {
                    opcode = DLOAD;
                    type = "java/lang/Double";
                    desc = "D";
                } else if (param == Float.TYPE) {
                    opcode = FLOAD;
                    type = "java/lang/Float";
                    desc = "F";
                } else if (param == Long.TYPE) {
                    opcode = LLOAD;
                    type = "java/lang/Long";
                    desc = "J";
                } else if (param == Character.TYPE) {
                    type = "java/lang/Character";
                    desc = "C";
                } else /*if (param == Short.TYPE)*/
                 {
                    type = "java/lang/Short";
                    desc = "S";
                }
                cv.visitTypeInsn(NEW, type);
                cv.visitInsn(DUP);
                cv.visitVarInsn(opcode, indexInParameterArray);
                cv.visitMethodInsn(INVOKESPECIAL, type, "<init>",
                    "(" + desc + ")V");
            } else {
                cv.visitVarInsn(ALOAD, indexInParameterArray);
            }
            indexInParameterArray += (((param == Double.TYPE) ||
            (param == Long.TYPE)) ? 2 : 1);

            // Stores the object in the array
            cv.visitInsn(AASTORE);
        }

        // So now we have the Method object and the array of objects on the stack,
        // Pushes on the stack the reference to the functional interface name
        cv.visitFieldInsn(GETSTATIC, this.stubClassFullName.replace('.', '/'),
            FUNCTIONAL_INTERFACE_NAME_FIELD_NAME, FUNCTIONAL_INTERFACE_NAME_TYPE);

        cv.visitMethodInsn(INVOKESTATIC,
            "org/objectweb/proactive/core/mop/MethodCall",
            "getComponentMethodCall",
            "(" + METHOD_TYPE + OBJECT_ARRAY_TYPE +
            FUNCTIONAL_INTERFACE_NAME_TYPE + ")" + METHODCALL_TYPE);
        //        }
        // Now, call 'reify' on the proxy object
        cv.visitMethodInsn(INVOKEINTERFACE,
            "org/objectweb/proactive/core/mop/Proxy", "reify",
            "(" + METHODCALL_TYPE + ")" + OBJECT_TYPE);

        // If the return type of the method is a primitive type,
        // we want to unwrap it first
        if (m.getReturnType().isPrimitive()) {
            this.createUnwrappingCode(cv, m.getReturnType());
        } else {
            // If the return type is a reference type,
            // we need to insert a type check
            cv.visitTypeInsn(CHECKCAST, Type.getInternalName(m.getReturnType()));
        }
        createReturnCode(cv, m.getReturnType());
        cv.visitMaxs(0, 0); // max stack and max locals automatically computed

        return cv;
    }

    protected void createFields() {
        // Creates the field that points to the active object
        this.classGenerator.visitField(ACC_PROTECTED, PROXY_FIELD_NAME,
            PROXY_TYPE, null, null);
    }

    protected void createStaticVariables() {
        // Creates fields that contains the array of Method objects
        // that represent the reified methods of this class
        this.classGenerator.visitField(ACC_PROTECTED | ACC_STATIC, "methods",
            METHOD_ARRAY_TYPE, null, null);

        // creates and set the field that points to the functional interface name
        this.classGenerator.visitField(ACC_PROTECTED | ACC_STATIC,
            FUNCTIONAL_INTERFACE_NAME_FIELD_NAME,
            FUNCTIONAL_INTERFACE_NAME_TYPE, fcInterfaceName, null);
    }

    protected void createStaticInitializer() throws ClassNotFoundException {
        // Creates the class initializer method itself
        CodeVisitor cv = this.classGenerator.visitMethod(ACC_STATIC,
                "<clinit>", "()V", null, null);

        // Creates an array of Method objects that we will store into the static
        // variable 'methods' of type 'Method[]'
        // Pushes the size of the array on to the stack
        pushInt(cv, this.methods.length);

        // Creates an array of Method objects of that size
        cv.visitTypeInsn(ANEWARRAY, "java/lang/reflect/Method");

        // Stores the reference to this newly-created array into the static variable 'methods'
        cv.visitFieldInsn(PUTSTATIC, this.stubClassFullName.replace('.', '/'),
            "methods", METHOD_ARRAY_TYPE);

        // Pushes on the stack the size of the array
        pushInt(cv, interfacesToImplement.size());

        // Creates an array of class objects of that size
        cv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

        // Stores the reference to this newly-created array as the local variable with index '1'
        cv.visitVarInsn(ASTORE, 1);

        // Make as many calls to Class.forName as is needed to fill in the array
        for (int i = 0; i < interfacesToImplement.size(); i++) {
            // Load onto the stack a pointer to the array
            cv.visitVarInsn(ALOAD, 1);

            // Load the index in the array where we want to store the result
            pushInt(cv, i);

            // Loads the generatedClassName of the class onto the stack
            String s = ((Class) interfacesToImplement.get(i)).getName();
            cv.visitLdcInsn(s);

            // Performs the call to Class.forName
            cv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName",
                "(Ljava/lang/String;)Ljava/lang/Class;");

            // Stores the result of the invocation of forName into the array
            // The index into which to store as well as the reference to the array
            // are already on the stack
            cv.visitInsn(AASTORE);
        }

        // Now, lookup each of the Method objects and store it into the 'method' array
        for (int i = 0; i < this.methods.length; i++) {
            // Stacks up the reference to the array of methods and the index in the array
            cv.visitFieldInsn(GETSTATIC,
                this.stubClassFullName.replace('.', '/'), "methods",
                METHOD_ARRAY_TYPE);
            pushInt(cv, i);

            // Now, we load onto the stack a pointer to the class that contains the method
            int indexInClassArray = interfacesToImplement.indexOf(this.methods[i].getDeclaringClass());
            if (indexInClassArray == -1) {
            }

            // Load a pointer to the Class array (local variable number 1)
            cv.visitVarInsn(ALOAD, 1);

            // Access element number 'indexInClassArray'
            pushInt(cv, indexInClassArray);
            cv.visitInsn(AALOAD);

            // Now, perform a call to 'getDeclaredMethod'
            // First, stack up the simple generatedClassName of the method to solve
            cv.visitLdcInsn(this.methods[i].getName());

            // Now, we want to create an array of type Class[] for representing
            // the parameters to this method. We choose to store this array into the
            // slot number 2
            // Pushes the size of the array
            pushInt(cv, this.methods[i].getParameterTypes().length);

            // Creates an array of class objects of that size
            cv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

            // Stores the reference to this newly-created array as the local variable with index '2'
            cv.visitVarInsn(ASTORE, 2);

            // Stack up the class objects that represent the types of all the arguments to this method
            for (int j = 0; j < this.methods[i].getParameterTypes().length;
                    j++) {
                Class currentParameter = this.methods[i].getParameterTypes()[j];

                // Load onto the stack a pointer to the array of Class objects (for parameters)
                cv.visitVarInsn(ALOAD, 2);

                // Load the index in the array where we want to store the result
                pushInt(cv, j);

                // If the type of the parameter is a primitive one, we use the predefined
                // constants (like java.lang.Integer.TYPE) instead of calling Class.forName
                if (currentParameter.isPrimitive()) {
                    // Loads that static variable
                    cv.visitFieldInsn(GETSTATIC,
                        Type.getInternalName(Utils.getWrapperClass(
                                currentParameter)), "TYPE", "Ljava/lang/Class;");
                } else {
                    // Load the generatedClassName of the parameter class onto the stack
                    cv.visitLdcInsn(currentParameter.getName());

                    // Performs a call to Class.forName
                    cv.visitMethodInsn(INVOKESTATIC, "java/lang/Class",
                        "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
                }

                // Stores the result in the array
                cv.visitInsn(AASTORE);
            }

            // Loads the array
            cv.visitVarInsn(ALOAD, 2);

            // Perform the actual call to 'getDeclaredMethod'
            cv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class",
                "getDeclaredMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)" + METHOD_TYPE);

            // Now that we have the result, let's store it into the array
            cv.visitInsn(AASTORE);
        }

        // And returns
        cv.visitInsn(RETURN);

        // Needed stack size
        // Needed locals
        cv.visitMaxs(0, 0);

        return;
    }

    protected void createGetAndSetProxyMethods() {
        // Do the getProxy method first
        CodeVisitor cv = this.classGenerator.visitMethod(ACC_PUBLIC,
                "getProxy", "()" + PROXY_TYPE, null, null);

        // Now, fills in the instruction list
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, this.stubClassFullName.replace('.', '/'),
            PROXY_FIELD_NAME, PROXY_TYPE);
        cv.visitInsn(ARETURN);

        // Needed stack size
        // Needed locals
        cv.visitMaxs(0, 0);

        // Now, do the setProxy method
        cv = this.classGenerator.visitMethod(ACC_PUBLIC, "setProxy",
                "(" + PROXY_TYPE + ")V", null, null);

        // Now, fills in the instruction list
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 1);
        cv.visitFieldInsn(PUTFIELD, this.stubClassFullName.replace('.', '/'),
            PROXY_FIELD_NAME, PROXY_TYPE);
        cv.visitInsn(RETURN);

        // Needed stack size
        // Needed locals
        cv.visitMaxs(0, 0);

        return;
    }

    protected static int lengthOfType(Class cl) {
        int result;
        if (cl.isPrimitive()) {
            if ((cl.equals(Long.TYPE)) || (cl.equals(Double.TYPE))) {
                result = 2;
            } else {
                result = 1;
            }
        } else {
            result = 1;
        }
        return result;
    }

    /**
     * implementation of abstract method defined in mother class
     */
    protected void createDefaultMethods() {
        createGetAndSetProxyMethods();
    }
}
