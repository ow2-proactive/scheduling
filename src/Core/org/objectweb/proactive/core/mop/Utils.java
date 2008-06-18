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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.proactive.core.util.converter.MakeDeepCopy;


/**
 * This class contains static convenience and utility methods
 */
public abstract class Utils extends Object {

    /**
     * Static variables
     */
    public static final Class<?> JAVA_LANG_NUMBER = silentForName("java.lang.Number");
    public static final Class<?> JAVA_LANG_CHARACTER = silentForName("java.lang.Character");
    public static final Class<?> JAVA_LANG_BOOLEAN = silentForName("java.lang.Boolean");
    public static final Class<?> JAVA_LANG_VOID = silentForName("java.lang.Void");
    public static final Class<?> JAVA_LANG_RUNTIMEEXCEPTION = silentForName("java.lang.RuntimeException");
    public static final Class<?> JAVA_LANG_EXCEPTION = silentForName("java.lang.Exception");
    public static final Class<?> JAVA_LANG_THROWABLE = silentForName("java.lang.Throwable");

    /**
     * The char used to escaped "meta" information in generated classname.
     */
    public static final char STUB_ESCAPE_CHAR = '_';
    public static final String STUB_ESCAPE = "" + STUB_ESCAPE_CHAR + STUB_ESCAPE_CHAR;

    /**
     * Used to replace '.'
     */
    public static final char STUB_PACKAGE_SEPARATOR_CHAR = 'P';
    public static final String STUB_PACKAGE_SEPARATOR = "" + STUB_ESCAPE_CHAR + STUB_PACKAGE_SEPARATOR_CHAR;

    /**
     * Separate many Type classname in case of parameterizing Type.
     */
    public static final char STUB_GENERICS_SEPARATOR_CHAR = 'D';
    public static final String STUB_GENERICS_SEPARATOR = "" + STUB_ESCAPE_CHAR + STUB_GENERICS_SEPARATOR_CHAR;

    //prefix and suffix
    public static final String STUB_DEFAULT_PREFIX = STUB_ESCAPE_CHAR + "Stub";

    // stub on generic types are generated with a suffix that indicates the parameterizing types
    public static final String STUB_GENERICS_SUFFIX = STUB_ESCAPE_CHAR + "Generics";

    //  packages
    public static final String STUB_DEFAULT_PACKAGE = "pa.stub.";

    //  stub on generic types are generated in a different package
    public static final String STUB_GENERICS_PACKAGE = "parameterized.";

    /**
     * Static methods
     */

    /**
     * Removes the keyword 'native' from the String given as argument.
     *
     * We assume there is only one occurrence of 'native' in the string.
     *
     * @return the input String minus the first occurrence of 'native'.
     * @param  in The String the keyword 'native' is to be removed from.
     */
    static public String getRidOfNative(String in) {
        String result;
        int leftindex;
        int rightindex;

        leftindex = in.indexOf("native");
        if (leftindex == -1) {
            return in;
        }
        rightindex = leftindex + 6;

        result = in.substring(0, leftindex) + in.substring(rightindex, in.length());
        return result;
    }

    static public String getRidOfAbstract(String in) {
        String result;
        int leftindex;
        int rightindex;

        leftindex = in.indexOf("abstract");
        if (leftindex == -1) {
            return in;
        }
        rightindex = leftindex + 8;

        result = in.substring(0, leftindex) + in.substring(rightindex, in.length());
        return result;
    }

    static public String getRidOfNativeAndAbstract(String in) {
        String s = in;
        s = getRidOfAbstract(s);
        return getRidOfNative(s);
    }

