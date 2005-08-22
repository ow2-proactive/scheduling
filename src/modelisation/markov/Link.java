package modelisation.markov;

public class Link {
    private State from;
    private State to;
    private String value;

    public Link(State from, State to, String value) {
        this.from = from;
        this.to = to;
        this.value = value;
    }

    public static void doLink(State from, State to, String value) {
        Link tmp = new Link(from, to, value);
        from.addOutLink(tmp);
        to.addInLink(tmp);
    }

    public State getTo() {
        return to;
    }

    public State getFrom() {
        return from;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return from.getpName() + " ---> " + to.getpName() + " value " + value;
    }
}
