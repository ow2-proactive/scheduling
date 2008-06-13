package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

public class Base {
    private int N = 0;

    private final void count(int y, int l, int d, int r) {
        int todo = ~(l | d | r);
        if (todo != 0) {
            if (y == 0) {
                N++;
            } else {
                do {
                    int q = -todo & todo;
                    todo ^= q;
                    count(y - 1, (l | q) << 1, d | q, (r | q) >> 1);
                } while (todo != 0);
            }
        }
    }

    public static int run(Query query) {
        int n = query.n;
        Base o = new Base();
        o.count(n - query.done - 1, query.left, query.down, query.right);
        return (o.N);
    }

    public static void main(String[] args) {
        System.out.println("" + run(new BaseQuery(args)));
    }
}
