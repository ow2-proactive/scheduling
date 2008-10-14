package org.ow2.proactive.resourcemanager.gui.stats;

public class StatsItem {

    private String aggregate;
    private String value;

    public StatsItem(String givenType, String givenValue) {
        aggregate = givenType;
        value = givenValue;
    }

    public String getAggregate() {
        return aggregate;
    }

    public String getValue() {
        return value;
    }
}
