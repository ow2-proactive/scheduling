package org.ow2.proactive.scheduler.ext.matsci.common.data;

/**
 * MatSciTopology a duplicate of the Resource Manager topology definitions, used inside MatSci
 *
 * @author The ProActive Team
 */
public enum MatSciTopology {

    ARBITRARY("arbitrary"),

    BEST_PROXIMITY("bestProximity"),

    SINGLE_HOST("singleHost"),

    SINGLE_HOST_EXCLUSIVE("singleHostExclusive"),

    MULTIPLE_HOSTS_EXCLUSIVE("multipleHostsExclusive"),

    DIFFERENT_HOSTS_EXCLUSIVE("differentHostsExclusive"),

    THRESHHOLD_PROXIMITY("thresholdProximity");

    private String ref;

    private MatSciTopology(String str) {
        this.ref = str;
    }

    public String toString() {
        return ref;
    }

    public static MatSciTopology getTopology(String str) {
        for (MatSciTopology s : MatSciTopology.values()) {
            if (s.toString().equals(str)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Wrong topology : " + str);
    }
}
