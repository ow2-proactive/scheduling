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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;

import org.objectweb.proactive.core.ProActiveRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.List;
import java.util.Vector;


/**
 * Abstract parent class for Interface implementation generation.
 * It defines a skeleton for the classes generation, and provides a set of utility
 * methods that will be used by derived classes.
 *
 * @author Matthieu Morel
 */
public abstract class AbstractInterfaceClassGenerator implements Constants {
    // A few constants that come in handy when using ASM in our case
    protected static final String METHODCALL_TYPE = "Lorg/objectweb/proactive/core/mop/MethodCall;";
    protected static final String OBJECT_TYPE = "Ljava/lang/Object;";
    protected static final String OBJECT_ARRAY_TYPE = "[Ljava/lang/Object;";
    protected static final String METHOD_TYPE = "Ljava/lang/reflect/Method;";
    protected static final String METHOD_ARRAY_TYPE = "[Ljava/lang/reflect/Method;";
    protected static final String FUNCTIONAL_INTERFACE_NAME_TYPE = "Ljava/lang/String;";
    protected static final String FUNCTIONAL_INTERFACE_NAME_FIELD_NAME = "fcFunctionalInterfaceName";
    protected static final String SUPER_CLASS_NAME = "org/objectweb/proactive/core/component/ProActiveInterface";

    // Those fields contain information about the class
    // for which we are building a wrapper
    protected Class cl;
    protected String className;
    protected String packageName;
    protected Method[] methods;
    protected List interfacesToImplement; // contains Class object corresponding to the interfaces
    protected ClassWriter classGenerator;

    // The following fields have to do with
    // the actual generation of the stub
    protected String stubClassSimpleName;
    protected String stubClassFullName;

    /**
     * Method createStaticInitializer.
     */
    protected abstract void createStaticInitializer()
        throws ClassNotFoundException;

    /**
     * Method createFields.
     */
    protected abstract void createFields();

    /**
     * Method createDefaultMethods.
     */
    protected abstract void createDefaultMethods();

    /**
     * Method createMethod.
     * @param i
     * @param method
     * @return CodeVisitor
     */
    protected abstract CodeVisitor createMethod(int i, Method method);

    /**
     * Method createStaticVariables.
     */
    protected abstract void createStaticVariables();

    // ASM : added utility methods
    static void pushInt(CodeVisitor cv, int i) {
        if ((i >= -128) && (i < 128)) {
            cv.visitIntInsn(BIPUSH, i);
        } else if ((i >= -32768) && (i < 32768)) {
            cv.visitIntInsn(SIPUSH, i);
        } else {
            cv.visitLdcInsn(new Integer(i));
        }
    }

    /** utility method
     * @param javaModifier
     * @return the code identifying the java modifier with ASM
     */
    protected static int convertJavaModifierToASM(int javaModifier) {
        int result = 0;

        if (Modifier.isAbstract(javaModifier)) {
            result = result | Constants.ACC_ABSTRACT;
        }
        if (Modifier.isFinal(javaModifier)) {
            result = result | Constants.ACC_FINAL;
        }
        if (Modifier.isInterface(javaModifier)) {
            result = result | Constants.ACC_INTERFACE;
        }
        if (Modifier.isNative(javaModifier)) {
            result = result | Constants.ACC_NATIVE;
        }
        if (Modifier.isPrivate(javaModifier)) {
            result = result | Constants.ACC_PRIVATE;
        }
        if (Modifier.isProtected(javaModifier)) {
            result = result | Constants.ACC_PROTECTED;
        }
        if (Modifier.isPublic(javaModifier)) {
            result = result | Constants.ACC_PUBLIC;
        }
        if (Modifier.isStatic(javaModifier)) {
            result = result | Constants.ACC_STATIC;
        }
        if (Modifier.isSynchronized(javaModifier)) {
            result = result | Constants.ACC_SYNCHRONIZED;
        }
        if (Modifier.isTransient(javaModifier)) {
            result = result | Constants.ACC_TRANSIENT;
        }
        if (Modifier.isVolatile(javaModifier)) {
            result = result | Constants.ACC_VOLATILE;
        }

        return result;
    }

    /** utility method
     * @param modifiers
     * @return modifiers after applying masks
     */
    protected static int removeNativeAndAbstractModifiers(int modifiers) {
        // In order to set to 0 the bit that represents 'native', we first
        // compute the mask that contains 1s everywhere and a 0 where the bit
        // is, and then apply this mask with an 'and', bitwise.
        int result = modifiers & (~Modifier.NATIVE);
        result = result & (~Modifier.ABSTRACT);
        return result;
    }

    /** utility method
     * @return the full class name of the stub
     */
    public String getStubClassFullName() {
        return this.stubClassFullName;
    }

