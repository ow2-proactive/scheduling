package org.objectweb.proactive.examples.masterslave.nqueens.query;

import java.util.Vector;

import org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory;


public class DiagQuery extends Query {
    public int sym;
    public int scale;

    public DiagQuery(int n, int done, int sym, int s, int l, int d, int r) {
        super(n, done, l, d, r);
        this.sym = sym;
        this.scale = s;
    }

    public Long run(SlaveMemory memory) {
        return new Long(Diag.run(this) * scale);
    }

    private DiagQuery next(int q) {
        int l = (left | q) << 1;
        int d = (down | q);
        int r = (right | q) >> 1;
        return (new DiagQuery(n, done + 1, sym, scale, l, d, r));
    }

    public Vector split(Vector v) {
        int y = n - done - 1;
        int todo = ~(left | down | right | ((y >= sym) ? 2 : 0));
        while (todo != 0) {
            int q = -todo & todo;
            todo ^= q;
            v.add(next(q));
        }
        return (v);
    }
}
