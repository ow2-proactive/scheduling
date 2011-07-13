package org.ow2.proactive.scheduler.ext.mapreduce;

import org.apache.hadoop.mapreduce.Counter;


/**
 * The {@link FakeHadoopCounter} class act as a {@link Counter} class. We need
 * this class because we cannot directly create an instance of the
 * {@link Counter} class since the only constructor provided by that class is
 * protected.
 *
 * @author The ProActive Team
 *
 */
public class FakeHadoopCounter extends Counter {

    public FakeHadoopCounter() {
        super();
    }

    public FakeHadoopCounter(String name, String displayName) {
        super(name, displayName);
    }
}