    /** actually creates the bytecode corresponding to the generated class
     * @throws ClassNotFoundException
     * @return the bytecode corresponding to the generated class
     */
    public byte[] create() throws ClassNotFoundException {
        // Creates the class generator
        this.classGenerator = this.createClassGenerator();

        // Add a public no-arg constructor
        this.createConstructor();

        // Creates all the methods in the class
        for (int i = 0; i < this.methods.length; i++) {
            // walkaround (NOT CLEAN) to avoid generating reified calls for the proxy methods
            if (!(methods[i].getName().equals("getProxy") ||
                    methods[i].getName().equals("setProxy"))) {
                CodeVisitor mg = this.createMethod(i, this.methods[i]);
            }
        }

        this.createDefaultMethods();

        // Creates the fields of the class
        this.createFields();

        // Create the static fields
        this.createStaticVariables();

        // Creates the static initializer
        this.createStaticInitializer();

        return this.classGenerator.toByteArray();
    }

    /**
     * @return a visitor for writing Java classes
     */
    protected ClassWriter createClassGenerator() {
        String[] interfaces = new String[interfacesToImplement.size()];
        for (int i = 0; i < interfacesToImplement.size(); i++) {
            interfaces[i] = ((Class) interfacesToImplement.get(i)).getName()
                             .replace('.', '/');
        }

        ClassWriter cw = new ClassWriter(true);
        cw.visit(Constants.ACC_PUBLIC | Constants.ACC_SUPER, // Same access modifiers as superclass or public ???
            this.stubClassFullName, SUPER_CLASS_NAME, // Superclass
            interfaces, // declared interfaces
            "<generated>");
        return cw;
    }

    protected void createConstructor() {
        // Actually creates the method generator (ASM : uses the visitor)
        CodeVisitor cv = this.classGenerator.visitMethod(ACC_PUBLIC, "<init>",
                "()V", null, null);

        //Calls the constructor of the super class
        cv.visitVarInsn(ALOAD, 0);

        //cv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        cv.visitMethodInsn(INVOKESPECIAL, SUPER_CLASS_NAME, "<init>", "()V");

        // And returns from the constructor
        cv.visitInsn(RETURN);

        // Needed stack size
        // Needed locals
        cv.visitMaxs(0, 0);

        return;
    }

    protected CodeVisitor createMethodGenerator(Method m) {
        // Extracts modifiers
        int flags = convertJavaModifierToASM(m.getModifiers());

        // Modifies the modifiers in order to remove 'native' and 'abstract'
        flags = removeNativeAndAbstractModifiers(flags);

        // Extracts return and arguments types
        String mDesc = Type.getMethodDescriptor(m);

        // Actually creates the method generator
        CodeVisitor cv = this.classGenerator.visitMethod(flags, // access flags
                m.getName(), // Method generatedClassName
                mDesc, // return and argument types
                null, // exceptions
                null); // Attributes
        return cv;
    }

    protected void createUnwrappingCode(CodeVisitor cv, Class c) {
        // Depending on the type of the wrapper, the code differs
        // The parameter 'c' represents the primitive type, not the type of the
        // wrapper
        if (c == Void.TYPE) {
            cv.visitInsn(POP);
        } else if (c.isPrimitive()) {
            String type;
            String meth;
            String desc;
            if (c == Byte.TYPE) {
                type = "java/lang/Byte";
                meth = "byteValue";
                desc = "B";
            } else if (c == Integer.TYPE) {
                type = "java/lang/Integer";
                meth = "intValue";
                desc = "I";
            } else if (c == Boolean.TYPE) {
                type = "java/lang/Boolean";
                meth = "booleanValue";
                desc = "Z";
            } else if (c == Double.TYPE) {
                type = "java/lang/Double";
                meth = "doubleValue";
                desc = "D";
            } else if (c == Float.TYPE) {
                type = "java/lang/Float";
                meth = "floatValue";
                desc = "F";
            } else if (c == Long.TYPE) {
                type = "java/lang/Long";
                meth = "longValue";
                desc = "J";
            } else if (c == Character.TYPE) {
                type = "java/lang/Character";
                meth = "charValue";
                desc = "C";
            } else /*if (result == Short.TYPE)*/
             {
                type = "java/lang/Short";
                meth = "shortValue";
                desc = "S";
            }
            cv.visitTypeInsn(CHECKCAST, type);
            cv.visitMethodInsn(INVOKEVIRTUAL, type, meth, "()" + desc);
        } else {
            cv.visitTypeInsn(CHECKCAST, Type.getInternalName(c));
        }

        return;
    }

