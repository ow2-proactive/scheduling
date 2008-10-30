/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.common.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.common.scripting.GenerationScript;
import org.ow2.proactive.resourcemanager.common.scripting.Script;
import org.ow2.proactive.resourcemanager.common.scripting.SelectionScript;
import org.ow2.proactive.resourcemanager.common.scripting.SimpleScript;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.scheduler.Tools;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ProActiveTask;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.task.ForkEnvironment;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * JobFactory_stax is am implementation of the JobFactory using XPATH
 *
 * @author The ProActive Team
 * @date 2 July 07
 * @since ProActive Scheduling 0.9.1
 *
 */
public class JobFactory_stax extends JobFactory {

    public static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.FACTORY);
    /** Location of the schema used to parse job descriptor. */
    public static final String SCHEMA_LOCATION = "/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/dev/schedulerjob.rng";
    /** Variables styleScheet location. */
    public static final String STYLESHEET_LOCATION = "/org/ow2/proactive/scheduler/common/xml/stylesheets/variables.xsl";
    /** Job name space. */
    public static final String JOB_NAMESPACE = "urn:proactive:jobdescriptor:dev";
    /** Job prefix. */
    public static final String JOB_PREFIX = "js";

    //Are define only the needed tags. If more are needed, just create them.
    //JOBS
    private static final String ELEMENT_JOB = "job";
    private static final String ELEMENT_TASKFLOW = "taskFlow";
    private static final String ELEMENT_PROACTIVE = "proActive";
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
    private static final String ELEMENT_PROACTIVE_EXECUTABLE = "proActiveExecutable";
    private static final String ELEMENT_TASK_DEPENDENCES = "depends";
    private static final String ELEMENT_TASK_DEPENDENCES_TASK = "task";
    private static final String ATTRIBUTE_TASK_RESULTPREVIEW = "resultPreviewClass";
    private static final String ATTRIBUTE_TASK_PRECIOUSRESULT = "preciousResult";
    private static final String ATTRIBUTE_TASK_CLASSNAME = "class";
    private static final String ELEMENT_TASK_PARAMETER = "parameter";
    private static final String ATTRIBUTE_TASK_WALLTIME = "walltime";
    private static final String ATTRIBUTE_TASK_FORK = "fork";
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
     * @see org.ow2.proactive.scheduler.common.job.JobFactory#createJob(java.lang.String)
     */
    public Job createJob(String filePath) throws JobCreationException {
        try {
            //Check if the file exist
            File f = new File(filePath);
            if (!f.exists()) {
                throw new FileNotFoundException("This file has not been found : " + filePath);
            }
            //validate content using the proper XML schema
            validate(filePath);
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
            throw new JobCreationException("Exception occured during Job creation", e);
        }
    }

    /**
     * Validate the given job descriptor using the internal RELAX_NG Schema.
     * 
     * @param filePath the path of the file to validate
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
        } catch (Exception e) {
            throw new JobCreationException("Exception occured during Job creation !", e);
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
            public JobId getId() {
                throw new RuntimeException("Not Available !");
            }

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
                jtmp.setPriority(JobPriority.findPriority(cursorJob.getAttributeValue(i)));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_CANCELJOBONERROR)) {
                jtmp.setCancelJobOnError(Boolean.parseBoolean(cursorJob.getAttributeValue(i)));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_JOB_LOGFILE)) {
                jtmp.setLogFile(cursorJob.getAttributeValue(i));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_RESTARTTASKONERROR)) {
                jtmp.setRestartTaskOnError(RestartMode.getMode(cursorJob.getAttributeValue(i)));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_MAXNUMBEROFEXECUTION)) {
                jtmp.setMaxNumberOfExecution(Integer.parseInt(cursorJob.getAttributeValue(i)));
            } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_JOB_PROJECTNAME)) {
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
                            jtmp.getEnv().setJobClasspath(getClasspath(cursorJob));
                        } else if (current.equals(JobFactory_stax.ELEMENT_COMMON_DESCRIPTION)) {
                            jtmp.setDescription(getDescription(cursorJob));
                        } else if (current.equals(JobFactory_stax.ELEMENT_TASKFLOW)) {
                            job = new TaskFlowJob();
                            continu = false;
                        } else if (current.equals(JobFactory_stax.ELEMENT_PROACTIVE)) {
                            job = new ProActiveJob();
                            //as the ProActive Job has only one task, set it now
                            //with the neededNodes property
                            if (cursorJob.getAttributeCount() > 0) {
                                ProActiveTask paTask = new ProActiveTask();
                                paTask.setNumberOfNodesNeeded(Integer
                                        .parseInt(cursorJob.getAttributeValue(0)));
                                ((ProActiveJob) job).setTask(paTask);
                            }
                            continu = false;
                        }
                        break;
                }
            }
            //if this point is reached, fill the real job using the temporary one
            job.setDescription(jtmp.getDescription());
            job.setEnv(jtmp.getEnv());
            job.setLogFile(jtmp.getLogFile());
            job.setName(jtmp.getName());
            job.setPriority(jtmp.getPriority());
            job.setProjectName(jtmp.getProjectName());
            job.setCancelJobOnError(jtmp.isCancelJobOnError());
            job.setRestartTaskOnError(jtmp.getRestartTaskOnError());
            job.setMaxNumberOfExecution(jtmp.getMaxNumberOfExecution());
            job.setGenericInformations(jtmp.getGenericInformations());
        } catch (Exception e) {
            throw new JobCreationException("Exception occured during Job properties creation !", e);
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
            throw new JobCreationException("Exception occured while creating variables !", e);
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
            throw new JobCreationException("Exception occured while creating generic information !", e);
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
            throw new JobCreationException("Exception occured while creating classpath entries !", e);
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
            throw new JobCreationException("Exception occured while reading description !", e);
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
                        case PROACTIVE:
                            //fill the existing task
                            t = createTask(cursorTask, ((ProActiveJob) job).getTask());
                            //add task to the job
                            ((ProActiveJob) job).setTask((ProActiveTask) t);
                            break;
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
            throw new JobCreationException("Exception occured while filling job with task !", e);
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
            };
            //parse job attributes and fill the temporary one
            int attrLen = cursorTask.getAttributeCount();
            for (int i = 0; i < attrLen; i++) {
                String attrName = cursorTask.getAttributeLocalName(i);
                if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_NAME)) {
                    tmpTask.setName(cursorTask.getAttributeValue(i));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_CANCELJOBONERROR)) {
                    tmpTask.setCancelJobOnError(Boolean.parseBoolean(cursorTask.getAttributeValue(i)));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_RESTARTTASKONERROR)) {
                    tmpTask.setRestartTaskOnError(RestartMode.getMode(cursorTask.getAttributeValue(i)));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_COMMON_MAXNUMBEROFEXECUTION)) {
                    tmpTask.setMaxNumberOfExecution(Integer.parseInt(cursorTask.getAttributeValue(i)));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_RESULTPREVIEW)) {
                    tmpTask.setResultPreview(cursorTask.getAttributeValue(i));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_PRECIOUSRESULT)) {
                    tmpTask.setPreciousResult(Boolean.parseBoolean(cursorTask.getAttributeValue(i)));
                } else if (attrName.equals(JobFactory_stax.ATTRIBUTE_TASK_WALLTIME)) {
                    tmpTask.setWallTime(Tools.formatDate(cursorTask.getAttributeValue(i)));
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
                        } else if (current.equals(JobFactory_stax.ELEMENT_SCRIPT_SELECTION)) {
                            tmpTask.setSelectionScript(createSelectionScript(cursorTask));
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
                        } else if (current.equals(JobFactory_stax.ELEMENT_PROACTIVE_EXECUTABLE)) {
                            toReturn = (taskToFill != null) ? taskToFill : new ProActiveTask();
                            setProActiveExecutable((ProActiveTask) toReturn, cursorTask);
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorTask.getLocalName().equals(JobFactory_stax.ELEMENT_TASK)) {
                            continu = false;
                        }
                        break;
                }
            }
            //fill the real job with common attribute if it is a new one
            if (taskToFill == null) {
                toReturn.setCleaningScript(tmpTask.getCleaningScript());
                toReturn.setDescription(tmpTask.getDescription());
                toReturn.setGenericInformations(tmpTask.getGenericInformations());
                toReturn.setName(tmpTask.getName());
                toReturn.setPostScript(tmpTask.getPostScript());
                toReturn.setPreciousResult(tmpTask.isPreciousResult());
                toReturn.setPreScript(tmpTask.getPreScript());
                toReturn.setResultPreview(tmpTask.getResultPreview());
                toReturn.setSelectionScript(tmpTask.getSelectionScript());
                toReturn.setWallTime(tmpTask.getWallTime());
                //set the following properties only if it is needed.
                if (tmpTask.getCancelJobOnErrorProperty().isSet()) {
                    toReturn.setCancelJobOnError(tmpTask.isCancelJobOnError());
                }
                if (tmpTask.getRestartTaskOnErrorProperty().isSet()) {
                    toReturn.setRestartTaskOnError(tmpTask.getRestartTaskOnError());
                }
                if (tmpTask.getMaxNumberOfExecutionProperty().isSet()) {
                    toReturn.setMaxNumberOfExecution(tmpTask.getMaxNumberOfExecution());
                }
            }
            //check if walltime and fork are consistency
            if ((toReturn instanceof JavaTask) && toReturn.getWallTime() > 0 &&
                !((JavaTask) toReturn).isFork()) {
                ((JavaTask) toReturn).setFork(true);
            }
            return toReturn;
        } catch (Exception e) {
            throw new JobCreationException("Exception occured while filling task !", e);
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
            throw new JobCreationException("Exception occured while filling task !", e);
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
            int eventType;
            while (cursorScript.hasNext()) {
                eventType = cursorScript.next();
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
            throw new JobCreationException("Exception occured while creating " + currentScriptTag +
                " script !", e);
        }
    }

    /**
     * Get the selection script defined at the specified cursor.
     * Leave the method with cursor at the end of the 'ELEMENT_SCRIPT_SELECTION' script. 
     * 
     * @param cursorScript the streamReader with the cursor on the 'ELEMENT_SCRIPT_SELECTION' tag.
     * @return the script defined at the specified cursor.
     */
    private SelectionScript createSelectionScript(XMLStreamReader cursorScript) throws JobCreationException {
        return (SelectionScript) createScript(cursorScript, true);
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
                throw new JobCreationException("Exception occured while reading script arguments !", e);
            }
        } else {
            return null;
        }
    }

    /**
     * Add the proActive Executable to this ProActive Task.
     * The cursor is currently at the beginning of the 'ELEMENT_PROACTIVE_EXECUTABLE' tag.
     * 
     * @param paTask the task in which to add the ProActive Executable.
     * @param cursorExec the streamReader with the cursor on the 'ELEMENT_PROACTIVE_EXECUTABLE' tag.
     */
    private void setProActiveExecutable(ProActiveTask paTask, XMLStreamReader cursorExec)
            throws JobCreationException {
        try {
            //parsing executable attribute class that must exist
            paTask.setExecutableClassName(cursorExec.getAttributeValue(0));
            //parsing executable tags
            int eventType;
            while (cursorExec.hasNext()) {
                eventType = cursorExec.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_TASK_PARAMETER)) {
                            paTask.addArgument(cursorExec.getAttributeValue(0), replace(cursorExec
                                    .getAttributeValue(1)));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_PROACTIVE_EXECUTABLE)) {
                            return;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new JobCreationException("Exception occured while setting ProActive Executable !", e);
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
            if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_STATICCOMMAND)) {
                String command = replace(cursorExec.getAttributeValue(0));
                int eventType;
                while (cursorExec.hasNext()) {
                    eventType = cursorExec.next();
                    switch (eventType) {
                        case XMLEvent.START_ELEMENT:
                            if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_ARGUMENT)) {
                                command = command + " " + replace(cursorExec.getAttributeValue(0));
                            }
                            break;
                        case XMLEvent.END_ELEMENT:
                            if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_NATIVE_EXECUTABLE)) {
                                nativeTask.setCommandLine(command);
                                return;
                            }
                            break;
                    }
                }
            } else if (cursorExec.getLocalName().equals(JobFactory_stax.ELEMENT_SCRIPT_DYNAMICCOMMAND)) {
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
            throw new JobCreationException("Exception occured while setting Native Executable !", e);
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
                    javaTask.setFork(Boolean.parseBoolean(cursorExec.getAttributeValue(i)));
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
                            javaTask.addArgument(cursorExec.getAttributeValue(0), replace(cursorExec
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
            throw new JobCreationException("Exception occured while setting Java Executable !", e);
        }
    }

    /**
     * Construct the dependences between tasks.
     */
    private void makeDependences() {
        if (dependences != null && dependences.size() > 0) {
            if (job.getType() == JobType.TASKSFLOW) {
                TaskFlowJob tfj = (TaskFlowJob) job;
                for (Task t : tfj.getTasks()) {
                    ArrayList<String> names = dependences.get(t.getName());
                    if (names != null) {
                        for (String name : names) {
                            t.addDependence(tfj.getTask(name));
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
     */
    private String replace(String str) {
        //TODO improve this (critical) method ? it is called many time during the parsing.
        //    	Pattern pattern = Pattern.compile("\\$\\{[^\\}]+\\}",Pattern.CASE_INSENSITIVE);
        //    	Matcher matcher = pattern.matcher(str);
        if (!variables.isEmpty() && str.matches(".*\\$\\{[^\\}]+\\}.*")) {
            for (Entry e : variables.entrySet()) {
                str = str.replaceAll("\\$\\{" + (String) e.getKey() + "\\}", (String) e.getValue());
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
            System.out.print("cp   : ");
            if (job.getEnv().getJobClasspath() != null) {
                String cp = "cp   : ";
                for (String s : job.getEnv().getJobClasspath()) {
                    cp += (s + ":");
                }
                logger.debug(cp);
            }
            logger.debug("info : " + job.getGenericInformations());
            logger.debug("TASKS ------------------------------------------------");
            ArrayList<Task> tasks = new ArrayList<Task>();
            switch (job.getType()) {
                case PROACTIVE:
                    tasks.add(((ProActiveJob) job).getTask());
                    break;
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
                logger.debug("selec : " + t.getSelectionScript());
                logger.debug("pre   : " + t.getPreScript());
                logger.debug("post  : " + t.getPostScript());
                logger.debug("clean : " + t.getCleaningScript());
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
                    logger.debug("args  : " + ((JavaTask) t).getArguments());
                    logger.debug("fork  : " + ((JavaTask) t).isFork());
                    if (((JavaTask) t).getForkEnvironment() != null) {
                        logger.debug("forkJ  : " + ((JavaTask) t).getForkEnvironment().getJavaHome());
                        logger.debug("forkP  : " + ((JavaTask) t).getForkEnvironment().getJVMParameters());
                    }
                } else if (t instanceof NativeTask) {
                    logger.debug("cmd   : " + ((NativeTask) t).getCommandLine());
                    logger.debug("gensc : " + ((NativeTask) t).getGenerationScript());
                } else if (t instanceof ProActiveTask) {
                    logger.debug("class : " + ((ProActiveTask) t).getExecutableClassName());
                    logger.debug("args  : " + ((ProActiveTask) t).getArguments());
                }
                logger.debug("--------------------------------------------------");
            }
        }
    }

}
