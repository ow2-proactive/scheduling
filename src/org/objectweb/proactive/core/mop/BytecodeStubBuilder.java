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
package org.objectweb.proactive.core.mop;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

public class BytecodeStubBuilder {
    // Those fields contain information about the class
    // for which we are building a wrapper
    protected Class cl;
    protected String className;
    protected String packageName;
    protected Method[] methods;

    // The following fields have to do with
    // the actual genration of the stub
    protected String stubClassSimpleName;
    protected String stubClassFullName;
    protected ClassGen classGenerator;

    // We only instanciate the following InstructionList objet once and then
    // clean it and reuse it for each new method that we want to build
    protected InstructionList il = new InstructionList();

    // A few constants that come in handy when using BCEL in our case
    protected static final Type CLASS_TYPE;
    protected static final Type CLASS_ARRAY_TYPE;
    protected static final Type OBJECT_TYPE;
    protected static final Type OBJECT_ARRAY_TYPE;
    protected static final Type METHOD_TYPE;
    protected static final Type METHOD_ARRAY_TYPE;
    protected static final Type PROXY_TYPE;

    protected static final String STUB_INTERFACE_NAME;
    protected static final String PROXY_FIELD_NAME;

    // The static initializer that sets up all the constant fields
    static {
        CLASS_TYPE = convertClassNameToType("java.lang.Class");
        CLASS_ARRAY_TYPE = new ArrayType(CLASS_TYPE, 1);
        OBJECT_TYPE = convertClassNameToType("java.lang.Object");
        OBJECT_ARRAY_TYPE = new ArrayType(OBJECT_TYPE, 1);
        METHOD_TYPE = convertClassNameToType("java.lang.reflect.Method");
        METHOD_ARRAY_TYPE = new ArrayType(METHOD_TYPE, 1);
        PROXY_TYPE = convertClassNameToType("org.objectweb.proactive.core.mop.Proxy");

        STUB_INTERFACE_NAME = "org.objectweb.proactive.core.mop.StubObject";
        PROXY_FIELD_NAME = "myProxy";
    }

    public BytecodeStubBuilder(String classname) throws ClassNotFoundException {
//      System.out.println ("Creating builder for class "+classname);

        // Obtains the object that represents the type we want to create
        // a wrapper class for. This call may fail with a ClassNotFoundException
        // if the class corresponding to this type cannot be found.
        this.cl = Class.forName(classname);

        // Keep this info at hand for performance purpose
        this.className = classname;

        // Fills in all the infos about this class
        this.setInfos();
    }

    protected ClassGen createClassGenerator() {
        String superclassName;
        String[] interfaces = new String[2];
        interfaces[0] = "java.io.Serializable";
        interfaces[1] = STUB_INTERFACE_NAME;

        if (this.cl.isInterface()) {
            superclassName = "java.lang.Object";
        } else {
            superclassName = this.className;
        }

        return new ClassGen(this.stubClassFullName, // Fully-qualified class name
                            superclassName, // Superclass
                            "<generated>",
                            Constants.ACC_PUBLIC |
                            Constants.ACC_SUPER, // Same access modifiers as superclass or public ???
                            interfaces); // No declared implemented interfaces
    }

    public byte[] create() {
//    System.out.println ("Creating stub class for class "+this.className);

        // Creates the class generator
        this.classGenerator = this.createClassGenerator();

        // Add a public no-arg constructor
        this.createConstructor();

        // Creates all the methods in the class
        for (int i = 0; i < this.methods.length; i++) {
            MethodGen mg = this.createMethod(i, this.methods[i]);
            this.classGenerator.addMethod(mg.getMethod());
            il.dispose();
        }

        // Creates the two methods getProxy and setProxy
        // declared in interface StubObject
        this.createGetAndSetProxyMethods();

        // Creates the fields of the class
        this.createFields();

        // Create the static fields
        this.createStaticVariables();

        // Creates the static initializer
        this.createStaticInitializer();

        // Actually creates the class and returns the result
        JavaClass theClass = this.classGenerator.getJavaClass();
        /*
        // Next few lines for debugging only
        try {
          theClass.dump (theClass.getClassName()+".class");
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }*/
        return theClass.getBytes();
    }

