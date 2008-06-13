package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.util.List;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public interface MulticastWorkerDispatch {

    public abstract List<Result> solve(List<Query> query);

    public abstract List<String> getName();

    public abstract void printNbSolvedQueries();

    public abstract List<BooleanWrapper> ping() throws Exception;

}
