package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

public class Diag {
    private int SYM;
    private int N = 0;

    private final void countup(int y, int l, int d, int r) {
        if (y > SYM) {
            int todo = ~(l | d | r | 2);
            while (todo != 0) {
                int q = -todo & todo;
                todo ^= q;
                countup(y - 1, (l | q) << 1, d | q, (r | q) >> 1);
            }
        } else {
            int todo = ~(l | d | r);
            while (todo != 0) {
                int q = -todo & todo;
                todo ^= q;
                count(y - 1, (l | q) << 1, d | q, (r | q) >> 1);
            }
        }
    }

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

    public static int run(DiagQuery query) {
        int n = query.n;
        Diag o = new Diag();
        o.SYM = query.sym;
        int y = n - query.done - 1;
        if (y >= query.sym) {
            o.countup(y, query.left, query.down, query.right);
        } else {
            o.count(y, query.left, query.down, query.right);
        }
        return (o.N);
    }
}
