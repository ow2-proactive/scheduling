package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public interface WorkerItf {

    public abstract Result solve(Query query);

    public abstract String getName();

    public abstract void printNbSolvedQueries();

    public abstract BooleanWrapper ping() throws Exception;

}