    protected MethodGen createMethodGenerator(Method m) {
        // Extracts modifiers
        int flags = convertJavaModifierToBCEL(m.getModifiers());

        // Modifies the modifiers in order to remove 'native' and 'abstract'
        flags = removeNativeAndAbstractModifiers(flags);

        // Extracts return and arguments types
        Type returnType = convertClassToType(m.getReturnType());
        Type[] argumentTypes = new Type[m.getParameterTypes().length];
        for (int i = 0; i < argumentTypes.length; i++) {
            argumentTypes[i] = convertClassToType(m.getParameterTypes()[i]);
        }

        // Creates argument names
        String[] argumentNames = new String[argumentTypes.length];
        for (int i = 0; i < argumentNames.length; i++) {
            argumentNames[i] = new String("arg" + i);
        }

        // Actually creates the method generator
        MethodGen mg = new MethodGen(flags, // access flags
                                     returnType, // return type
                                     argumentTypes,
                                     argumentNames, // arg names
                                     m.getName(), // Method name
                                     this.stubClassFullName, // Class name
                                     il, // Instructions list
                                     this.classGenerator.getConstantPool()
        );

        return mg;
    }

    protected static int removeNativeAndAbstractModifiers(int modifiers) {
        // In order to set to 0 the bit that represents 'native', we first
        // compute the mask that contains 1s everywhere and a 0 where the bit
        // is, and then apply this mask with an 'and', bitwise.
        int result = modifiers & (~java.lang.reflect.Modifier.NATIVE);
        result = result & (~java.lang.reflect.Modifier.ABSTRACT);
        return result;
    }


