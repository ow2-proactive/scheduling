package org.objectweb.proactive.p2p.v2.monitoring;

public class Link {
    protected String source;
    protected String destination;

    public Link() {
    }

    public Link(String s, String d) {
        this.source = s;
        this.destination = d;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }

    public boolean equals(Object o) {
        System.out.println("Link.equals()");
        Link l = (Link) o;
        return this.getSource().equals(l.getSource()) &&
        this.getDestination().equals(l.getDestination());
    }
}
