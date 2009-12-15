/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.util.RegexpMatcher;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.utils.Tools;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * JobFactory_stax provide an implementation of the JobFactory using StAX
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 *
 * $Id$
 */
public class JobFactory_stax extends JobFactory {

    public static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.FACTORY);
    /** Location of the schema used to parse job descriptor. */
    public static final String SCHEMA_LOCATION = "/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/2.0/schedulerjob.rng";
    /** Variables styleScheet location. */
    public static final String STYLESHEET_LOCATION = "/org/ow2/proactive/scheduler/common/xml/stylesheets/variables.xsl";
    /** Job name space. */
    public static final String JOB_NAMESPACE = "urn:proactive:jobdescriptor:2.0";
    /** Job prefix. */
    public static final String JOB_PREFIX = "js";
    /** Variables pattern definition */
    //private static final Pattern variablesPattern = Pattern.compile(".*\\$\\{[^\\}]+\\}.*", Pattern.DOTALL);
    private static final String variablesPattern = "\\$\\{[^\\}]+\\}";

    //Are define only the needed tags. If more are needed, just create them.
    //JOBS
    private static final String ELEMENT_JOB = "job";
    private static final String ELEMENT_TASKFLOW = "taskFlow";
    private static final String ATTRIBUTE_JOB_PRIORITY = "priority";
    private static final String ATTRIBUTE_JOB_PROJECTNAME = "projectName";
    private static final String ATTRIBUTE_JOB_LOGFILE = "logFile";
    private static final String ELEMENT_JOB_CLASSPATHES = "jobClasspath";
    private static final String ELEMENT_JOB_PATH_ELEMENT = "pathElement";
    //COMMON
    private static final String ATTRIBUTE_COMMON_CANCELJOBONERROR = "cancelJobOnError";
    private static final String ATTRIBUTE_COMMON_RESTARTTASKONERROR = "restartTaskOnError";
    private static final String ATTRIBUTE_COMMON_MAXNUMBEROFEXECUTION = "maxNumberOfExecution";
    private static final String ATTRIBUTE_COMMON_NAME = "name";
    private static final String ELEMENT_COMMON_DESCRIPTION = "description";
    private static final String ELEMENT_COMMON_GENERIC_INFORMATION = "genericInformation";
    private static final String ELEMENT_COMMON_INFO = "info";
    //VARIABLES
    private static final String ELEMENT_VARIABLES = "variables";
    private static final String ELEMENT_VARIABLE = "variable";
    //TASKS
    private static final String ELEMENT_TASK = "task";
    private static final String ELEMENT_JAVA_EXECUTABLE = "javaExecutable";
    private static final String ELEMENT_NATIVE_EXECUTABLE = "nativeExecutable";
    private static final String ELEMENT_TASK_DEPENDENCES = "depends";
    private static final String ELEMENT_TASK_DEPENDENCES_TASK = "task";
    private static final String ATTRIBUTE_TASK_RESULTPREVIEW = "resultPreviewClass";
    private static final String ATTRIBUTE_TASK_PRECIOUSRESULT = "preciousResult";
    private static final String ATTRIBUTE_TASK_CLASSNAME = "class";
    private static final String ELEMENT_TASK_PARAMETER = "parameter";
    private static final String ATTRIBUTE_TASK_WALLTIME = "walltime";
    private static final String ATTRIBUTE_TASK_FORK = "fork";

    //NATIVE TASK ATTRIBUTES
    private static final String ATTRIBUTE_TASK_NB_NODES = "numberOfNodes";
    private static final String ATTRIBUTE_TASK_COMMAND_VALUE = "value";
    private static final String ATTRIBUTE_TASK_WORKDING_DIR = "workingDir";

    //SCRIPTS
    private static final String ELEMENT_SCRIPT_SELECTION = "selection";
    private static final String ELEMENT_SCRIPT_PRE = "pre";
    private static final String ELEMENT_SCRIPT_POST = "post";
    private static final String ELEMENT_SCRIPT_CLEANING = "cleaning";
    private static final String ELEMENT_SCRIPT_SCRIPT = "script";
    private static final String ELEMENT_SCRIPT_STATICCOMMAND = "staticCommand";
    private static final String ELEMENT_SCRIPT_DYNAMICCOMMAND = "dynamicCommand";
    private static final String ELEMENT_SCRIPT_ARGUMENTS = "arguments";
    private static final String ELEMENT_SCRIPT_ARGUMENT = "argument";
    private static final String ELEMENT_SCRIPT_FILE = "file";
    private static final String ELEMENT_SCRIPT_CODE = "code";
    private static final String ATTRIBUTE_SCRIPT_URL = "url";
    //FORK ENVIRONMENT
    private static final String ELEMENT_FORK_ENVIRONMENT = "forkEnvironment";
    private static final String ATTRIBUTE_FORK_JAVAHOME = "javaHome";
    private static final String ATTRIBUTE_FORK_JVMPARAMETERS = "jvmParameters";

    //DATASPACES
    private static final String ELEMENT_DS_INPUTSPACE = "inputSpace";
    private static final String ELEMENT_DS_OUTPUTSPACE = "outputSpace";
    private static final String ELEMENT_DS_INPUTFILES = "inputFiles";
    private static final String ELEMENT_DS_OUTPUTFILES = "outputFiles";
    private static final String ELEMENT_DS_FILES = "files";
    private static final String ATTRIBUTE_DS_INCLUDES = "includes";
    private static final String ATTRIBUTE_DS_EXCLUDES = "excludes";
    private static final String ATTRIBUTE_DS_ACCESSMODE = "accessMode";

    /** XML input factory */
    private XMLInputFactory xmlif = null;
    /** Instance variables of the XML files. */
    private HashMap<String, String> variables = new HashMap<String, String>();
    /** Job instance : to be sent to the user once created. */
    private Job job = null;
    /** Instance that will temporary store the dependences between tasks */
    private HashMap<String, ArrayList<String>> dependences = null;

    /**
     * Create a new instance of JobFactory_stax.
     */
    JobFactory_stax() {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
        //System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
        xmlif = XMLInputFactory.newInstance();
        xmlif.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
        xmlif.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.TRUE);
        xmlif.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
        xmlif.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.factories.JobFactory#createJob(java.lang.String)
     */
    @Override
    public Job createJob(String filePath) throws JobCreationException {
        clean();
        try {
            //Check if the file exist
            File f = new File(filePath);
            return createJob(f);
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.factories.JobFactory#createJob(java.net.URI)
     */
    @Override
    public Job createJob(URI filePath) throws JobCreationException {
        clean();
        try {
            //Check if the file exist
            File f = new File(filePath);
            return createJob(f);
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    private Job createJob(File f) throws JobCreationException {
        try {
            //Check if the file exist
            if (!f.exists()) {
                throw new FileNotFoundException("This file has not been found : " + f.getAbsolutePath());
            }
            //validate content using the proper XML schema
            validate(f);
            XMLStreamReader xmlsr = xmlif.createXMLStreamReader(new FileReader(f));
            //Create the job starting at the first cursor position of the XML Stream reader
            createJob(xmlsr);
            //Close the stream
            xmlsr.close();
            //make dependences
            makeDependences();
            logger.info("Job successfully created !");
            //debug mode only
            displayJobInfo();
            return job;
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    private void clean() {
        this.variables = new HashMap<String, String>();
        this.job = null;
        this.dependences = null;
    }

    /**
     * Validate the given job descriptor using the internal RELAX_NG Schema.
     *
     * @param file the file to validate
     */
    private void validate(File file) throws URISyntaxException, VerifierConfigurationException, SAXException,
            IOException {
        // We use sun multi validator (msv)
        VerifierFactory vfactory = new com.sun.msv.verifier.jarv.TheFactoryImpl();
        InputStream schemaStream = this.getClass().getResourceAsStream(SCHEMA_LOCATION);
        Schema schema = vfactory.compileSchema(schemaStream);
        Verifier verifier = schema.newVerifier();
        ValidatingErrorHandler veh = new ValidatingErrorHandler();
        verifier.setErrorHandler(veh);
        try {
            verifier.verify(file);
        } catch (SAXException e) {
            //nothing to do, check after
        }
        if (veh.mistakes > 0) {
            throw new SAXException(veh.mistakesStack.toString());
        }
    }

    /**
     * Start parsing and creating the job.
     *
     * @throws JobCreationException if an error occurred during job creation process.
     */
    private void createJob(XMLStreamReader cursorRoot) throws JobCreationException {
        //start parsing
        try {
            int eventType;
            while (cursorRoot.hasNext()) {
                eventType = cursorRoot.next();
                if (eventType == XMLEvent.START_ELEMENT) {
                    String current = cursorRoot.getLocalName();
                    if (current.equals(JobFactory_stax.ELEMENT_JOB)) {
                        //first tag of the job.
                        createAndFillJob(cursorRoot);
                    } else if (current.equals(JobFactory_stax.ELEMENT_TASK)) {
                        //once here, the job instance has been created
                        fillJobWithTasks(cursorRoot);
                    }
                }
            }
            //as the job attributes are declared before variable evaluation, 
            //replace variables in this attributes after job creation (after variables evaluation)
            job.setName(replace(job.getName()));
            job.setProjectName(replace(job.getProjectName()));
            job.setLogFile(replace(job.getLogFile()));
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Create the real job and fill it with its property.
     * Leave the method at the first tag that define the real type of job.
     *
     * @param cursorJob the streamReader with the cursor on the job element.
     * @throws JobCreationException if an exception occurs during job creation.
     */
    private void createAndFillJob(XMLStreamReader cursorJob) throws JobCreationException {
        //create a job that will just temporary store the common properties of the job
        Job jtmp = new Job() {

            /**  */
            private static final long serialVersionUID = 200;

            @Override
            public JobId getId() {
                throw new RuntimeException("Not Available !");
            }

            @Override
            public JobType getType() {
                throw new RuntimeException("Not Available !");
            }
        };
        //parse job attributes and fill the temporary one
        int attrLen = cursorJob.getAttributeCount();
        for (int i = 0; i < attrLen; i++) {
            String attrName = cursorJob.getAttributeLocalName(i);
            if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_NAME)) {
                jtmp.setName(cursorJob.getAttributeValue(i));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_JOB_PRIORITY)) {
                jtmp.setPriority(JobPriority.findPriority(replace(cursorJob.getAttributeValue(i))));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_CANCELJOBONERROR)) {
                jtmp.setCancelJobOnError(Boolean.parseBoolean(replace(cursorJob.getAttributeValue(i))));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_JOB_LOGFILE)) {
                //don't replace() here it is done at the end of the job
                jtmp.setLogFile(cursorJob.getAttributeValue(i));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_RESTARTTASKONERROR)) {
                jtmp.setRestartTaskOnError(RestartMode.getMode(replace(cursorJob.getAttributeValue(i))));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_MAXNUMBEROFEXECUTION)) {
                jtmp.setMaxNumberOfExecution(Integer.parseInt(replace(cursorJob.getAttributeValue(i))));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_JOB_PROJECTNAME)) {
                //don't replace() here it is done at the end of the job
                jtmp.setProjectName(cursorJob.getAttributeValue(i));
            }
        }
        //parse job elements and fill the temporary one
        try {
            int eventType;
            boolean continu = true;
            while (continu && cursorJob.hasNext()) {
                eventType = cursorJob.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String current = cursorJob.getLocalName();
                        if (current.equals(JobFactory_stax.ELEMENT_VARIABLES)) {
                            createVariables(cursorJob);
                        } else if (current.equals(JobFactory_stax.ELEMENT_COMMON_GENERIC_INFORMATION)) {
                            jtmp.setGenericInformations(getGenericInformations(cursorJob));
                        } else if (current.equals(JobFactory_stax.ELEMENT_JOB_CLASSPATHES)) {
                            jtmp.getEnvironment().setJobClasspath(getClasspath(cursorJob));
                        } else if (current.equals(JobFactory_stax.ELEMENT_COMMON_DESCRIPTION)) {
                            jtmp.setDescription(getDescription(cursorJob));
                        } else if (current.equals(JobFactory_stax.ELEMENT_DS_INPUTSPACE)) {
                            jtmp.setInputSpace(getIOSpace(cursorJob));
                        } else if (current.equals(JobFactory_stax.ELEMENT_DS_OUTPUTSPACE)) {
                            jtmp.setOutputSpace(getIOSpace(cursorJob));
                        } else if (current.equals(JobFactory_stax.ELEMENT_TASKFLOW)) {
                            job = new TaskFlowJob();
                            continu = false;
                        }
                        break;
                }
            }
            //if this point is reached, fill the real job using the temporary one
            job.setDescription(jtmp.getDescription());
            job.setEnvironment(jtmp.getEnvironment());
            job.setLogFile(jtmp.getLogFile());
            job.setName(jtmp.getName());
            job.setPriority(jtmp.getPriority());
            job.setProjectName(jtmp.getProjectName());
            job.setCancelJobOnError(jtmp.isCancelJobOnError());
            job.setRestartTaskOnError(jtmp.getRestartTaskOnError());
            job.setMaxNumberOfExecution(jtmp.getMaxNumberOfExecution());
            job.setGenericInformations(jtmp.getGenericInformations());
            job.setInputSpace(jtmp.getInputSpace());
            job.setOutputSpace(jtmp.getOutputSpace());
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Create the map of variables found in this document.
     * Leave the method with the cursor at the end of 'ELEMENT_VARIABLES' tag
     *
     * @param cursorVariables the streamReader with the cursor on the 'ELEMENT_VARIABLES' tag.
     */
    private void createVariables(XMLStreamReader cursorVariables) throws JobCreationException {
        try {
            int eventType;
            while (cursorVariables.hasNext()) {
                eventType = cursorVariables.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (cursorVariables.getLocalName().equals(JobFactory_stax.ELEMENT_VARIABLE)) {
                            variables.put(cursorVariables.getAttributeValue(0), replace(cursorVariables
                                    .getAttributeValue(1)));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorVariables.getLocalName().equals(JobFactory_stax.ELEMENT_VARIABLES)) {
                            return;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Get the defined generic informations of the entity.
     * Leave the method at the end of 'ELEMENT_COMMON_GENERIC_INFORMATION' tag.
     *
     * @param cursorInfo the streamReader with the cursor on the 'ELEMENT_COMMON_GENERIC_INFORMATION' tag.
     * @return the list of generic information as a hashMap.
     */
    private HashMap<String, String> getGenericInformations(XMLStreamReader cursorInfo)
            throws JobCreationException {
        HashMap<String, String> infos = new HashMap<String, String>();
        try {
            int eventType;
            while (cursorInfo.hasNext()) {
                eventType = cursorInfo.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (cursorInfo.getLocalName().equals(JobFactory_stax.ELEMENT_COMMON_INFO)) {
                            infos.put(cursorInfo.getAttributeValue(0), replace(cursorInfo
                                    .getAttributeValue(1)));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorInfo.getLocalName().equals(
                                JobFactory_stax.ELEMENT_COMMON_GENERIC_INFORMATION)) {
                            return infos;
                        }
                        break;
                }
            }
            return infos;
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Get the defined classPath.
     * Leave the method at the end of 'ELEMENT_JOB_CLASSPATHES' tag.
     *
     * @param cursorClasspath the streamReader with the cursor on the 'ELEMENT_JOB_CLASSPATHES' tag.
     * @return the list of classPath entries as an array of string.
     */
    private String[] getClasspath(XMLStreamReader cursorClasspath) throws JobCreationException {
        try {
            ArrayList<String> pathEntries = new ArrayList<String>();
            int eventType;
            while (cursorClasspath.hasNext()) {
                eventType = cursorClasspath.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (cursorClasspath.getLocalName().equals(JobFactory_stax.ELEMENT_JOB_PATH_ELEMENT)) {
                            pathEntries.add(replace(cursorClasspath.getAttributeValue(0)));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorClasspath.getLocalName().equals(JobFactory_stax.ELEMENT_JOB_CLASSPATHES)) {
                            return pathEntries.toArray(new String[] {});
                        }
                        break;
                }
            }
            return pathEntries.toArray(new String[] {});
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Get the description of the entity.
     * Leave the method with the cursor at the end of 'ELEMENT_COMMON_DESCRIPTION' tag.
     *
     * @param cursorVariables the streamReader with the cursor on the 'ELEMENT_COMMON_DESCRIPTION' tag.
     * @return the description between the tags.
     */
    private String getDescription(XMLStreamReader cursorVariables) throws JobCreationException {
        try {
            String description = "";
            //if description tag exists, then we have a characters event next.
            int eventType = cursorVariables.next();
            if (eventType == XMLEvent.CHARACTERS) {
                description = replace(cursorVariables.getText());
            }
            //go to the description END_ELEMENT
            while (cursorVariables.next() != XMLEvent.END_ELEMENT)
                ;
            return description;
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Get the INPUT/OUTPUT space URL of the job.
     * Leave the method with the cursor at the end of 'ELEMENT_DS_INPUT/OUTPUTSPACE' tag.
     *
     * @param cursorVariables the streamReader with the cursor on the 'ELEMENT_DS_INPUT/OUTPUTSPACE' tag.
     * @return the INPUT/OUTPUT space URL of this tag.
     */
    private String getIOSpace(XMLStreamReader cursorVariables) throws JobCreationException {
        try {
            String url = replace(cursorVariables.getAttributeValue(0));
            //go to the END_ELEMENT
            while (cursorVariables.next() != XMLEvent.END_ELEMENT)
                ;
            return url;
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Fill the created Job with coming tasks..
     * Leave the method with the cursor at the end of the file (nothing more has to be parsed).
     *
     * @param cursorTask the streamReader with the cursor on the first 'ELEMENT_TASK' tag.
     */
    private void fillJobWithTasks(XMLStreamReader cursorTask) throws JobCreationException {
        try {
            int eventType = -1;
            while (cursorTask.hasNext()) {
                //if use to keep the cursor on the task tag for the first loop
                if (eventType == -1) {
                    eventType = cursorTask.getEventType();
                } else {
                    eventType = cursorTask.next();
                }
                if (eventType == XMLEvent.START_ELEMENT &&
                    cursorTask.getLocalName().equals(JobFactory_stax.ELEMENT_TASK)) {
                    Task t;
                    switch (job.getType()) {
                        case TASKSFLOW:
                            //create new task
                            t = createTask(cursorTask);
                            //add task to the job
                            ((TaskFlowJob) job).addTask(t);
                            break;
                        case PARAMETER_SWEEPING:
                            throw new RuntimeException("Job Parameter Sweeping is not yet implemented !");
                    }
                }
            }
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Create the task that is at the given cursorTask.
     * Leave the method with the cursor at the end of 'ELEMENT_TASK' tag.
     *
     * @param cursorTask the streamReader with the cursor on the 'ELEMENT_TASK' tag.
     * @return The newly created task that can be any type.
     */
    private Task createTask(XMLStreamReader cursorTask) throws JobCreationException {
        return createTask(cursorTask, null);
    }

    /**
     * Fill the given task by the information that are at the given cursorTask.
     * Leave the method with the cursor at the end of 'ELEMENT_TASK' tag.
     *
     * @param cursorTask the streamReader with the cursor on the 'ELEMENT_TASK' tag.
     * @param taskToFill the task to fill. (This method won't create a new one if this parameter is not null)
     * @return The newly created task that can be any type.
     */
    private Task createTask(XMLStreamReader cursorTask, Task taskToFill) throws JobCreationException {
        try {
            Task toReturn = null;
            Task tmpTask = (taskToFill != null) ? taskToFill : new Task() {

                /**  */
                private static final long serialVersionUID = 200;
            };
            //parse job attributes and fill the temporary one
            int attrLen = cursorTask.getAttributeCount();
            for (int i = 0; i < attrLen; i++) {
                String attrName = cursorTask.getAttributeLocalName(i);
                if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_NAME)) {
                    tmpTask.setName(cursorTask.getAttributeValue(i));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_NB_NODES)) {
                    tmpTask
                            .setNumberOfNeededNodes(Integer
                                    .parseInt(replace(cursorTask.getAttributeValue(i))));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_CANCELJOBONERROR)) {
                    tmpTask.setCancelJobOnError(Boolean
                            .parseBoolean(replace(cursorTask.getAttributeValue(i))));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_RESTARTTASKONERROR)) {
                    tmpTask.setRestartTaskOnError(RestartMode
                            .getMode(replace(cursorTask.getAttributeValue(i))));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_MAXNUMBEROFEXECUTION)) {
                    tmpTask.setMaxNumberOfExecution(Integer
                            .parseInt(replace(cursorTask.getAttributeValue(i))));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_RESULTPREVIEW)) {
                    tmpTask.setResultPreview(replace(cursorTask.getAttributeValue(i)));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_PRECIOUSRESULT)) {
                    tmpTask.setPreciousResult(Boolean.parseBoolean(replace(cursorTask.getAttributeValue(i))));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_WALLTIME)) {
                    tmpTask.setWallTime(Tools.formatDate(replace(cursorTask.getAttributeValue(i))));
                }
            }
            int eventType;
            boolean continu = true;
            while (continu && cursorTask.hasNext()) {
                eventType = cursorTask.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String current = cursorTask.getLocalName();
                        if (current.equals(JobFactory_stax.ELEMENT_COMMON_GENERIC_INFORMATION)) {
                            tmpTask.setGenericInformations(getGenericInformations(cursorTask));
                        } else if (current.equals(JobFactory_stax.ELEMENT_COMMON_DESCRIPTION)) {
                            tmpTask.setDescription(getDescription(cursorTask));
                        } else if (current.equals(JobFactory_stax.ELEMENT_DS_INPUTFILES)) {
                            setIOFIles(cursorTask, ELEMENT_DS_INPUTFILES, tmpTask);
                        } else if (current.equals(JobFactory_stax.ELEMENT_DS_OUTPUTFILES)) {
                            setIOFIles(cursorTask, ELEMENT_DS_OUTPUTFILES, tmpTask);
                        } else if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_SELECTION)) {
                            tmpTask.setSelectionScripts(createSelectionScript(cursorTask));
                        } else if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_PRE)) {
                            tmpTask.setPreScript(createScript(cursorTask));
                        } else if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_POST)) {
                            tmpTask.setPostScript(createScript(cursorTask));
                        } else if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_CLEANING)) {
                            tmpTask.setCleaningScript(createScript(cursorTask));
                        } else if (current.equals(JobFactory_stax.ELEMENT_TASK_DEPENDENCES)) {
                            createdependences(cursorTask, tmpTask);
                        } else if (current.equals(JobFactory_stax.ELEMENT_JAVA_EXECUTABLE)) {
                            toReturn = (taskToFill != null) ? taskToFill : new JavaTask();
                            setJavaExecutable((JavaTask) toReturn, cursorTask);
                        } else if (current.equals(JobFactory_stax.ELEMENT_NATIVE_EXECUTABLE)) {
                            toReturn = (taskToFill != null) ? taskToFill : new NativeTask();
                            setNativeExecutable((NativeTask) toReturn, cursorTask);
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorTask.getLocalName().equals(JobFactory_stax.ELEMENT_TASK)) {
                            continu = false;
                        }
                        break;
                }
            }
            //fill the real task with common attribute if it is a new one
            if (taskToFill == null) {
                autoCopyfields(CommonAttribute.class, tmpTask, toReturn);
                autoCopyfields(Task.class, tmpTask, toReturn);
                //set the following properties only if it is needed.
                if (toReturn.getCancelJobOnErrorProperty().isSet()) {
                    toReturn.setCancelJobOnError(toReturn.isCancelJobOnError());
                }
                if (toReturn.getRestartTaskOnErrorProperty().isSet()) {
                    toReturn.setRestartTaskOnError(toReturn.getRestartTaskOnError());
                }
                if (toReturn.getMaxNumberOfExecutionProperty().isSet()) {
                    toReturn.setMaxNumberOfExecution(toReturn.getMaxNumberOfExecution());
                }
            }
            //check if walltime and fork are consistency
            if ((toReturn instanceof JavaTask) && toReturn.getWallTime() > 0 &&
                !((JavaTask) toReturn).isFork()) {
                ((JavaTask) toReturn).setFork(true);
            }
            return toReturn;
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Create the list of includes/excludes pattern for the given INPUT/OUTPUT files
     * Leave the method with the cursor at the end of 'ELEMENT_DS_INPUT/OUTPUTFILES' tag.
     *
     * @param cursorTask the streamReader with the cursor on the 'ELEMENT_DS_INPUT/OUTPUTFILES' tag.
     * @param endTag the final tag for this tag : ELEMENT_DS_INPUTFILES or ELEMENT_DS_INPUTFILES
     * @param task the task in which to add the input/output files selector
     * @throws JobCreationException
     */
    private void setIOFIles(XMLStreamReader cursorTask, String endTag, Task task) throws JobCreationException {
        try {
            int eventType;
            boolean continu = true;
            while (continu && cursorTask.hasNext()) {
                eventType = cursorTask.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String current = cursorTask.getLocalName();
                        if (current.equals(JobFactory_stax.ELEMENT_DS_FILES)) {
                            int attrLen = cursorTask.getAttributeCount();
                            FileSelector selector = null;
                            for (int i = 0; i < attrLen; i++) {
                                String attrName = cursorTask.getAttributeLocalName(i);
                                if (attrName.equals(JobFactory_stax.ATTRIBUTE_DS_INCLUDES)) {
                                    if (selector == null) {
                                        selector = new FileSelector();
                                    }
                                    selector.setIncludes(new String[] { replace(cursorTask
                                            .getAttributeValue(i)) });
                                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_DS_EXCLUDES)) {
                                    if (selector == null) {
                                        selector = new FileSelector();
                                    }
                                    selector.setExcludes(new String[] { replace(cursorTask
                                            .getAttributeValue(i)) });
                                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_DS_ACCESSMODE) &&
                                    selector != null) {
                                    String accessMode = replace(cursorTask.getAttributeValue(i));
                                    if (endTag.equals(ELEMENT_DS_INPUTFILES)) {
                                        task.addInputFiles(selector, InputAccessMode
                                                .getAccessMode(accessMode));
                                    } else {
                                        task.addOutputFiles(selector, OutputAccessMode
                                                .getAccessMode(accessMode));
                                    }
                                }
                            }
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorTask.getLocalName().equals(endTag)) {
                            continu = false;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Add the dependences to the current task.
     * Leave this method at the end of the 'ELEMENT_TASK_DEPENDENCES' tag.
     *
     * @param cursorDepends the streamReader with the cursor on the 'ELEMENT_TASK_DEPENDENCES' tag.
     * @param t the task on which to apply the dependences.
     */
    private void createdependences(XMLStreamReader cursorDepends, Task t) throws JobCreationException {
        try {
            if (dependences == null) {
                dependences = new HashMap<String, ArrayList<String>>();
            }
            ArrayList<String> depends = new ArrayList<String>();
            int eventType;
            while (cursorDepends.hasNext()) {
                eventType = cursorDepends.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (cursorDepends.getLocalName()
                                .equals(JobFactory_stax.ELEMENT_TASK_DEPENDENCES_TASK)) {
                            depends.add(cursorDepends.getAttributeValue(0));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorDepends.getLocalName().equals(JobFactory_stax.ELEMENT_TASK_DEPENDENCES)) {
                            dependences.put(t.getName(), depends);
                            return;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Get the script defined at the specified cursor.
     * Leave the method with cursor at the end of the corresponding script.
     *
     * @param cursorScript the streamReader with the cursor on the corresponding script tag (pre, post, cleaning, selection, generation).
     * @param selection tell if the script is a selection script or not.
     * @return the script defined at the specified cursor.
     */
    private Script<?> createScript(XMLStreamReader cursorScript, boolean selection)
            throws JobCreationException {
        String currentScriptTag = cursorScript.getLocalName();
        try {
            boolean isDynamic = true;
            Script<?> toReturn = null;
            int eventType = -1;
            while (cursorScript.hasNext()) {
                if (selection && eventType == -1) {
                    eventType = cursorScript.getEventType();
                } else {
                    eventType = cursorScript.next();
                }
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String current = cursorScript.getLocalName();
                        if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_CODE)) {
                            String language = null;
                            String content = "";
                            if (cursorScript.getAttributeCount() > 0) {
                                language = cursorScript.getAttributeValue(0);
                            }
                            //goto script content
                            if (cursorScript.next() == XMLEvent.CHARACTERS) {
                                content = replace(cursorScript.getText());
                            }
                            toReturn = new SimpleScript(content, language);
                        } else if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_FILE)) {
                            String path = null;
                            String url = null;
                            if (JobFactory_stax.ATTRIBUTE_SCRIPT_URL.equals(cursorScript
                                    .getAttributeLocalName(0))) {
                                url = replace(cursorScript.getAttributeValue(0));
                            } else {
                                path = replace(cursorScript.getAttributeValue(0));
                            }

                            //go to the next 'arguments' start element or the 'file' end element
                            while (true) {
                                int ev = cursorScript.next();
                                if (((ev == XMLEvent.START_ELEMENT) && cursorScript.getLocalName().equals(
                                        ELEMENT_SCRIPT_ARGUMENTS)) ||
                                    (ev == XMLEvent.END_ELEMENT)) {
                                    break;
                                }
                            }

                            if (url != null) {
                                toReturn = new SimpleScript(new URL(url), getArguments(cursorScript));
                            } else {
                                toReturn = new SimpleScript(new File(path), getArguments(cursorScript));
                            }
                        } else if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_SCRIPT)) {
                            if (cursorScript.getAttributeCount() > 0) {
                                isDynamic = !"static".equals(cursorScript.getAttributeValue(0));
                            }
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorScript.getLocalName().equals(currentScriptTag)) {
                            if (selection) {
                                return new SelectionScript(toReturn, isDynamic);
                            } else {
                                return toReturn;
                            }
                        }
                        break;
                }
            }
            return toReturn;
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Get the selection script defined at the specified cursor.
     * Leave the method with cursor at the end of the 'ELEMENT_SCRIPT_SELECTION' script.
     *
     * @param cursorScript the streamReader with the cursor on the 'ELEMENT_SCRIPT_SELECTION' tag.
     * @return the script defined at the specified cursor.
     */
    private List<SelectionScript> createSelectionScript(XMLStreamReader cursorScript)
            throws JobCreationException {
        List<SelectionScript> scripts = new ArrayList<SelectionScript>();
        String selectionTag = cursorScript.getLocalName();
        try {
            SelectionScript newOne = null;
            int eventType;
            while (cursorScript.hasNext()) {
                eventType = cursorScript.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String current = cursorScript.getLocalName();
                        if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_SCRIPT)) {
                            newOne = (SelectionScript) createScript(cursorScript, true);
                            scripts.add(newOne);
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorScript.getLocalName().equals(selectionTag)) {
                            if (scripts.size() == 0) {
                                return null;
                            } else {
                                return scripts;
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
        return scripts;
    }

    /**
     * Get the script defined at the specified cursor.
     * Leave the method with cursor at the end of the corresponding script.
     *
     * @param cursorScript the streamReader with the cursor on the corresponding script tag (pre, post, cleaning, generation).
     * @return the script defined at the specified cursor.
     */
    private Script<?> createScript(XMLStreamReader cursorScript) throws JobCreationException {
        return createScript(cursorScript, false);
    }

    /**
     * Get the arguments at the given tag and return them as a string array.
     * Leave the cursor on the end 'ELEMENT_SCRIPT_ARGUMENTS' tag.
     *
     * @param cursorArgs the streamReader with the cursor on the 'ELEMENT_SCRIPT_ARGUMENTS' tag.
     * @return the arguments as a string array, null if no args.
     */
    private String[] getArguments(XMLStreamReader cursorArgs) throws JobCreationException {
        if (cursorArgs.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_ARGUMENTS)) {
            ArrayList<String> args = new ArrayList<String>();
            try {
                int eventType;
                while (cursorArgs.hasNext()) {
                    eventType = cursorArgs.next();
                    switch (eventType) {
                        case XMLEvent.START_ELEMENT:
                            if (cursorArgs.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_ARGUMENT)) {
                                args.add(replace(cursorArgs.getAttributeValue(0)));
                            }
                            break;
                        case XMLEvent.END_ELEMENT:
                            if (cursorArgs.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_ARGUMENTS)) {
                                return args.toArray(new String[] {});
                            }
                            break;
                    }
                }
                return args.toArray(new String[] {});
            } catch (Exception e) {
                throw new JobCreationException(e.getMessage(), e);
            }
        } else {
            return null;
        }
    }

    /**
     * Add the Native Executable to this native Task.
     * The cursor is currently at the beginning of the 'ELEMENT_NATIVE_EXECUTABLE' tag.
     *
     * @param nativeTask the task in which to add the Native Executable.
     * @param cursorExec the streamReader with the cursor on the 'ELEMENT_NATIVE_EXECUTABLE' tag.
     */
    private void setNativeExecutable(NativeTask nativeTask, XMLStreamReader cursorExec)
            throws JobCreationException {
        try {
            //one step ahead to go to the command (static or dynamic)
            while (cursorExec.next() != XMLEvent.START_ELEMENT)
                ;
            ArrayList<String> command = new ArrayList<String>();
            if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_STATICCOMMAND)) {

                for (int i = 0; i < cursorExec.getAttributeCount(); i++) {
                    String attrName = cursorExec.getAttributeLocalName(i);
                    if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_COMMAND_VALUE)) {
                        String[] parsedCommandLine = Tools.parseCommandLine(replace(cursorExec
                                .getAttributeValue(i)));
                        for (String pce : parsedCommandLine) {
                            command.add(pce);
                        }
                    }
                    if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_WORKDING_DIR)) {
                        nativeTask.setWorkingDir(replace(cursorExec.getAttributeValue(i)));
                    }
                }

                int eventType;
                while (cursorExec.hasNext()) {
                    eventType = cursorExec.next();
                    switch (eventType) {
                        case XMLEvent.START_ELEMENT:
                            if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_ARGUMENT)) {
                                command.add(replace(cursorExec.getAttributeValue(0)));
                            }
                            break;
                        case XMLEvent.END_ELEMENT:
                            if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_NATIVE_EXECUTABLE)) {
                                nativeTask.setCommandLine(command.toArray(new String[] {}));
                                return;
                            }
                            break;
                    }
                }
            } else if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_DYNAMICCOMMAND)) {
                for (int i = 0; i < cursorExec.getAttributeCount(); i++) {
                    String attrName = cursorExec.getAttributeLocalName(i);
                    if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_WORKDING_DIR)) {
                        nativeTask.setWorkingDir(replace(cursorExec.getAttributeValue(i)));
                    }
                }

                //one step ahead to go to the generation tag
                while (cursorExec.next() != XMLEvent.START_ELEMENT)
                    ;
                //create generation script
                Script<?> script = createScript(cursorExec);
                GenerationScript gscript = new GenerationScript(script);
                nativeTask.setGenerationScript(gscript);
                //goto the end of native executable tag
                while (cursorExec.hasNext()) {
                    if (cursorExec.next() == XMLEvent.END_ELEMENT &&
                        cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_NATIVE_EXECUTABLE)) {
                        return;
                    }
                }
            } else {
                throw new RuntimeException("Unknow command type : " + cursorExec.getLocalName());
            }
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Add the Java Executable to this java Task.
     * The cursor is currently at the beginning of the 'ELEMENT_JAVA_EXECUTABLE' tag.
     *
     * @param javaTask the task in which to add the Java Executable.
     * @param cursorExec the streamReader with the cursor on the 'ELEMENT_JAVA_EXECUTABLE' tag.
     */
    private void setJavaExecutable(JavaTask javaTask, XMLStreamReader cursorExec) throws JobCreationException {
        try {
            //parsing executable attributes
            int attrCount = cursorExec.getAttributeCount();
            for (int i = 0; i < attrCount; i++) {
                String attrName = cursorExec.getAttributeLocalName(i);
                if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_CLASSNAME)) {
                    javaTask.setExecutableClassName(cursorExec.getAttributeValue(i));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_FORK)) {
                    javaTask.setFork(Boolean.parseBoolean(replace(cursorExec.getAttributeValue(i))));
                }
            }
            //parsing executable tags
            int eventType;
            while (cursorExec.hasNext()) {
                eventType = cursorExec.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String current = cursorExec.getLocalName();
                        if (current.equals(JobFactory_stax.ELEMENT_FORK_ENVIRONMENT)) {
                            ForkEnvironment forkEnv = new ForkEnvironment();
                            attrCount = cursorExec.getAttributeCount();
                            for (int i = 0; i < attrCount; i++) {
                                String attrName = cursorExec.getAttributeLocalName(i);
                                if (attrName.equals(JobFactory_stax.ATTRIBUTE_FORK_JAVAHOME)) {
                                    forkEnv.setJavaHome(replace(cursorExec.getAttributeValue(i)));
                                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_FORK_JVMPARAMETERS)) {
                                    forkEnv.setJVMParameters(replace(cursorExec.getAttributeValue(i)));
                                }
                            }
                            javaTask.setForkEnvironment(forkEnv);
                        } else if (current.equals(JobFactory_stax.ELEMENT_TASK_PARAMETER)) {
                            javaTask.addArgument(replace(cursorExec.getAttributeValue(0)), replace(cursorExec
                                    .getAttributeValue(1)));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_JAVA_EXECUTABLE)) {
                            return;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new JobCreationException(e.getMessage(), e);
        }
    }

    /**
     * Construct the dependences between tasks.
     *
     * @throws JobCreationException if a dependences name is unknown.
     */
    private void makeDependences() throws JobCreationException {
        if (dependences != null && dependences.size() > 0) {
            if (job.getType() == JobType.TASKSFLOW) {
                TaskFlowJob tfj = (TaskFlowJob) job;
                for (Task t : tfj.getTasks()) {
                    ArrayList<String> names = dependences.get(t.getName());
                    if (names != null) {
                        for (String name : names) {
                            if (tfj.getTask(name) == null) {
                                throw new JobCreationException("Unknow dependence : " + name);
                            } else {
                                t.addDependence(tfj.getTask(name));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Replace the variables inside the given string by its value if needed.<br/>
     * This method looks for ${...} pattern and replace this pattern by the corresponding user variable
     * define in the 'ELEMENT_VARIABLES' tag.
     *
     * @param str the string in which to look for.
     * @return the string with variables replaced by values.
     * @throws JobCreationException if a Variable has not been found
     */
    private String replace(String str) throws JobCreationException {
        if (str == null || "".equals(str)) {
            return str;
        }
        str = str.trim();
        //impl1 - do not search in System properties
        //        if (!variables.isEmpty() && variablesPattern.matcher(str).matches()) {
        //            for (Entry<String, String> e : variables.entrySet()) {
        //                str = str.replaceAll("\\$\\{" + (String) e.getKey() + "\\}", (String) e.getValue());
        //            }
        //        }
        //        return str;
        //impl2 - also search in System properties
        String[] strs = RegexpMatcher.matches(variablesPattern, str);
        String replacement;
        if (strs.length != 0) {
            //for each entry
            for (String s : strs) {
                //remove ${ and }
                s = s.substring(2, s.length() - 1);
                //search the key (first in variables)
                replacement = variables.get(s);
                if (replacement == null) {
                    //if not found in System properties
                    replacement = System.getProperty(s);
                }
                if (replacement == null) {
                    throw new JobCreationException("Variable '" + s + "' not found in the definition (${" +
                        s + "})");
                }
                replacement = replacement.replaceAll("\\\\", "\\\\\\\\");
                str = str.replaceFirst("\\$\\{" + s + "\\}", replacement);
            }
        }
        return str;
    }

    /**
     * ValidatingErrorHandler...
     *
     * @author The ProActive Team
     * @since ProActive Scheduling 0.9.1
     */
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

    private void displayJobInfo() {
        if (logger.isDebugEnabled()) {
            logger.debug("type : " + job.getType());
            logger.debug("name : " + job.getName());
            logger.debug("desc : " + job.getDescription());
            logger.debug("proj : " + job.getProjectName());
            logger.debug("prio : " + job.getPriority());
            logger.debug("logf : " + job.getLogFile());
            logger.debug("cjoe : " + job.isCancelJobOnError());
            logger.debug("rtoe : " + job.getRestartTaskOnError());
            logger.debug("mnoe : " + job.getMaxNumberOfExecution());
            logger.debug("insp : " + job.getInputSpace());
            logger.debug("ousp : " + job.getOutputSpace());
            logger.debug("cp   : ");
            if (job.getEnvironment().getJobClasspath() != null) {
                String cp = "cp   : ";
                for (String s : job.getEnvironment().getJobClasspath()) {
                    cp += (s + ":");
                }
                logger.debug(cp);
            }
            logger.debug("info : " + job.getGenericInformations());
            logger.debug("TASKS ------------------------------------------------");
            ArrayList<Task> tasks = new ArrayList<Task>();
            switch (job.getType()) {
                case TASKSFLOW:
                    tasks.addAll(((TaskFlowJob) job).getTasks());
                    break;
            }
            for (Task t : tasks) {
                logger.debug("name  : " + t.getName());
                logger.debug("desc  : " + t.getDescription());
                logger.debug("node  : " + t.getNumberOfNodesNeeded());
                logger.debug("cjoe  : " + t.isCancelJobOnError());
                logger.debug("res   : " + t.isPreciousResult());
                logger.debug("rtoe  : " + t.getRestartTaskOnError());
                logger.debug("mnoe  : " + t.getMaxNumberOfExecution());
                logger.debug("wall  : " + t.getWallTime());
                logger.debug("prev  : " + t.getResultPreview());
                logger.debug("selec : " + t.getSelectionScripts());
                logger.debug("pre   : " + t.getPreScript());
                logger.debug("post  : " + t.getPostScript());
                logger.debug("clean : " + t.getCleaningScript());
                try {
                    logger.debug("ifsel : length=" + t.getInputFilesList().size());
                } catch (NullPointerException npe) {
                }
                try {
                    logger.debug("ofsel : inc=" + t.getOutputFilesList().size());
                } catch (NullPointerException npe) {
                }
                if (t.getDependencesList() != null) {
                    String dep = "dep   : ";
                    for (Task tdep : t.getDependencesList()) {
                        dep += tdep.getName() + " ";
                    }
                    logger.debug(dep);
                } else {
                    logger.debug("dep   : null");
                }
                logger.debug("infos : " + t.getGenericInformations());
                if (t instanceof JavaTask) {
                    logger.debug("class : " + ((JavaTask) t).getExecutableClassName());
                    try {
                        logger.debug("args  : " + ((JavaTask) t).getArguments());
                    } catch (Exception e) {
                        logger.debug("Cannot get args  : " + e.getMessage(), e);
                    }
                    logger.debug("fork  : " + ((JavaTask) t).isFork());
                    if (((JavaTask) t).getForkEnvironment() != null) {
                        logger.debug("forkJ  : " + ((JavaTask) t).getForkEnvironment().getJavaHome());
                        logger.debug("forkP  : " + ((JavaTask) t).getForkEnvironment().getJVMParameters());
                    }
                } else if (t instanceof NativeTask) {
                    logger.debug("cmd   : " + ((NativeTask) t).getCommandLine());
                    logger.debug("gensc : " + ((NativeTask) t).getGenerationScript());
                }
                logger.debug("--------------------------------------------------");
            }
        }
    }

    /**
     * Copy fields belonging to 'cFrom' from 'from' to 'to'.
     * Will only iterate on non-private field.
     * Private fields in 'cFrom' won't be set in 'to'.
     *
     * @param <T> check type given as argument is equals or under this type.
     * @param klass the klass in which to find the fields
     * @param from the T object in which to get the value
     * @param to the T object in which to set the value
     */
    private static <T> void autoCopyfields(Class<T> klass, T from, T to) throws IllegalArgumentException,
            IllegalAccessException {
        for (Field f : klass.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                f.set(to, f.get(from));
            }
        }
    }

}
