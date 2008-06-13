package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.util.Vector;


public class FirstDiagQuery extends Query {
    int scale;

    public FirstDiagQuery(int n, int s) {
        super(n, 1, 2, 1, 0);
        initParameters = new int[] { n, s };
        down |= ~((1 << n) - 1);
        scale = s;
    }

    public long run() {
        Vector v = split(new Vector());
        int n = v.size();
        long r = 0;
        for (int i = 0; i < n; i++) {
            r += ((Query) v.get(i)).run();
        }
        return (r);
    }

    private DiagQuery next(int q, int sym) {
        int l = (left | q) << 1;
        int d = (down | q);
        int r = (right | q) >> 1;
        return (new DiagQuery(n, 2, sym, scale, l, d, r));
    }

    public Vector split(Vector v) {
        int nq1 = n - 1;
        for (int i = 2; i < nq1; i++)
            v.add(next(1 << i, nq1 - i));
        return (v);
    }
}
