/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job.factories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.ProActiveJob;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ProActiveTask;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.util.RegexpMatcher;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.Tools;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * JobFactory_xpath is am implementation of the JobFactory using XPATH
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 * $Id$
 */
public class JobFactory_xpath extends JobFactory {

    public static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.FACTORY);
    /** Location of the schema used to parse job descriptor. */
    public static final String SCHEMA_LOCATION = "/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/dev/schedulerjob.rng";
    /** Variables styleScheet location. */
    public static final String STYLESHEET_LOCATION = "/org/ow2/proactive/scheduler/common/xml/stylesheets/variables.xsl";
    /** Job name space. */
    public static final String JOB_NAMESPACE = "urn:proactive:jobdescriptor:dev";
    /** Job prefix. */
    public static final String JOB_PREFIX = "js";
    private static final String variablesPattern = "\\$\\{[^\\}]+\\}";

    //JOBS
    private static final String JOB_TAG = "job";
    private static final String JOB_TASKFLOW = "taskFlow";
    private static final String JOB_PROACTIVE = "proActive";
    private static final String JOB_ATTRIBUTE_PRIORITY = "@priority";
    private static final String JOB_ATTRIBUTE_PROJECTNAME = "@projectName";
    private static final String JOB_ATTRIBUTE_LOGFILE = "@logFile";
    private static final String JOB_VARIABLES = "variables";
    private static final String JOB_VARIABLE = "variable";
    private static final String VARIABLE_VALUE = "value";
    //COMMON
    private static final String ATTRIBUTE_CANCELJOBONERROR = "@cancelJobOnError";
    private static final String ATTRIBUTE_RESTARTTASKONERROR = "@restartTaskOnError";
    private static final String ATTRIBUTE_MAXNUMBEROFEXECUTION = "@maxNumberOfExecution";
    private static final String ATTRIBUTE_ID = "@name";
    private static final String TAG_DESCRIPTION = "description";
    private static final String GENERIC_INFORMATION = "genericInformation/info";
    private static final String GENERIC_INFO_ATTRIBUTE_NAME = "@name";
    private static final String GENERIC_INFO_ATTRIBUTE_VALUE = "@value";
    //TASKS
    private static final String TASK_TAG = "task";
    private static final String JAVA_EXECUTABLE = "javaExecutable";
    private static final String NATIVE_EXECUTABLE = "nativeExecutable";
    private static final String PROACTIVE_EXECUTABLE = "proActiveExecutable";
    private static final String TASK_DEPENDENCES_REF = "depends/task/@ref";
    private static final String TASK_ATTRIBUTE_RESULTPREVIEW = "@resultPreviewClass";
    private static final String TASK_ATTRIBUTE_PRECIOUSRESULT = "@preciousResult";
    private static final String TASK_ATTRIBUTE_CLASSNAME = "@class";
    private static final String TASK_TAG_PARAMETERS = "parameters/parameter";
    private static final String TASK_ATTRIBUTE_NEEDEDNODES = "@neededNodes";
    private static final String TASK_ATTRIBUTE_WALLTIME = "@walltime";
    private static final String TASK_ATTRIBUTE_FORK = "@fork";
    //SCRIPTS
    private static final String TASK_TAG_SELECTION = "selection";
    private static final String TASK_TAG_PRE = "pre";
    private static final String TASK_TAG_POST = "post";
    private static final String TASK_TAG_CLEANING = "cleaning";
    private static final String TASK_TAG_SCRIPT = "script";
    private static final String SCRIPT_STATICCOMMAND = "staticCommand";
    private static final String SCRIPT_DYNAMICCOMMAND = "dynamicCommand";
    private static final String SCRIPT_ATTRIBUTE_VALUE = "@value";
    private static final String SCRIPT_TAG_ARGUMENTS = "arguments/argument";
    private static final String SCRIPT_TAG_GENERATION = "generation";
    private static final String SCRIPT_TAG_FILE = "file";
    private static final String SCRIPT_TAG_CODE = "code";
    private static final String SCRIPT_ATTRIBUTE_URL = "@url";
    private static final String SCRIPT_ATTRIBUTE_PATH = "@path";
    private static final String SCRIPT_ATTRIBUTE_LANGUAGE = "@language";
    private static final String SCRIPT_ATTRIBUTE_TYPE = "@type";
    //CLASSPATH
    private static final String CP_TAG_CLASSPATHES = "jobClasspath/pathElement";
    private static final String CP_ATTRIBUTE_PATH = "@path";
    //FORK ENVIRONMENT
    private static final String FORK_TAG_ENVIRONMENT = "forkEnvironment";
    private static final String FORK_ATTRIBUTE_JAVAHOME = "@javaHome";
    private static final String FORK_ATTRIBUTE_JVMPARAMETERS = "@jvmParameters";

    /** Xpath factory instance */
    private XPath xpath;

    JobFactory_xpath() {
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new SchedulerNamespaceContext());
    }

    /**
     * Creates a job using the given job descriptor
     *
     * @param filePath
     *            the path to a job descriptor
     * @return a Job instance
     * @throws JobCreationException
     */
    public Job createJob(String filePath) throws JobCreationException {
        Job job = null;

        try {
            File f = new File(filePath);
            if (!f.exists()) {
                throw new FileNotFoundException("This file has not been found : " + filePath);
            }
            validate(filePath);
            Node rootNode = transformVariablesAndGetDOM(new FileInputStream(f));
            job = createJob(rootNode);
        } catch (Exception e) {
            logger.debug("", e);
            throw new JobCreationException("Exception occured during Job creation", e);

        }
        logger.info("Job successfully created !");
        return job;
    }

    /**
     * Validate the given job descriptor using the internal RELAX_NG Schema.
     *
     * @param filePath
     */
    private void validate(String filePath) throws URISyntaxException, VerifierConfigurationException,
            SAXException, IOException {
        // We use sun multi validator (msv)
        VerifierFactory vfactory = new com.sun.msv.verifier.jarv.TheFactoryImpl();

        InputStream schemaStream = this.getClass().getResourceAsStream(SCHEMA_LOCATION);
        Schema schema = vfactory.compileSchema(schemaStream);
        Verifier verifier = schema.newVerifier();
        ValidatingErrorHandler veh = new ValidatingErrorHandler();
        verifier.setErrorHandler(veh);
        verifier.verify(filePath);
        if (veh.mistakes > 0) {
            System.err.println(veh.mistakes + " mistakes.");
            throw new SAXException(veh.mistakesStack.toString());
        }
    }

    /**
     * Parse the given octet stream to a XML DOM, and transform variable
     * definitions using a stylesheet
     *
     * @param input
     * @return
     * @throws JobCreationException
     */
    private Node transformVariablesAndGetDOM(InputStream input) throws ParserConfigurationException,
            SAXException, IOException, JobCreationException {
        // create a new parser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        DocumentBuilder db = factory.newDocumentBuilder();

        Document docSource = db.parse(input);

        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        DOMSource domSource = new DOMSource(docSource);
        TransformerFactory tfactory = TransformerFactory.newInstance();

        Source stylesheetSource = new StreamSource(this.getClass().getResourceAsStream(STYLESHEET_LOCATION));
        Transformer transformer = null;
        try {
            transformer = tfactory.newTransformer(stylesheetSource);
        } catch (TransformerConfigurationException e1) {
            logger.debug("", e1);
        }
        DOMResult result = new DOMResult();

        NodeList nList = domSource.getNode().getFirstChild().getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (JOB_VARIABLES.equals(node.getLocalName())) {
                nList = node.getChildNodes();
                for (int j = 0; j < nList.getLength(); j++) {
                    node = nList.item(j);
                    if (JOB_VARIABLE.equals(node.getLocalName())) {
                        node = node.getAttributes().getNamedItem(VARIABLE_VALUE);
                        node.setNodeValue(replace(node.getNodeValue()));
                    }
                }
                break;
            }
        }

        try {
            transformer.transform(domSource, result);
        } catch (TransformerException e1) {
            throw new JobCreationException(
                "A variable in the Variables definition cannot be found neither in JVM arguments nor in local variable definition.");
        }
        return result.getNode();
    }

    /**
     * Replace variable from JVM property in the given string.
     *
     * @param str the string in which to do the replacement.
     * @return the string where variables from JVM has been replaced.
     */
    private String replace(String str) {
        str = str.trim();
        String[] strs = RegexpMatcher.matches(variablesPattern, str);
        String replacement;
        if (strs.length != 0) {
            //for each entry
            for (String s : strs) {
                //remove ${ and }
                s = s.substring(2, s.length() - 1);
                replacement = System.getProperty(s);
                if (replacement != null) {
                    str = str.replaceFirst("\\$\\{" + s + "\\}", replacement);
                }
            }
        }
        return str;
    }

    private Job createJob(Node rootNode) throws XPathExpressionException, InvalidScriptException,
            SAXException, ClassNotFoundException, IOException, UserException {
        Job job = null;

        // JOB
        XPathExpression exp = xpath.compile(addPrefixes("/" + JOB_TAG));
        Node jobNode = (Node) exp.evaluate(rootNode, XPathConstants.NODE);
        JobType jt = null;

        // JOB TYPE
        Node tfNode = (Node) xpath.evaluate(addPrefixes(JOB_TASKFLOW), jobNode, XPathConstants.NODE);
        if (tfNode != null) {
            jt = JobType.TASKSFLOW;
        }
        Node paNode = (Node) xpath.evaluate(addPrefixes(JOB_PROACTIVE), jobNode, XPathConstants.NODE);
        if (paNode != null) {
            jt = JobType.PROACTIVE;
        }
        if (jt == null) {
            throw new SAXException("Invalid XML : Job must have a valid type");
        }

        switch (jt) {
            case PROACTIVE:
                job = new ProActiveJob();
                break;
            case TASKSFLOW:
                job = new TaskFlowJob();
                break;
            case PARAMETER_SWEEPING:
                throw new RuntimeException("Job Parameter Sweeping is not yet implemented !");
        }
        // JOB NAME
        job.setName(replace((String) xpath.evaluate(ATTRIBUTE_ID, jobNode, XPathConstants.STRING)));
        logger.debug(ATTRIBUTE_ID + " : " + job.getName());

        // JOB PRIORITY
        String prio = xpath.evaluate(JOB_ATTRIBUTE_PRIORITY, jobNode);
        job.setPriority(JobPriority.findPriority(prio));
        logger.debug(JOB_ATTRIBUTE_PRIORITY + "priority = " + job.getPriority());

        // CANCEL JOB ON ERROR
        String cancel = xpath.evaluate(ATTRIBUTE_CANCELJOBONERROR, jobNode);
        if (!"".equals(cancel)) {
            job.setCancelJobOnError(Boolean.parseBoolean(cancel));
            logger.debug(ATTRIBUTE_CANCELJOBONERROR + " = " + job.isCancelJobOnError());
        }

        // RESTART TASK ON ERROR
        String restart = xpath.evaluate(ATTRIBUTE_RESTARTTASKONERROR, jobNode);
        job.setRestartTaskOnError(RestartMode.getMode(restart));
        logger.debug(ATTRIBUTE_RESTARTTASKONERROR + " = " + job.getRestartTaskOnError());

        // MAX NUMBER OF EXECUTION ON ERROR
        String number = xpath.evaluate(ATTRIBUTE_MAXNUMBEROFEXECUTION, jobNode);
        if (!"".equals(number)) {
            job.setMaxNumberOfExecution(Integer.parseInt(number));
            logger.debug(ATTRIBUTE_MAXNUMBEROFEXECUTION + " = " + job.getMaxNumberOfExecution());
        }

        // JOB PROJECT NAME
        String projectName = (String) xpath.evaluate(JOB_ATTRIBUTE_PROJECTNAME, jobNode,
                XPathConstants.STRING);
        if (!"".equals(projectName)) {
            job.setProjectName(replace(projectName));
            logger.debug(JOB_ATTRIBUTE_PROJECTNAME + " = " + projectName);
        }

        // JOB LOG FILE
        String logFile = xpath.evaluate(JOB_ATTRIBUTE_LOGFILE, jobNode);
        if (!"".equals(logFile)) {
            job.setLogFile(replace(logFile));
            logger.debug(JOB_ATTRIBUTE_LOGFILE + " = " + logFile);
        }

        // JOB DESCRIPTION
        String description = (String) xpath.evaluate(addPrefixes(JOB_TAG + "/" + TAG_DESCRIPTION), rootNode,
                XPathConstants.STRING);
        if (description.length() > 0) {
            job.setDescription(((String) description).trim());
            logger.debug(JOB_TAG + "/" + TAG_DESCRIPTION + " = " + description);
        }

        //JOB GENERIC INFORMATION
        NodeList list = (NodeList) xpath.evaluate(addPrefixes(GENERIC_INFORMATION), jobNode,
                XPathConstants.NODESET);
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                String name = (String) xpath.evaluate(GENERIC_INFO_ATTRIBUTE_NAME, n, XPathConstants.STRING);
                String value = (String) xpath
                        .evaluate(GENERIC_INFO_ATTRIBUTE_VALUE, n, XPathConstants.STRING);

                logger.debug(name + "->" + value);

                if ((name != null) && (value != null)) {
                    job.addGenericInformation(name, value);
                }
            }
        }

        //JOB CLASSPATH
        String[] classpathEntries = null;
        NodeList classPathNodes = (NodeList) xpath.evaluate(addPrefixes(CP_TAG_CLASSPATHES), jobNode,
                XPathConstants.NODESET);
        if (classPathNodes != null) {
            classpathEntries = new String[classPathNodes.getLength()];
            for (int i = 0; i < classPathNodes.getLength(); i++) {
                Node n = classPathNodes.item(i);
                String path = (String) xpath.evaluate(CP_ATTRIBUTE_PATH, n, XPathConstants.STRING);
                classpathEntries[i] = path;
            }
        }

        // JOB EXECUTION ENVIRONMENT
        if (classpathEntries != null && classpathEntries.length != 0) {
            job.getEnvironment().setJobClasspath(classpathEntries);
        }
        return createTasks(jobNode, job, xpath);
    }

    Job createTasks(Node jobNode, Job job, XPath xpath) throws XPathExpressionException,
            ClassNotFoundException, IOException, InvalidScriptException, UserException {
        Map<Task, ArrayList<String>> tasks = new HashMap<Task, ArrayList<String>>();

        // TASKS
        NodeList list = null;
        switch (job.getType()) {
            case TASKSFLOW:
                list = (NodeList) xpath.evaluate(addPrefixes(JOB_TASKFLOW + "/" + TASK_TAG), jobNode,
                        XPathConstants.NODESET);
                break;
            default:
                list = (NodeList) xpath.evaluate(addPrefixes(JOB_PROACTIVE + "/" + TASK_TAG), jobNode,
                        XPathConstants.NODESET);
        }
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                Node taskNode = list.item(i);
                Task task = null;

                // TASK PROCESS
                Node process = (Node) xpath.evaluate(addPrefixes(JAVA_EXECUTABLE), taskNode,
                        XPathConstants.NODE);
                if (process != null) { // JAVA TASK
                    task = createJavaTask(process);
                } else if ((process = (Node) xpath.evaluate(addPrefixes(NATIVE_EXECUTABLE), taskNode,
                        XPathConstants.NODE)) != null) { // NATIVE TASK
                    task = createNativeTask(process);
                } else if ((process = (Node) xpath.evaluate(addPrefixes(PROACTIVE_EXECUTABLE), taskNode,
                        XPathConstants.NODE)) != null) { // APPLICATION TASK
                    task = createProActiveTask(process);
                } else {
                    throw new RuntimeException("Unknow process !!");
                }

                task = createTask(taskNode, task);

                //check if walltime and fork attribute are consistency.
                //if the walltime is set and fork not, fork must be true.
                if ((task instanceof JavaTask) && task.getWallTime() > 0 && !((JavaTask) task).isFork()) {
                    ((JavaTask) task).setFork(true);
                    logger
                            .info("For javatask, setting a walltime implies the task to be forked, so your task will be forked anyway !");
                }
                switch (job.getType()) {
                    case PROACTIVE:
                        ((ProActiveJob) job).setTask((ProActiveTask) task);
                        break;
                    default:
                        ((TaskFlowJob) job).addTask(task);
                }

                // TASK DEPENDS
                NodeList refList = (NodeList) xpath.evaluate(addPrefixes(TASK_DEPENDENCES_REF), taskNode,
                        XPathConstants.NODESET);
                ArrayList<String> depList = new ArrayList<String>();
                if (refList != null) {
                    for (int j = 0; j < refList.getLength(); j++) {
                        Node ref = refList.item(j);
                        depList.add(ref.getNodeValue());
                    }
                }
                tasks.put(task, depList);
            }
        }

        // Dependencies
        HashMap<String, Task> depends = new HashMap<String, Task>();

        for (Task td : tasks.keySet())
            depends.put(td.getName(), td);
        if (job.getType() != JobType.PROACTIVE) {
            for (Entry<Task, ArrayList<String>> task : tasks.entrySet()) {
                // task.getKey().setJobId(job.getId());
                ArrayList<String> depListStr = task.getValue();
                for (int i = 0; i < depListStr.size(); i++) {
                    if (depends.containsKey(depListStr.get(i))) {
                        task.getKey().addDependence(depends.get(depListStr.get(i)));
                    } else {
                        System.err.println("Can't resolve dependence : " + depListStr.get(i));
                    }
                }
            }
        }

        return job;
    }

    private Task createTask(Node taskNode, Task task) throws XPathExpressionException,
            ClassNotFoundException, InvalidScriptException, MalformedURLException {
        // TASK NAME
        task.setName((String) xpath.evaluate(ATTRIBUTE_ID, taskNode, XPathConstants.STRING));
        logger.debug(ATTRIBUTE_ID + " = " + task.getName());

        // TASK DESCRIPTION
        String desc = (String) xpath.evaluate(addPrefixes(TAG_DESCRIPTION), taskNode, XPathConstants.STRING);
        if (desc.length() > 0) {
            task.setDescription(desc);
        }
        logger.debug(TAG_DESCRIPTION + " = " + task.getDescription());

        // TASK GENERIC INFORMATION
        NodeList list = (NodeList) xpath.evaluate(addPrefixes(GENERIC_INFORMATION), taskNode,
                XPathConstants.NODESET);
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                String name = (String) xpath.evaluate(GENERIC_INFO_ATTRIBUTE_NAME, n, XPathConstants.STRING);
                String value = (String) xpath
                        .evaluate(GENERIC_INFO_ATTRIBUTE_VALUE, n, XPathConstants.STRING);
                logger.debug(GENERIC_INFORMATION + " = " + name + ":" + value);
                if ((name != null) && (value != null)) {
                    task.addGenericInformation(name, value);
                }
            }
        }

        // TASK RESULT DESCRIPTION
        String previewClassName = (String) xpath.evaluate(TASK_ATTRIBUTE_RESULTPREVIEW, taskNode,
                XPathConstants.STRING);
        if (!previewClassName.equals("")) {
            task.setResultPreview(previewClassName);
            logger.debug(TASK_ATTRIBUTE_RESULTPREVIEW + " = " + previewClassName);
        }
        // TASK WALLTIME
        String wallTime = (String) xpath.evaluate(TASK_ATTRIBUTE_WALLTIME, taskNode, XPathConstants.STRING);
        if (wallTime != null && !wallTime.equals("")) {
            task.setWallTime(Tools.formatDate(wallTime));
            logger.debug(TASK_ATTRIBUTE_WALLTIME + " = " + wallTime + " ( " + Tools.formatDate(wallTime) +
                "ms )");
        }

        // TASK PRECIOUS RESULT
        task.setPreciousResult(((String) xpath.evaluate(TASK_ATTRIBUTE_PRECIOUSRESULT, taskNode,
                XPathConstants.STRING)).equals("true"));
        logger.debug(TASK_ATTRIBUTE_PRECIOUSRESULT + " = " + task.isPreciousResult());

        // CANCEL JOB ON ERROR
        String cancel = (String) xpath.evaluate(ATTRIBUTE_CANCELJOBONERROR, taskNode, XPathConstants.STRING);
        if (!"".equals(cancel)) {
            task.setCancelJobOnError(Boolean.parseBoolean(cancel));
            logger.debug(ATTRIBUTE_CANCELJOBONERROR + " = " + task.isCancelJobOnError());
        }

        // TASK RESTART ON ERROR
        String restart = (String) xpath.evaluate(ATTRIBUTE_RESTARTTASKONERROR, taskNode,
                XPathConstants.STRING);
        task.setRestartTaskOnError(RestartMode.getMode(restart));
        logger.debug(ATTRIBUTE_RESTARTTASKONERROR + " = " + task.getRestartTaskOnError());

        // TASK NUMBER OF EXECUTION ON ERROR
        String noe = (String) xpath.evaluate(ATTRIBUTE_MAXNUMBEROFEXECUTION, taskNode, XPathConstants.STRING);
        if (noe != "") {
            task.setMaxNumberOfExecution(Integer.parseInt(noe));
            logger.debug(ATTRIBUTE_MAXNUMBEROFEXECUTION + " = " + task.getMaxNumberOfExecution());
        }

        // TASK VERIF
        Node verifNode = (Node) xpath.evaluate(addPrefixes(TASK_TAG_SELECTION + "/" + TASK_TAG_SCRIPT),
                taskNode, XPathConstants.NODE);
        if (verifNode != null) {
            task.setSelectionScript(createSelectionScript(verifNode));
            logger.debug(TASK_TAG_SELECTION + "/" + TASK_TAG_SCRIPT + " set");
        }

        // TASK PRE
        Node preNode = (Node) xpath.evaluate(addPrefixes(TASK_TAG_PRE + "/" + TASK_TAG_SCRIPT), taskNode,
                XPathConstants.NODE);
        if (preNode != null) {
            task.setPreScript(createScript(preNode));
            logger.debug(TASK_TAG_PRE + "/" + TASK_TAG_SCRIPT + " set");
        }

        // TASK POST
        Node postNode = (Node) xpath.evaluate(addPrefixes(TASK_TAG_POST + "/" + TASK_TAG_SCRIPT), taskNode,
                XPathConstants.NODE);
        if (postNode != null) {
            task.setPostScript(createScript(postNode));
            logger.debug(TASK_TAG_POST + "/" + TASK_TAG_SCRIPT + " set");
        }

        // TASK CLEAN
        Node cleaningNode = (Node) xpath.evaluate(addPrefixes(TASK_TAG_CLEANING + "/" + TASK_TAG_SCRIPT),
                taskNode, XPathConstants.NODE);
        if (cleaningNode != null) {
            task.setCleaningScript(createScript(cleaningNode));
            logger.debug(TASK_TAG_CLEANING + "/" + TASK_TAG_SCRIPT + " set");
        }

        return task;
    }

    private Task createNativeTask(Node executable) throws XPathExpressionException, ClassNotFoundException,
            IOException, InvalidScriptException {
        Node scNode = (Node) xpath.evaluate(addPrefixes(SCRIPT_STATICCOMMAND), executable,
                XPathConstants.NODE);
        Node dcNode = (Node) xpath.evaluate(addPrefixes(SCRIPT_DYNAMICCOMMAND), executable,
                XPathConstants.NODE);
        NativeTask desc = new NativeTask();
        if (scNode != null) {
            ArrayList<String> cmd = new ArrayList<String>();
            // static command
            String[] cmds = Tools.parseCommandLine((String) xpath.evaluate(SCRIPT_ATTRIBUTE_VALUE, scNode,
                    XPathConstants.STRING));
            for (String s : cmds) {
                cmd.add(s);
            }

            NodeList args = (NodeList) xpath.evaluate(addPrefixes(SCRIPT_TAG_ARGUMENTS), scNode,
                    XPathConstants.NODESET);
            if (args != null) {
                for (int i = 0; i < args.getLength(); i++) {
                    Node arg = args.item(i);
                    String value = (String) xpath
                            .evaluate(SCRIPT_ATTRIBUTE_VALUE, arg, XPathConstants.STRING);

                    if (value != null) {
                        cmd.add(value);
                    }
                }
            }
            desc.setCommandLine(cmd.toArray(new String[] {}));
        } else {
            // dynamic command
            Node scriptNode = (Node) xpath.evaluate(
                    addPrefixes(SCRIPT_TAG_GENERATION + "/" + TASK_TAG_SCRIPT), dcNode, XPathConstants.NODE);
            Script<?> script = createScript(scriptNode);
            GenerationScript gscript = new GenerationScript(script);
            desc.setGenerationScript(gscript);
        }

        return desc;
    }

    private JavaTask createJavaTask(Node process) throws XPathExpressionException, ClassNotFoundException,
            IOException {
        JavaTask desc = new JavaTask();
        //EXECUTABLE CLASS NAME
        desc.setExecutableClassName((String) xpath.compile(TASK_ATTRIBUTE_CLASSNAME).evaluate(process,
                XPathConstants.STRING));
        logger.debug(TASK_ATTRIBUTE_CLASSNAME + " = " + desc.getExecutableClassName());

        //FORKED JAVA TASK PARAMETERS
        boolean fork = "true".equals((String) xpath.evaluate(TASK_ATTRIBUTE_FORK, process,
                XPathConstants.STRING));
        desc.setFork(fork);
        logger.debug(TASK_ATTRIBUTE_FORK + " = " + fork);

        //javaEnvironment
        ForkEnvironment forkEnv = new ForkEnvironment();
        String javaHome = (String) xpath.evaluate(addPrefixes(FORK_TAG_ENVIRONMENT + "/" +
            FORK_ATTRIBUTE_JAVAHOME), process, XPathConstants.STRING);
        if (javaHome != null) {
            forkEnv.setJavaHome(javaHome);
            logger.debug(FORK_TAG_ENVIRONMENT + "/" + FORK_ATTRIBUTE_JAVAHOME + " = " + javaHome);
        }

        String jvmParameters = (String) xpath.evaluate(addPrefixes(FORK_TAG_ENVIRONMENT + "/" +
            FORK_ATTRIBUTE_JVMPARAMETERS), process, XPathConstants.STRING);
        if (jvmParameters != null) {
            forkEnv.setJVMParameters(jvmParameters);
            logger.debug(FORK_TAG_ENVIRONMENT + "/" + FORK_ATTRIBUTE_JVMPARAMETERS + " = " + jvmParameters);
        }
        if (javaHome.length() > 0 || jvmParameters.length() > 0) {
            desc.setForkEnvironment(forkEnv);
        }

        //EXECUTABLE PARAMETERS
        NodeList args = (NodeList) xpath.evaluate(addPrefixes(TASK_TAG_PARAMETERS), process,
                XPathConstants.NODESET);
        if (args != null) {
            for (int i = 0; i < args.getLength(); i++) {
                Node arg = args.item(i);
                String name = (String) xpath
                        .evaluate(GENERIC_INFO_ATTRIBUTE_NAME, arg, XPathConstants.STRING);
                String value = (String) xpath.evaluate(GENERIC_INFO_ATTRIBUTE_VALUE, arg,
                        XPathConstants.STRING);

                if ((name != null) && (value != null)) {
                    desc.getArguments().put(name, value);
                }
            }
        }

        for (Entry<String, String> entry : desc.getArguments().entrySet())
            logger.debug("arg: " + entry.getKey() + " = " + entry.getValue());

        return desc;
    }

    private ProActiveTask createProActiveTask(Node process) throws XPathExpressionException,
            ClassNotFoundException, IOException {
        ProActiveTask desc = new ProActiveTask();

        desc.setExecutableClassName((String) xpath.compile(TASK_ATTRIBUTE_CLASSNAME).evaluate(process,
                XPathConstants.STRING));
        logger.debug(TASK_ATTRIBUTE_CLASSNAME + " = " + desc.getExecutableClassName());

        // TASK NEEDED_NODES
        int neededNodes = ((Double) xpath.evaluate(
                addPrefixes("/job/proActive/" + TASK_ATTRIBUTE_NEEDEDNODES), process, XPathConstants.NUMBER))
                .intValue();
        desc.setNumberOfNodesNeeded(neededNodes);
        logger.debug(TASK_ATTRIBUTE_NEEDEDNODES + " = " + neededNodes);

        NodeList args = (NodeList) xpath.evaluate(addPrefixes(TASK_TAG_PARAMETERS), process,
                XPathConstants.NODESET);
        if (args != null) {
            for (int i = 0; i < args.getLength(); i++) {
                Node arg = args.item(i);
                String name = (String) xpath
                        .evaluate(GENERIC_INFO_ATTRIBUTE_NAME, arg, XPathConstants.STRING);
                String value = (String) xpath.evaluate(GENERIC_INFO_ATTRIBUTE_VALUE, arg,
                        XPathConstants.STRING);

                if ((name != null) && (value != null)) {
                    desc.getArguments().put(name, value);
                }
            }
        }

        for (Entry<String, String> entry : desc.getArguments().entrySet())
            logger.debug("arg: " + entry.getKey() + " = " + entry.getValue());

        return desc;
    }

    private String[] getArguments(Node node) throws XPathExpressionException {
        String[] parameters = null;
        NodeList args = (NodeList) xpath.evaluate(addPrefixes(SCRIPT_TAG_ARGUMENTS), node,
                XPathConstants.NODESET);

        if (args != null && args.getLength() > 0) {
            parameters = new String[args.getLength()];
            for (int i = 0; i < args.getLength(); i++) {
                Node arg = args.item(i);
                String value = (String) xpath.evaluate(GENERIC_INFO_ATTRIBUTE_VALUE, arg,
                        XPathConstants.STRING);
                parameters[i] = value;
            }
        }

        return parameters;
    }

    private Script<?> createScript(Node node) throws XPathExpressionException, InvalidScriptException,
            MalformedURLException {
        // JOB TYPE
        Node fileNode = (Node) xpath.evaluate(addPrefixes(SCRIPT_TAG_FILE), node, XPathConstants.NODE);
        Node codeNode = (Node) xpath.evaluate(addPrefixes(SCRIPT_TAG_CODE), node, XPathConstants.NODE);

        if (fileNode != null) {
            // file
            String url = (String) xpath.evaluate(SCRIPT_ATTRIBUTE_URL, fileNode, XPathConstants.STRING);

            if ((url != null) && (!url.equals(""))) {
                logger.debug(url);

                return new SimpleScript(new URL(url), getArguments(fileNode));
            }

            String path = (String) xpath.evaluate(SCRIPT_ATTRIBUTE_PATH, fileNode, XPathConstants.STRING);

            if ((path != null) && (!path.equals(""))) {
                logger.debug(path);

                return new SimpleScript(new File(path), getArguments(fileNode));
            }
        } else {
            // code
            String engine = (String) xpath.evaluate(SCRIPT_ATTRIBUTE_LANGUAGE, codeNode,
                    XPathConstants.STRING);

            if (((engine != null) && (!engine.equals(""))) && (node.getTextContent() != null)) {
                String script = node.getTextContent();

                try {
                    return new SimpleScript(script, engine);
                } catch (InvalidScriptException e) {
                    logger.debug("", e);
                }
            }
        }

        // schema should check this...?
        throw new InvalidScriptException("The script is not recognized");
    }

    private SelectionScript createSelectionScript(Node node) throws XPathExpressionException,
            InvalidScriptException, MalformedURLException {
        Script<?> script = createScript(node);
        //is the script static or dynamic
        boolean isStatic = "static".equals((String) xpath.evaluate(SCRIPT_ATTRIBUTE_TYPE, node,
                XPathConstants.STRING));
        logger.debug("selection script dynamic = " + !isStatic);
        return new SelectionScript(script, !isStatic);
    }

    private static String addPrefixes(String unprefixedPath) {
        if ((JOB_PREFIX != null) || (JOB_PREFIX.length() > 0)) {
            String pr = JOB_PREFIX + ":";
            unprefixedPath = " " + unprefixedPath + " ";

            StringTokenizer st = new StringTokenizer(unprefixedPath, "/");
            StringBuilder sb = new StringBuilder();
            boolean slash_start = false;
            boolean in_the_middle = false;

            while (st.hasMoreElements()) {
                String token = st.nextToken().trim();

                if (token.length() > 0) {
                    if (in_the_middle || slash_start) {
                        if (!token.startsWith("@")) {
                            sb.append("/" + pr + token);
                        } else {
                            sb.append("/" + token);
                        }
                    } else {
                        if (!token.startsWith("@")) {
                            sb.append(pr + token);
                        } else {
                            sb.append(token);
                        }

                        in_the_middle = true;
                    }
                } else {
                    slash_start = true;
                }
            }

            return sb.toString();
        }

        return unprefixedPath;
    }

    private class ValidatingErrorHandler implements ErrorHandler {
        private int mistakes = 0;
        private StringBuilder mistakesStack = null;

        /**
         * Create a new instance of ValidatingErrorHandler.
         *
         */
        public ValidatingErrorHandler() {
            mistakesStack = new StringBuilder();
        }

        /**
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        public void error(SAXParseException exception) throws SAXException {
            appendAndPrintMessage("ERROR:" + exception.getMessage() + " at line " +
                exception.getLineNumber() + ", column " + exception.getColumnNumber());
        }

        /**
         * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        public void fatalError(SAXParseException exception) throws SAXException {
            appendAndPrintMessage("ERROR:" + exception.getMessage() + " at line " +
                exception.getLineNumber() + ", column " + exception.getColumnNumber());
        }

        /**
         * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        public void warning(SAXParseException exception) throws SAXException {
            appendAndPrintMessage("WARNING:" + exception.getMessage() + " at line " +
                exception.getLineNumber() + ", column " + exception.getColumnNumber());
        }

        private void appendAndPrintMessage(String msg) {
            mistakesStack.append(msg + "\n");
            System.err.println(msg);
            mistakes++;
        }
    }

    private class SchedulerNamespaceContext implements NamespaceContext {
        /**
         * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
         */
        public String getNamespaceURI(String prefix) {
            if ((prefix == null) || (prefix.length() == 0)) {
                throw new NullPointerException("Null prefix");
            } else if (prefix.equals(JOB_PREFIX)) {
                return JOB_NAMESPACE;
            } else if ("xml".equals(prefix)) {
                return XMLConstants.XML_NS_URI;
            }

            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
         * This method isn't necessary for XPath processing.
         */
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        /**
         * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
         * This method isn't necessary for XPath processing either.
         */
        public Iterator<?> getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }

}
