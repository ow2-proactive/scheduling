package org.objectweb.proactive.core.component.asmgen;


import org.apache.log4j.Logger;


/**
 * Utility class for bytecode generation operations.
 * 
 * @author Matthieu Morel
 *
 */
public class Utils {
    protected static Logger logger = Logger.getLogger(Utils.class.getName());
    public static final String GENERATED_DEFAULT_PREFIX = "Generated_";
    public static final String REPRESENTATIVE_DEFAULT_POSTFIX = "_representative";
    public static final String COMPOSITE_REPRESENTATIVE_POSTFIX = "_composite";
    public static final String STUB_DEFAULT_PACKAGE = null;

    public static String convertClassNameToRepresentativeClassName(String classname) {
        if (classname.length() == 0) {
            return classname;
        }

        int n = classname.lastIndexOf('.');
        if (n == -1) {
            // no package
            return STUB_DEFAULT_PACKAGE + GENERATED_DEFAULT_PREFIX + classname;
        } else {
            return STUB_DEFAULT_PACKAGE + classname.substring(0, n + 1) + GENERATED_DEFAULT_PREFIX + 
                   classname.substring(n + 1);
        }
    }

    public static String getMetaObjectClassName(String functionalInterfaceName, String javaInterfaceName) {
        // just a way to have an identifier (possibly not unique ... but readable)
        return (GENERATED_DEFAULT_PREFIX + javaInterfaceName.replace('.', '_') + "_" + 
               functionalInterfaceName.replace('.', '/').replace('-', '_'));
    }

    public static String getMetaObjectComponentRepresentativeClassName(String functionalInterfaceName, 
                                                                                String javaInterfaceName) {
        // just a way to have an identifier (possibly not unique ... but readable)
        return (getMetaObjectClassName(functionalInterfaceName, javaInterfaceName) + REPRESENTATIVE_DEFAULT_POSTFIX);
    }


}