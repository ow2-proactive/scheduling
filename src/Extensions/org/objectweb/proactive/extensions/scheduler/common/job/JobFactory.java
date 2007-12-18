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
package org.objectweb.proactive.extensions.scheduler.common.job;

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
import java.util.Map.Entry;
import java.util.StringTokenizer;

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

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.objectweb.proactive.extensions.scheduler.common.exception.JobCreationException;
import org.objectweb.proactive.extensions.scheduler.common.exception.UserException;
import org.objectweb.proactive.extensions.scheduler.common.scripting.GenerationScript;
import org.objectweb.proactive.extensions.scheduler.common.scripting.InvalidScriptException;
import org.objectweb.proactive.extensions.scheduler.common.scripting.Script;
import org.objectweb.proactive.extensions.scheduler.common.scripting.SelectionScript;
import org.objectweb.proactive.extensions.scheduler.common.scripting.SimpleScript;
import org.objectweb.proactive.extensions.scheduler.common.task.JavaTask;
import org.objectweb.proactive.extensions.scheduler.common.task.NativeTask;
import org.objectweb.proactive.extensions.scheduler.common.task.ProActiveTask;
import org.objectweb.proactive.extensions.scheduler.common.task.ResultPreview;
import org.objectweb.proactive.extensions.scheduler.common.task.Task;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.ProActiveExecutable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class JobFactory {
    public static final String SCHEMA_LOCATION = "/org/objectweb/proactive/extensions/scheduler/common/xml/schemas/jobdescriptor/0.9/schedulerjob.rng";
    public static final String STYLESHEET_LOCATION = "/org/objectweb/proactive/extensions/scheduler/common/xml/stylesheets/variables.xsl";
    public static final String JOB_NAMESPACE = "urn:proactive:jobdescriptor:0.9";
    public static final String JOB_PREFIX = "js";
    private static XPath xpath;

    /**
     * Singleton Pattern
     */
    private static JobFactory factory = null;

    private JobFactory() {
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new SchedulerNamespaceContext());
    }

    public static JobFactory getFactory() {
        if (factory == null) {
            factory = new JobFactory();
        }
        return factory;
    }

    /**
     * Creates a job using the given job descriptor
     * @param filePath the path to a job descriptor
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
            throw new JobCreationException("Exception occured during Job creation", e);
        }
        return job;
    }

    /**
     * Validate the given job descriptor using the internal RELAX_NG Schema
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
            System.exit(1);
        }
    }

    /**
     * Parse the given octet stream to a XML DOM, and transform variable definitions using a stylesheet
     * @param input
     * @return
     */
    private Node transformVariablesAndGetDOM(InputStream input) throws ParserConfigurationException,
            SAXException, IOException {
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
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        DOMResult result = new DOMResult();

        try {
            transformer.transform(domSource, result);
        } catch (TransformerException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return result.getNode();
    }

    @SuppressWarnings("unchecked")
    private Job createJob(Node rootNode) throws XPathExpressionException, InvalidScriptException,
            SAXException, ClassNotFoundException, IOException, UserException {
        Job job = null;

        // JOB
        XPathExpression exp = xpath.compile(addPrefixes("/job"));

        Node jobNode = (Node) exp.evaluate(rootNode, XPathConstants.NODE);

        JobType jt = null;

        // JOB TYPE
        Node tfNode = (Node) xpath.evaluate(addPrefixes("taskFlow"), jobNode, XPathConstants.NODE);
        if (tfNode != null) {
            jt = JobType.TASKSFLOW;
        }
        Node paNode = (Node) xpath.evaluate(addPrefixes("proActive"), jobNode, XPathConstants.NODE);
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

            //	TODO	job = new ParameterSweepingJob();
            //			ParameterSweepingJob jobPS = (ParameterSweepingJob) job;
            default:
                job = new TaskFlowJob();
        }
        // JOB NAME
        job.setName((String) xpath.evaluate("@name", jobNode, XPathConstants.STRING));
        System.out.println("Job : " + job.getName());
        // JOB PRIORITY
        String prio = xpath.evaluate("@priority", jobNode);
        if (!"".equals(prio)) {
            job.setPriority(JobPriority.findPriority(prio));
        } else {
            job.setPriority(JobPriority.NORMAL);
        }

        // JOB CANCEL ON EXCEPTION
        String cancel = xpath.evaluate("@cancelOnException", jobNode);
        if (!"".equals(cancel)) {
            job.setCancelOnError(Boolean.parseBoolean(cancel));
        } else {
            job.setCancelOnError(false);
        }

        // JOB LOG FILE
        String logFile = xpath.evaluate("@logFile", jobNode);
        if (!"".equals(logFile)) {
            job.setLogFile(logFile);
        }

        // JOB DESCRIPTION
        Object description = xpath.evaluate(addPrefixes("/job/description"), rootNode, XPathConstants.STRING);

        if (description != null) {
            System.out.println("Job description = " + description);
            job.setDescription((String) description);
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
                list = (NodeList) xpath.evaluate(addPrefixes("taskFlow/task"), jobNode,
                        XPathConstants.NODESET);
                break;
            default:
                list = (NodeList) xpath.evaluate(addPrefixes("proActive/task"), jobNode,
                        XPathConstants.NODESET);
        }
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                Node taskNode = list.item(i);
                Task task = null;

                // TASK PROCESS
                Node process = (Node) xpath.evaluate(addPrefixes("javaExecutable"), taskNode,
                        XPathConstants.NODE);
                if (process != null) { // JAVA TASK
                    task = createJavaTask(process);
                } else if ((process = (Node) xpath.evaluate(addPrefixes("nativeExecutable"), taskNode,
                        XPathConstants.NODE)) != null) { // NATIVE TASK
                    task = createNativeTask(process);
                } else if ((process = (Node) xpath.evaluate(addPrefixes("proActiveExecutable"), taskNode,
                        XPathConstants.NODE)) != null) { // APPLICATION TASK
                    task = createProActiveTask(process);
                } else {
                    throw new RuntimeException("Unknow process !!");
                }

                task = createTask(taskNode, task);

                switch (job.getType()) {
                    case PROACTIVE:
                        ((ProActiveJob) job).setTask((ProActiveTask) task);
                        break;
                    default:
                        ((TaskFlowJob) job).addTask(task);
                }

                // TASK DEPENDS
                NodeList refList = (NodeList) xpath.evaluate(addPrefixes("depends/task/@ref"), taskNode,
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
                //task.getKey().setJobId(job.getId());
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

    @SuppressWarnings("unchecked")
    private Task createTask(Node taskNode, Task task) throws XPathExpressionException,
            ClassNotFoundException, InvalidScriptException, MalformedURLException {
        // TASK NAME
        task.setName((String) xpath.evaluate("@name", taskNode, XPathConstants.STRING));
        System.out.println("name = " + task.getName());

        // TASK DESCRIPTION
        task.setDescription((String) xpath.evaluate(addPrefixes("description"), taskNode,
                XPathConstants.STRING));
        System.out.println("desc = " + task.getDescription());

        // TASK RESULT DESCRIPTION
        String previewClassName = (String) xpath.evaluate("@resultPreviewClass", taskNode,
                XPathConstants.STRING);
        if (!previewClassName.equals("")) {
            System.out.println("Preview class = " + previewClassName);
            Class<? extends ResultPreview> descriptorClass = (Class<? extends ResultPreview>) Class
                    .forName(previewClassName);
            task.setResultPreview(descriptorClass);
        }

        // TASK PRECIOUS RESULT
        task.setPreciousResult(((String) xpath.evaluate("@preciousResult", taskNode, XPathConstants.STRING))
                .equals("true"));
        System.out.println("final = " + task.isPreciousResult());

        // TASK RETRIES
        String rerunnable = (String) xpath.evaluate("@retries", taskNode, XPathConstants.STRING);
        if (rerunnable != "") {
            task.setRerunnable(Integer.parseInt(rerunnable));
        } else {
            task.setRerunnable(1);
        }
        System.out.println("reRun = " + task.getRerunnable());

        // TASK VERIF
        Node verifNode = (Node) xpath
                .evaluate(addPrefixes("selection/script"), taskNode, XPathConstants.NODE);
        if (verifNode != null) {
            task.setSelectionScript(createSelectionScript(verifNode));
        }

        // TASK PRE
        Node preNode = (Node) xpath.evaluate(addPrefixes("pre/script"), taskNode, XPathConstants.NODE);
        if (preNode != null) {
            System.out.println("PRE");
            task.setPreScript(createScript(preNode));
        }

        // TASK POST
        Node postNode = (Node) xpath.evaluate(addPrefixes("post/script"), taskNode, XPathConstants.NODE);
        if (postNode != null) {
            System.out.println("POST");
            task.setPostScript(createScript(postNode));
        }
        return task;
    }

    private Task createNativeTask(Node executable) throws XPathExpressionException, ClassNotFoundException,
            IOException, InvalidScriptException {
        Node scNode = (Node) xpath.evaluate(addPrefixes("staticCommand"), executable, XPathConstants.NODE);
        Node dcNode = (Node) xpath.evaluate(addPrefixes("dynamicCommand"), executable, XPathConstants.NODE);
        NativeTask desc = new NativeTask();
        if (scNode != null) {
            // static command
            String cmd = (String) xpath.evaluate("@value", scNode, XPathConstants.STRING);
            NodeList args = (NodeList) xpath.evaluate(addPrefixes("arguments/argument"), scNode,
                    XPathConstants.NODESET);
            if (args != null) {
                for (int i = 0; i < args.getLength(); i++) {
                    Node arg = args.item(i);
                    String value = (String) xpath.evaluate("@value", arg, XPathConstants.STRING);

                    if (value != null) {
                        cmd += (" " + value);
                    }
                }
            }
            desc.setCommandLine(cmd);
        } else {
            // dynamic command
            Node scriptNode = (Node) xpath.evaluate(addPrefixes("generation/script"), dcNode,
                    XPathConstants.NODE);
            Script<?> script = createScript(scriptNode);
            GenerationScript gscript = new GenerationScript(script);
            desc.setGenerationScript(gscript);
        }

        return desc;
    }

    @SuppressWarnings("unchecked")
    private JavaTask createJavaTask(Node process) throws XPathExpressionException, ClassNotFoundException,
            IOException {
        JavaTask desc = new JavaTask();
        desc.setTaskClass((Class<JavaExecutable>) Class.forName((String) xpath.compile("@class").evaluate(
                process, XPathConstants.STRING)));
        // TODO Verify that class extends Task
        System.out.println("task = " + desc.getTaskClass().getCanonicalName());
        NodeList args = (NodeList) xpath.evaluate(addPrefixes("parameters/parameter"), process,
                XPathConstants.NODESET);

        if (args != null) {
            for (int i = 0; i < args.getLength(); i++) {
                Node arg = args.item(i);
                String name = (String) xpath.evaluate("@name", arg, XPathConstants.STRING);
                String value = (String) xpath.evaluate("@value", arg, XPathConstants.STRING);

                if ((name != null) && (value != null)) {
                    desc.getArguments().put(name, value);
                }
            }
        }

        for (Entry<String, Object> entry : desc.getArguments().entrySet())
            System.out.println("arg: " + entry.getKey() + " = " + entry.getValue());

        return desc;
    }

    @SuppressWarnings("unchecked")
    private ProActiveTask createProActiveTask(Node process) throws XPathExpressionException,
            ClassNotFoundException, IOException {
        ProActiveTask desc = new ProActiveTask();
        desc.setTaskClass((Class<ProActiveExecutable>) Class.forName((String) xpath.compile("@class")
                .evaluate(process, XPathConstants.STRING)));
        // TODO Verify that class extends Task
        System.out.println("task = " + desc.getTaskClass().getCanonicalName());

        NodeList args = (NodeList) xpath.evaluate(addPrefixes("parameters/parameter"), process,
                XPathConstants.NODESET);

        if (args != null) {
            for (int i = 0; i < args.getLength(); i++) {
                Node arg = args.item(i);
                String name = (String) xpath.evaluate("@name", arg, XPathConstants.STRING);
                String value = (String) xpath.evaluate("@value", arg, XPathConstants.STRING);

                // TASK NEEDED_NODES
                int neededNodes = ((Double) xpath.evaluate(addPrefixes("/job/proActive/@neededNodes"), arg,
                        XPathConstants.NUMBER)).intValue();
                desc.setNumberOfNodesNeeded(neededNodes);

                if ((name != null) && (value != null)) {
                    desc.getArguments().put(name, value);
                }
            }
        }

        for (Entry<String, Object> entry : desc.getArguments().entrySet())
            System.out.println("arg: " + entry.getKey() + " = " + entry.getValue());

        return desc;
    }

    private String[] getArguments(Node node) throws XPathExpressionException {
        String[] parameters = null;
        NodeList args = (NodeList) xpath.evaluate(addPrefixes("arguments/argument"), node,
                XPathConstants.NODESET);

        if (args != null) {
            parameters = new String[args.getLength()];

            for (int i = 0; i < args.getLength(); i++) {
                Node arg = args.item(i);
                String value = (String) xpath.evaluate("@value", arg, XPathConstants.STRING);
                parameters[i] = value;
            }
        }

        return parameters;
    }

    private Script<?> createScript(Node node) throws XPathExpressionException, InvalidScriptException,
            MalformedURLException {
        // JOB TYPE
        Node fileNode = (Node) xpath.evaluate(addPrefixes("file"), node, XPathConstants.NODE);
        Node codeNode = (Node) xpath.evaluate(addPrefixes("code"), node, XPathConstants.NODE);

        if (fileNode != null) {
            // file
            String url = (String) xpath.evaluate("@url", fileNode, XPathConstants.STRING);

            if ((url != null) && (!url.equals(""))) {
                System.out.println(url);

                return new SimpleScript(new URL(url), getArguments(fileNode));
            }

            String path = (String) xpath.evaluate("@path", fileNode, XPathConstants.STRING);

            if ((path != null) && (!path.equals(""))) {
                System.out.println(path);

                return new SimpleScript(new File(path), getArguments(fileNode));
            }
        } else {
            // code
            String engine = (String) xpath.evaluate("@language", codeNode, XPathConstants.STRING);

            if (((engine != null) && (!engine.equals(""))) && (node.getTextContent() != null)) {
                String script = node.getTextContent();

                try {
                    return new SimpleScript(script, engine);
                } catch (InvalidScriptException e) {
                    e.printStackTrace();
                }
            }
        }

        // schema should check this...?
        throw new InvalidScriptException("The script is not recognized");
    }

    private SelectionScript createSelectionScript(Node node) throws XPathExpressionException,
            InvalidScriptException, MalformedURLException {
        Script<?> script = createScript(node);

        return new SelectionScript(script, true);
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

        public ValidatingErrorHandler() {
        }

        public void error(SAXParseException exception) throws SAXException {
            System.err.println("ERROR:" + exception.getMessage() + " at line " + exception.getLineNumber() +
                ", column " + exception.getColumnNumber());
            mistakes++;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            System.err.println("ERROR:" + exception.getMessage() + " at line " + exception.getLineNumber() +
                ", column " + exception.getColumnNumber());
            mistakes++;
        }

        public void warning(SAXParseException exception) throws SAXException {
            System.err.println("WARNING:" + exception.getMessage() + " at line " + exception.getLineNumber() +
                ", column " + exception.getColumnNumber());
        }
    }

    private class SchedulerNamespaceContext implements NamespaceContext {
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

        // This method isn't necessary for XPath processing.
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.
        public Iterator<?> getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }
}
