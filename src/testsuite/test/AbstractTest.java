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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

import testsuite.bean.Beanable;
import testsuite.manager.AbstractManager;
import testsuite.result.TestResult;


/**
 * <p>Super-class of all tests and benchs.</p>
 * <p><b>Warning :</b> if you want to create your tests and use them with <i>TestSuite</i>. The method <code>runTest()</code>
 * must be implemented like this :</p>
 * <p><code>
 * public void runTest(){<br/>
 * ... initTest() ... <br/>
 * ... preConditions() ... <br/>
 * ... // test's code ... <br/>
 * ... postConditions() ... <br/>
 * ... endTest() ... <br/>
 * }
 * </code></p>
 *
 * @author Alexandre di Costanzo
 *
 */
public abstract class AbstractTest implements Serializable, Beanable {

    /** The name of the test. */
    private String name = "AbstractTest with no name";

    /** The Description of the test. */
    private String description = "AbstractTest with no description.";

    /** Logger. */
    protected static Logger logger = null;
    protected AbstractManager manager = null;

    /**
     * Construct a new <code>AbstractTest</code> with defaults settings and a default logger.
     */
    public AbstractTest() {
        logger = Logger.getLogger(getClass().getName());
    }

    /**
     * Construct a new <code>AbstractTest</code>.
     * @param logger the logger of this test
     * @param name name of this test
     */
    public AbstractTest(String name) {
        logger = Logger.getLogger(getClass().getName());
        this.name = name;
    }

    /**
     * Construct a new <code>AbstractTest</code>.
     * @param logger the logger of this test
     * @param name name of this test
     * @param description description of this test
     */
    public AbstractTest(String name, String description) {
        logger = Logger.getLogger(getClass().getName());
        this.name = name;
        this.description = description;
    }

    /**
     * To check if the pre-conditions of a test are valids or not.
     * @return <b>true</b> if the pre-conditions are verified, <b>false</b> else
     * @throws Exception if an error is up during the pre-conditions validating
     */
    public abstract boolean preConditions() throws Exception;

    /**
     * To check if the post-conditions of a test are valids or not.
     * @return <b>true</b> if the post-conditions are verified, <b>false</b> else
     * @throws Exception if an error is up during the post-conditions validating
     */
    public abstract boolean postConditions() throws Exception;

    /**
     * To initialise the test, for example, open a network connection.
     * @throws Exception if an error is up during the intialisation
     */
    public abstract void initTest() throws Exception;

    /**
     * To end the test, for example, close a network connection.
     * @throws Exception if an error is up during the ending
     */
    public abstract void endTest() throws Exception;

    /**
     * This method is called at the beginning of a group of tests.<br>
     * Override it to use it.
     */
    public void uponInitOfGroupOfTests() throws Exception {
    }
    ;

    /**
     * This method is called at the end of a group of tests.<br>
     * Override it to use it.
     */
    public void uponEndOfGroupOfTests() throws Exception {
    }
    ;

    /**
     * Run a test.
     */
    public abstract TestResult runTest();

    public abstract boolean isFailed();

    /**
     * Gets the description of a test.
     * @return a String of the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the name of a test.
     * @return a String of the test
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the description of a test.
     * @param description a String of the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the name of a test.
     * @param name a String of the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the logger of a test.
     * @return Logger of a test
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Return a String with the name and the description of a test.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name + " : " + description;
    }

    /**
     * @see testsuite.bean.Beanable#loadAttributes()
     */
    public void loadAttributes() throws IOException {
        String filename = getClass().getResource(getClass().getName() +
                ".class").getPath();
        filename = filename.replaceAll(".class", ".prop");
        loadAttributes(new File(filename.replaceAll("%20", " ")));
    }

    /**
     * @see testsuite.bean.Beanable#loadAttributes(java.io.File)
     */
    public void loadAttributes(File propsFile) throws IOException {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(propsFile);
        props.load(in);
        in.close();

        loadAttributes(props);
    }

    /**
     * @see testsuite.bean.Beanable#loadAttributes(java.util.Properties)
     */
    public void loadAttributes(Properties properties) {
        Class[] parameterTypes = { String.class };
        Enumeration enumerator = properties.propertyNames();
        Method setter = null;

        while (enumerator.hasMoreElements()) {
            String name = (String) enumerator.nextElement();
            String value = properties.getProperty(name);
            try {
                setter = getClass().getMethod("set" + name, parameterTypes);
                Object[] args = { value };
                try {
                    setter.invoke(this, args);
                } catch (IllegalArgumentException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Problem in loading attributes", e1);
                    }
                } catch (IllegalAccessException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Problem in loading attributes", e1);
                    }
                } catch (InvocationTargetException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Problem in loading attributes", e1);
                    }
                }
            } catch (SecurityException e) {
                // do nothing
            } catch (NoSuchMethodException e) {
                // do nothing
            }
        }
    }

    /**
     * @return
     */
    public AbstractManager getManager() {
        return manager;
    }

    /**
     * @param manager
     */
    public void setManager(AbstractManager manager) {
        this.manager = manager;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this.getClass().getName().compareTo(obj.getClass().getName()) == 0) {
            return true;
        } else {
            return false;
        }
    }
}