    protected void createReturnCode(CodeVisitor cv, Class c) {
        if (c == Void.TYPE) {
            cv.visitInsn(RETURN);
        } else if (c.isPrimitive()) {
            int opcode;
            if (c == Double.TYPE) {
                opcode = DRETURN;
            } else if (c == Float.TYPE) {
                opcode = FRETURN;
            } else if (c == Long.TYPE) {
                opcode = LRETURN;
            } else {
                opcode = IRETURN;
            }
            cv.visitInsn(opcode);
        } else {
            cv.visitInsn(ARETURN);
        }
    }

    /**
     * Returns the offset which must be added to some opcodes to get an opcode of
     * the given type. More precisely, returns the offset which must be added to
     * an opc_iXXXX opcode to get the opc_YXXXX opcode corresponding to the given
     * type. For example, if the given type is double the result is 3, which
     * means that opc_dload, opc_dstore, opc_dreturn... opcodes are equal to
     * opc_iload+3, opc_istore+3, opc_ireturn+3...
     *
     * @param type a Java class representing a Java type (primitive or not).
     * @return the opcode offset of the corresponding to the given type.
     */
    protected int getOpcodeOffset(final Class type) {
        if (type == Double.TYPE) {
            return 3;
        } else if (type == Float.TYPE) {
            return 2;
        } else if (type == Long.TYPE) {
            return 1;
        } else if (type.isPrimitive()) {
            return 0;
        }
        return 4;
    }

    /**
     * Return the size of the given type. This size is 2 for the double and long
     * types, and 1 for the other types.
     *
     * @param type a Java class representing a Java type (primitive or not).
     * @return the size of the given type.
     */
    protected int getSize(final Class type) {
        return (((type == Double.TYPE) || (type == Long.TYPE)) ? 2 : 1);
    }

    protected Class defineClass(final String className, final byte[] bytes) {
        // The following code invokes defineClass on the current thread classloader by reflection
        try {
            Class clc = Class.forName("java.lang.ClassLoader");
            Class[] argumentTypes = new Class[4];
            argumentTypes[0] = className.getClass();
            argumentTypes[1] = bytes.getClass();
            argumentTypes[2] = Integer.TYPE;
            argumentTypes[3] = Integer.TYPE;

            Method method = clc.getDeclaredMethod("defineClass", argumentTypes);
            method.setAccessible(true);

            Object[] effectiveArguments = new Object[4];
            effectiveArguments[0] = className;
            effectiveArguments[1] = bytes;
            effectiveArguments[2] = new Integer(0);
            effectiveArguments[3] = new Integer(bytes.length);

            return (Class) method.invoke(Thread.currentThread()
                                               .getContextClassLoader(),
                effectiveArguments);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();

            //cat.error(cnfe.toString());
            throw new ProActiveRuntimeException(cnfe.toString());
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();

            //cat.error(nsme.toString());
            throw new ProActiveRuntimeException(nsme.toString());
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            throw new ProActiveRuntimeException(iae.toString());
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
            throw new ProActiveRuntimeException(ite.toString());
        }
    }

    protected Class loadClass(final String className)
        throws ClassNotFoundException {
        // try to fetch the class from the default class loader
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    /**
     * This method is called by the constructor
     */
    protected void setInfos() throws ClassNotFoundException {
        // This Vector keeps track of all the methods accessible from this class
        Vector tempVector = new Vector();
        Class[] params;
        Object exists;

        // If the target type is an interface, the only thing we have to do is to
        // get the list of all its public methods.
        for (int j = 0; j < interfacesToImplement.size(); j++) {
            //Class interface_class = Class.forName((String) interfacesToImplement.get(j));
            Class interface_class = (Class) interfacesToImplement.get(j);
            Method[] allPublicMethods = interface_class.getMethods();
            for (int i = 0; i < allPublicMethods.length; i++) {
                tempVector.addElement(allPublicMethods[i]);
            }
        }

        // Turns the vector into an array of type Method[]
        this.methods = new Method[tempVector.size()];
        tempVector.copyInto(this.methods);

        // Determines which methods are valid for reification
        // It is the responsibility of method checkMethod in class Utils
        // to decide if a method is valid for reification or not
        Vector v = new Vector();
        int initialNumberOfMethods = this.methods.length;

        for (int i = 0; i < initialNumberOfMethods; i++) {
            if (org.objectweb.proactive.core.mop.Utils.checkMethod(
                        this.methods[i])) {
                v.addElement(this.methods[i]);
            }
        }

        Method[] validMethods = new Method[v.size()];
        v.copyInto(validMethods);

        // Installs the list of valid methods as an instance variable of this object
        this.methods = validMethods;

        this.packageName = null;
        this.stubClassSimpleName = org.objectweb.proactive.core.mop.Utils.getSimpleName(this.stubClassFullName);

        return;
    }
}
