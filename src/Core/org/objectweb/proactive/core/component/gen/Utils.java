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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.util.ClassDataCache;


/**
 * Utility class for bytecode generation operations.
 *
 * @author The ProActive Team
 *
 */
public class Utils {

    /**
     * The char used to escaped "meta" information in generated classname.
     */
    public static final char GEN_ESCAPE_CHAR = 'C';
    public static final String GEN_ESCAPE = "" + GEN_ESCAPE_CHAR + GEN_ESCAPE_CHAR;

    /**
     * Used to replace '.'
     */
    public static final char GEN_PACKAGE_SEPARATOR_CHAR = 'P';
    public static final String GEN_PACKAGE_SEPARATOR = "" + GEN_ESCAPE_CHAR + GEN_PACKAGE_SEPARATOR_CHAR;

    /**
     * Separate many interface name.
     */
    public static final char GEN_ITF_NAME_SEPARATOR_CHAR = 'I';
    public static final String GEN_ITF_NAME_SEPARATOR = "" + GEN_ESCAPE_CHAR + GEN_ITF_NAME_SEPARATOR_CHAR;

    /**
     * Separate the signature part (a classname, ...) and the name part of an interface.
     */
    public static final char GEN_MIDDLE_SEPARATOR_CHAR = 'O';
    public static final String GEN_MIDDLE_SEPARATOR = "" + GEN_ESCAPE_CHAR + GEN_MIDDLE_SEPARATOR_CHAR;

    // prefix and suffix
    public static final String GENERATED_DEFAULT_PREFIX = GEN_ESCAPE_CHAR + "generated";
    public static final String REPRESENTATIVE_DEFAULT_SUFFIX = GEN_ESCAPE_CHAR + "representative";
    public static final String GATHERCAST_ITF_PROXY_DEFAULT_SUFFIX = GEN_ESCAPE_CHAR + "gathercastItfProxy";
    public static final String COMPOSITE_REPRESENTATIVE_SUFFIX = GEN_ESCAPE_CHAR + "composite";
    public static final String OUTPUT_INTERCEPTOR_SUFFIX = GEN_ESCAPE_CHAR + "outputInterceptor";

    // packages
    public static final String STUB_DEFAULT_PACKAGE = null;

    public static boolean isRepresentativeClassName(String classname) {
        return (classname.startsWith(GENERATED_DEFAULT_PREFIX) && classname
                .endsWith(REPRESENTATIVE_DEFAULT_SUFFIX));
    }

    public static boolean isMetaObjectClassName(String classname) {
        throw new ProActiveRuntimeException("not implemented yet");
    }

    public static boolean isGathercastProxyClassName(String classname) {
        return (classname.startsWith(GENERATED_DEFAULT_PREFIX) && classname
                .endsWith(GATHERCAST_ITF_PROXY_DEFAULT_SUFFIX));
    }

    /**
     * Parse a representative classname and rebuild the interface signature i.e. the Java interface name.
     * @param className
     * @return the interface signature
     */
    public static String getInterfaceSignatureFromRepresentativeClassName(String className) {
        if (!isRepresentativeClassName(className)) {
            return null;
        }
        String tmp = className.replaceAll("^" + GENERATED_DEFAULT_PREFIX, "");
        tmp = tmp.replaceAll(REPRESENTATIVE_DEFAULT_SUFFIX + "$", "");
        tmp = unEscapeClassesName(tmp, false).get(0).toString();

        return tmp;
    }

    public static String getInterfaceNameFromRepresentativeClassName(String className) {
        if (!isRepresentativeClassName(className)) {
            return null;
        }
        String tmp = className.replaceAll("^" + GENERATED_DEFAULT_PREFIX, "");
        tmp = tmp.replaceAll(REPRESENTATIVE_DEFAULT_SUFFIX + "$", "");
        tmp = unEscapeClassesName(tmp, true).get(1).toString();

        return tmp;
    }

    public static String getInterfaceSignatureFromGathercastProxyClassName(String className) {
        if (!isGathercastProxyClassName(className)) {
            return null;
        }
        String tmp = className.replaceAll("^" + GENERATED_DEFAULT_PREFIX, "");
        tmp = tmp.replaceAll(GATHERCAST_ITF_PROXY_DEFAULT_SUFFIX + "$", "");
        tmp = unEscapeClassesName(tmp, false).get(0).toString();

        return tmp;
    }

    public static String getMetaObjectClassName(String functionalInterfaceName, String javaInterfaceName) {
        // just a way to have an identifier (possibly not unique ? ... but readable)
        return (GENERATED_DEFAULT_PREFIX + escapeString(javaInterfaceName) + GEN_MIDDLE_SEPARATOR + escapeString(functionalInterfaceName));
    }

    public static String getMetaObjectComponentRepresentativeClassName(String functionalInterfaceName,
            String javaInterfaceName) {
        // just a way to have an identifier (possibly not unique ... but readable)
        return (getMetaObjectClassName(functionalInterfaceName, javaInterfaceName) + REPRESENTATIVE_DEFAULT_SUFFIX);
    }

    public static String getGatherProxyItfClassName(ProActiveInterfaceType gatherItfType) {
        return (getMetaObjectClassName(gatherItfType.getFcItfName(), gatherItfType.getFcItfSignature()) + GATHERCAST_ITF_PROXY_DEFAULT_SUFFIX);
    }

