package org.objectweb.proactive.core.component.gen;

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


public class OutputInterceptorClassGenerator
    extends AbstractInterfaceClassGenerator {
    List outputInterceptors;
    private static OutputInterceptorClassGenerator instance;

    public static OutputInterceptorClassGenerator instance() {
        if (instance == null) {
            return new OutputInterceptorClassGenerator();
        } else {
            return instance;
        }
    }

    public ProActiveInterface generateInterface(
        ProActiveInterface representative, List outputInterceptors)
        throws InterfaceGenerationFailedException {
        this.outputInterceptors = outputInterceptors;
        ProActiveInterface generated = generateInterface(representative.getFcItfName(),
                representative.getFcItfOwner(),
                (InterfaceType) representative.getFcItfType(), false, true);
        ((StubObject) generated).setProxy(((StubObject) representative).getProxy());
        return generated;
    }

    public ProActiveInterface generateInterface(final String interfaceName,
        Component owner, InterfaceType interfaceType, boolean isInternal,
        boolean isFunctionalInterface)
        throws InterfaceGenerationFailedException {
        try {
            String representativeClassName = org.objectweb.proactive.core.component.gen.Utils.getOutputInterceptorClassName(interfaceName,
                    interfaceType.getFcItfSignature());
            Class generated_class;

            // check whether class has already been generated
            try {
                generated_class = loadClass(representativeClassName);
            } catch (ClassNotFoundException cnfe) {
                CtMethod[] reifiedMethods;
                CtClass generatedCtClass = pool.makeClass(representativeClassName);

                //this.fcInterfaceName = fcInterfaceName;
                //isPrimitive = ((ProActiveComponentRepresentativeImpl) owner).getHierarchicalType()
                //                                                    .equals(ComponentParameters.PRIMITIVE);
                List interfacesToImplement = new ArrayList();

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

                // add outputInterceptorsField
                // TODO_M change the list for a table
                CtField outputInterceptorsField = new CtField(pool.get(
                            List.class.getName()), "outputInterceptors",
                        generatedCtClass);
                generatedCtClass.addField(outputInterceptorsField,
                    "new java.util.ArrayList();");
                CtMethod outputInterceptorsSetter = CtNewMethod.setter("setOutputInterceptors",
                        outputInterceptorsField);
                generatedCtClass.addMethod(outputInterceptorsSetter);
                generatedCtClass.addInterface(pool.get(
                        OutputInterceptorHelper.class.getName()));
                //                methodsListField.setModifiers(Modifier.STATIC);
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

                // convert the bytes into a Class
                generated_class = defineClass(representativeClassName, bytecode);
            }

            ProActiveInterfaceImpl reference = (ProActiveInterfaceImpl) generated_class.newInstance();
            reference.setFcItfName(interfaceName);
            reference.setFcItfOwner(owner);
            reference.setFcType(interfaceType);
            reference.setFcIsInternal(isInternal);

            ((OutputInterceptorHelper) reference).setOutputInterceptors(outputInterceptors);

            return reference;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InterfaceGenerationFailedException("Cannot generate representative with javassist",
                e);
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

            body += ("org.objectweb.proactive.core.mop.MethodCall methodCall = org.objectweb.proactive.core.mop.MethodCall.getComponentMethodCall(" +
            "(java.lang.reflect.Method)overridenMethods[" + i + "]" +
            ", parameters, interfaceName," + isFunctionalInterface + ");\n");

            // delegate to outputinterceptors
            body += "java.util.ListIterator it = outputInterceptors.listIterator();\n";
            body += "while (it.hasNext()) {\n";
            body += "  ((org.objectweb.proactive.core.component.interception.OutputInterceptor) it.next()).beforeOutputMethodInvocation(methodCall);\n";
            body += "}\n";

            CtClass returnType = reifiedMethods[i].getReturnType();
            if (returnType != CtClass.voidType) {
                body += "Object result = ";
            }
            body += ("myProxy.reify(methodCall);\n");

            //              delegate to outputinterceptors
            body += "it = outputInterceptors.listIterator();\n";
            // use output interceptors in reverse order after invocation
            // go to the end of the list first
            body += "while (it.hasNext()) {\n";
            body += "it.next();\n";
            body += "}\n";
            body += "while (it.hasPrevious()) {\n";
            body += "  ((org.objectweb.proactive.core.component.interception.OutputInterceptor) it.previous()).afterOutputMethodInvocation(methodCall);\n";
            body += "}\n";

            // return casted result
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
                body += "result ";
            }

            if (postWrap != null) {
                body += postWrap;
            }
            body += ";";
            body += "\n}";
            //                     System.out.println("method : " + reifiedMethods[i].getName() +
            //                         " : \n" + body);
            CtMethod methodToGenerate = CtNewMethod.make(reifiedMethods[i].getReturnType(),
                    reifiedMethods[i].getName(),
                    reifiedMethods[i].getParameterTypes(),
                    reifiedMethods[i].getExceptionTypes(), body, generatedClass);
            generatedClass.addMethod(methodToGenerate);
        }
    }
}