    /**
     * Checks if the given method can be reified.
     *
     * Criteria for NOT being reifiable are :
     * <UL>
     * <LI> method is final
     * <LI> method is static
     * <LI> method is finalize ()
     * </UL>
     *
     * @return True if the method is reifiable
     * @param  met The method to be checked
     */
    static public boolean checkMethod(Method met) {
        int modifiers = met.getModifiers();

        // Final methods cannot be reified since we cannot redefine them
        // in a subclass
        if (Modifier.isFinal(modifiers)) {
            return false;
        }

        // Static methods cannot be reified since they are not 'virtual'
        if (Modifier.isStatic(modifiers)) {
            return false;
        }
        if (!(Modifier.isPublic(modifiers))) {
            return false;
        }

        // If method is finalize (), don't reify it
        if ((met.getName().equals("finalize")) && (met.getParameterTypes().length == 0)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a String representing the'source code style' declaration
     * of the Class object representing an array type given as argument.
     *
     * The problem is that the <code>toString()</code> method of class Class
     * does not
     * return what we are expecting, i-e the type definition that appears in
     * the source code (like <code>char[][]</code>).
     *
     * @param cl A class object representing an array type.
     * @return A String with the'source code representation' of that array type
     */
    static public String sourceLikeForm(Class<?> cl) {
        if (!(cl.isArray())) {
            //to fix an issue with jdk1.3 and inner class
            // A$B should be A.B in source code
            //System.out.println("Remplacing in " + cl.getName());
            return cl.getName().replace('$', '.');
        } else {
            int nb = 0;
            Class<?> current = cl;
            String result = "";

            do {
                current = current.getComponentType();
                result = "[]" + result;
                nb++;
            } while ((current.getComponentType()) != null);

            result = current.getName() + result;
            return result;
        }
    }

    /*
     * Returns the name of the wrapper class for class <code>cl</code>.
     * If <code>cl</code> is not a primitive type, returns <code>null</code>
     */
    static public String nameOfWrapper(Class<?> cl) {
        String str = cl.getName();

        if (cl.isPrimitive()) {
            if (str.equals("int")) {
                return "java.lang.Integer";
            } else if (str.equals("boolean")) {
                return "java.lang.Boolean";
            } else if (str.equals("byte")) {
                return "java.lang.Byte";
            } else if (str.equals("short")) {
                return "java.lang.Short";
            } else if (str.equals("long")) {
                return "java.lang.Long";
            } else if (str.equals("float")) {
                return "java.lang.Float";
            } else if (str.equals("double")) {
                return "java.lang.Double";
            } else if (str.equals("void")) {
                return "void";
            } else if (str.equals("char")) {
                return "java.lang.Character";
            } else {
                throw new InternalException("Unknown primitive type: " + cl.getName());
            }
        } else {
            return null;
        }
    }

    /*
     * Extract the package name from the fully qualified class name given as
     * an argument
     */
    public static String getPackageName(String fqnameofclass) {
        int indexoflastdot;

        indexoflastdot = fqnameofclass.lastIndexOf('.');

        if (indexoflastdot == -1) {
            return "";
        } else {
            return fqnameofclass.substring(0, indexoflastdot);
        }
    }

    /**
     * Extracts the simple name of the class from its fully qualified name
     */
    public static String getSimpleName(String fullyQualifiedNameOfClass) {
        int indexOfLastDot = fullyQualifiedNameOfClass.lastIndexOf('.');
        if (indexOfLastDot == -1) // There are no dots
        {
            return fullyQualifiedNameOfClass;
        } else {
            // If last character is a dot, returns an empty string
            if (indexOfLastDot == (fullyQualifiedNameOfClass.length() - 1)) {
                return "";
            } else {
                return fullyQualifiedNameOfClass.substring(indexOfLastDot + 1);
            }
        }
    }

    /**
     * Returns the Class<?> object that is a wrapper for the given <code>cl</code>
     * class.
     */
    public static Class<?> getWrapperClass(Class<?> cl) {
        if (!(cl.isPrimitive())) {
            return null;
        }
        String s = Utils.nameOfWrapper(cl);
        try {
            return MOP.forName(s);
        } catch (ClassNotFoundException e) {
            throw new InternalException("Cannot load wrapper class " + s);
        }
    }

    /**
     * Performs the opposite operation as getWrapperClass
     */
    public static Class<?> getPrimitiveType(Class<?> cl) {
        Field cst;
        if (Utils.isWrapperClass(cl)) {
            // These types are not classes , yet class static variables
            // We want to locale the TYPE field in the class
            try {
                cst = cl.getField("TYPE");
                return (Class<?>) cst.get(null);
            } catch (NoSuchFieldException e) {
                throw new InternalException("Cannot locate constant TYPE in class " + cl.getName());
            } catch (SecurityException e) {
                throw new InternalException("Access to field TYPE in class " + cl.getName() + " denied");
            } catch (IllegalAccessException e) {
                throw new InternalException("Access to field TYPE in class " + cl.getName() + " denied");
            }
        } else {
            throw new InternalException("Not a wrapper class: " + cl.getName());
        }
    }

    /**
     * Tests if the class given as an argument is a wrapper class
     * How can we be sure that all subclasses of java.lang.Number are wrappers ??
     */
    public static boolean isWrapperClass(Class<?> cl) {
        if (Utils.JAVA_LANG_NUMBER.isAssignableFrom(cl)) {
            return true;
        } else if (Utils.JAVA_LANG_BOOLEAN.isAssignableFrom(cl)) {
            return true;
        } else if (Utils.JAVA_LANG_CHARACTER.isAssignableFrom(cl)) {
            return true;
        } else if (Utils.JAVA_LANG_VOID.isAssignableFrom(cl)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getRelativePath(String className) {
        String packageName;
        String result;
        int indexOfDot;
        int indexOfLastDot;

        packageName = Utils.getPackageName(className);

        indexOfDot = packageName.indexOf('.', 0);
        result = "";
        indexOfLastDot = 0;

        while (indexOfDot != -1) {
            result = result + File.separator + packageName.substring(indexOfLastDot, indexOfDot);
            indexOfLastDot = indexOfDot + 1;
            indexOfDot = packageName.indexOf('.', indexOfDot + 1);
            if (indexOfDot == -1) {
                result = result + File.separator +
                    packageName.substring(indexOfLastDot, packageName.length());
            }
        }

        if (result.equals("")) {
            result = File.separator + packageName;
        }

        return result;
    }

    /*
       public static String getStubName(String nameOfClass) {
         return Utils.getPackageName(nameOfClass) + "." + STUB_DEFAULT_PREFIX + Utils.getSimpleName(nameOfClass);
       }
     */
    public static boolean isNormalException(Class<?> exc) {
        boolean result;

        if (Utils.JAVA_LANG_THROWABLE.isAssignableFrom(exc)) {
            // It is a subclass of Throwable
            if (Utils.JAVA_LANG_EXCEPTION.isAssignableFrom(exc)) {
                if (Utils.JAVA_LANG_RUNTIMEEXCEPTION.isAssignableFrom(exc)) {
                    result = false;
                } else {
                    result = true;
                }
            } else {
                result = false; // This must be an Error
            }
        } else {
            result = false;
        }

        return result;
    }

    public static Class<?> decipherPrimitiveType(String str) {
        if (str.equals("int")) {
            return java.lang.Integer.TYPE;
        } else if (str.equals("boolean")) {
            return java.lang.Boolean.TYPE;
        } else if (str.equals("byte")) {
            return java.lang.Byte.TYPE;
        } else if (str.equals("short")) {
            return java.lang.Short.TYPE;
        } else if (str.equals("long")) {
            return java.lang.Long.TYPE;
        } else if (str.equals("float")) {
            return java.lang.Float.TYPE;
        } else if (str.equals("double")) {
            return java.lang.Double.TYPE;
        } else if (str.equals("void")) {
            return java.lang.Void.TYPE;
        } else if (str.equals("char")) {
            return java.lang.Character.TYPE;
        }

        return null;
    }

    public static boolean isSuperTypeInArray(String className, Class<?>[] types) {
        try {
            Class<?> c = MOP.forName(className);
            return isSuperTypeInArray(c, types);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }
    }

    public static boolean isSuperTypeInArray(Class<?> c, Class<?>[] types) {
        for (int i = 0; i < types.length; i++) {
            if (types[i].isAssignableFrom(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method optimizes the copy of primitive arrays.
     * This deep copy method does not replace the standard one
     * and uses it. The deep copy is replaced only for arrays of primitive
     * data type of 1 dimension by using the System.arraycopy method.
     * @param source The source array to copy
     * @return The deep copy of the source array
     */
    public static final Object[] makeDeepCopy(final Object[] source) throws java.io.IOException {
        if (source == null) {
            return null;
        }
        Class<?> cl;
        Object obj;

        // Check if there is only primitive type or array of primitive types
        for (int i = source.length; --i >= 0;) {
            if ((obj = source[i]) == null) {
                continue;
            }

            // If the class of the current obj is not a string or a primitive wrapper or an
            // array of primitive type use the classic deep copy method
            if (((cl = obj.getClass()) != String.class) && !Utils.isWrapperClass(cl) &&
                (!cl.isArray() || !cl.getComponentType().isPrimitive())) {
                return (Object[]) Utils.makeDeepCopy((Object) source);
            }
        }

        // At this point we can be sure that elements in the source array
        // are not complex type objects so we can optimize the copy of the source array
        int i;
        int j;
        int len = 0;

        // The result array will be the copy of the source array
        final Object[] ret = new Object[source.length];
        for (i = source.length; --i >= 0;) {
            // If the current source element was already copied by
            // the internal loop continue to the next element  
            if (ret[i] != null) {
                continue;
            }

            // If the current element is an array of primitive type use the arraycopy method
            if (((obj = source[i]) != null) && (cl = obj.getClass()).isArray()) {
                len = Array.getLength(obj);
                ret[i] = Array.newInstance(cl.getComponentType(), len);
                System.arraycopy(obj, 0, ret[i], 0, len);
                // Here we need to seek through all other source args to find same references
                // to preserve the sematics of the source array
                for (j = i; --j >= 0;) {
                    if ((ret[j] == null) && (source[i] == source[j])) {
                        ret[j] = ret[i];
                    }
                }
            } else {
                // If the source element is a primitive wrapper or a string (immutable type)
                // just pass the reference 
                ret[i] = obj;
            }
        }
        return ret;
    }

    /**
     * Make a deep copy of source object using a ProActiveObjectStream.
     * @param source The object to copy.
     * @return the copy.
     * @throws java.io.IOException
     */
    public static Object makeDeepCopy(Object source) throws java.io.IOException {
        if (source == null) {
            return null;
        }
        try {
            return MakeDeepCopy.WithProActiveObjectStream.makeDeepCopy(source);
        } catch (ClassNotFoundException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }

    public static String convertClassNameToStubClassName(String classname, Class<?>[] genericParameters) {
        if (classname.length() == 0) {
            return classname;
        }
        String packageName = getPackageName(classname);
        if (!packageName.equals("")) {
            packageName += ".";
        }
        packageName = STUB_DEFAULT_PACKAGE +
            (((genericParameters == null) || (genericParameters.length == 0)) ? "" : STUB_GENERICS_PACKAGE) +
            packageName;

        String genericsDifferentiator = "";
        if (genericParameters != null) {
            for (Class<?> gClassName : genericParameters) {
                if (!genericsDifferentiator.equals("")) {
                    genericsDifferentiator += STUB_GENERICS_SEPARATOR;
                }
                genericsDifferentiator += escapeClassName(gClassName.getName());
            }
            if (!genericsDifferentiator.equals("")) {
                genericsDifferentiator = STUB_GENERICS_SUFFIX + genericsDifferentiator;
            }
        }

        return packageName + STUB_DEFAULT_PREFIX + escapeClassName(getSimpleName(classname)) +
            genericsDifferentiator;
    }

    public static boolean isStubClassName(String classname) {
        if (classname.startsWith(STUB_DEFAULT_PACKAGE)) {
            // Extracts the simple name from the fully-qualified class name
            int index = classname.lastIndexOf(".");
            if (index != -1) {
                return classname.startsWith(Utils.STUB_DEFAULT_PREFIX, index + 1);
            } else {
                return classname.startsWith(Utils.STUB_DEFAULT_PREFIX);
            }
        } else {
            return false;
        }
    }

    public static String convertStubClassNameToClassName(String stubclassname) {
        return unEscapeStubClassesName(stubclassname, false).get(0).toString();
    }

    public static String[] getNamesOfParameterizingTypesFromStubClassName(String stubClassName) {
        if (!isStubClassName(stubClassName)) {
            return new String[] {};
        }
        if (!stubClassName.startsWith(Utils.STUB_DEFAULT_PACKAGE + Utils.STUB_GENERICS_PACKAGE)) {
            // no generics
            return new String[] {};
        }
        List<CharSequence> classesName = unEscapeStubClassesName(stubClassName, true);
        classesName.remove(0);
        String[] result = new String[classesName.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = classesName.get(i).toString();
        }
        return result;
    }

    /**
     * Escape some char ('.', Utils.STUB_ESCAPE_CHAR) to do a valid String usable as part of a stub classname.
     *
     * @param className
     * @return
     */
    private static String escapeClassName(String className) {
        StringBuilder sb = new StringBuilder(className.length() * 2);
        for (int i = 0; i < className.length(); i++) {
            switch (className.charAt(i)) {
                case '.':
                    sb.append(STUB_PACKAGE_SEPARATOR);
                    break;
                case STUB_ESCAPE_CHAR:
                    sb.append(STUB_ESCAPE);
                    break;
                default:
                    sb.append(className.charAt(i));
                    break;
            }
        }
        return sb.toString();
    }

    /** Gives all the real classname contains in a Stub classname.
     * The First element of the result is the classname and next the parameterizings types.
     * @param stubClassName
     * @return the list of elements containing in the given stubClassName
     * @throws IllegalArgumentException if the given escapedClassesName isn't well escaped
     */
    private static List<CharSequence> unEscapeStubClassesName(String stubClassName,
            boolean withParameterizingTypes) throws IllegalArgumentException {
        ArrayList<CharSequence> result = new ArrayList<CharSequence>();
        StringBuilder sb = new StringBuilder(stubClassName.length());
        boolean stubFlag = false;
        boolean genericFlag = false;
        boolean genericPackage = false;

        if (isStubClassName(stubClassName)) {
            String temp = "";
            if (stubClassName.startsWith(Utils.STUB_DEFAULT_PACKAGE + Utils.STUB_GENERICS_PACKAGE)) {
                genericPackage = true;
                // remove generics stuff
                temp = stubClassName.substring((Utils.STUB_DEFAULT_PACKAGE + Utils.STUB_GENERICS_PACKAGE)
                        .length());
            } else {
                temp = stubClassName.substring(Utils.STUB_DEFAULT_PACKAGE.length());
            }
            temp = Utils.getPackageName(temp);
            if (temp.length() != 0) {
                sb.append(temp);
                sb.append('.');
            }

            //a Stub classe is necessary under a package i.e. Utils.STUB_DEFAULT_PACKAGE
            int begin = stubClassName.lastIndexOf('.');
            if (begin == -1) {
                begin = 0;
            }
            for (int i = begin + 1; i < stubClassName.length(); i++) {
                char c = stubClassName.charAt(i);
                if (c != STUB_ESCAPE_CHAR) {
                    sb.append(c);
                } else {
                    i++;
                    switch (stubClassName.charAt(i)) {
                        // one char Flags : 'STUB_ESCAPE_CHAR''a_char'
                        case STUB_PACKAGE_SEPARATOR_CHAR:
                            sb.append('.');
                            break;
                        case STUB_ESCAPE_CHAR:
                            sb.append(STUB_ESCAPE_CHAR);
                            ;
                            break;
                        case STUB_GENERICS_SEPARATOR_CHAR:
                            result.add(sb);
                            sb = new StringBuilder(stubClassName.length());
                            break;

                        // multiple char Flags <=> 'STUB_ESCAPE_CHAR'"a_string"
                        case 'S':
                            if (stubClassName.startsWith(STUB_DEFAULT_PREFIX, i - 1)) { //Stub
                                if (stubFlag) {
                                    throw new IllegalArgumentException(
                                        "The escapedClassesName is not a well formed escaped string at index " +
                                            i +
                                            ", the flag STUB_DEFAULT_PREFIX (" +
                                            STUB_DEFAULT_PREFIX +
                                            ") are present twice : " + stubClassName);
                                }
                                stubFlag = true;
                                i += (STUB_DEFAULT_PREFIX.length() - 2); // 2 char _S
                            }
                            break;
                        case 'G':
                            if (stubClassName.startsWith(STUB_GENERICS_SUFFIX, i - 1)) { //Generics
                                if (genericFlag || !genericPackage) {
                                    throw new IllegalArgumentException(
                                        "The escapedClassesName is not a well formed escaped string at index " +
                                            i +
                                            ", the flag STUB_GENERICS_SUFFIX (" +
                                            STUB_GENERICS_SUFFIX +
                                            ") are present twice  or this class is not in the STUB_GENERICS_PACKAGE (" +
                                            STUB_DEFAULT_PACKAGE +
                                            STUB_GENERICS_PACKAGE +
                                            "): " +
                                            stubClassName);
                                }
                                genericFlag = true;
                                i += (STUB_GENERICS_SUFFIX.length() - 2); // 2 char _G

                                result.add(sb);
                                if (!withParameterizingTypes) {
                                    return result;
                                } else {
                                    sb = new StringBuilder(stubClassName.length());
                                }
                            }
                            break;
                        default:
                            //ERROR
                            throw new IllegalArgumentException(
                                "The escapedClassesName is not a well formed escaped string at index " + i +
                                    " : " + stubClassName);
                    }
                }
            }
            result.add(sb);
            return result;
        } else {
            result.add(stubClassName);
            return result;
        }
    }

    /**
     * Looks for all super interfaces of the given interface, and adds them in the given List
     * @param cl the base interface
     * @param superItfs a vector that will list all Class<?> instances corresponding to the super interfaces of cl
     */
    public static void addSuperInterfaces(Class<?> cl, List<Class<?>> superItfs) {
        if (!cl.isInterface()) {
            return;
        }
        Class<?>[] super_interfaces = cl.getInterfaces();
        for (int i = 0; i < super_interfaces.length; i++) {
            superItfs.add(super_interfaces[i]);
            addSuperInterfaces(super_interfaces[i], superItfs);
        }
    }

    /**
     * Gets all super-interfaces from the interfaces of this list, and
     * adds them to this list.
     * @param interfaces a list of interfaces
     */
    public static void addSuperInterfaces(List<Class<?>> interfaces) {
        for (int i = 0; i < interfaces.size(); i++) {
            Class<?>[] super_itfs_table = interfaces.get(i).getInterfaces();
            List<Class<?>> super_itfs = new ArrayList<Class<?>>(super_itfs_table.length); // resizable list
            for (int j = 0; j < super_itfs_table.length; j++) {
                super_itfs.add(super_itfs_table[j]);
            }
            addSuperInterfaces(super_itfs);
            for (int j = 0; j < super_itfs.size(); j++) {
                if (!interfaces.contains(super_itfs.get(j))) {
                    interfaces.add(super_itfs.get(j));
                }
            }
        }
    }

    private static final Class<?> silentForName(String classname) {
        try {
            return MOP.forName(classname);
        } catch (ClassNotFoundException e) {
            System.err
                    .println("Static initializer in class org.objectweb.proactive.core.mop.Utils: Cannot load class " +
                        classname);
            return null;
        }
    }

    /**
     * Searches a method with the given parameters in the given reifiedClass
     * Note that a call to checkMethodExistence(reifiedClass, methodName, null) is different to a call to checkMethodExistence(reifiedClass, methodName, new Class<?>[0])
     * The former means that no checking is done on the parameters, whereas the latter means that we look for a method with no parameters.
     * @param reifiedClass the class where to search the method
     * @param methodName the name of the method
     * @param parametersTypes the parametersTypes list
     * @return true if the method was found, false otherwise
     */
    public static boolean checkMethodExistence(Class<?> reifiedClass, String methodName,
            Class<?>[] parametersTypes) {
        Method[] methods = reifiedClass.getDeclaredMethods();
        for (Method m : methods) {
            int modifiers = m.getModifiers();
            if (!Modifier.isPrivate(modifiers)) {
                // is it the right method name
                if (m.getName().equals(methodName)) {
                    // do we check the whole signature
                    if (parametersTypes != null) {
                        // does the method has the right signature
                        if (Arrays.equals(m.getParameterTypes(), parametersTypes)) {
                            // the method exists with the right parameters
                            return true;
                        }
                    } else {
                        // the method exists, we don't bother about the parameters
                        return true;
                    }
                }
            }
        }
        if (reifiedClass.getSuperclass() != null) {
            return checkMethodExistence(reifiedClass.getSuperclass(), methodName, parametersTypes);
        }
        return false;
    }
}
