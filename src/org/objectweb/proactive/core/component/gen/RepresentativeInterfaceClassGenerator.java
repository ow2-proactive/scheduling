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
package org.objectweb.proactive.core.component.gen;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.ProActiveInterfaceImpl;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.StubObject;

/**
 * This class generates representative interfaces objects, which are created on the client side along with the component representative
 * object (@see org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeImpl).
 * 
 * @author Matthieu Morel
 *
 */
public class RepresentativeInterfaceClassGenerator
    extends AbstractInterfaceClassGenerator {
    private static RepresentativeInterfaceClassGenerator instance;

    // this boolean for deciding of a possible indirection for the functionnal calls
    protected boolean isPrimitive = false;

    public RepresentativeInterfaceClassGenerator() {
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
        byte[] b = (byte[]) getGeneratedClassesCache().get(classname);
        if (b!=null) {
            return b;
        }
        if (Utils.isRepresentativeClassName(classname)) {
            // try to generate a representative
            logger.info(
            "Trying to generate representative class : " + classname);
            b = generateInterfaceByteCode(classname);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    public ProActiveInterface generateInterface(final String interfaceName,
        Component owner, InterfaceType interfaceType, boolean isInternal,
        boolean isFunctionalInterface)
        throws InterfaceGenerationFailedException {
        try {
            Class generated_class = generateInterfaceClass(interfaceName, interfaceType.getFcItfSignature(), isFunctionalInterface);

            ProActiveInterfaceImpl reference = (ProActiveInterfaceImpl) generated_class.newInstance();
            reference.setFcItfName(interfaceName);
            reference.setFcItfOwner(owner);
            reference.setFcType(interfaceType);
            reference.setFcIsInternal(isInternal);

            return reference;
        } catch (Exception e) {
            throw new InterfaceGenerationFailedException("Cannot generate representative with javassist",
                e);
        }
    }

    public Class generateInterfaceClass(final String interfaceName, String interfaceSignature, boolean isFunctionalInterface) throws NotFoundException, CannotCompileException, IOException {
        String representativeClassName = org.objectweb.proactive.core.component.gen.Utils.getMetaObjectComponentRepresentativeClassName(interfaceName,
                interfaceSignature);
        Class generated_class;

        // check whether class has already been generated
        try {
            generated_class = loadClass(representativeClassName);
        } catch (ClassNotFoundException cnfe) {
            byte[] bytecode = generateInterfaceByteCode(representativeClassName);

            // convert the bytes into a Class
            generated_class = defineClass(representativeClassName, bytecode);
        }
        return generated_class;
    }

    public static byte[] generateInterfaceByteCode(String representativeClassName) {
        try {
            String interfaceName = Utils.getInterfaceNameFromRepresentativeClassName(representativeClassName);
            String interfaceSignature = Utils.getInterfaceSignatureFromRepresentativeClassName(representativeClassName);
            boolean isFunctionalInterface = (!Utils.getInterfaceNameFromRepresentativeClassName(representativeClassName).endsWith("-controller"));
        CtMethod[] reifiedMethods;
        CtClass generatedCtClass = pool.makeClass(representativeClassName);

        //this.fcInterfaceName = fcInterfaceName;
        //isPrimitive = ((ProActiveComponentRepresentativeImpl) owner).getHierarchicalType()
        //                                                    .equals(ComponentParameters.PRIMITIVE);
        List interfacesToImplement = new ArrayList();

        // add interface to reify
        CtClass functional_itf = pool.get(interfaceSignature);
        generatedCtClass.addInterface(functional_itf);

        interfacesToImplement.add(functional_itf);

        // add Serializable interface
        interfacesToImplement.add(pool.get(Serializable.class.getName()));
        generatedCtClass.addInterface(pool.get(
                Serializable.class.getName()));

        // add StubObject, so we can set the proxy
        generatedCtClass.addInterface(pool.get(
                StubObject.class.getName()));

        //interfacesToImplement.add(pool.get(StubObject.class.getName()));
        List interfacesToImplementAndSuperInterfaces = new ArrayList(interfacesToImplement);
        addSuperInterfaces(interfacesToImplementAndSuperInterfaces);
        generatedCtClass.setSuperclass(pool.get(
                ProActiveInterfaceImpl.class.getName()));
        JavassistByteCodeStubBuilder.createStubObjectMethods(generatedCtClass);
        CtField interfaceNameField = new CtField(ClassPool.getDefault()
                                                          .get(String.class.getName()),
                "interfaceName", generatedCtClass);
        interfaceNameField.setModifiers(Modifier.STATIC);
        generatedCtClass.addField(interfaceNameField,
            "\"" + interfaceName + "\"");

        CtField methodsField = new CtField(pool.get(
                    "java.lang.reflect.Method[]"), "overridenMethods",
                generatedCtClass);
        methodsField.setModifiers(Modifier.STATIC);

        generatedCtClass.addField(methodsField);

        // list all methods to implement
        Map methodsToImplement = new HashMap();
        List classesIndexer = new Vector();

        CtClass[] params;
        CtClass itf;

        // now get the methods from implemented interfaces
        Iterator it = interfacesToImplementAndSuperInterfaces.iterator();
        while (it.hasNext()) {
            itf = (CtClass) it.next();
            if (!classesIndexer.contains(itf.getName())) {
                classesIndexer.add(itf.getName());
            }

            CtMethod[] declaredMethods = itf.getDeclaredMethods();

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

                // this gives the actual declaring Class of this method
                methodsToImplement.put(key, currentMethod);
            }
        }

        reifiedMethods = (CtMethod[]) (methodsToImplement.values()
                                                         .toArray(new CtMethod[methodsToImplement.size()]));

        // Determines which reifiedMethods are valid for reification
        // It is the responsibility of method checkMethod in class Utils
        // to decide if a method is valid for reification or not
        Vector v = new Vector();
        int initialNumberOfMethods = reifiedMethods.length;

        for (int i = 0; i < initialNumberOfMethods; i++) {
            if (JavassistByteCodeStubBuilder.checkMethod(
                        reifiedMethods[i])) {
                v.addElement(reifiedMethods[i]);
            }
        }
        CtMethod[] validMethods = new CtMethod[v.size()];
        v.copyInto(validMethods);

        reifiedMethods = validMethods;

        JavassistByteCodeStubBuilder.createStaticInitializer(generatedCtClass,
            reifiedMethods, classesIndexer);

        createReifiedMethods(generatedCtClass, reifiedMethods,
            isFunctionalInterface);

        //                generatedCtClass.writeFile("generated/");
        //                System.out.println("[JAVASSIST] generated class : " +
        //                    representativeClassName);
        byte[] bytecode = generatedCtClass.toBytecode();
        RepresentativeInterfaceClassGenerator.generatedClassesCache.put(representativeClassName,
            generatedCtClass.toBytecode());
        if (logger.isDebugEnabled()) {
            logger.debug("added " + representativeClassName +
                " to cache");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("generated classes cache is : " +
                generatedClassesCache.toString());
        }
        return bytecode;
        } catch (Exception e) {
            logger.error("Cannot generate class : " + representativeClassName);
            return null;
        }
    }

    protected static void createReifiedMethods(CtClass generatedClass,
        CtMethod[] reifiedMethods, boolean isFunctionalInterface)
        throws NotFoundException, CannotCompileException {
        for (int i = 0; i < reifiedMethods.length; i++) {
            CtClass[] paramTypes = reifiedMethods[i].getParameterTypes();
            String body = ("{\nObject[] parameters = new Object[" +
                paramTypes.length + "];\n");
            for (int j = 0; j < paramTypes.length; j++) {
                if (paramTypes[j].isPrimitive()) {
                    body += ("  parameters[" + j + "]=" +
                    JavassistByteCodeStubBuilder.wrapPrimitiveParameter(paramTypes[j],
                        "$" + (j + 1)) + ";\n");
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
            body += ("myProxy.reify(org.objectweb.proactive.core.mop.MethodCall.getComponentMethodCall(" +
            "(java.lang.reflect.Method)overridenMethods[" + i + "]" +
            ", parameters, interfaceName," + isFunctionalInterface + "))");
            if (postWrap != null) {
                body += postWrap;
            }
            body += ";";
            body += "\n}";
            //         System.out.println("method : " + reifiedMethods[i].getName() +
            //             " : \n" + body);
            CtMethod methodToGenerate = CtNewMethod.make(reifiedMethods[i].getReturnType(),
                    reifiedMethods[i].getName(),
                    reifiedMethods[i].getParameterTypes(),
                    reifiedMethods[i].getExceptionTypes(), body, generatedClass);
            generatedClass.addMethod(methodToGenerate);
        }
    }
    
}
