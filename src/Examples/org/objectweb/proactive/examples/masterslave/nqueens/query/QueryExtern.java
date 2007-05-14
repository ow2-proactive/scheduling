package org.objectweb.proactive.examples.masterslave.nqueens.query;

import java.io.Serializable;

import org.objectweb.proactive.examples.masterslave.util.Pair;
import org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;


public class QueryExtern implements Serializable, Task<Pair<Long, Long>> {
    private Query query;

    public QueryExtern(Query query) {
        this.query = query;
    }

    public Pair<Long, Long> run(SlaveMemory memory) {
        long begin = System.currentTimeMillis();
        long answer = query.run();
        long time = System.currentTimeMillis() - begin;
        return new Pair(answer, time);
    }
}
