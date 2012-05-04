package org.ow2.proactive.scheduler.ext.matsci.common.data;

/**
 * DSSource
 *
 * @author The ProActive Team
 */
public enum DSSource {
    INPUT("input"),

    OUTPUT("output"),

    GLOBAL("global");

    private String ref;

    private DSSource(String str) {
        this.ref = str;
    }

    public String toString() {
        return ref;
    }

    public static DSSource getSource(String str) {
        for (DSSource s : DSSource.values()) {
            if (s.toString().equals(str)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Wrong dataspace source : " + str);
    }
}
