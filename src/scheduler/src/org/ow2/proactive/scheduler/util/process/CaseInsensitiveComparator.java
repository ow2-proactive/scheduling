package org.ow2.proactive.scheduler.util.process;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Case-insensitive string comparator.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CaseInsensitiveComparator implements Comparator<String>, Serializable {
    public static final Comparator<String> INSTANCE = new CaseInsensitiveComparator();

    private CaseInsensitiveComparator() {
    }

    public int compare(String lhs, String rhs) {
        return lhs.compareToIgnoreCase(rhs);
    }
}
