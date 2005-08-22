package modelisation.markov;

import java.util.ArrayList;


public class State {
    private ArrayList inLinks;
    private ArrayList outLinks;
    private String pName;
    private String tName;
    protected int number;

    public State(String pName, String tName) {
        this.pName = pName;
        this.tName = tName;
        this.inLinks = new ArrayList();
        this.outLinks = new ArrayList();
    }

    public State(int number, int numberTime) {
        this("p" + number, "T" + numberTime);
        this.number = number;
    }

    public State(int number) {
        this("p" + number, null);
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }

    public void addInLink(Link l) {
        this.inLinks.add(l);
    }

    public void addOutLink(Link l) {
        this.outLinks.add(l);
    }

    public String getpName() {
        return this.pName;
    }

    public String gettName() {
        return this.tName;
    }

    public String getOutlinksValues() {
        Object[] array = outLinks.toArray();
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                tmp.append("+");
            }
            tmp.append(((Link) array[i]).getValue());
        }
        return tmp.toString();
    }

    public String getInlinksValuesWithP() {
        Object[] array = inLinks.toArray();
        StringBuffer tmp = new StringBuffer();
        Link l;
        for (int i = 0; i < array.length; i++) {
            l = (Link) array[i];
            if (i != 0) {
                tmp.append("-");
            }

            tmp.append(l.getValue()).append("*").append(l.getFrom().getpName());
        }
        return tmp.toString();
    }

    public String getOutlinksTime() {
        Object[] array = outLinks.toArray();
        StringBuffer tmp = new StringBuffer();
        Link l;
        int ok = 0;
        String sum = this.getSumOfOutlinksValues();

        //	System.out.println("Sum is " + sum);
        //	System.out.println("State is " + this);
        for (int i = 0; i < array.length; i++) {
            l = (Link) array[i];

            if (l.getTo().gettName() != null) {
                if (ok != 0) {
                    tmp.append("-");
                }
                ok++;
                tmp.append("(").append(l.getValue()).append(")/(").append(sum);
                tmp.append(")*").append(l.getTo().gettName());
            }

            //tmp.append(l.getValue()).append("*").append(l.getFrom().getpName());
        }
        return tmp.toString();
    }

    public String getSumOfOutlinksValues() {
        Object[] array = outLinks.toArray();
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                tmp.append("+");
            }
            tmp.append(((Link) array[i]).getValue());
        }
        return tmp.toString();
    }

    public String toString() {
        if ((outLinks != null) && (outLinks.size() != 0)) {
            return pName + " " + tName + " linked to " + outLinks.get(0);
        } else {
            return pName;
        }
    }
}
