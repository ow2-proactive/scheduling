package modelisation.markov;

import java.util.ArrayList;

public class ForwarderChain {


    private final static String LAMBDA = "lambda";
    private final static String DELTA = "delta";
    private final static String GAMMA = "gamma1";
//    private final static String GAMMA2 = "gamma1/2";
    private final static String NU = "nu";

    private int n;

    private ArrayList stateList;


    public ForwarderChain(int n) {
        this.n = n;
        this.stateList = new ArrayList();
    }

    public void generateChain() {
        //first create the head
        State p0 = new State(0, 0);
        this.stateList.add(p0);
        State p1 = new State(1, 1);
        this.stateList.add(p1);
        State p2 = new State(2);
        this.stateList.add(p2);
        State p3 = new State(3, 3);
        this.stateList.add(p3);
        State p4 = new State(4, 4);
        this.stateList.add(p4);
        State p5 = new State(5);
        this.stateList.add(p5);
        State p6 = new State(6, 6);
        this.stateList.add(p6);

        Link.doLink(p0, p2, GAMMA);
        Link.doLink(p1, p4, DELTA);
        Link.doLink(p2, p3, LAMBDA);
        Link.doLink(p2, p5, NU);
        Link.doLink(p3, p2, GAMMA);
        Link.doLink(p3, p6, NU);
        Link.doLink(p4, p6, NU);
        Link.doLink(p4, p0, GAMMA);
//        Link.doLink(p5, p7, DELTA);
        Link.doLink(p5, p6, LAMBDA);
//        Link.doLink(p6, p8, DELTA);
        Link.doLink(p6, p1, GAMMA);
        for (int i = 2; i <= n; i++) {
//            System.out.println(" XXXXXXX i = " + i);
//            if ((i % 2) != 0)
            this.addNuColumn(i);
//            else
            this.addDeltaColumn(i);
        }
        //	this.generateStateEquations();
        //	System.out.println(this.generateStateEquations());
        //	System.out.println( this.generateTimeEquations());
    }


    public String generateStateEquations() {
        Object[] stateArray = stateList.toArray();
        StringBuffer tmp = new StringBuffer();
        State s;
        for (int i = 0; i < stateArray.length; i++) {
            //	tmp = new StringBuffer();
            s = (State) stateArray[i];
            tmp.append("e" + s.getNumber() + ":=");
            tmp.append("(").append(s.getOutlinksValues()).append(")*").append(s.getpName());
            tmp.append("-").append(s.getInlinksValuesWithP()).append(":\n");
        }
        return tmp.toString();
    }

    public String generateTimeEquations() {
        Object[] stateArray = stateList.toArray();
        StringBuffer tmp = new StringBuffer();
        State s;
        //	System.out.println("Generate Time equations");

        //we only consider probs with an even number
        for (int i = 0; i < stateArray.length; i = i + 1) {
            s = (State) stateArray[i];
            int number = s.getNumber();
            if (s.gettName() != null) {
                tmp.append("eqt" + number + ":=").append(s.gettName());
//                System.out.println(" outlinks " + s.getOutlinksTime());
                if (!s.getOutlinksTime().equals("")) {
                    tmp.append("-").append(s.getOutlinksTime());
                }
                tmp.append(":\n");
                tmp.append("vect" + number + ":=").append("1/(").append(s.getSumOfOutlinksValues());
                tmp.append("):\n");
            }
        }
        return tmp.toString();
    }


    /**
     * Add a column to the markov chain
     * This column is linked to the previous by
     * a link with value delta
     * The states created will be
     * P(2i-1) and P(2i)
     */
    public void addNuColumn(int i) {
        //	System.out.println("addNormalColumn");

        State tmp1 = new State((4 * i - 1));
        this.stateList.add(tmp1);
        State tmp2 = new State((4 * i), (4 * i));
        this.stateList.add(tmp2);
        Link.doLink(tmp1, tmp2, LAMBDA);
        State previous1 = this.getStateByName("p" + (4 * i - 3));
        //	System.out.println(previous1);
        State previous2 = this.getStateByName("p" + (4 * i - 2));
        //	System.out.println(previous2);
        State previous3 = this.getStateByName("p" + (4 * i - 4));
        //	System.out.println(previous3);
        Link.doLink(previous1, tmp1, DELTA);
        Link.doLink(previous2, tmp2, DELTA);
        Link.doLink(tmp2, previous3, GAMMA);
    }

    /**
     * Add a column to the markov chain
     * This column is linked to the previous by
     * a link with value  nu
     * The states created will be
     * P(2i-1) and P(2i)
     * Time will be T'(i/2)
     */
    public void addDeltaColumn(int i) {
        //	System.out.println("addPrimeColumn");

        State tmp1 = new State((4 * i + 1));
        this.stateList.add(tmp1);
        State tmp2 = new State((4 * i + 2), (4 * i + 2));
        this.stateList.add(tmp2);
        Link.doLink(tmp1, tmp2, LAMBDA);
        State previous1 = this.getStateByName("p" + (4 * i - 1));
        //	System.out.println(previous1);
        State previous2 = this.getStateByName("p" + (4 * i));
        //	System.out.println(previous2);
        State previous3 = this.getStateByName("p" + (4 * i - 2));
        //	System.out.println(previous3);
        Link.doLink(previous1, tmp1, NU);
        Link.doLink(previous2, tmp2, NU);
        Link.doLink(tmp2, previous3, GAMMA);


    }

