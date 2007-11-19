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
package org.objectweb.proactive.core.component.gen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.ProActiveInterfaceImpl;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.ClassDataCache;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Creates Interface implementations for the functional interfaces of the
 * component metaobject.
 *<br>
 * The functional calls are delegated to the "impl" field, whose value is set during
 * binding operations.
 *<br>
 * - In case of a primitive component, the impl field will be the reified object to
 * which the body is attached.<br>
 * - In case of a composite component, the impl field will be a component
 * representative.<br>
 * - For a parallel component, the impl field will be a group of component representatives.<br>
 *
 *  @author Matthieu Morel
 *
 */
public class MetaObjectInterfaceClassGenerator
    extends AbstractInterfaceClassGenerator {
    protected static final String IMPL_FIELD_NAME = "impl"; //delegatee
    private static MetaObjectInterfaceClassGenerator instance;

    // this boolean for deciding of a possible indirection for the functionnal calls
    protected boolean isPrimitive = false;

    public MetaObjectInterfaceClassGenerator() {
    }

    public static MetaObjectInterfaceClassGenerator instance() {
        if (instance == null) {
            return new MetaObjectInterfaceClassGenerator();
        } else {
            return instance;
        }
    }

    @Override
    public ProActiveInterface generateInterface(final String interfaceName,
        Component owner, ProActiveInterfaceType interfaceType,
        boolean isInternal, boolean isFunctionalInterface)
        throws InterfaceGenerationFailedException {
        try {
            if (ProActiveLogger.getLogger(Loggers.COMPONENTS_GEN_ITFS)
                                   .isDebugEnabled()) {
                ProActiveLogger.getLogger(Loggers.COMPONENTS_GEN_ITFS)
                               .debug("generating metaobject interface reference");
            }

            String generatedClassFullName = org.objectweb.proactive.core.component.gen.Utils.getMetaObjectClassName(interfaceName,
                    interfaceType.getFcItfSignature());

            Class<?> generated_class;

            // check whether class has already been generated
            try {
                generated_class = loadClass(generatedClassFullName);
            } catch (ClassNotFoundException cnfe) {
                CtMethod[] reifiedMethods;
                CtClass generatedCtClass = pool.makeClass(generatedClassFullName);

                //this.fcInterfaceName = fcInterfaceName;
                //isPrimitive = ((ProActiveComponentRepresentativeImpl) owner).getHierarchicalType()
                //                                                    .equals(ComponentParameters.PRIMITIVE);
                List<CtClass> interfacesToImplement = new ArrayList<CtClass>();

                // add interface to reify
                CtClass functional_itf = pool.get(interfaceType.getFcItfSignature());
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
                List<CtClass> interfacesToImplementAndSuperInterfaces = new ArrayList<CtClass>(interfacesToImplement);
                addSuperInterfaces(interfacesToImplementAndSuperInterfaces);

                generatedCtClass.setSuperclass(pool.get(
                        ProActiveInterfaceImpl.class.getName()));
                JavassistByteCodeStubBuilder.createStubObjectMethods(generatedCtClass);

                CtField implField = new CtField(pool.get(Object.class.getName()),
                        IMPL_FIELD_NAME, generatedCtClass);
                generatedCtClass.addField(implField);
                CtMethod implGetter = CtNewMethod.getter("getFcItfImpl",
                        implField);
                generatedCtClass.addMethod(implGetter);
                CtMethod implSetter = CtNewMethod.setter("setFcItfImpl",
                        implField);
                generatedCtClass.addMethod(implSetter);

                // field for overridden methods
                CtField methodsField = new CtField(pool.get(
                            "java.lang.reflect.Method[]"), "overridenMethods",
                        generatedCtClass);
                methodsField.setModifiers(Modifier.STATIC);
                generatedCtClass.addField(methodsField);

                // field for generics parameters
                CtField genericTypesMappingField = new CtField(pool.get(
                            "java.util.Map"), "genericTypesMapping",
                        generatedCtClass);

                genericTypesMappingField.setModifiers(Modifier.STATIC);
                generatedCtClass.addField(genericTypesMappingField);

                // list all methods to implement
                Map<String, CtMethod> methodsToImplement = new HashMap<String, CtMethod>();
                List<String> classesIndexer = new Vector<String>();

                CtClass[] params;
                CtClass itf;

                // now get the methods from implemented interfaces
                Iterator<CtClass> it = interfacesToImplementAndSuperInterfaces.iterator();
                while (it.hasNext()) {
                    itf = it.next();
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

                        // this gives the actual declaring Class<?> of this method
                        methodsToImplement.put(key, currentMethod);
                    }
                }

                reifiedMethods = methodsToImplement.values()
                                                   .toArray(new CtMethod[methodsToImplement.size()]);

                // Determines which reifiedMethods are valid for reification
                // It is the responsibility of method checkMethod in class Utils
                // to decide if a method is valid for reification or not
                Vector<CtMethod> v = new Vector<CtMethod>();
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
                    reifiedMethods, classesIndexer,
                    interfaceType.getFcItfSignature(), null);

                createMethods(generatedCtClass, reifiedMethods, interfaceType);

                //                                generatedCtClass.writeFile("generated/");
                //                                System.out.println("[JAVASSIST] generated class : " +
                //                                    generatedClassFullName);
                byte[] bytecode = generatedCtClass.toBytecode();
                ClassDataCache.instance()
                              .addClassData(generatedClassFullName, bytecode);
                if (logger.isDebugEnabled()) {
                    logger.debug("added " + generatedClassFullName +
                        " to cache");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("generated classes cache is : " +
                        ClassDataCache.instance().toString());
                }

                // convert the bytes into a Class<?>
                generated_class = Utils.defineClass(generatedClassFullName,
                        bytecode);
            }

            ProActiveInterfaceImpl reference = (ProActiveInterfaceImpl) generated_class.newInstance();
            reference.setFcItfName(interfaceName);
            reference.setFcItfOwner(owner);
            reference.setFcType(interfaceType);
            reference.setFcIsInternal(isInternal);

            return reference;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InterfaceGenerationFailedException(
                "Cannot generate meta object representative on interface [" +
                interfaceName + "] with signature [" +
                interfaceType.getFcItfSignature() + "] with javassist", e);
        }
    }

    private void createMethods(CtClass generatedCtClass,
        CtMethod[] reifiedMethods, InterfaceType interfaceType)
        throws CannotCompileException, NotFoundException {
        for (int i = 0; i < reifiedMethods.length; i++) {
            CtClass[] paramTypes = reifiedMethods[i].getParameterTypes();

            String body = "return ";
            body += ("((" + interfaceType.getFcItfSignature() + ")");
            body += (IMPL_FIELD_NAME + ")." + reifiedMethods[i].getName() +
            "(");
            for (int j = 0; j < paramTypes.length; j++) {
                body += ("(" + paramTypes[j].getName() + ")" + ("$" + (j + 1)));
                if (j < (paramTypes.length - 1)) {
                    body += ",";
                }
            }
            body += ")";

            body += ";";

            //            System.out.println("method : " + reifiedMethods[i].getName() +
            //                " : \n" + body);
            CtMethod methodToGenerate = CtNewMethod.make(reifiedMethods[i].getReturnType(),
                    reifiedMethods[i].getName(),
                    reifiedMethods[i].getParameterTypes(),
                    reifiedMethods[i].getExceptionTypes(), body,
                    generatedCtClass);
            generatedCtClass.addMethod(methodToGenerate);
        }
    }
}
