/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.xml;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.descriptor.legacyparser.VariablesHandler;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.xml.sax.SAXException;


/**
 * This class provides a Variable Contract between the deployment descriptor and the application program.
 * Variables can be defined of different types, thus enforcing different requirements to the contract.
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class VariableContract implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);
    public static VariableContract xmlproperties = null;
    public static final Lock lock = new Lock();
    private boolean closed;
    private static final Pattern variablePattern = Pattern.compile("(\\$\\{(.*?)\\})");
    private static final Pattern legalPattern = Pattern.compile("^\\$\\{[\\w\\.]+\\}$");

    private class PropertiesDatas implements Serializable {
        public String value;
        public VariableContractType type;
        public String setFrom; //Descriptor, Program

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(value).append(" type=").append(type).append(" setFrom=").append(setFrom);
            return sb.toString();
        }
    }

    private HashMap<String, PropertiesDatas> variablesMap;

    /**
     * Constructor of the class. Creates a new instance.
     *
     */
    public VariableContract() {
        variablesMap = new HashMap<String, PropertiesDatas>();
        closed = false;
    }

    /**
     * Marks the contract as closed. No more variables can be defined or set.
     */
    public void close() {
        closed = true;
    }

    /**
     * Tells if this contract is closed or not.
     *
     * @return True if it is closed, false otherwise.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Method for setting variables value from the deploying application.
     *
     * @param name
     *            The name of the variable.
     * @param value
     *            Value of the variable
     * @throws NullPointerException
     *             if the arguments are null.
     * @throws IllegalArgumentException
     *             if setting the value breaches the variable (contract) type
     */
    public void setVariableFromProgram(String name, String value, VariableContractType type) {
        setVariableFrom(name, value, type, "Program");
        setFromJavaProperty(name, type);
    }

    /**
     * Finds and sets all pending values for java properties
     *
     */
    public void setJavaPropertiesValues() {
        //before closing we set the JavaProperties values
        java.util.Iterator<String> it = variablesMap.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            PropertiesDatas data = variablesMap.get(name);
            setFromJavaProperty(name, data.type);
        } // while
    }

    /**
     * If the variable can be set from the javaProperty, then it looks
     * for the corresponding.
     * @param name
     * @param type
     */
    public void setFromJavaProperty(String name, VariableContractType type) {
        if (!type.hasSetAbility("JavaProperty")) {
            return;
        }

        try {
            String value = System.getProperty(name);
            if (logger.isDebugEnabled()) {
                logger.debug("Found java property " + name + "=" + value);
            }
            if (value == null) {
                value = "";
            }
            setVariableFrom(name, value, type, "JavaProperty");
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to get java property: " + name);
            }
        }
    }

    /**
     * Method for setting variables value from the deploying application. If the variable is not defined,
     * this method will also define it.
     * @param name  The name of the variable
     * @param value Value of the variable
     * @param from        Tells the origin of the setting. Ex: Program, Descriptor.
     * @throws NullPointerException if the arguments are null.
     * @throws IllegalArgumentException if setting the value breaches the variable (contract) type
     */
    private void setVariableFrom(String name, String value, VariableContractType type, String from) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting from " + from + ": " + type + " " + name + "=" + value);
        }

        if (closed) {
            throw new IllegalArgumentException("Variable Contract is Closed. Variables can no longer be set");
        }

        checkGenericLogic(name, value, type);

        if ((value.length() > 0) && !type.hasSetAbility(from)) {
            throw new IllegalArgumentException("Variable " + name + " can not be set from " + from +
                " for type: " + type);
        }

        if ((value.length() <= 0) && !type.hasSetEmptyAbility(from)) {
            throw new IllegalArgumentException("Variable " + name + " can not be set empty from " + from +
                " for type: " + type);
        }

        if (variablesMap.containsKey(name)) {
            PropertiesDatas var = variablesMap.get(name);

            if (!type.hasPriority(var.setFrom, from)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping, lower priority (" + from + " < " + var.setFrom + ") for type: " +
                        type);
                }
                return;
            }
        }

        unsafeAdd(name, value, type, from);
    }

    /**
     * Method for setting a group of variables from the program.
     * @see #setVariableFromProgram(String name, String value, VariableContractType type)
     * @throws NullPointerException if the arguments are null.
     * @throws IllegalArgumentException if setting the value breaches the variable (contract) type
     */
    public void setVariableFromProgram(HashMap<String, String> map, VariableContractType type)
            throws NullPointerException {
        if ((map == null) || (type == null)) {
            throw new NullPointerException("Null arguments");
        }

        String name;
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            name = (String) it.next();
            setVariableFromProgram(name, (String) map.get(name), type);
        }
    }

    /**
     * Method for setting variables value from the deploying application.
     * @param name        The name of the variable.
     * @param value       Value of the variable
     * @throws NullPointerException if the arguments are null.
     * @throws IllegalArgumentException if setting the value breaches the variable (contract) type
     */
    public void setDescriptorVariable(String name, String value, VariableContractType type) {
        setVariableFrom(name, value, type, "Descriptor");
        setFromJavaProperty(name, type);
    }

    /**
     * Loads the variable contract from a Java Properties file format
     * @param file The file location.
     * @throws org.xml.sax.SAXException
     */
    @SuppressWarnings("unchecked")
    public void load(String file) throws org.xml.sax.SAXException {
        Properties properties = new Properties();
        if (logger.isDebugEnabled()) {
            logger.debug("Loading propeties file:" + file);
        }

        // Open the file
        try {
            FileInputStream stream = new FileInputStream(file);
            properties.load(stream);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Curret Working Directory: " + System.getProperty("user.dir"));
            }

            throw new org.xml.sax.SAXException("Tag property cannot open file : [" + file + "]");
        }

        String name;
        String value;
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            name = (String) it.next();
            value = properties.getProperty(name);
            setDescriptorVariable(name, value, VariableContractType.DescriptorVariable);
        }
    }

    /**
     * Loads a file with Variable Contract tags into the this instance.
     * @param file
     */
    public void loadXML(String file) {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading XML variable file:" + file);
        }
        VariablesHandler.createVariablesHandler(file, this);
    }

    /**
     * Returns the value of the variable name passed as parameter.
     * @param name The name of the variable.
     * @return The value of the variable.
     */
    public String getValue(String name) {
        if (variablesMap.containsKey(name)) {
            PropertiesDatas var = variablesMap.get(name);

            return var.value;
        }

        return null;
    }

    /**
     * Replaces the variables inside a text with their values.
     *
     * @param         text        Text with variables inside.
     * @return        The text with the values
     */
    public String transform(String text) throws SAXException {
        if (text == null) {
            return null;
        }

        Matcher m = variablePattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            if (!isLegalName(m.group(1))) {
                throw new SAXException("Error, malformed variable:" + m.group(1));
            }

            String name = m.group(2);
            String value = getValue(name);

            if ((value == null) || (value.length() <= 0)) {
                throw new SAXException("Error, variable value not found: " + name + "=?");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Matched:" + name + " = " + value);
                //logger.debug(m);
            }
            m.appendReplacement(sb, value);
        }
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * Checks common parameters.
     * @param name Must be not null or empty, and legal.
     * @param value Must be not null
     * @param type Checks if variable is already defined with different type
     */
    private void checkGenericLogic(String name, String value, VariableContractType type) {

        /*
         * Generic Logical checks
         */
        if (name == null) {
            throw new NullPointerException("Variable Name is null.");
        }

        if (name.length() <= 0) {
            throw new IllegalArgumentException("Variable Name is empty.");
        }

        if (!isLegalName("${" + name + "}")) {
            throw new IllegalArgumentException("Illegal variable name:" + name);
        }

        if (value == null) {
            throw new NullPointerException("Variable Value is null.");
        }

        if (type == null) {
            throw new NullPointerException("Variable Type is null.");
        }

        if (variablesMap.containsKey(name) && !variablesMap.get(name).type.equals(type)) {
            throw new IllegalArgumentException("Variable " + name + " is already defined with type: " +
                variablesMap.get(name).type);
        }
    }

    /**
     * Unsafe adding to the list of variables. No logical checking is done.
     * @param name Name of the variable
     * @param value Value of the variable
     * @param type The type of the variable
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    private void unsafeAdd(String name, String value, VariableContractType type, String setFrom)
            throws NullPointerException, IllegalArgumentException {
        if (name == null) {
            throw new NullPointerException("XML Variable Name is null.");
        }
        if (name.length() <= 0) {
            throw new IllegalArgumentException("XML Variable Name is empty.");
        }
        if (value == null) {
            throw new NullPointerException("XML Variable Value is null.");
        }

        PropertiesDatas data;
        if (variablesMap.containsKey(name)) {
            data = variablesMap.get(name);
            if (logger.isDebugEnabled()) {
                logger.debug("...Modifying variable registry: " + name + "=" + value);
            }
        } else {
            data = new PropertiesDatas();
            if (logger.isDebugEnabled()) {
                logger.debug("...Creating new registry for variable: " + name + "=" + value);
            }
        }

        data.type = type;
        data.value = value;
        data.setFrom = setFrom;
        variablesMap.put(name, data);
    }

    /**
     * Checks if there are empty values in the contract. All errors are printed through
     * the logger.
     * @return True if the contract has no empty values.
     */
    public boolean checkContract() {
        boolean retval = true;
        String name;
        java.util.Iterator<String> it = variablesMap.keySet().iterator();
        while (it.hasNext()) {
            name = it.next();
            PropertiesDatas data = variablesMap.get(name);

            if (data.value.length() <= 0) {
                logger.error(data.type.getEmptyErrorMessage(name));
                retval = false;
            }
        }

        return retval;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        PropertiesDatas var;
        String name;
        java.util.Iterator<String> it = variablesMap.keySet().iterator();
        while (it.hasNext()) {
            name = it.next();
            var = variablesMap.get(name);

            sb.append(name).append("=").append(var).append("\n");
        }

        return sb.toString();
    }

    public Map<String, String> toMap() {
        Map<String, String> res = new HashMap<String, String>();

        PropertiesDatas var;
        String name;
        java.util.Iterator<String> it = variablesMap.keySet().iterator();
        while (it.hasNext()) {
            name = it.next();
            var = variablesMap.get(name);

            res.put(name, var.value);
        }

        return res;
    }

    /**
     * This methods tells if a variable name is acceptable
     * @param var The variable name, without the ${} wrapping.
     * @return true if the variable is legal, false otherwise.
     */
    public boolean isLegalName(String var) {
        Matcher m = legalPattern.matcher(var);
        return m.matches();
    }

    /**
     * Class used for exclusive access to global static variable:
     * org.objectweb.proactive.core.xml.XMLProperties.xmlproperties
     *
     * @author The ProActive Team
     */
    static public class Lock {
        private boolean locked;

        private Lock() {
            locked = false;
        }

        /**
         * Call this method to release the lock on the XMLProperty variable.
         * This method will also clean the variable contents.
         */
        public synchronized void release() {
            locked = false;
            notify();
        }

        /**
         * Call this method to get the lock on the XMLProperty object instance.
         */
        public synchronized void aquire() {
            while (locked) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            locked = true;
        }
    }
}
