package org.ow2.proactive.scheduler.util.process;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Case-insensitive string comparator.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CaseInsensitiveComparator implements Comparator<String>, Serializable {
    /**  */
    public static final Comparator<String> INSTANCE = new CaseInsensitiveComparator();

    private CaseInsensitiveComparator() {
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * @param lhs first string.
     * @param rhs second string.
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second.  
     */
    public int compare(String lhs, String rhs) {
        return lhs.compareToIgnoreCase(rhs);
    }
}
