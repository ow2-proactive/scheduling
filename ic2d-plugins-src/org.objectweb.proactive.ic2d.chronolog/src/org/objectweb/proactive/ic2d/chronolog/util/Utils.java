package org.objectweb.proactive.ic2d.chronolog.util;

public class Utils {
    /**
     * Checks if a string is contained in an array of string.
     * @param ar The array of string
     * @param o the element 
     * @return Returns <code>true</code> if str is contained in ar; <code>false</code> otherwise
     */
    public static final boolean contains(final Object[] ar, final Object o) {
        for (final Object oo : ar) {
            if (oo.equals(o))
                return true;
        }
        return false;
    }

}
