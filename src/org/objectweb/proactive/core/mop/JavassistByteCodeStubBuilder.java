/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.mop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;


/**
 * This class generates the bytecode for proactive stubs using Javassist.
 *
 * @author Matthieu Morel
 *
 */
public class JavassistByteCodeStubBuilder {
    private static CtMethod proxyGetter;
    private static CtMethod proxySetter;

    /**
     * <p>Creates the bytecode for a stub on the given class</p>
     * <p>This method should be accessed by one thread only for a given class name, otherwise
     * it may lead to unsupported concurrent class generation, resulting in a "frozen class" javassist runtime exception </p>
     * @param className the name of the class on which a stub class is created
     * @return the bytecode for the corresponding stub class
     * @throws NoClassDefFoundError if the specified classname does not correspond to a class in the classpath
     */
    public static byte[] create(String className) throws NoClassDefFoundError {
        CtClass generatedClass = null;
        CtMethod[] reifiedMethods;
        try {
            ClassPool pool = ClassPool.getDefault();
            generatedClass = pool.makeClass(Utils.convertClassNameToStubClassName(
                        className));
            
            CtClass superClass = null;
            try {
            	superClass = pool.get(className);
            } catch (NotFoundException e) {
            	// may happen in environments with multiple classloaders: className is not available
            	// in the initial classpath of javassist's class pool
            	// ==> try to append classpath of the class corresponding to className
            	pool.appendClassPath(new ClassClassPath(Class.forName(className)));
            	superClass = pool.get(className);
            }
            CtField outsideOfConstructorField = new CtField(pool.get(CtClass.booleanType.getName()), "outsideOfConstructor", generatedClass);
            
            generatedClass.addField(outsideOfConstructorField, (superClass.isInterface()? " false" : "true"));
            if (superClass.isInterface()) {
                generatedClass.addInterface(superClass);
                generatedClass.setSuperclass(pool.get(Object.class.getName()));
            } else {
                generatedClass.setSuperclass(superClass);
            }
            generatedClass.addInterface(pool.get(Serializable.class.getName()));
            generatedClass.addInterface(pool.get(StubObject.class.getName()));

            createStubObjectMethods(generatedClass);

            
            CtField methodsField = new CtField(pool.get(
                        "java.lang.reflect.Method[]"), "overridenMethods",
                    generatedClass);
            
            
            methodsField.setModifiers(Modifier.STATIC);
            generatedClass.addField(methodsField);

            //   This map is used for keeping track of the method signatures / methods that are to be reified
            java.util.Map<String, CtMethod> temp = new HashMap<String, CtMethod>();

            // Recursively calls getDeclaredMethods () on the target type
            // and each of its superclasses, all the way up to java.lang.Object
            // We have to be careful and only take into account overriden methods once
            CtClass currentClass = superClass;

            CtClass[] params;
            Object exists;

            List<String> classesIndexer = new Vector<String>();
            classesIndexer.add(superClass.getName());

            // If the target type is an interface, the only thing we have to do is to
            // get the list of all its public reifiedMethods.
            if (superClass.isInterface()) {
                CtMethod[] allPublicMethods = superClass.getMethods();
                for (int i = 0; i < allPublicMethods.length; i++) {
                    String key = "";
                    key = key + allPublicMethods[i].getName();
                    params = allPublicMethods[i].getParameterTypes();
                    for (int k = 0; k < params.length; k++) {
                        key = key + params[k].getName();
                    }
                    temp.put(key, allPublicMethods[i]);
                }
                classesIndexer.add("java.lang.Object");
            } else // If the target type is an actual class, we climb up the tree
             {
                do {
                    if (!classesIndexer.contains(currentClass.getName())) {
                        classesIndexer.add(currentClass.getName());
                    }

                    // The declared reifiedMethods for the current class
                    CtMethod[] declaredMethods = currentClass.getDeclaredMethods();

                    // For each method declared in this class
                    for (int i = 0; i < declaredMethods.length; i++) {
                        CtMethod currentMethod = declaredMethods[i];

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
                            if ((key.equals("finalize")) &&
                                    (params.length == 0)) {
                                // Do nothing, simply avoid adding this method to the list
                            } else {
                                // If not, adds this method to the Vector that
                                // holds all the reifiedMethods for this class
                                //                                tempVector.addElement(currentMethod);
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

            // now get the methods from implemented interfaces
            List<CtClass> superInterfaces = new Vector<CtClass>();
            addSuperInterfaces(superClass, superInterfaces);
            
            CtClass[] implementedInterfacesTable = (superInterfaces.toArray(new CtClass[superInterfaces.size()]));
            for (int i=0; i< implementedInterfacesTable.length; i++) {
                
            }
            for (int itfsIndex = 0;
                    itfsIndex < implementedInterfacesTable.length;
                    itfsIndex++) {
                if (!classesIndexer.contains(
                            implementedInterfacesTable[itfsIndex].getName())) {
                    classesIndexer.add(implementedInterfacesTable[itfsIndex].getName());
                }

                //              The declared methods for the current interface
                CtMethod[] declaredMethods = implementedInterfacesTable[itfsIndex].getDeclaredMethods();

                // For each method declared in this class
                for (int i = 0; i < declaredMethods.length; i++) {
                    CtMethod currentMethod = declaredMethods[i];

                    // Build a key with the simple name of the method
                    // and the names of its parameters in the right order
                    String key = "";
                    key = key + currentMethod.getName();
                    params = currentMethod.getParameterTypes();
                    for (int k = 0; k < params.length; k++) {
                        key = key + params[k].getName();
                    }

                    // replace with current one, because this gives the actual declaring Class of this method
                    temp.put(key, currentMethod);
                }
            }

            reifiedMethods = (temp.values().toArray(new CtMethod[temp.size()]));

            // Determines which reifiedMethods are valid for reification
            // It is the responsibility of method checkMethod 
            // to decide if a method is valid for reification or not
            Vector<CtMethod> v = new Vector<CtMethod>();
            int initialNumberOfMethods = reifiedMethods.length;

            for (int i = 0; i < initialNumberOfMethods; i++) {
                if (checkMethod(reifiedMethods[i])) {
                    v.addElement(reifiedMethods[i]);
                }
            }
            CtMethod[] validMethods = new CtMethod[v.size()];
            v.copyInto(validMethods);

            // Installs the list of valid reifiedMethods as an instance variable of this object
            reifiedMethods = validMethods;

            // create static block with method initializations
            createStaticInitializer(generatedClass, reifiedMethods,
                classesIndexer);

            createReifiedMethods(generatedClass, reifiedMethods, superClass.isInterface());
//                        generatedClass.writeFile();
//                        System.out.println("[JAVASSIST] generated class : " + className);

            // detach to fix some "frozen class" errors encountered in some large scale deployments 
            generatedClass.detach();
            return generatedClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoClassDefFoundError("Cannot generated stub for class " +
                className + " with javassist : " + e.getMessage());
        }
    }

    /**
     * @param generatedClass
     * @param reifiedMethods
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private static void createReifiedMethods(CtClass generatedClass,
        CtMethod[] reifiedMethods, boolean stubOnInterface)
        throws NotFoundException, CannotCompileException {
        for (int i = 0; i < reifiedMethods.length; i++) {
            CtClass[] paramTypes = reifiedMethods[i].getParameterTypes();
            String body = ("{\nObject[] parameters = new Object[" +
                paramTypes.length + "];\n");
            for (int j = 0; j < paramTypes.length; j++) {
                if (paramTypes[j].isPrimitive()) {
                    body += ("  parameters[" + j + "]=" +
                    wrapPrimitiveParameter(paramTypes[j], "$" + (j + 1)) +
                    ";\n");
                } else {
                    body += ("  parameters[" + j + "]=$" + (j + 1) + ";\n");
                }
            }
            CtClass returnType = reifiedMethods[i].getReturnType();
            String postWrap = null;
            String preWrap = null;

            if (returnType != CtClass.voidType) {
                if (!returnType.isPrimitive()) {
                    preWrap = "(" + returnType.getName() + ")";
                } else {
                    //boolean, byte, char, short, int, long, float, double
                    if (returnType.equals(CtClass.booleanType)) {
                        preWrap = "((Boolean)";
                        postWrap = ").booleanValue()";
                    }
                    if (returnType.equals(CtClass.byteType)) {
                        preWrap = "((Byte)";
                        postWrap = ").byteValue()";
                    }
                    if (returnType.equals(CtClass.charType)) {
                        preWrap = "((Character)";
                        postWrap = ").charValue()";
                    }
                    if (returnType.equals(CtClass.shortType)) {
                        preWrap = "((Short)";
                        postWrap = ").shortValue()";
                    }
                    if (returnType.equals(CtClass.intType)) {
                        preWrap = "((Integer)";
                        postWrap = ").intValue()";
                    }
                    if (returnType.equals(CtClass.longType)) {
                        preWrap = "((Long)";
                        postWrap = ").longValue()";
                    }
                    if (returnType.equals(CtClass.floatType)) {
                        preWrap = "((Float)";
                        postWrap = ").floatValue()";
                    }
                    if (returnType.equals(CtClass.doubleType)) {
                        preWrap = "((Double)";
                        postWrap = ").doubleValue()";
                    }
                }
                body += "return ";
                if (preWrap != null) {
                    body += preWrap;
                }
            }
            body += ("myProxy.reify(org.objectweb.proactive.core.mop.MethodCall.getMethodCall(" +
            "(java.lang.reflect.Method)overridenMethods[" + i + "]" +
            ", parameters))");
            if (postWrap != null) {
                body += postWrap;
            }
            body += ";";
            
            // the following is for inserting conditional statement for method code executing
            // within or outside the construction of the object
            if (!stubOnInterface) {
                String preReificationCode = "if (outsideOfConstructor) ";
                // outside of constructor : object is already constructed
                
                String postReificationCode = "\n} else {\n";
                // if inside constructor (i.e. in a method called by a
                // constructor from a super class)
                if (!reifiedMethods[i].getReturnType().equals(CtClass.voidType)) {
                    postReificationCode += "return ";
                }
                postReificationCode += "super." + reifiedMethods[i].getName() + "(";
                for (int j = 0; j < paramTypes.length; j++) {
                    postReificationCode += "$" + (j + 1)
                            + (((j + 1) < paramTypes.length) ? "," : "");
                }

                postReificationCode += ");";
                body = preReificationCode + body + postReificationCode;
            }
            body += "\n}";
//            System.out.println("method : " + reifiedMethods[i].getName()
//                    + " : \n" + body);
            CtMethod methodToGenerate = null;
            try {
                methodToGenerate = CtNewMethod.make(reifiedMethods[i].getReturnType(),
                        reifiedMethods[i].getName(),
                        reifiedMethods[i].getParameterTypes(),
                        reifiedMethods[i].getExceptionTypes(), body, generatedClass);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            generatedClass.addMethod(methodToGenerate);
        }
    }

    /**
     * @param generatedClass
     * @param reifiedMethods
     * @param classesIndexer
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    public static void createStaticInitializer(CtClass generatedClass,
        CtMethod[] reifiedMethods, List<String> classesIndexer)
        throws CannotCompileException, NotFoundException {
        CtConstructor classInitializer = generatedClass.makeClassInitializer();

        String classInitializerBody = "{\n";
        classInitializerBody += ("overridenMethods = new java.lang.reflect.Method[" +
        reifiedMethods.length + "];\n");
        classInitializerBody += ("Class classes[] = new Class[" +
        (classesIndexer.size()) + "];\n");
        classInitializerBody += "Class[] temp;\n";

        int methodsIndex = 0;
        Iterator<String> it = classesIndexer.iterator();
        int index = 0;
        while (it.hasNext()) {
            classInitializerBody += ("classes[" + index +
            "] = Class.forName(\"" + it.next() + "\");\n");
            index++;
        }
        for (int i = 0; i < reifiedMethods.length; i++) {
            CtClass[] paramTypes = reifiedMethods[i].getParameterTypes();
            classInitializerBody += ("temp = new Class[" + paramTypes.length +
            "];\n");
            for (int n = 0; n < paramTypes.length; n++) {
                if (paramTypes[n].isPrimitive()) {
                    classInitializerBody += ("temp[" + n + "] = " +
                    getClassTypeInitializer(paramTypes[n], false) + ";\n");
                } else {
                    classInitializerBody += ("temp[" + n +
                    "] = Class.forName(\"" +
                    getClassTypeInitializer(paramTypes[n], false) + "\");\n");
                }
            }
            classInitializerBody += ("overridenMethods[" + (methodsIndex) +
            "] = classes[" +
            classesIndexer.indexOf(reifiedMethods[i].getDeclaringClass()
                                                    .getName()) +
            "].getDeclaredMethod(\"" + reifiedMethods[i].getName() +
            "\", temp);\n");
            methodsIndex++;
        }

        classInitializerBody += "\n}";
        //System.out.println(classInitializerBody);
        classInitializer.setBody(classInitializerBody);
    }

    /**
     * @param generatedClass
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    public static void createStubObjectMethods(CtClass generatedClass)
        throws CannotCompileException, NotFoundException {
        CtField proxyField = new CtField(ClassPool.getDefault().get(Proxy.class.getName()),
                "myProxy", generatedClass);
        generatedClass.addField(proxyField);
        proxyGetter = CtNewMethod.getter("getProxy", proxyField);
        generatedClass.addMethod(proxyGetter);
        proxySetter = CtNewMethod.setter("setProxy", proxyField);
        generatedClass.addMethod(proxySetter);
    }

    private static String getClassTypeInitializer(CtClass param,
        boolean elementInArray) throws NotFoundException {
        if (param.isArray()) {
            return "[" +
            getClassTypeInitializer(param.getComponentType(), true);
        } else if (param.equals(CtClass.byteType)) {
            return elementInArray ? "B" : "Byte.TYPE";
        } else if (param.equals(CtClass.charType)) {
            return elementInArray ? "C" : "Character.TYPE";
        } else if (param.equals(CtClass.doubleType)) {
            return elementInArray ? "D" : "Double.TYPE";
        } else if (param.equals(CtClass.floatType)) {
            return elementInArray ? "F" : "Float.TYPE";
        } else if (param.equals(CtClass.intType)) {
            return elementInArray ? "I" : "Integer.TYPE";
        } else if (param.equals(CtClass.longType)) {
            return elementInArray ? "L" : "Long.TYPE";
        } else if (param.equals(CtClass.shortType)) {
            return elementInArray ? "S" : "Short.TYPE";
        } else if (param.equals(CtClass.booleanType)) {
            return elementInArray ? "Z" : "Boolean.TYPE";
        } else if (param.equals(CtClass.voidType)) {
            return elementInArray ? "V" : "Void.TYPE";
        } else {
            return elementInArray ? ("L" + param.getName() + ";")
                                  : (param.getName());
        }
    }

    public static String wrapPrimitiveParameter(CtClass paramType,
        String paramString) {
        if (CtClass.booleanType.equals(paramType)) {
            return "new Boolean(" + paramString + ")";
        }
        if (CtClass.byteType.equals(paramType)) {
            return "new Byte(" + paramString + ")";
        }
        if (CtClass.charType.equals(paramType)) {
            return "new Character(" + paramString + ")";
        }
        if (CtClass.doubleType.equals(paramType)) {
            return "new Double(" + paramString + ")";
        }
        if (CtClass.floatType.equals(paramType)) {
            return "new Float(" + paramString + ")";
        }
        if (CtClass.intType.equals(paramType)) {
            return "new Integer(" + paramString + ")";
        }
        if (CtClass.longType.equals(paramType)) {
            return "new Long(" + paramString + ")";
        }
        if (CtClass.shortType.equals(paramType)) {
            return "new Short(" + paramString + ")";
        }

        // that should not happen
        return null;
    }

    static public boolean checkMethod(CtMethod met) throws NotFoundException {
        int modifiers = met.getModifiers();

        // Final reifiedMethods cannot be reified since we cannot redefine them
        // in a subclass
        if (Modifier.isFinal(modifiers)) {
            return false;
        }

        // Static reifiedMethods cannot be reified since they are not 'virtual'
        if (Modifier.isStatic(modifiers)) {
            return false;
        }
        if (!(Modifier.isPublic(modifiers))) {
            return false;
        }

        // If method is finalize (), don't reify it
        if ((met.getName().equals("finalize")) &&
                (met.getParameterTypes().length == 0)) {
            return false;
        }
        
        if ((met.getSignature().equals(proxyGetter.getSignature()) || met.getSignature().equals(proxySetter.getSignature()))) {
            return false;
        }

        return true;
    }
    
    private static void addSuperInterfaces(CtClass cl, List<CtClass> superItfs) throws NotFoundException {
        if (!cl.isInterface() && !Modifier.isAbstract(cl.getModifiers())) {
        	// inspect interfaces AND abstract classes
            return;
        }
        CtClass[] super_interfaces = cl.getInterfaces();
        for (int i = 0; i < super_interfaces.length; i++) {
            superItfs.add(super_interfaces[i]);
            addSuperInterfaces(super_interfaces[i], superItfs);
        }
    }
    
}
