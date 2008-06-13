package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.util.Vector;


public class BaseQuery extends Query {
    public BaseQuery(String[] args) {
        // unused constructor in NQueen Manager
        super(0, 0, 0, 0, 0);
        int size = args.length - 1;
        n = Integer.parseInt(args[0]);
        int l = 0;
        int d = ~((1 << n) - 1);
        int r = 0;
        for (int i = 0; i < size; i++) {
            int q = 1 << Integer.parseInt(args[i + 1]);
            l = (l | q) << 1;
            d = d | q;
            r = (r | q) >> 1;
        }
        done = size;
        left = l;
        down = d;
        right = r;
    }

    public BaseQuery(int nbQueens, int queenLine) {
        super(0, 0, 0, 0, 0);
        initParameters = new int[] { nbQueens, queenLine };
        n = nbQueens;
        int l = 0;
        int d = ~((1 << n) - 1);
        int r = 0;

        int q = 1 << queenLine;
        l = (l | q) << 1;
        d = d | q;
        r = (r | q) >> 1;

        done = 1;
        left = l;
        down = d;
        right = r;
    }

    public BaseQuery(int n, int done, int left, int down, int right) {
        super(n, done, left, down, right);
        initParameters = new int[] { n, done, left, down, right };
    }

    public long run() {
        return Base.run(this);
    }

    public Vector split(Vector v) {
        int n = this.n;
        int QUEENS = (1 << n) - 1;
        int l = left;
        int d = down;
        int r = right;
        for (int todo = QUEENS & ~(l | d | r); todo != 0;) {
            int q = -todo & todo;
            todo ^= q;
            v.add(new BaseQuery(n, done + 1, (l | q) << 1, d | q, (r | q) >> 1));
        }
        return (v);
    }
}
