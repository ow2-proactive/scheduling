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

import testsuite.exception.AssertionFailedException;
import testsuite.exception.NotStandAloneException;

import testsuite.result.TestResult;

import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * <p>Define a generic functional AbstractTest.</p>
 * <p>If you want to use a test with the results of other tests. You must do create an action method with params.
 * This method can return a result, it will be accessible by <code>getOut()</code>.</p>
 * <p><b>Warning : </b> The array of tests must be in the same order of args of action method.</p>
 * @author Alexandre di Costanzo
 *
 */
public abstract class FunctionalTest extends AbstractTest
    implements Serializable {

    /** To know if the test run with success or not. If you don't run anytime a test
     * this one is considered failed.
     * */
    private boolean failed = true;

    /** The result of the output of the test. Only if postconditions are verified. Null if method action return void. */
    private Object out = null;

    /** Precedents tests, their out will be use in method action as parameters. Must be in the same oder of the
     *  parameters.
     */
    private FunctionalTest[] tests = null;

    /**
     * Construct a new FunctionalTest.
     */
    public FunctionalTest() {
        super();
        setName("A test with no name");
        setDescription("A test with no description.");
    }

    /**
     * Construct a new FunctionalTest with logger and name.
     * @param logger a logger.
     * @param name a name.
     */
    public FunctionalTest(String name) {
        super(name);
        setDescription("A test with no description.");
    }

    /**
     * Construct a new FunctionalTest with name and description.
     * @param name a name.
     * @param description a description.
     */
    public FunctionalTest(String name, String description) {
        super(name, description);
    }

    /**
     *  <p>Preconditions are not essential for a test.</p>
     * <p>But you can override this method, it call before running a test.
     * If preconditions are not verified the tes don't run and is failed.</p>
     * @see testsuite.test.AbstractTest#preConditions()
     */
    public boolean preConditions() throws Exception {
        return true;
    }

    /**
     *  <p>Postconditions are not essential for a test.</p>
     * <p>But you can override this method, it call after running a test.
     * If postconditions are not verified the test is failed.</p>
     * @see testsuite.test.AbstractTest#postConditions()
     */
    public boolean postConditions() throws Exception {
        return true;
    }

    /**
     *  <p>AbstractTest preconditions, run action or action with params if you write it and set the before tests
     *  put the output in out variable and test postconditions.</p>
     * <p>You can get the output with method <code>getOut()</code>.</p>
     * <p>To know if a test run successfully call the method <code>isFailled()</code></p>
     * @see testsuite.test.AbstractTest#runTest()
     */
    public TestResult runTest() {
    	try {
    		initTest();
    	} catch(Exception e) {
    		logger.error("Cannot initialize test", e);
    	}
        // preconditions
		if (logger.isDebugEnabled()){
			logger.debug("Test Preconditions of "+this.getName());
		}
        try {
            if (!preConditions()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Preconditions are not verified");
                }
                failed = true;
                return new TestResult(this, TestResult.GLOBAL_RESULT,
                    "Preconditions not verified");
            }
            if (logger.isDebugEnabled()){
            	logger.debug("Preconditions success in "+this.getName());
            }
        } catch (AssertionFailedException e) {
        	logger.error("Failed assertion" + e);
        	failed=true;
        	logger.warn(this.getName() + ": [FAILED]");
        	return new TestResult(this, TestResult.ERROR, "An assertion failed", e);
        } catch (Exception e1) {
            logger.error("Exception in preconditions", e1);
            failed = true;
            return new TestResult(this, TestResult.ERROR, "In Preconditions", e1);
        }

        // test
        try {
            action();
            if (logger.isDebugEnabled()) {
                logger.debug("Action method of " + this.getName() +
                    " runs with success");
            }
        } catch (NotStandAloneException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Not executed Test : not a standalone test.");
            }
            return null;
        } catch (AssertionFailedException e) {
        	logger.error("Failed assertion" + e);
        	failed=true;
        	logger.warn(this.getName() + ": [FAILED]");
        	return new TestResult(this, TestResult.ERROR, "An assertion failed", e);
        } catch (RuntimeException e) {
            logger.fatal("Exception during the test", e);
            failed = true;
            return new TestResult(this, TestResult.ERROR,
                "During the test execution", e);
        } catch (Exception e) {
            logger.fatal("Exception during the test", e);
            failed = true;
            return new TestResult(this, TestResult.ERROR,
                "During the test execution", e);
        }
        failed = false;
        // postconditions
        if (logger.isDebugEnabled()){
        	logger.debug("Test Postconditions of "+this.getName());
        }
        try {
            if (!postConditions()) {
                logger.warn("Postconditions are not verified");
                failed = true;
            }
			if (logger.isDebugEnabled()){
				 logger.debug("Postconditions success in "+this.getName());
			 }
	    	try {
	    		endTest();
	    	} catch(Exception e) {
	    		logger.error("Cannot correctly end test", e);
	    	}
        } catch (AssertionFailedException e) {
        	logger.error("Failed assertion" + e);
        	failed=true;
        	logger.warn(this.getName() + ": [FAILED]");
            return new TestResult(this, TestResult.ERROR, "An assertion failed in postconditions", e);
        } catch (Exception e1) {
            logger.error("Exception in postcondition", e1);
            failed = true;
            return new TestResult(this, TestResult.ERROR, "In Postconditions",
                e1);
        }
        if (failed) {
            logger.warn(this.getName() + ": [FAILED]");
            return new TestResult(this, TestResult.GLOBAL_RESULT,
                "Test run with success but Postconditions not verified");
        } else {
            if (logger.isInfoEnabled()) {
                logger.info(this.getName() + ": [SUCCESS]");
            }
            return new TestResult(this, TestResult.RESULT, " runs with success");
        }
    }

    public TestResult runTestCascading() {
        // preconditions
        try {
            if (!preConditions()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Preconditions are not verified");
                }
                failed = true;
                return new TestResult(this, TestResult.GLOBAL_RESULT,
                    "Preconditions not verified");
            }
        } catch (Exception e1) {
            logger.error("Exception in preconditions", e1);
            failed = true;
            return new TestResult(this, TestResult.ERROR, "In Preconditions", e1);
        }

        // test
        try {
            Method[] methods = getClass().getMethods();
            Method actionWithParams = null;
            for (int i = 0; i < methods.length; i++) {
                if ((methods[i].getName().compareTo("action") == 0) &&
                        (methods[i].getReturnType().getName().compareTo("void") != 0)) {
                    actionWithParams = methods[i];
                    break;
                }
            }
            if (actionWithParams != null) {
                Object[] args = null;
                if (tests != null) {
                    args = new Object[tests.length];
                    for (int i = 0; i < tests.length; i++)
                        args[i] = tests[i].getOut();
                } else {
                    args = new Object[1];
                    args[0] = null;
                }
                out = actionWithParams.invoke(this, args);
                if (logger.isDebugEnabled()) {
                    logger.debug("Action method of " + this.getName() +
                        " runs without error");
                }
            } else {
                logger.fatal("No action method was found with good signature");
                throw new Exception("No action method with params was found");
            }
        } catch (RuntimeException e) {
            logger.fatal("Test failed", e);
            failed = true;
            out = null;
            return new TestResult(this, TestResult.ERROR,
                "During the test execution", e);
        } catch (Exception e) {
            logger.fatal("Test failed", e);
            failed = true;
            out = null;
            if (!(e instanceof InvocationTargetException)) {
                return new TestResult(this, TestResult.ERROR,
                    "During the test execution", e);
            } else {
                return new TestResult(this, TestResult.ERROR,
                    "During the test execution",
                    ((InvocationTargetException) e).getTargetException());
            }
        }
        failed = false;
        // postconditions
        try {
            if (!postConditions()) {
                logger.warn("Postconditions are not verified");
                failed = true;
            }
        } catch (Exception e1) {
            logger.error("Exception in postcondition", e1);
            failed = true;
            out = null;
            return new TestResult(this, TestResult.ERROR, "In Postconditions",
                e1);
        }
        if (failed) {
            logger.warn(this.getName() + ": [FAILED]");
            return new TestResult(this, TestResult.GLOBAL_RESULT,
                "Test run with success but Postconditions not verified");
        } else {
            if (logger.isInfoEnabled()) {
                logger.info(this.getName() + ": [SUCCESS]");
            }
            return new TestResult(this, TestResult.RESULT, " runs with success");
        }
    }

    /**
     *  <p>Your test code.</p>
     * @throws Exception if one error is up during the execution.
     */
    public abstract void action() throws Exception;

    /**
     * <p>To know if a test run successfully.</p>
     * <p><b>Warning : </b>if you don't have already run the test, this method return <b>false</b>.</p>
     * @return <b>true</b> if a test run successfully, <b>false</b> else.
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * To get the output result of action method if you use an action method with args.
     * @return the output of your test or null.
     */
    public Object getOut() {
        return out;
    }

    /**
     * Get the precedent tests of  a test.
     * @return the before tests.
     */
    public FunctionalTest[] getTests() {
        return tests;
    }

    /**
     * The output result of this tests will be use to run this test with action method with params.
     * @param tests the before tests.
     */
    public void setTests(FunctionalTest[] tests) {
        this.tests = tests;
    }
}