    protected static int convertJavaModifierToBCEL(int javaModifier) {
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
        if (Modifier.isStrict(javaModifier)) {
            result = result | Constants.ACC_STRICT;
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

    protected MethodGen createMethod(int methodIndex, Method m) {
        ConstantPoolGen cp = this.classGenerator.getConstantPool();
        MethodGen mg = this.createMethodGenerator(m);

        InstructionFactory factory = new InstructionFactory(this.classGenerator);

        // First, check if the method is called from within the constructor
        // Load 'this' onto the stack
        il.append(factory.createThis());

        // Gets the value of the field 'outsideConstructor'
//    System.out.println ("BOO:"+this.stubClassFullName);
        il.append(factory.createGetField(this.stubClassFullName, "outsideConstructor", Type.BOOLEAN));

        // Now we create the code for the case where we are outside of
        // the constructor, i.e. we want the call to be reified

        // Pushes on the stack the reference to the proxy object
        InstructionHandle outsideConstructorHandle = il.append(factory.createThis());
        il.append(factory.createGetField(this.stubClassFullName, PROXY_FIELD_NAME, PROXY_TYPE));

        // Pushes on the stack the Method object that represents the current method
        il.append(factory.createGetStatic(this.stubClassFullName, "methods", METHOD_ARRAY_TYPE));
        il.append(new PUSH(this.classGenerator.getConstantPool(), methodIndex));
        il.append(factory.createArrayLoad(METHOD_TYPE));

        // Create an array of type Object[] for holding all the parameters
        // Push on the stack the size of the array
        il.append(new PUSH(this.classGenerator.getConstantPool(), m.getParameterTypes().length));
        // Creates an array of class objects of that size
        il.append((Instruction) factory.createNewArray(OBJECT_TYPE, (byte) 1));

        // Fill in the array with the parameters
        for (int i = 0; i < m.getParameterTypes().length; i++) {
            // First, duplicate the reference to the array of type Object[]
            // That currently sits on top of the stack
            il.append(factory.createDup(1));

            // Load the array index for storing the result
            il.append(new PUSH(this.classGenerator.getConstantPool(), i));

            Type theType = convertClassToType(m.getParameterTypes()[i]);

            // If it is a primitive type, we need to create the wrapper here
            if (m.getParameterTypes()[i].isPrimitive()) {
                // If we have a primitive type, we need to first create a wrapper objet
                String nameOfWrapper = Utils.nameOfWrapper(m.getParameterTypes()[i]);
                il.append(factory.createNew(nameOfWrapper));
                il.append(factory.createDup(1));

                // Load the primitive value on to the stack
                il.append(factory.createLoad(theType, (i + 1)));

                // Now, we call the constructor of the wrapper
                Type[] argtypes = new Type[1];
                argtypes[0] = convertClassToType(m.getParameterTypes()[i]);
                il.append(factory.createInvoke(nameOfWrapper,
                                               "<init>",
                                               Type.VOID,
                                               argtypes,
                                               Constants.INVOKESPECIAL));
            } else {
                // Simply pushes the argument on to the stack
                il.append(factory.createLoad(theType, (i + 1)));
            }

            // Stores the object in the array
            il.append(factory.createArrayStore(OBJECT_TYPE));
        }

        // So now we have the Method object and the array of objects on the stack,
        // Let's call the static method MethodCall.getMethodCall.
        Type[] argtypes = new Type[2];
        argtypes[0] = METHOD_TYPE;
        argtypes[1] = OBJECT_ARRAY_TYPE;
        il.append(factory.createInvoke("org.objectweb.proactive.core.mop.MethodCall",
                                       "getMethodCall",
                                       convertClassNameToType("org.objectweb.proactive.core.mop.MethodCall"),
                                       argtypes,
                                       Constants.INVOKESTATIC));

        // Now, call 'reify' on the proxy object
        argtypes = new Type[1];
        argtypes[0] = convertClassNameToType("org.objectweb.proactive.core.mop.MethodCall");
        il.append(factory.createInvoke("org.objectweb.proactive.core.mop.Proxy",
                                       "reify",
                                       Type.OBJECT,
                                       argtypes,
                                       Constants.INVOKEINTERFACE));

        // If the return type of the method is a primitive type,
        // we want to unwrap it first
        if (m.getReturnType().isPrimitive()) {
            this.createUnwrappingCode(factory, m.getReturnType());
        } else {
            // If the return type is a reference type,
            // we need to insert a type check
            il.append(factory.createCheckCast((ReferenceType) convertClassToType(m.getReturnType())));
        }

        // Writes the code for inside constructor

        // Now we need to perform the call to super.blablabla if need be
        // Let's stack up the arguments
        InstructionHandle inConstructorHandle = il.append(factory.createThis());

        // The following line is for inserting the conditional branch instruction
        // at the beginning of the method. If the condition is satisfied, the
        // control flows move to the previous instruction
        il.insert(outsideConstructorHandle, factory.createBranchInstruction(Constants.IFEQ, inConstructorHandle));

        for (int i = 0; i < m.getParameterTypes().length; i++) {
            Type theType = convertClassToType(m.getParameterTypes()[i]);
            il.append(factory.createLoad(theType, (i + 1)));
        }

        // And perform the call
        String declaringClassName = this.methods[methodIndex].getDeclaringClass().getName();
        il.append(factory.createInvoke(declaringClassName,
                                       m.getName(),
                                       convertClassToType(m.getReturnType()),
                                       convertClassArrayToTypeArray(m.getParameterTypes()),
                                       Constants.INVOKESPECIAL));

        // Returns the result to the caller
        InstructionHandle returnHandle = il.append(factory.createReturn(convertClassToType(m.getReturnType())));

        // insert  a jump from the
        il.insert(inConstructorHandle, new GOTO(returnHandle));

        mg.removeLocalVariables();
        mg.setMaxStack(); // Needed stack size
        mg.setMaxLocals(); // Needed stack size

        return mg;
    }

    protected void createUnwrappingCode(InstructionFactory factory, Class c) {
        // Depending on the type of the wrapper, the code differs
        // The parameter 'c' represents the primitive type, not the type of the
        // wrapper

        if (c.equals(Void.TYPE)) {
            // There is nothing to do, simply pop the object returned by reify
            il.append(factory.createPop(1));
        } else {
            String nameOfPrimitiveType = c.getName();
            String nameOfWrapperClass = Utils.nameOfWrapper(c);
            // First, we should check that the object on top of
            // the stack is of the correct type
            il.append(factory.createCheckCast((ReferenceType) convertClassNameToType(nameOfWrapperClass)));
            // And then perform the call
            il.append(factory.createInvoke(nameOfWrapperClass,
                                           nameOfPrimitiveType + "Value",
                                           convertClassToType(c),
                                           new Type[0],
                                           Constants.INVOKEVIRTUAL));
        }

        return;
    }

    protected void createFields() {
        // Gets a reference to the constant pool
        ConstantPoolGen cp = this.classGenerator.getConstantPool();

        // Creates the boolean field that indicates whether or not
        // we are currently inside the constructor of the stub
        int flags1 = Constants.ACC_PROTECTED;
        Type type1 = Type.BOOLEAN;
        String name1 = "outsideConstructor";
        FieldGen f1 = new FieldGen(flags1, type1, name1, cp);

        this.classGenerator.addField(f1.getField());

        // Creates the field that points to the handler inside the MOP
        int flags2 = Constants.ACC_PROTECTED;
        Type type2 = PROXY_TYPE;
        String name2 = PROXY_FIELD_NAME;
        FieldGen f2 = new FieldGen(flags2, type2, name2, cp);

        this.classGenerator.addField(f2.getField());

        return;
    }

    protected void createConstructor() {
        // Actually creates the method generator
        MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, // access flags
                                     Type.VOID, // return type
                                     new Type[0],
                                     new String[0], // arg names
                                     "<init>", // Method name
                                     this.stubClassFullName, // Class name
                                     il, // Instructions list
                                     this.classGenerator.getConstantPool()
        );

        // Now, fills in the instruction list
        InstructionFactory factory = new InstructionFactory(this.classGenerator);

        // Calls the constructor of the super class
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createInvoke(this.className, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));

        // Sets the value of 'outsideConstructor' to true
        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(new ICONST(1));
        il.append(factory.createPutField(this.stubClassFullName, "outsideConstructor", Type.BOOLEAN));

        // And returns from the constructor
        il.append(InstructionConstants.RETURN);

        mg.setMaxStack(); // Needed stack size
        mg.setMaxLocals(); // Needed locals

        // Add the method to the class
        this.classGenerator.addMethod(mg.getMethod());

        // Recycling the InstructionList object
        this.il.dispose();

        return;
    }

