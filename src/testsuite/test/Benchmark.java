/*
 * Created on Jul 17, 2003
 *
 */
package testsuite.test;

import testsuite.result.BenchmarkResult;
import testsuite.result.TestResult;

import java.io.Serializable;


/**
 * Define a generic benchmark.
 *
 * @author Alexandre di Costanzo
 *
 */
public abstract class Benchmark extends AbstractTest implements Serializable {

    /** Time in ms of a benchmark. */
    private long resultTime = 0;

    /** To know if the benchmark run with success or not. If you don't run anytime a benchmark
     * this one is considered failed.
     * */
    private boolean failed = true;

    /**
     * Constrcut a new benchmark.
     */
    public Benchmark() {
        super();
        setName("Benchmark with no name");
        setDescription("Benchmark with no description.");
    }

    /**
     * Construct a new benchmark with name and a specified logger.
     * @param logger a specified logger.
     * @param name name of a benchmark.
     */
    public Benchmark(String name) {
        super(name);
        setDescription("Benchmark with no description.");
    }

    /**
     * Construct a new benchmark with name and description.
     * @param name name of a benchmark.
     * @param description description of a benchmark.
     */
    public Benchmark(String name, String description) {
        super(name, description);
    }

    /**
     * <p>No preconditions in benchmark.</p>
     * <p>But you can override this method, it call before running a benchmark.
     * If preconditions are not verified the benchmark don't run and is failed.</p>
     * @see testsuite.test.AbstractTest#preConditions()
     */
    public boolean preConditions() throws Exception {
        return true;
    }

    /**
     * <p>No postconditions in benchmark.</p>
     * <p>But you can override this method, it call after running a benchmark.
     * If postconditions are not verified the benchmark is failed.</p>
     * @see testsuite.test.AbstractTest#postConditions()
     */
    public boolean postConditions() throws Exception {
        return true;
    }

    /**
     * <p>AbstractTest preconditions, run action, put the resultTime of benchmark in resultTime variable and test postconditions.</p>
     * <p>You can see the resultTime with method <code>getResult()</code>.</p>
     * <p>To know if a benchmark run successfully call the method <code>isFailled()</code></p>
     * @see testsuite.test.AbstractTest#runTest()
     */
    public TestResult runTest() {
        // preconditions
        try {
            if (!preConditions()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Preconditions are not verified");
                }
                failed = true;
                return new BenchmarkResult(this, TestResult.GLOBAL_RESULT,
                    "Preconditions not verified");
            }
        } catch (Exception e1) {
            logger.error("Exception in preconditions", e1);
            failed = true;
            return new BenchmarkResult(this, TestResult.ERROR,
                "In Preconditions", e1);
        }

        // benchmark
        try {
            resultTime = action();
            if (logger.isInfoEnabled()) {
                logger.info("Bench runs with success in " + resultTime + "ms");
            }
            failed = false;
        } catch (Exception e) {
            logger.fatal("Exception during the bench", e);
            failed = true;
            return new BenchmarkResult(this, TestResult.ERROR,
                "In benchmark execution", e);
        }

        // postconditions
        try {
            if (!postConditions()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Postconditions are not verified");
                }
                failed = true;
            }
        } catch (Exception e1) {
            logger.error("Exception in postcondition", e1);
            failed = true;
            return new BenchmarkResult(this, TestResult.ERROR,
                "In Postconditions", e1);
        }
        if (failed) {
            logger.warn("The bench [FAILED]");
            return new BenchmarkResult(this, TestResult.GLOBAL_RESULT,
                "Benchmark run with success but Postconditions not verified");
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("The bench [SUCCESS]");
            }
            return new BenchmarkResult(this, BenchmarkResult.RESULT,
                "Runned without problems");
        }
    }

    /**
     * <p>Your benchmark code.</p>
     * <p><b>Warning : </b> You must to include the measure in your code.</p>
     * @return time in milliseconds of the benchmark.
     * @throws Exception if one error is up during the execution.
     */
    public abstract long action() throws Exception;

    /**
     * <p>Time in milliseconds of a benchmark.</p>
     * <p><b>Warning : </b>if you don't have already run the bench, time = 0 ms.</p>
     * <p><b>Warning : </b>you can have a positive time but the benchmark failed. Check your resultTime with method
     * <code>isFailled()</code></p>
     * @return time in milliseconds.
     */
    public long getResultTime() {
        return resultTime;
    }

    /**
     * <p>To know if a benchmark run successfully.</p>
     * <p><b>Warning : </b>if you don't have already run the bench, this method return <b>false</b>.</p>
     * @return <b>true</b> if a benchmark run successfully, <b>false</b> else.
     */
    public boolean isFailed() {
        return failed;
    }
}
