package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.util.HashSet;

public class ClassUtility {

    private static final HashSet<Class<?>> wrapperClasses;

    static {
        wrapperClasses = new HashSet<Class<?>>();
        wrapperClasses.add(Boolean.class);
        wrapperClasses.add(Character.class);
        wrapperClasses.add(Byte.class);
        wrapperClasses.add(Short.class);
        wrapperClasses.add(Integer.class);
        wrapperClasses.add(Long.class);
        wrapperClasses.add(Float.class);
        wrapperClasses.add(Double.class);
        wrapperClasses.add(Void.class);
    }
    
    public static boolean isWrapperClass(Class<?> clazz) {
        return wrapperClasses.contains(clazz);
    }
    
    private ClassUtility() {
    }
}
