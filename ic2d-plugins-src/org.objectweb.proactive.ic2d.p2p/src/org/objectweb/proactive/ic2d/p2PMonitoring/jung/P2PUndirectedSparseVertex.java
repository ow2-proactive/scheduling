package org.objectweb.proactive.ic2d.p2PMonitoring.jung;

import edu.uci.ics.jung.graph.impl.UndirectedSparseVertex;


public class P2PUndirectedSparseVertex extends UndirectedSparseVertex {
    protected int noa;
    protected int maxNoa;
    protected String name;

    public P2PUndirectedSparseVertex() {
        super();
    }

    public int getMaxNoa() {
        return maxNoa;
    }

    public void setMaxNoa(int maxNOA) {
        this.maxNoa = maxNOA;
    }

    public int getNoa() {
        return noa;
    }

    public void setNoa(int noa) {
        this.noa = noa;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
