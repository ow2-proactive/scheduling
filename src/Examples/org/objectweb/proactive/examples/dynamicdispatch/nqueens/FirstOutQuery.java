package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.util.Vector;


public class FirstOutQuery extends Query {
    public FirstOutQuery(int n) {
        super(n, 0, 0, ~((1 << n) - 1), 0);
        initParameters = new int[] { n };
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

    public Vector split(Vector v) {
        int nq1 = n - 1;
        int j = n - 2;
        int LASTMASK = (1 << nq1) | 1;
        int ENDBIT = (1 << nq1) >> 1;
        for (int i = 1; i < j; i++, j--) {
            v.add(new OutQuery(1 << i, n, i, j, LASTMASK, ENDBIT));
            LASTMASK |= ((LASTMASK >> 1) | (LASTMASK << 1));
            ENDBIT >>= 1;
        }
        // And the diag
        v.add(new FirstDiagQuery(n, 8));
        return (v);
    }
}