    protected void createStaticVariables() {
        // Gets a reference to the constant pool
        ConstantPoolGen cp = this.classGenerator.getConstantPool();

        // Creates fields that contains the array of Method objects
        // that represent the reified methods of this class
        int flags1 = Constants.ACC_PROTECTED | Constants.ACC_STATIC;
        Type type1 = METHOD_ARRAY_TYPE;
        String name1 = "methods";
        FieldGen f1 = new FieldGen(flags1, type1, name1, cp);

        this.classGenerator.addField(f1.getField());

        return;
    }

    protected void createStaticInitializer() {
        // Creates the class initializer method itself
        MethodGen mg = new MethodGen(Constants.ACC_STATIC, // Static method
                                     Type.VOID, // returns mothing
                                     new Type[0], // No arguments
                                     new String[0],
                                     Constants.STATIC_INITIALIZER_NAME,
                                     this.stubClassFullName,
                                     il,
                                     this.classGenerator.getConstantPool()
        );

        // An instruction factory for helping us
        InstructionFactory factory = new InstructionFactory(this.classGenerator);

        // Creates an array of Method objects that we will store into the static
        // variable 'methods' of type 'Method[]'
        // Pushes the size of the array on to the stack
        il.append(new PUSH(this.classGenerator.getConstantPool(), this.methods.length));
        // Creates an array of Method objects of that size
        il.append((Instruction) factory.createNewArray(METHOD_TYPE, (byte) 1));

        // Stores the reference to this newly-created array into the static variable 'methods'
        il.append(factory.createPutStatic(this.stubClassFullName, "methods", METHOD_ARRAY_TYPE));

        // Creates an array of Class objects that represent all the superclasses of
        // the stub class.
        Vector vectorOfSuperClasses = new Vector();
        Class currentClass = cl;
        while (currentClass != null) {
            vectorOfSuperClasses.addElement(currentClass);
            currentClass = currentClass.getSuperclass();
        }

        // Pushes on the stack the size of the array
        il.append(new PUSH(this.classGenerator.getConstantPool(), vectorOfSuperClasses.size()));
        // Creates an array of class objects of that size
        il.append((Instruction) factory.createNewArray(CLASS_TYPE, (byte) 1));
        // Stores the reference to this newly-created array as the local variable with index '1'
        il.append(factory.createStore(Type.OBJECT, 1));

        // Make as many calls to Class.forName as is needed to fill in the array
        for (int i = 0; i < vectorOfSuperClasses.size(); i++) {
            // Load onto the stack a pointer to the array
            il.append(factory.createLoad(Type.OBJECT, 1));
            // Load the index in the array where we want to store the result
            il.append(new PUSH(this.classGenerator.getConstantPool(), i));
            // Loads the name of the class onto the stack
            String s = ((Class) vectorOfSuperClasses.elementAt(i)).getName();
            il.append(new PUSH(this.classGenerator.getConstantPool(), s));
            // Performs the call to Class.forName
            Type[] argstypes = new Type[1];
            argstypes[0] = Type.STRING;
            il.append(factory.createInvoke("java.lang.Class",
                                           "forName",
                                           CLASS_TYPE,
                                           argstypes,
                                           Constants.INVOKESTATIC));

            // Stores the result of the invocation of forName into the array
            // The index into which to store as well as the reference to the array
            // are already on the stack
            il.append(factory.createArrayStore(Type.OBJECT));
        }

        // Now, lookup each of the Method objects and store it into the 'method' array
        for (int i = 0; i < this.methods.length; i++) {
            // Stacks up the reference to the array of methods and the index in the array
            il.append(factory.createGetStatic(this.stubClassFullName, "methods", METHOD_ARRAY_TYPE));
            il.append(new PUSH(this.classGenerator.getConstantPool(), i));

            // Now, we load onto the stack a pointer to the class that contains the method
            int indexInClassArray = vectorOfSuperClasses.indexOf(this.methods[i].getDeclaringClass());
            if (indexInClassArray == -1) {
                System.err.println("Problem : cannot find index for class " + this.methods[i].getDeclaringClass());
            }
            // Load a pointer to the Class array (local variable number 1)
            il.append(factory.createLoad(Type.OBJECT, 1));
            // Access element number 'indexInClassArray'
            il.append(new PUSH(this.classGenerator.getConstantPool(), indexInClassArray));
            il.append(factory.createArrayLoad(CLASS_TYPE));
            // Now, perform a call to 'getDeclaredMethod'
            // First, stack up the simple name of the method to solve
            il.append(new PUSH(this.classGenerator.getConstantPool(), this.methods[i].getName()));
            // Now, we want to create an array of type Class[] for representing
            // the parameters to this method. We choose to store this array into the
            // slot number 2
            // Pushes the size of the array
            il.append(new PUSH(this.classGenerator.getConstantPool(), this.methods[i].getParameterTypes().length));
            // Creates an array of class objects of that size
            il.append((Instruction) factory.createNewArray(CLASS_TYPE, (byte) 1));
            // Stores the reference to this newly-created array as the local variable with index '2'
            il.append(factory.createStore(Type.OBJECT, 2));

            // Stack up the class objects that represent the types of all the arguments to this method
            for (int j = 0; j < this.methods[i].getParameterTypes().length; j++) {
                Class currentParameter = this.methods[i].getParameterTypes()[j];

                // Load onto the stack a pointer to the array of Class objects (for parameters)
                il.append(factory.createLoad(Type.OBJECT, 2));
                // Load the index in the array where we want to store the result
                il.append(new PUSH(this.classGenerator.getConstantPool(), j));

                // If the type of the parameter is a primitive one, we use the predefined
                // constants (like java.lang.Integer.TYPE) instead of calling Class.forName
                if (currentParameter.isPrimitive()) {
                    // Loads that static variable
                    il.append(factory.createGetStatic(Utils.getWrapperClass(currentParameter).getName(),
                                                      "TYPE",
                                                      CLASS_TYPE));
                } else {
                    // Load the name of the parameter class onto the stack
                    il.append(new PUSH(this.classGenerator.getConstantPool(), currentParameter.getName()));
                    // Performs a call to Class.forName
                    Type[] argstypes = new Type[1];
                    argstypes[0] = Type.STRING;
                    il.append(factory.createInvoke("java.lang.Class",
                                                   "forName",
                                                   CLASS_TYPE,
                                                   argstypes,
                                                   Constants.INVOKESTATIC));
                }

                // Stores the result in the array
                il.append(factory.createArrayStore(Type.OBJECT));
            }

            // Loads the array
            il.append(factory.createLoad(Type.OBJECT, 2));

            // Perform the actual call to 'getDeclaredMethod'
            Type[] argstypes = new Type[2];
            argstypes[0] = Type.STRING;
            argstypes[1] = CLASS_ARRAY_TYPE;
            il.append(factory.createInvoke("java.lang.Class",
                                           "getDeclaredMethod",
                                           METHOD_TYPE,
                                           argstypes,
                                           Constants.INVOKEVIRTUAL));
            // Now that we have the result, let's store it into the array
            il.append(factory.createArrayStore(Type.OBJECT));
        }

        // And returns
        il.append(InstructionConstants.RETURN);

        mg.setMaxStack(); // Needed stack size
        mg.setMaxLocals(); // Needed locals

        // Add the method to the class
        this.classGenerator.addMethod(mg.getMethod());

        // Recycling the InstructionList object
        this.il.dispose();

        return;
    }

