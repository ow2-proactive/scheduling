package modelisation.markov;


public class MapleForwarder {

    private int n;

    public MapleForwarder(int n) {
        this.n = n;
    }


    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("usage: java modelisation.markov.MapleForwarder <n>");
            System.exit(-1);
        }
        MapleForwarder maple = new MapleForwarder(Integer.parseInt(args[0]));
    }


}
