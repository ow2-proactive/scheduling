/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s):
*
* ################################################################
*/
package testsuite.test;

import testsuite.result.BenchmarkResult;
import testsuite.result.TestResult;

import testsuite.timer.Timeable;

import java.io.Serializable;


/**
 * Define a generic benchmark.
 *
 * @author Alexandre di Costanzo
 *
 */
public abstract class Benchmark extends AbstractTest implements Serializable {

  //  /** Time in ms of a benchmark. */
    long resultTime = 0;

    /** To know if the benchmark run with success or not. If you don't run anytime a benchmark
     * this one is considered failed.
     * */
    private boolean failed = true;
    protected Timeable timer = null;

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
			resultTime =action();
            if (logger.isInfoEnabled()) {
                logger.info("Bench action method runs with success in " +
                    resultTime + "ms");
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
                logger.warn("Postconditions are not verified");
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

    /**
     * @param resultTime
     */
    public void setResultTime(long resultTime) {
        this.resultTime = resultTime;
    }

    /**
     * @return
     */
    public Timeable getTimer() {
        return this.timer;
    }

    /**
     * @param timeable
     */
    public void setTimer(Timeable timeable) {
        this.timer = timeable;
    }

    public void setTimer(String className) {
        try {
            Class c = getClass().getClassLoader().loadClass(className);
            this.timer = (Timeable) c.newInstance();
        } catch (ClassNotFoundException e) {
            logger.warn(className + " was not found. Use default timer", e);
        } catch (InstantiationException e) {
            logger.warn(className +
                " could't be instancied. Use default timer", e);
        } catch (IllegalAccessException e) {
            logger.warn(className + " illegal access. Use default timer", e);
        }
    }
}
