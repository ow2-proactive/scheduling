package org.objectweb.proactive.examples.masterslave.nqueens.query;

import java.io.Serializable;
import java.util.Vector;


public abstract class Query implements Serializable {
    public int n;
    public int done;
    public int left;
    public int down;
    public int right;
    public int queryId;

    // static generation of task id, may fail if more than 1 manager on the same jvm 
    public static int taskId = 0;
    public boolean resolve = false;

    public Query(int n, int done, int left, int down, int right) {
        this.n = n;
        this.done = done;
        this.left = left;
        this.down = down;
        this.right = right;

        this.queryId = Query.taskId++;
    }

    public String toString() {
        //return(mask((left | down | right), n));
        return ("<" + n + "," + done + type() + "{" + mask(left, n) + "," +
        mask(down, n) + "," + mask(right, n) + "}>");
    }

    public String type() {
        if (this instanceof OutQuery) {
            return ("Out");
        }
        if (this instanceof DiagQuery) {
            return ("Diag");
        }
        if (this instanceof BaseQuery) {
            return ("Base");
        }
        if (this instanceof FirstOutQuery) {
            return ("FirstOut");
        }
        if (this instanceof FirstDiagQuery) {
            return ("firstDiag");
        }
        return ("?");
    }

    public String mask(int n, int l) {
        if (l == 0) {
            return ("");
        }
        return (mask(n >>> 1, l - 1) + (((n % 2) == 0) ? "." : "*"));
    }

    public abstract long run();

    public abstract Vector split(Vector v);

    /**
     * @param buffer
     */
    public void toStringBuffer(StringBuffer buffer) {
        // TODO some day
    }
}
