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
*
*  Contributor(s): Eric Bruneton
*
* ################################################################
*/
package org.objectweb.proactive.core.mop;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class ASMBytecodeStubBuilder implements Constants {
	
	protected static Logger logger = Logger.getLogger(ASMBytecodeStubBuilder.class.getName());
	
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
	protected ClassWriter classGenerator;

	// A few constants that come in handy when using ASM in our case
	protected static final String METHODCALL_TYPE = "Lorg/objectweb/proactive/core/mop/MethodCall;";
	protected static final String OBJECT_TYPE = "Ljava/lang/Object;";
	protected static final String OBJECT_ARRAY_TYPE = "[Ljava/lang/Object;";
	protected static final String METHOD_TYPE = "Ljava/lang/reflect/Method;";
	protected static final String METHOD_ARRAY_TYPE = "[Ljava/lang/reflect/Method;";
	protected static final String PROXY_TYPE = "Lorg/objectweb/proactive/core/mop/Proxy;";
	protected static final String STUB_INTERFACE_NAME = "org/objectweb/proactive/core/mop/StubObject";
	protected static final String PROXY_FIELD_NAME = "myProxy";

	public ASMBytecodeStubBuilder(String classname) throws ClassNotFoundException {
		// Obtains the object that represents the type we want to create
		// a wrapper class for. This call may fail with a ClassNotFoundException
		// if the class corresponding to this type cannot be found.
		logger.debug("ASMBytecodeStubBuilder.init<> classname " + classname);
		try {
		this.cl = Class.forName(classname);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw e;
		}
		logger.debug("ASMBytecodeStubBuilder.init<> 1");
		// Keep this info at hand for performance purpose
		this.className = classname;
		logger.debug("ASMBytecodeStubBuilder.init<> 2");
		// Fills in all the infos about this class
		this.setInfos();
	}
////Modif FAb
//	public ASMBytecodeStubBuilder(String classname, ClassLoader classLoader) throws ClassNotFoundException {
//		// Obtains the object that represents the type we want to create
//		// a wrapper class for. This call may fail with a ClassNotFoundException
//		// if the class corresponding to this type cannot be found.
//		if (classLoader != null) {
//			
//		
//		  this.cl = classLoader.loadClass(classname);
//		} else {
//			this.cl = Class.forName(classname);
//			
//		}
//		// Keep this info at hand for performance purpose
//		this.className = classname;
//
//		// Fills in all the infos about this class
//		this.setInfos();
//	}

	protected ClassWriter createClassGenerator() {
		String superclassName;
		String[] interfaces;

		// If we generate a stub for an interface type, we have to explicitely declare
		// that we implement the interface. Also, the name of the superclass is not the reified
		// type, but java.lang.Object
		if (this.cl.isInterface()) {
			superclassName = "java.lang.Object";
			interfaces = new String[3];
			interfaces[0] = "java/io/Serializable";
			interfaces[1] = STUB_INTERFACE_NAME;
			interfaces[2] = this.cl.getName().replace('.', '/');
		} else {
			superclassName = this.className;
			interfaces = new String[2];
			interfaces[0] = "java/io/Serializable";
			interfaces[1] = STUB_INTERFACE_NAME;
		}

		ClassWriter cw = new ClassWriter(true);
		cw.visit(Constants.ACC_PUBLIC | Constants.ACC_SUPER, // Same access modifiers as superclass or public ???
		this.stubClassFullName.replace('.', '/'), // Fully-qualified class name
		superclassName.replace('.', '/'), // Superclass
		interfaces, // declared interfaces
		"<generated>");
		return cw;
	}

	public byte[] create() {
		// Creates the class generator
		this.classGenerator = this.createClassGenerator();

		// Add a public no-arg constructor
		this.createConstructor();

		// Creates all the methods in the class
		for (int i = 0; i < this.methods.length; i++) {
			CodeVisitor mg = this.createMethod(i, this.methods[i]);
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

		// Next few lines for debugging only
//		try {
//			java.io.File file = new java.io.File ("generated/" , "ASM_" + stubClassSimpleName + ".class");
//			System.out.println("writing down the generated stub : " + file.getAbsolutePath());
//			java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
//			fos.write(this.classGenerator.toByteArray());
//			fos.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return this.classGenerator.toByteArray();
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
		m.getName(), // Method name
		mDesc, // return and argument types
	null, // exceptions
	null); // Attributes
		return cv;
	}

	protected static int removeNativeAndAbstractModifiers(int modifiers) {
		// In order to set to 0 the bit that represents 'native', we first
		// compute the mask that contains 1s everywhere and a 0 where the bit
		// is, and then apply this mask with an 'and', bitwise.
		int result = modifiers & (~Modifier.NATIVE);
		result = result & (~Modifier.ABSTRACT);
		return result;
	}

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

	protected CodeVisitor createMethod(int methodIndex, Method m) {
		CodeVisitor cv = createMethodGenerator(m);
		//InstructionFactory factory = new InstructionFactory(classGenerator);
		Label inConstructorHandle = new Label();

		if (this.cl.isInterface() == false) {
			// First, check if the method is called from within the constructor
			// Load 'this' onto the stack
			cv.visitVarInsn(ALOAD, 0);

			// Gets the value of the field 'outsideConstructor'
			cv.visitFieldInsn(GETFIELD, this.stubClassFullName.replace('.', '/'), "outsideConstructor", "Z");

			// The following line is for inserting the conditional branch instruction
			// at the beginning of the method. If the condition is satisfied, the
			// control flows move to the previous instruction
			cv.visitJumpInsn(IFEQ, inConstructorHandle);
		}

		// Now we create the code for the case where we are outside of
		// the constructor, i.e. we want the call to be reified

		// Pushes on the stack the reference to the proxy object
		cv.visitVarInsn(ALOAD, 0);
		cv.visitFieldInsn(GETFIELD, this.stubClassFullName.replace('.', '/'), PROXY_FIELD_NAME, PROXY_TYPE);

		// Pushes on the stack the Method object that represents the current method
		cv.visitFieldInsn(GETSTATIC, this.stubClassFullName.replace('.', '/'), "methods", METHOD_ARRAY_TYPE);
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
				} else /*if (param == Short.TYPE)*/ {
					type = "java/lang/Short";
					desc = "S";
				}
				cv.visitTypeInsn(NEW, type);
				cv.visitInsn(DUP);
				cv.visitVarInsn(opcode, indexInParameterArray);
				cv.visitMethodInsn(INVOKESPECIAL, type, "<init>", "(" + desc + ")V");
			} else {
				cv.visitVarInsn(ALOAD, indexInParameterArray);
			}
			indexInParameterArray += (param == Double.TYPE || param == Long.TYPE ? 2 : 1);

			// Stores the object in the array
			cv.visitInsn(AASTORE);
		}

		// So now we have the Method object and the array of objects on the stack,
		// Let's call the static method MethodCall.getMethodCall.
		cv.visitMethodInsn(
			INVOKESTATIC,
			"org/objectweb/proactive/core/mop/MethodCall",
			"getMethodCall",
			"(" + METHOD_TYPE + OBJECT_ARRAY_TYPE + ")" + METHODCALL_TYPE);

		// Now, call 'reify' on the proxy object
		cv.visitMethodInsn(
			//INVOKEVIRTUAL,
			// BUGFIX: use INVOKEINTERFACE because Proxy is an interface
			INVOKEINTERFACE,
			"org/objectweb/proactive/core/mop/Proxy",
			"reify",
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

		// Writes the code for inside constructor
		// What follows is a quick (but not dirty, simply non-optimized) fix to the problem
		// of stubs built for interfaces, not classes. We simply do not perform the call
		if (this.cl.isInterface() == false) {
			// The following lines are for inserting the conditional branch instruction
			// at the beginning of the method. If the condition is satisfied, the
			// control flows move to the previous instruction
			Label returnHandle = new Label();
			cv.visitJumpInsn(GOTO, returnHandle);

			// Now we need to perform the call to super.blablabla if need be
			// Let's stack up the arguments
			cv.visitLabel(inConstructorHandle);
			cv.visitVarInsn(ALOAD, 0);


			// This is for browsing the parameter array, not forgetting that some parameters
			// require two slots (longs and doubles)
			indexInParameterArray = 1;
			for (int i = 0; i < paramTypes.length; ++i) {
				cv.visitVarInsn(ILOAD + getOpcodeOffset(paramTypes[i]), indexInParameterArray);
				indexInParameterArray += getSize(paramTypes[i]);
			}

			// And perform the call
			String declaringClassName = this.methods[methodIndex].getDeclaringClass().getName();
			cv.visitMethodInsn(
				INVOKESPECIAL,
				declaringClassName.replace('.', '/'),
				m.getName(),
				Type.getMethodDescriptor(m));

			// Returns the result to the caller
			cv.visitLabel(returnHandle);
			createReturnCode(cv, m.getReturnType());

		} else {
			// If we are an interface, we need to remove from the top of the stack
			// the value of  boolean field that tells whether we are inside a constructor
			// or not
			createReturnCode(cv, m.getReturnType());
		}
		cv.visitMaxs(0, 0); // max stack and max locals automatically computed

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
			} else /*if (result == Short.TYPE)*/ {
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

	protected void createFields() {
		// Creates the boolean field that indicates whether or not
		// we are currently inside the constructor of the stub
		// This is only necessary if we are generating a stub for
		// a type that is not an interface
		if (!this.cl.isInterface()) {
			this.classGenerator.visitField(ACC_PROTECTED, "outsideConstructor", "Z", null, null);
		}
		// Creates the field that points to the handler inside the MOP
		this.classGenerator.visitField(ACC_PROTECTED, PROXY_FIELD_NAME, PROXY_TYPE, null, null);
	}

	protected void createConstructor() {
		// Actually creates the method generator (ASM : uses the visitor)
		CodeVisitor cv = this.classGenerator.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

		if (!this.cl.isInterface()) {
			// Calls the constructor of the super class
			cv.visitVarInsn(ALOAD, 0);
			cv.visitMethodInsn(INVOKESPECIAL, this.className.replace('.', '/'), "<init>", "()V");

			// Sets the value of 'outsideConstructor' to true
			cv.visitVarInsn(ALOAD, 0);
			cv.visitInsn(ICONST_1);
			cv.visitFieldInsn(PUTFIELD, this.stubClassFullName.replace('.', '/'), "outsideConstructor", "Z");
		} else {
			// Calls the constructor of the super class
			cv.visitVarInsn(ALOAD, 0);
			cv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		}

		// And returns from the constructor
		cv.visitInsn(RETURN);

		// Needed stack size
		// Needed locals
		cv.visitMaxs(0, 0);


		return;
	}

	protected void createStaticVariables() {
		// Creates fields that contains the array of Method objects
		// that represent the reified methods of this class
		this.classGenerator.visitField(ACC_PROTECTED | ACC_STATIC, "methods", METHOD_ARRAY_TYPE, null, null);
		return;
	}

	protected void createStaticInitializer() {
		// Creates the class initializer method itself
		CodeVisitor cv = this.classGenerator.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);

		// Creates an array of Method objects that we will store into the static
		// variable 'methods' of type 'Method[]'
		// Pushes the size of the array on to the stack
		pushInt(cv, this.methods.length);
		// Creates an array of Method objects of that size
		cv.visitTypeInsn(ANEWARRAY, "java/lang/reflect/Method");

		// Stores the reference to this newly-created array into the static variable 'methods'
		cv.visitFieldInsn(PUTSTATIC, this.stubClassFullName.replace('.', '/'), "methods", METHOD_ARRAY_TYPE);

		// Creates an array of Class objects that represent all the superclasses of
		// the stub class.
		List vectorOfSuperClasses = new Vector();
		Class currentClass = cl;
		while (currentClass != null) {
			vectorOfSuperClasses.add(currentClass);
			currentClass = currentClass.getSuperclass();
		}
		
		// BUGFIX #300591 do not forget implemented interfaces and super-interfaces
		Utils.addSuperInterfaces(cl, vectorOfSuperClasses);
		
		// Pushes on the stack the size of the array
		pushInt(cv, vectorOfSuperClasses.size());
		// Creates an array of class objects of that size
		cv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
		// Stores the reference to this newly-created array as the local variable with index '1'
		cv.visitVarInsn(ASTORE, 1);

		// Make as many calls to Class.forName as is needed to fill in the array
		for (int i = 0; i < vectorOfSuperClasses.size(); i++) {
			// Load onto the stack a pointer to the array
			cv.visitVarInsn(ALOAD, 1);
			// Load the index in the array where we want to store the result
			pushInt(cv, i);
			// Loads the name of the class onto the stack
			String s = ((Class) vectorOfSuperClasses.get(i)).getName();
			cv.visitLdcInsn(s);
			// Performs the call to Class.forName
			cv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");

			// Stores the result of the invocation of forName into the array
			// The index into which to store as well as the reference to the array
			// are already on the stack
			cv.visitInsn(AASTORE);
		}

		// Now, lookup each of the Method objects and store it into the 'method' array
		for (int i = 0; i < this.methods.length; i++) {
			// Stacks up the reference to the array of methods and the index in the array
			cv.visitFieldInsn(GETSTATIC, this.stubClassFullName.replace('.', '/'), "methods", METHOD_ARRAY_TYPE);
			pushInt(cv, i);

			// Now, we load onto the stack a pointer to the class that contains the method
			int indexInClassArray = vectorOfSuperClasses.indexOf(this.methods[i].getDeclaringClass());
			if (indexInClassArray == -1) {
			}
			// Load a pointer to the Class array (local variable number 1)
			cv.visitVarInsn(ALOAD, 1);
			// Access element number 'indexInClassArray'
			pushInt(cv, indexInClassArray);
			cv.visitInsn(AALOAD);
			// Now, perform a call to 'getDeclaredMethod'
			// First, stack up the simple name of the method to solve
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
			for (int j = 0; j < this.methods[i].getParameterTypes().length; j++) {
				Class currentParameter = this.methods[i].getParameterTypes()[j];

				// Load onto the stack a pointer to the array of Class objects (for parameters)
				cv.visitVarInsn(ALOAD, 2);
				// Load the index in the array where we want to store the result
				pushInt(cv, j);

				// If the type of the parameter is a primitive one, we use the predefined
				// constants (like java.lang.Integer.TYPE) instead of calling Class.forName
				if (currentParameter.isPrimitive()) {
					// Loads that static variable
					cv.visitFieldInsn(
						GETSTATIC,
						Type.getInternalName(Utils.getWrapperClass(currentParameter)),
						"TYPE",
						"Ljava/lang/Class;");
				} else {
					// Load the name of the parameter class onto the stack
					cv.visitLdcInsn(currentParameter.getName());
					// Performs a call to Class.forName
					cv.visitMethodInsn(
						INVOKESTATIC,
						"java/lang/Class",
						"forName",
						"(Ljava/lang/String;)Ljava/lang/Class;");
				}

				// Stores the result in the array
				cv.visitInsn(AASTORE);
			}

			// Loads the array
			cv.visitVarInsn(ALOAD, 2);

			// Perform the actual call to 'getDeclaredMethod'
			cv.visitMethodInsn(
				INVOKEVIRTUAL,
				"java/lang/Class",
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
		CodeVisitor cv = this.classGenerator.visitMethod(ACC_PUBLIC, "getProxy", "()" + PROXY_TYPE, null, null);

		// Now, fills in the instruction list
		cv.visitVarInsn(ALOAD, 0);
		cv.visitFieldInsn(GETFIELD, this.stubClassFullName.replace('.', '/'), PROXY_FIELD_NAME, PROXY_TYPE);
		cv.visitInsn(ARETURN);

		// Needed stack size
		// Needed locals
		cv.visitMaxs(0, 0);

		// Now, do the setProxy method
		cv = this.classGenerator.visitMethod(ACC_PUBLIC, "setProxy", "(" + PROXY_TYPE + ")V", null, null);

		// Now, fills in the instruction list
		cv.visitVarInsn(ALOAD, 0);
		cv.visitVarInsn(ALOAD, 1);
		cv.visitFieldInsn(PUTFIELD, this.stubClassFullName.replace('.', '/'), PROXY_FIELD_NAME, PROXY_TYPE);
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
	 * This method is called by the constructor
	 */

	protected void setInfos() {
		logger.debug("ASMByteCodeStubBuilder.setInfos()");
		// This hashtable is used for keeping track of the method signatures
		// we have already met while going up the inheritance branch
		java.util.Hashtable temp = new java.util.Hashtable();

		// Recursively calls getDeclaredMethods () on the target type
		// and each of its superclasses, all the way up to java.lang.Object
		// We have to be careful and only take into account overriden methods once
		Class currentClass = this.cl;

		// This Vector keeps track of all the methods accessible from this class
		Vector tempVector = new Vector();
		Class[] params;
		Object exists;

		// If the target type is an interface, the only thing we have to do is to
		// get the list of all its public methods.
		logger.debug("ASMByteCodeStubBuilder.setInfos() 1");
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
				logger.debug("ASMByteCodeStubBuilder.setInfos() 2");
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
					logger.debug("ASMByteCodeStubBuilder.setInfos() 3");
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

		Vector v = new Vector();
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

	public String getStubClassFullName() {
		return this.stubClassFullName;
	}

	// ASM : added utility methods

	static void pushInt(CodeVisitor cv, int i) {
		if (i >= -128 && i < 128) {
			cv.visitIntInsn(BIPUSH, i);
		} else if (i >= -32768 && i < 32768) {
			cv.visitIntInsn(SIPUSH, i);
		} else {
			cv.visitLdcInsn(new Integer(i));
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
		return (type == Double.TYPE || type == Long.TYPE ? 2 : 1);
	}
}