    public State getStateByName(String s) {
        Object[] stateArray = stateList.toArray();
        for (int i = 0; i < stateArray.length; i++) {
            if (s.equals(((State) stateArray[i]).getpName()))
                return (State) stateArray[i];
        }
        return null;
    }

    public String toString() {
        Object[] stateArray = stateList.toArray();
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < stateArray.length; i++) {
            s.append("State " + stateArray[i] + "\n");

        }
        return s.toString();
    }


    public String generateMapleFile() {
        StringBuffer tmp = new StringBuffer();
        State s;
        Object[] stateArray = stateList.toArray();

        tmp.append("with(linalg):\n");
//        tmp.append(generateTimeEquations() + "\n");
//        tmp.append("A := genmatrix([");
//        for (int i = 0; i < (n + 1); i++) {
//            if (i != 0)
//                tmp.append(",");
//            tmp.append("eqt" + i);
//        }
//        tmp.append("],[");
//        for (int i = 0; i < stateArray.length; i = i + 2) {
//            s = (State) stateArray[i];
//            if (i != 0)
//                tmp.append(",");
//            tmp.append(s.gettName());
//        }
//
//        tmp.append("]):\n");
//        tmp.append("invA:=inverse(A):\nB:=linalg[matrix]([");
//        for (int i = 0; i < (n + 1); i++) {
//            if (i != 0)
//                tmp.append(",");
//            tmp.append("[vect" + i + "]");
//        }
//        tmp.append("]):\nS:=evalm(invA &* B):\n");
        tmp.append(generateStateEquations());
        tmp.append("e").append((4 * n + 3)).append(":=");
        for (int i = 0; i < (4 * n + 3); i++) {
            if (i != 0)
                tmp.append("+");
            tmp.append("p").append(i);
        }
        tmp.append(":\nC:=linalg[matrix]([");
        for (int i = 0; i < (4 * n + 2); i++) {
            if (i != 0)
                tmp.append(",");
//            if (i != (2 * n))
            tmp.append("[0]");
//            else
        }
        tmp.append(",[1]");

        tmp.append("]):\nP:=genmatrix([");
        for (int i = 0; i < (4 * n + 2); i++) {
            if (i != 0)
                tmp.append(",");
            //we remove the last equation
//            if (i != 2)
            tmp.append("e").append(i);
        }
        tmp.append(",e" + (4 * n + 3));
        tmp.append("],[");
        for (int i = 0; i < (4 * n + 3); i++) {
            if (i != 0)
                tmp.append(",");
            tmp.append("p" + i);
        }

        tmp.append("]):\ninvP:=inverse(P):\nR:=evalm(invP &* C);\n");


        tmp.append("\nP2:=genmatrix([");
        for (int i = 0; i < (4 * n + 3); i++) {
            if (i != 0)
                tmp.append(",");
            //we remove the last equation
//            if (i != 2)
            tmp.append("e").append(i);
        }
        tmp.append("],[");
        for (int i = 0; i < (4 * n + 3); i++) {
            if (i != 0)
                tmp.append(",");
            tmp.append("p" + i);
        }

        tmp.append("]):Q:=transpose(-P2):MQ:=minor(Q,3,3):\n");
        tmp.append("vectT:=array(1..");
        tmp.append(stateList.size() - 1);
        tmp.append("):");
        tmp.append("for i from 1 to ");
        tmp.append(stateList.size() - 1);
        tmp.append(" do vectT[i]:=-1: od:\nresultT:=evalm(inverse(MQ) &* vectT);\n");

        tmp.append("a:=lambda/(lambda+nu):\n");
        tmp.append("b:=lambda/(lambda+delta):\n");
//        Ptotal:=");
//        for (int i = 1; i < (n + 1); i++) {
//            if (i != 1)
//                tmp.append("+");
//            tmp.append("R[").append(2 * i).append(",1]");
        // 	if (((2*i)%4) == 0)
// 		    tmp.append("R[").append(2*i).append(",1]*b");
// 		else
// 		    tmp.append("R[").append(2*i).append(",1]*a");
//        }

//        tmp.append(":\nT:=(");
//
//        for (int i = 1; i < (2*n +1 ); i++) {
//            if (i != 1)
//                tmp.append("+");
//
//            tmp.append("R[").append(2 * i).append(",1]/Ptotal*S[");
//            tmp.append(i + 1).append(",1]");

        // 	if (((2*i)%4) == 0)
// 		    {
// 			tmp.append("R[").append(2*i).append(",1]*b/Ptotal*S[");
// 			tmp.append(i+1).append(",1]");
// 		    }
// 		else
// 		    {
// 			tmp.append("R[").append(2*i).append(",1]*a/Ptotal*S[");
// 			tmp.append(i+1).append(",1]");
// 		    }
//        }
//        tmp.append(")*1000:\n");

//        tmp.append("Result,lambda,nu,T;");
        tmp.append("for i from 1 to 2  do  printf(\"Time T%d= %f\\n\",i-1,resultT[i]*1000); od:");
        tmp.append("printf(\"Time T2= 0\\n\"):");
        tmp.append("for i from 3 to ").append(stateList.size()-1);
        tmp.append(" do  printf(\"Time T%d= %f\\n\",i,resultT[i]*1000); od:");

        return tmp.toString();

    }


    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("usage: java modelisation.markov.ForwarderChain <n>");
            System.exit(-1);
        }
        ForwarderChain chain = new ForwarderChain(Integer.parseInt(args[0]));
        chain.generateChain();
        System.out.println(chain.generateMapleFile());
    }
}
