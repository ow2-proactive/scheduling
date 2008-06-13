package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.io.Serializable;

import org.objectweb.proactive.examples.masterworker.util.Pair;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;


public class QueryExtern
        implements
        Serializable,
        org.objectweb.proactive.extensions.masterworker.interfaces.Task<org.objectweb.proactive.examples.masterworker.util.Pair<Long, Long>> {
    private Query query;

    public QueryExtern(Query query) {
        this.query = query;
    }

    public Pair<Long, Long> run(WorkerMemory memory) {
        long begin = System.currentTimeMillis();
        long answer = query.run();
        long time = System.currentTimeMillis() - begin;
        return new Pair(answer, time);
    }
}