    public static String getOutputInterceptorClassName(String functionalInterfaceName,
            String javaInterfaceName) {
        // just a way to have an identifier (possibly not unique ... but readable)
        return (getMetaObjectClassName(functionalInterfaceName, javaInterfaceName) + OUTPUT_INTERCEPTOR_SUFFIX);
    }

    public static Class<?> defineClass(final String className, final byte[] bytes)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        // The following code invokes defineClass on the current thread classloader by reflection
        Class<?> clc = Class.forName("java.lang.ClassLoader");
        Class<?>[] argumentTypes = new Class<?>[4];
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

        return (Class<?>) method.invoke(Thread.currentThread().getContextClassLoader(), effectiveArguments);
    }

    public static void createItfStubObjectMethods(CtClass generatedClass) throws CannotCompileException,
            NotFoundException {
        // FIXME namespace pollution !
        CtField senderItfIDField = new CtField(ClassPool.getDefault().get(ItfID.class.getName()),
            "senderItfID", generatedClass);
        CtField.Initializer initializer = CtField.Initializer.byExpr("null");
        generatedClass.addField(senderItfIDField, initializer);
        CtMethod senderItfIDGetter = CtNewMethod.getter("getSenderItfID", senderItfIDField);
        generatedClass.addMethod(senderItfIDGetter);
        CtMethod senderItfIDSetter = CtNewMethod.setter("setSenderItfID", senderItfIDField);
        generatedClass.addMethod(senderItfIDSetter);
    }

    /**
     * Retrieves the bytecode associated to the generated class of the given name
     */
    public static byte[] getClassData(String classname) {
        byte[] bytecode = ClassDataCache.instance().getClassData(classname);
        if (bytecode != null) {
            return bytecode;
        }
        if (Utils.isRepresentativeClassName(classname)) {
            // try to generate a representative
            //            logger.info("Trying to generate representative class : " + classname);
            bytecode = RepresentativeInterfaceClassGenerator.generateInterfaceByteCode(classname, null);

            if (bytecode != null) {
                return bytecode;
            }
        }

        if (Utils.isGathercastProxyClassName(classname)) {
            // try to generate a representative
            //            logger.info("Trying to generate representative class : " + classname);
            bytecode = GatherInterfaceGenerator.generateInterfaceByteCode(classname);

            if (bytecode != null) {
                return bytecode;
            }
        }

        return null;
    }

    /**
     * Escape classname, interface name and definition to use it in generated classname and retrieve allinformation.
     * @param str a name
     * @return an escaped String
     */
    private static String escapeString(String str) {
        StringBuilder sb = new StringBuilder(str.length() * 2);
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case GEN_ESCAPE_CHAR:
                    sb.append(GEN_ESCAPE);
                    break;
                case '.':
                    sb.append(GEN_PACKAGE_SEPARATOR);
                    break;
                case '-':
                    sb.append(GEN_ITF_NAME_SEPARATOR);
                    break;
                default:
                    sb.append(str.charAt(i));
                    break;
            }
        }
        return sb.toString();
    }

    /** Gives all the real classname contains in a Stub classname.
     * The First element of the result is the classname of the
     * @param generatedClassName
     * @return
     * @throws IllegalArgumentException if the given escapedClassesName aren't well escaped
     */
    private static ArrayList<CharSequence> unEscapeClassesName(String generatedClassName, boolean withItfName)
            throws IllegalArgumentException {
        ArrayList<CharSequence> result = new ArrayList<CharSequence>();
        StringBuilder sb = new StringBuilder(generatedClassName.length());
        boolean middleFlag = false;

        // if (isStubClassName(generatedClassName)) {
        int begin;
        if ((begin = generatedClassName.lastIndexOf('.')) == -1) {
            begin = 0;
        }
        for (int i = begin; i < generatedClassName.length(); i++) {
            char c = generatedClassName.charAt(i);
            if (c != GEN_ESCAPE_CHAR) {
                sb.append(c);
            } else {
                i++;
                switch (generatedClassName.charAt(i)) {
                    // one char Flags : 'GEN_ESCAPE_CHAR''a_char' 
                    case GEN_ESCAPE_CHAR:
                        sb.append(GEN_ESCAPE_CHAR);
                        break;
                    case GEN_PACKAGE_SEPARATOR_CHAR:
                        sb.append('.');
                        break;
                    case GEN_ITF_NAME_SEPARATOR_CHAR:
                        if (!middleFlag) {
                            throw new IllegalArgumentException(
                                "The generatedClassName is not a well formed escaped string at index " + i +
                                    ", the flag GEN_ITF_NAME_SEPARATOR (" + GEN_ITF_NAME_SEPARATOR +
                                    ") is present whereasthis is not the interface name part : " +
                                    generatedClassName);
                        }
                        sb.append('-');
                        break;
                    case GEN_MIDDLE_SEPARATOR_CHAR:
                        result.add(sb);
                        middleFlag = true;
                        if (!withItfName) {
                            return result;
                        }
                        sb = new StringBuilder(generatedClassName.length());
                        break;
                    default:
                        //ERROR
                        throw new IllegalArgumentException(
                            "The generatedClassName is not a well formed escaped string at index " + i +
                                " : " + generatedClassName);
                }
            }
        }
        result.add(sb);
        return result;
        //            } else {
        //                result.add(generatedClassName);
        //                return result;
        //            }
    }
}