    protected void createGetAndSetProxyMethods() {
        // Do the getProxy method first
        MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, // access flags
                                     PROXY_TYPE,
                                     new Type[0],
                                     new String[0], // arg names
                                     "getProxy", // Method name
                                     this.stubClassFullName, // Class name
                                     il, // Instructions list
                                     this.classGenerator.getConstantPool()
        );

        // Now, fills in the instruction list
        InstructionFactory factory = new InstructionFactory(this.classGenerator);

        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createGetField(this.stubClassFullName, PROXY_FIELD_NAME, PROXY_TYPE));
        il.append(factory.createReturn(Type.OBJECT));

        mg.setMaxStack(); // Needed stack size
        mg.setMaxLocals(); // Needed locals

        this.classGenerator.addMethod(mg.getMethod());

        this.il.dispose();

        // Now, do the setProxy method
        Type[] types = new Type[1];
        types[0] = PROXY_TYPE;
        String[] argnames = new String[1];
        argnames[0] = "p";

        mg = new MethodGen(Constants.ACC_PUBLIC, // access flags
                           Type.VOID,
                           types,
                           argnames,
                           "setProxy", // Method name
                           this.stubClassFullName, // Class name
                           il, // Instructions list
                           this.classGenerator.getConstantPool()
        );

        // Now, fills in the instruction list
        factory = new InstructionFactory(this.classGenerator);

        il.append(factory.createLoad(Type.OBJECT, 0));
        il.append(factory.createLoad(Type.OBJECT, 1));
        il.append(factory.createPutField(this.stubClassFullName, PROXY_FIELD_NAME, PROXY_TYPE));
        il.append(factory.createReturn(Type.VOID));

        mg.setMaxStack(); // Needed stack size
        mg.setMaxLocals(); // Needed locals

        this.classGenerator.addMethod(mg.getMethod());

        this.il.dispose();

        return;
    }

    protected static Type convertClassNameToType(String className) {
        try {
            return convertClassToType(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Converts a java.lang.Class object to its org.apache.bcel.generic.Type equivalent
     */

    protected static Type convertClassToType(Class cl) {
        if (cl.isPrimitive()) {
            if (cl.equals(Void.TYPE)) {
                return Type.VOID;
            } else if (cl.equals(Boolean.TYPE)) {
                return Type.BOOLEAN;
            } else if (cl.equals(Byte.TYPE)) {
                return Type.BYTE;
            } else if (cl.equals(Short.TYPE)) {
                return Type.SHORT;
            } else if (cl.equals(Integer.TYPE)) {
                return Type.INT;
            } else if (cl.equals(Character.TYPE)) {
                return Type.CHAR;
            } else if (cl.equals(Long.TYPE)) {
                return Type.LONG;
            } else if (cl.equals(Float.TYPE)) {
                return Type.FLOAT;
            } else if (cl.equals(Double.TYPE)) {
                return Type.DOUBLE;
            } else {
                return Type.UNKNOWN;
            }
        } else {
            if (cl.isArray()) {
                return new ArrayType(convertClassToType(cl.getComponentType()), 1);
            } else {
                return new ObjectType(cl.getName());
            }
        }
    }

    protected static Type[] convertClassArrayToTypeArray(Class[] cl) {
        Type[] result = new Type[cl.length];
        for (int i = 0; i < cl.length; i++) {
            result[i] = convertClassToType(cl[i]);
        }
        return result;
    }

    /**
     * This method is called by the constructor
     */

    protected void setInfos() {
        Class currentType;
        Class[] excs;

        // This hashtable is used for keeping track of the method signatures
        // we have already met while going up the inheritance branch
        java.util.Hashtable temp = new java.util.Hashtable();

        // Recursively calls getDeclaredMethods () on the target type
        // and each of its superclasses, all the way up to java.lang.Object
        // We have to be careful and only take into account overriden methods once
        Class currentClass = this.cl;

        // This Vector keeps track of all the methods accessible from this class
        java.util.Vector tempVector = new java.util.Vector();
        Class[] params;
        Object exists;

        // If the target type is an interface, the only thing we have to do is to
        // get the list of all its public methods.
        if (this.cl.isInterface()) {
            Method[] allPublicMethods = this.cl.getMethods();
            for (int i = 0; i < allPublicMethods.length; i++) {
                tempVector.addElement(allPublicMethods[i]);
            }
        } else // If the target type is an actual class, we climb up the tree
        {
            do // Loops from the current class up to java.lang.Object
            {
                // The declared methods for the current class
                Method[] declaredMethods = currentClass.getDeclaredMethods();

                // For each method declared in this class
                for (int i = 0; i < declaredMethods.length; i++) {
                    Method currentMethod = declaredMethods[i];
                    // Build a key with the simple name of the method
                    // and the names of its parameters in the right order
                    String key = "";
                    key = key + currentMethod.getName();
                    params = currentMethod.getParameterTypes();
                    for (int k = 0; k < params.length; k++) {
                        key = key + params[k].getName();
                    }

                    // Tests if we already have met this method in a subclass
                    exists = temp.get(key);
                    if (exists == null) {
                        // The only method we ABSOLUTELY want to be called directly
                        // on the stub (and thus not reified) is
                        // the protected void finalize () throws Throwable
                        if ((key.equals("finalize")) && (params.length == 0)) {
                            // Do nothing, simply avoid adding this method to the list
                        } else {
                            // If not, adds this method to the Vector that
                            // holds all the methods for this class
                            tempVector.addElement(currentMethod);
                            temp.put(key, currentMethod);
                        }
                    } else {
                        // We already know this method because it is overriden
                        // in a subclass. Then do nothing
                    }
                }
                currentClass = currentClass.getSuperclass();
            } while (currentClass != null); // Continue until we ask for the superclass of java.lang.Object
        }

        // Turns the vector into an array of type Method[]
        this.methods = new Method[tempVector.size()];
        tempVector.copyInto(this.methods);

        // Determines which methods are valid for reification
        // It is the responsibility of method checkMethod in class Utils
        // to decide if a method is valid for reification or not

        java.util.Vector v = new java.util.Vector();
        int initialNumberOfMethods = this.methods.length;

        for (int i = 0; i < initialNumberOfMethods; i++) {
            if (Utils.checkMethod(this.methods[i])) {
                v.addElement(this.methods[i]);
            }
        }
        Method[] validMethods = new Method[v.size()];
        v.copyInto(validMethods);

        // Installs the list of valid methods as an instance variable of this object
        this.methods = validMethods;

        this.packageName = Utils.getPackageName(this.className);
        this.stubClassFullName = Utils.convertClassNameToStubClassName(this.className);
        this.stubClassSimpleName = Utils.getSimpleName(this.stubClassFullName);

        return;
    }
}
