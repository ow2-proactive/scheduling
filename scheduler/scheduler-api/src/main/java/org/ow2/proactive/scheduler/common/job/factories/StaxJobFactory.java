/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.job.factories;

import static org.ow2.proactive.scheduler.common.SchedulerConstants.NEXT_EXECUTION;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.PARENT_JOB_ID;
import static org.ow2.proactive.scheduler.common.job.Job.JOB_DDL;
import static org.ow2.proactive.scheduler.common.task.CommonAttribute.GENERIC_INFO_START_AT_KEY;
import static org.ow2.proactive.scheduler.common.util.VariableSubstitutor.filterAndUpdate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.JobValidationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.globalvariables.GlobalVariablesData;
import org.ow2.proactive.scheduler.common.job.factories.globalvariables.GlobalVariablesParser;
import org.ow2.proactive.scheduler.common.job.factories.spi.JobValidatorRegistry;
import org.ow2.proactive.scheduler.common.job.factories.spi.JobValidatorService;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.util.Object2ByteConverter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scripting.ForkEnvironmentScript;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Tools;

import com.google.common.collect.ImmutableMap;


/**
 * StaxJobFactory provide an implementation of the JobFactory using StAX
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class StaxJobFactory extends JobFactory {

    public static final Logger logger = Logger.getLogger(StaxJobFactory.class);

    private static final String FILE_ENCODING = PASchedulerProperties.FILE_ENCODING.getValueAsString();

    public static final String MSG_UNABLE_TO_INSTANCIATE_JOB_VALIDATION_FACTORIES = "Unable to instanciate job validation factories";

    private static final Map<String, Job> jobCache = Collections.synchronizedMap(new LRUMap<>(PASchedulerProperties.SCHEDULER_STAX_JOB_CACHE.getValueAsInt()));

    private static final Map<String, JobCreationException> validationCache = Collections.synchronizedMap(new LRUMap<>(PASchedulerProperties.SCHEDULER_STAX_JOB_CACHE.getValueAsInt()));

    private static final JobCreationException EMPTY_EXCEPTION = new JobCreationException("EMPTY");

    private enum ScriptType {
        SELECTION,
        FLOW,
        OTHER
    }

    /**
     * XML input factory
     */
    private XMLInputFactory xmlInputFactory = null;

    /**
     * file relative path (relative file path (js) given in XML will be relative to this path)
     */
    private String relativePathRoot = "./";

    private GetJobContentGenerator getJobContentFactory = new GetJobContentGenerator();

    private boolean handleGlobalVariables;

    // generic info that are (most likely) varying upon each job execution
    private static final String[] VARYING_GENERIC_INFO = { GENERIC_INFO_START_AT_KEY, JOB_DDL, NEXT_EXECUTION,
                                                           PARENT_JOB_ID };

    /**
     * Create a new instance of StaxJobFactory.
     */
    public StaxJobFactory(boolean handleGlobalVariables) {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
        xmlInputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.TRUE);
        xmlInputFactory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
        xmlInputFactory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        this.handleGlobalVariables = handleGlobalVariables;
    }

    @Override
    public Job createJob(String filePath) throws JobCreationException {
        return createJob(filePath, null, null);
    }

    @Override
    public Job createJob(String filePath, Map<String, String> replacementVariables,
            Map<String, String> replacementGenericInfos) throws JobCreationException {
        try {
            return createJob(new File(filePath), replacementVariables, replacementGenericInfos, null, null, null);
        } catch (JobCreationException jce) {
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(e);
        }
    }

    @Override
    public Job createJob(String filePath, Map<String, String> replacementVariables,
            Map<String, String> replacementGenericInfos, Scheduler scheduler, SchedulerSpaceInterface space,
            String sessionId) throws JobCreationException {
        try {
            return createJob(new File(filePath),
                             replacementVariables,
                             replacementGenericInfos,
                             scheduler,
                             space,
                             sessionId);
        } catch (JobCreationException jce) {
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(e);
        }
    }

    @Override
    public Job createJob(URI filePath) throws JobCreationException {
        return createJob(filePath, null, null);
    }

    @Override
    public Job createJob(URI filePath, Map<String, String> replacementVariables,
            Map<String, String> replacementGenericInfos) throws JobCreationException {
        try {
            return createJob(new File(filePath), replacementVariables, replacementGenericInfos, null, null, null);
        } catch (JobCreationException jce) {
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(e);
        }
    }

    @Override
    public Job createJob(InputStream workflowStream) throws JobCreationException {
        return createJob(workflowStream, null, null);
    }

    @Override
    public Job createJob(InputStream workflowStream, Map<String, String> replacementVariables,
            Map<String, String> replacementGenericInfos) throws JobCreationException {
        return createJob(workflowStream, replacementVariables, replacementGenericInfos, null, null, null);
    }

    @Override
    public Job createJob(InputStream workflowStream, Map<String, String> replacementVariables,
            Map<String, String> replacementGenericInfos, Scheduler scheduler, SchedulerSpaceInterface space,
            String sessionId) throws JobCreationException {
        try {
            return createJobFromInputStream(workflowStream,
                                            replacementVariables,
                                            replacementGenericInfos,
                                            scheduler,
                                            space,
                                            sessionId);
        } catch (JobCreationException jce) {
            jce.pushTag(XMLTags.JOB.getXMLName());
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(e);
        }
    }

    private Job createJob(File file, Map<String, String> replacementVariables,
            Map<String, String> replacementGenericInfos, Scheduler scheduler, SchedulerSpaceInterface space,
            String sessionId) throws JobCreationException {
        try {
            if (!file.exists()) {
                throw new FileNotFoundException("This file has not been found: " + file.getAbsolutePath());
            }
            relativePathRoot = file.getParentFile().getAbsolutePath();
            try (InputStream inputStream = new FileInputStream(file)) {
                return createJobFromInputStream(inputStream,
                                                replacementVariables,
                                                replacementGenericInfos,
                                                scheduler,
                                                space,
                                                sessionId);
            }
        } catch (JobCreationException jce) {
            jce.pushTag(XMLTags.JOB.getXMLName());
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(e);
        }
    }

    private Job createJobFromInputStream(InputStream jobInputStream, Map<String, String> replacementVariables,
            Map<String, String> replacementGenericInfos, Scheduler scheduler, SchedulerSpaceInterface space,
            String sessionId) throws JobCreationException, IOException, XMLStreamException {
        long t0 = System.currentTimeMillis();
        if (replacementVariables != null) {
            replacementVariables = replacementVariables.entrySet()
                                                       .stream()
                                                       .collect(Collectors.toMap(e -> e.getKey(),
                                                                                 e -> e.getValue() != null ? e.getValue()
                                                                                                           : ""));
        }
        byte[] bytes = ValidationUtil.getInputStreamBytes(jobInputStream);
        String md5Job = DigestUtils.md5Hex(bytes);
        String md5Variables = DigestUtils.md5Hex(Object2ByteConverter.convertObject2Byte(replacementVariables));
        Map<String, String> filteredReplacementGenericInfo = excludeVaryingGenericInfo(replacementGenericInfos);
        String md5GenericInfo = DigestUtils.md5Hex(Object2ByteConverter.convertObject2Byte(filteredReplacementGenericInfo));
        String md5GlobalVariables = "";
        if (handleGlobalVariables) {
            md5GlobalVariables = GlobalVariablesParser.getInstance().getMD5();
        }
        String md5 = md5Job + "_" + md5Variables + "_" + md5GenericInfo + "_" + md5GlobalVariables;
        long t1 = System.currentTimeMillis();
        if (!validationCache.containsKey(md5)) {
            try {

                try (ByteArrayInputStream jobInputStreamForValidation = new ByteArrayInputStream(bytes)) {
                    validate(jobInputStreamForValidation);
                }
                validationCache.put(md5, EMPTY_EXCEPTION);
            } catch (JobCreationException e) {
                validationCache.put(md5, e);
                throw e;
            }
        } else {
            JobCreationException e = validationCache.get(md5);
            if (e != EMPTY_EXCEPTION) {
                throw e;
            }
        }
        long t2 = System.currentTimeMillis();
        long t3;
        Job job;
        if (!jobCache.containsKey(md5)) {
            Map<String, ArrayList<String>> dependencies = new LinkedHashMap<>();
            try (ByteArrayInputStream jobInpoutStreamForParsing = new ByteArrayInputStream(bytes)) {
                XMLStreamReader xmlsr = xmlInputFactory.createXMLStreamReader(jobInpoutStreamForParsing, FILE_ENCODING);
                job = createJob(xmlsr,
                                replacementVariables,
                                filteredReplacementGenericInfo,
                                dependencies,
                                new String(bytes));
                xmlsr.close();
            }
            t3 = System.currentTimeMillis();

            makeDependences(job, dependencies);
            jobCache.put(md5, job);
        } else {
            job = jobCache.get(md5);
            t3 = System.currentTimeMillis();
        }
        long t4 = System.currentTimeMillis();
        validate((TaskFlowJob) job, scheduler, space, sessionId);
        // we insert back the varying generic information
        insertVaryingGenericInfo(replacementGenericInfos, (TaskFlowJob) job);
        // as the value of variables can change after the validation (due to variable inferences), we must regenerate generic info values
        updateGenericInformation((TaskFlowJob) job, replacementGenericInfos);

        resolveCleaningScripts((TaskFlowJob) job, job.getVariablesAsReplacementMap());

        String updatedJobContent = getJobContentFactory.replaceVarsAndGenericInfo(job.getJobContent(),
                                                                                  job.getVariables(),
                                                                                  getGenericInfoKeepReferences(job,
                                                                                                               replacementGenericInfos));

        job.setJobContent(updatedJobContent);
        long t5 = System.currentTimeMillis();
        long d1 = t1 - t0;
        long d2 = t2 - t1;
        long d3 = t3 - t2;
        long d4 = t4 - t3;
        long d5 = t5 - t4;

        logger.debug("Job successfully created!");
        logger.debug(String.format("timer;%d;%d;%d;%d;%d", d1, d2, d3, d4, d5));
        //debug mode only
        displayJobInfo(job);
        return job;
    }

    private Map<String, String> excludeVaryingGenericInfo(Map<String, String> replacementGenericInfos) {
        if (replacementGenericInfos == null) {
            return new LinkedHashMap<>();
        }
        Map<String, String> filteredGenericInfo = new LinkedHashMap<>(replacementGenericInfos);
        for (String genericInfoKey : VARYING_GENERIC_INFO) {
            filteredGenericInfo.remove(genericInfoKey);
        }
        return filteredGenericInfo;
    }

    private void insertVaryingGenericInfo(Map<String, String> replacementGenericInfos, TaskFlowJob job) {
        if (replacementGenericInfos != null) {
            // we add filtered varying generic info back
            for (String genericInfoKey : VARYING_GENERIC_INFO) {
                if (replacementGenericInfos.containsKey(genericInfoKey)) {
                    job.addGenericInformation(genericInfoKey, replacementGenericInfos.get(genericInfoKey));
                }
            }
        }
    }

    private void updateGenericInformation(TaskFlowJob job, Map<String, String> replacementGenericInfo)
            throws JobCreationException {
        Map<String, String> jobReplacementMap = job.getVariablesAsReplacementMap();
        Set<String> jobVariableNames = jobReplacementMap.keySet();
        for (Map.Entry<String, String> entry : job.getUnresolvedGenericInformation().entrySet()) {
            String originalValue = entry.getValue();
            // replacementGenericInfo contains generic info provided at submission time, overriding the workflow g.i.
            if (replacementGenericInfo != null && replacementGenericInfo.containsKey(entry.getKey())) {
                originalValue = replacementGenericInfo.get(entry.getKey());
            }
            if (jobVariableNames.stream().anyMatch(name -> entry.getValue().contains("$" + name) ||
                                                           entry.getValue().contains("${" + name + "}"))) {
                job.addGenericInformation(entry.getKey(), replace(originalValue, jobReplacementMap));
            }
        }
        for (Task task : job.getTasks()) {
            Map<String, String> taskReplacementMap = task.getVariablesOverriden(job);
            Set<String> taskVariableNames = taskReplacementMap.keySet();
            for (Map.Entry<String, String> entry : task.getUnresolvedGenericInformation().entrySet()) {
                if (taskVariableNames.stream().anyMatch(name -> entry.getValue().contains("$" + name) ||
                                                                entry.getValue().contains("${" + name + "}"))) {
                    task.addGenericInformation(entry.getKey(), replace(entry.getValue(), taskReplacementMap));
                }
            }
        }
    }

    /*
     * Validate the given job descriptor
     */
    private void validate(InputStream jobInputStream) throws JobCreationException {
        Map<String, JobValidatorService> factories;
        try {
            factories = JobValidatorRegistry.getInstance().getRegisteredFactories();
        } catch (Exception e) {
            logger.error(MSG_UNABLE_TO_INSTANCIATE_JOB_VALIDATION_FACTORIES, e);
            throw new JobCreationException(MSG_UNABLE_TO_INSTANCIATE_JOB_VALIDATION_FACTORIES, e);
        }

        try {
            for (JobValidatorService factory : factories.values()) {
                factory.validateJob(jobInputStream);
            }
        } catch (JobValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new JobValidationException(true, e);
        }
    }

    /*
     * Validate the given job descriptor
     */
    private TaskFlowJob validate(TaskFlowJob job, Scheduler scheduler, SchedulerSpaceInterface space, String sessionId)
            throws JobCreationException {
        Map<String, JobValidatorService> factories;
        try {
            factories = JobValidatorRegistry.getInstance().getRegisteredFactories();
        } catch (Exception e) {
            throw new JobCreationException(MSG_UNABLE_TO_INSTANCIATE_JOB_VALIDATION_FACTORIES, e);
        }

        TaskFlowJob updatedJob = job;

        try {

            for (JobValidatorService factory : factories.values()) {
                updatedJob = factory.validateJob(updatedJob, scheduler, space, sessionId);
            }
        } catch (JobValidationException e) {
            fillUpdatedVariables(e, job);
            throw e;
        } catch (Exception e) {
            JobValidationException validationException = new JobValidationException(e);
            fillUpdatedVariables(validationException, job);
            throw validationException;
        }

        return updatedJob;
    }

    private void fillUpdatedVariables(JobValidationException e, TaskFlowJob job) {
        HashMap<String, String> updatedVariables = new HashMap<>();
        HashMap<String, String> updatedModels = new HashMap<>();
        HashMap<String, String> updatedDescriptions = new HashMap<>();
        HashMap<String, String> updatedGroups = new HashMap<>();
        HashMap<String, Boolean> updatedAdvanced = new HashMap<>();
        HashMap<String, Boolean> updatedHidden = new HashMap<>();
        for (JobVariable jobVariable : job.getVariables().values()) {
            updatedVariables.put(jobVariable.getName(), jobVariable.getValue());
            updatedModels.put(jobVariable.getName(), jobVariable.getModel());
            updatedDescriptions.put(jobVariable.getName(), jobVariable.getDescription());
            updatedGroups.put(jobVariable.getName(), jobVariable.getGroup());
            updatedAdvanced.put(jobVariable.getName(), jobVariable.isAdvanced());
            updatedHidden.put(jobVariable.getName(), jobVariable.isHidden());
        }
        for (Task task : job.getTasks()) {
            for (TaskVariable taskVariable : task.getVariables().values()) {
                updatedVariables.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getValue());
                updatedModels.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getModel());
                updatedDescriptions.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getDescription());
                updatedGroups.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getGroup());
                updatedAdvanced.put(task.getName() + ":" + taskVariable.getName(), taskVariable.isAdvanced());
                updatedHidden.put(task.getName() + ":" + taskVariable.getName(), taskVariable.isHidden());
            }
        }
        e.setUpdatedVariables(updatedVariables);
        e.setUpdatedModels(updatedModels);
        e.setUpdatedDescriptions(updatedDescriptions);
        e.setUpdatedGroups(updatedGroups);
        e.setUpdatedAdvanced(updatedAdvanced);
        e.setUpdatedHidden(updatedHidden);
    }

    /**
     * Start parsing and creating the job.
     *
     * @throws JobCreationException if an error occurred during job creation process.
     */
    private Job createJob(XMLStreamReader cursorRoot, Map<String, String> replacementVariables,
            Map<String, String> replacementGenericInfos, Map<String, ArrayList<String>> dependencies, String jobContent)
            throws JobCreationException {

        String current = null;
        GlobalVariablesData globalVariablesData = getConfiguredGlobalVariablesData(jobContent);
        //start parsing
        try {
            int eventType;
            Job job = null;
            while (cursorRoot.hasNext()) {
                eventType = cursorRoot.next();
                if (eventType == XMLEvent.START_ELEMENT) {
                    current = cursorRoot.getLocalName();
                    if (XMLTags.JOB.matches(current)) {
                        //first tag of the job.
                        job = createAndFillJob(cursorRoot,
                                               replacementVariables,
                                               replacementGenericInfos,
                                               jobContent,
                                               globalVariablesData);
                    } else if (XMLTags.TASK.matches(current)) {
                        //once here, the job instance has been created
                        fillJobWithTasks(cursorRoot, job, dependencies, globalVariablesData);
                    } else if (XMLTags.METADATA_VISUALIZATION.matches(current) && job != null) {
                        // Add workflow visualization's embedded html
                        job.setVisualization(getJobVisualization(cursorRoot));
                        // Metadata is the last element to parse
                        break;
                    }
                }
            }

            Map<String, JobVariable> globalVariables = new LinkedHashMap<>();
            for (String globalVariable : globalVariablesData.getVariables().keySet()) {
                globalVariables.put(globalVariable, job.getVariables().get(globalVariable));
            }
            job.setGlobalVariables(globalVariables);
            Map<String, String> globalGenericInfoMap = new LinkedHashMap<>();
            for (String globalGenericInfo : globalVariablesData.getGenericInformation().keySet()) {
                globalGenericInfoMap.put(globalGenericInfo, job.getGenericInformation().get(globalGenericInfo));
            }
            job.setGlobalGenericInformation(globalGenericInfoMap);
            return job;
        } catch (JobCreationException jce) {
            if (XMLTags.TASK.matches(current)) {
                jce.pushTag(XMLTags.TASK_FLOW.getXMLName());
            }
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(current, null, e);
        }
    }

    /**
     * Create the real job and fill it with its property. Leave the method at
     * the first tag that define the real type of job.
     *
     * @param cursorJob          the streamReader with the cursor on the job element.
     * @param submittedJobVariables job submission variables map taking priority on those defined in the xml
     * @param submittedGenericInfos job submission generic infos map taking priority on those defined in the xml
     * @param jobContent contains xml representation of this job
     * @throws JobCreationException if an exception occurs during job creation.
     */
    private Job createAndFillJob(XMLStreamReader cursorJob, Map<String, String> submittedJobVariables,
            Map<String, String> submittedGenericInfos, String jobContent, GlobalVariablesData globalVariablesData)
            throws JobCreationException {

        // A temporary job
        Job commonPropertiesHolder = new Job() {

            @Override
            public JobId getId() {
                throw new RuntimeException("Not Available!");
            }

            @Override
            public JobType getType() {
                throw new RuntimeException("Not Available!");
            }
        };

        // To allow variable replacements on the job attributes, store them until job variables are parsed
        Map<String, String> delayedJobAttributes = new LinkedHashMap<>();
        int attrLen = cursorJob.getAttributeCount();
        int i = 0;
        for (; i < attrLen; i++) {
            String attributeName = cursorJob.getAttributeLocalName(i);
            String attributeValue = cursorJob.getAttributeValue(i);
            delayedJobAttributes.put(attributeName, attributeValue);
        }

        Job job = commonPropertiesHolder;
        try {
            int eventType;

            // Start by adding to the temporary job, the global variables (with internal references enabled)
            commonPropertiesHolder.getVariables()
                                  .putAll(replaceVariablesInJobVariablesMap(globalVariablesData.getVariables(),
                                                                            convertToReplacementMap(globalVariablesData.getVariables())));
            commonPropertiesHolder.addGenericInformations(getResolvedGenericInformations(globalVariablesData.getGenericInformation(),
                                                                                         commonPropertiesHolder.getVariablesAsReplacementMap()));
            // Then add job submission variables, which will override eventually global variables
            if (submittedJobVariables != null) {
                for (String variableName : submittedJobVariables.keySet()) {
                    if (commonPropertiesHolder.getVariables().containsKey(variableName)) {
                        commonPropertiesHolder.getVariables()
                                              .get(variableName)
                                              .setValue(submittedJobVariables.get(variableName));
                    } else {
                        commonPropertiesHolder.getVariables().put(variableName,
                                                                  new JobVariable(variableName,
                                                                                  submittedJobVariables.get(variableName),
                                                                                  null));
                    }
                }
                // enable referencing of global variables by submitted variables
                commonPropertiesHolder.getVariables()
                                      .putAll(replaceVariablesInJobVariablesMap(commonPropertiesHolder.getVariables(),
                                                                                commonPropertiesHolder.getVariablesAsReplacementMap()));

            }
            // Then add job submission generic information, resolved using job submission and global variables
            if (submittedGenericInfos != null) {
                commonPropertiesHolder.addGenericInformations(getResolvedGenericInformations(submittedGenericInfos,
                                                                                             commonPropertiesHolder.getVariablesAsReplacementMap()));
            }

            // Continue to fill the temporary job with xml elements
            while (cursorJob.hasNext()) {
                eventType = cursorJob.next();
                if (eventType == XMLEvent.START_ELEMENT) {
                    String current = cursorJob.getLocalName();
                    if (XMLTags.VARIABLES.matches(current)) {

                        // Add resolved job variables using the job submission variables
                        // the final value of the variable can either be overwritten by a value of the job submission variables map or
                        // use in a pattern such value
                        Map<String, JobVariable> unresolvedJobVariablesMap = createUnresolvedJobVariables(cursorJob,
                                                                                                          globalVariablesData);
                        commonPropertiesHolder.getUnresolvedVariables().putAll(unresolvedJobVariablesMap);
                        Map<String, JobVariable> jobVariablesMap = replaceVariablesInJobVariablesMap(unresolvedJobVariablesMap,
                                                                                                     submittedJobVariables);
                        // For each variable present in the workflow, remove first the entry in commonPropertiesHolder before replacing
                        // this is to ensure preserving the order of variables defined in the workflow
                        for (String key : jobVariablesMap.keySet()) {
                            commonPropertiesHolder.getVariables().remove(key);
                        }
                        commonPropertiesHolder.getVariables().putAll(jobVariablesMap);

                    } else if (XMLTags.COMMON_GENERIC_INFORMATION.matches(current)) {
                        // Resolve the generic infos in the xml with the resolved variables
                        Map<String, String> resolvedJobVariables = commonPropertiesHolder.getVariablesAsReplacementMap();
                        Map<String, String> unresolvedGenericInformationsDefinedInWorkflow = getUnresolvedGenericInformations(cursorJob,
                                                                                                                              false,
                                                                                                                              globalVariablesData);
                        Map<String, String> resolvedGenericInformationsDefinedInWorkflow = getResolvedGenericInformations(unresolvedGenericInformationsDefinedInWorkflow,
                                                                                                                          resolvedJobVariables);
                        // Then add/replace the resolved generic infos in the xml with the ones specified at job submission
                        if (submittedGenericInfos != null) {
                            Map<String, String> submittedGenericInformations = getResolvedGenericInformations(submittedGenericInfos,
                                                                                                              commonPropertiesHolder.getVariablesAsReplacementMap());
                            resolvedGenericInformationsDefinedInWorkflow.putAll(submittedGenericInformations);
                        }

                        // Update the temporary job
                        commonPropertiesHolder.setGenericInformation(resolvedGenericInformationsDefinedInWorkflow);
                        commonPropertiesHolder.setUnresolvedGenericInformation(unresolvedGenericInformationsDefinedInWorkflow);

                    } else if (XMLTags.JOB_CLASSPATHES.matches(current)) {
                        logger.warn("Element " + XMLTags.JOB_CLASSPATHES.getXMLName() +
                                    " is no longer supported. Please define a " +
                                    XMLTags.FORK_ENVIRONMENT.getXMLName() + " per task if needed.");
                    } else if (XMLTags.COMMON_DESCRIPTION.matches(current)) {
                        commonPropertiesHolder.setDescription(getDescription(cursorJob,
                                                                             commonPropertiesHolder.getVariablesAsReplacementMap()));
                    } else if (XMLTags.DS_INPUT_SPACE.matches(current)) {
                        commonPropertiesHolder.setInputSpace(getIOSpace(cursorJob,
                                                                        commonPropertiesHolder.getVariablesAsReplacementMap()));
                    } else if (XMLTags.DS_OUTPUT_SPACE.matches(current)) {
                        commonPropertiesHolder.setOutputSpace(getIOSpace(cursorJob,
                                                                         commonPropertiesHolder.getVariablesAsReplacementMap()));
                    } else if (XMLTags.DS_GLOBAL_SPACE.matches(current)) {
                        commonPropertiesHolder.setGlobalSpace(getIOSpace(cursorJob,
                                                                         commonPropertiesHolder.getVariablesAsReplacementMap()));
                    } else if (XMLTags.DS_USER_SPACE.matches(current)) {
                        commonPropertiesHolder.setUserSpace(getIOSpace(cursorJob,
                                                                       commonPropertiesHolder.getVariablesAsReplacementMap()));
                    } else if (XMLTags.TASK_FLOW.matches(current)) {
                        job = new TaskFlowJob();
                        // Stop cursor at the beginning of 'taskflow' tag, at this level all job properties are extracted except metadata
                        break;
                    }
                }
            }

            handleJobAttributes(commonPropertiesHolder, delayedJobAttributes);

            //if this point is reached, fill the real job using the temporary one
            if (job != commonPropertiesHolder) {
                job.setDescription(commonPropertiesHolder.getDescription());
                job.setName(commonPropertiesHolder.getName());
                job.setPriority(commonPropertiesHolder.getPriority());
                job.setProjectName(commonPropertiesHolder.getProjectName());
                job.setWorkflowTags(commonPropertiesHolder.getWorkflowTags());
                job.setOnTaskError(commonPropertiesHolder.getOnTaskErrorProperty().getValue());
                job.setRestartTaskOnError(commonPropertiesHolder.getRestartTaskOnError());
                if (commonPropertiesHolder.getTaskRetryDelayProperty().isSet()) {
                    job.setTaskRetryDelay(commonPropertiesHolder.getTaskRetryDelay());
                }
                job.setMaxNumberOfExecution(commonPropertiesHolder.getMaxNumberOfExecution());
                job.setGenericInformation(commonPropertiesHolder.getGenericInformation());
                job.setUnresolvedGenericInformation(commonPropertiesHolder.getUnresolvedGenericInformation());
                job.setInputSpace(commonPropertiesHolder.getInputSpace());
                job.setOutputSpace(commonPropertiesHolder.getOutputSpace());
                job.setGlobalSpace(commonPropertiesHolder.getGlobalSpace());
                job.setUserSpace(commonPropertiesHolder.getUserSpace());
                job.setVariables(commonPropertiesHolder.getVariables());
                job.setUnresolvedVariables(commonPropertiesHolder.getUnresolvedVariables());
                job.setVisualization(commonPropertiesHolder.getVisualization());
                job.setJobContent(jobContent);

            }
            return job;
        } catch (JobCreationException jce) {
            jce.pushTag(cursorJob.getLocalName());
            throw jce;
        } catch (Exception e) {
            String temporaryAttribute = null;
            if (cursorJob.isStartElement() && cursorJob.getAttributeCount() > i) {
                temporaryAttribute = cursorJob.getAttributeLocalName(i);
            }
            throw new JobCreationException(cursorJob.getLocalName(), temporaryAttribute, e);
        }

    }

    private Map<String, String> getGenericInfoKeepReferences(Job job, Map<String, String> submittedGenericInfos) {
        // try to keep references to variables in generic information that will be exported as job content xml
        Set<String> variableNames = job.getVariables().keySet();
        Map<String, String> resolvedGenericInfo = job.getGenericInformation();
        Map<String, String> unresolvedGenericInfo = job.getUnresolvedGenericInformation();
        for (String key : resolvedGenericInfo.keySet()) {
            String unresolvedValue = unresolvedGenericInfo.get(key);
            if (submittedGenericInfos != null && submittedGenericInfos.containsKey(key)) {
                unresolvedValue = submittedGenericInfos.get(key);
            }
            final String unresolvedValueFinal = unresolvedValue;
            if (unresolvedValue != null &&
                variableNames.stream().anyMatch(name -> unresolvedValueFinal.contains("$" + name) ||
                                                        unresolvedValueFinal.contains("${" + name + "}"))) {
                resolvedGenericInfo.put(key, unresolvedValueFinal);
            }
        }
        return resolvedGenericInfo;
    }

    private synchronized GlobalVariablesData getConfiguredGlobalVariablesData(String jobContent) {
        if (handleGlobalVariables) {
            return GlobalVariablesParser.getInstance().getVariablesFor(jobContent);
        } else {
            return new GlobalVariablesData();
        }
    }

    private static Map<String, String> convertToReplacementMap(Map<String, JobVariable> jobVariables) {
        Map<String, String> replacementVariables = new LinkedHashMap<>(jobVariables.size());
        for (JobVariable variable : jobVariables.values()) {
            replacementVariables.put(variable.getName(), variable.getValue());
        }
        return replacementVariables;
    }

    private void handleJobAttributes(Job commonPropertiesHolder, Map<String, String> delayedJobAttributes)
            throws JobCreationException {
        for (Map.Entry<String, String> delayedAttribute : delayedJobAttributes.entrySet()) {
            String attributeName = delayedAttribute.getKey();
            String attributeValue = delayedAttribute.getValue();
            if (XMLAttributes.COMMON_NAME.matches(attributeName)) {
                commonPropertiesHolder.setName(replace(attributeValue,
                                                       commonPropertiesHolder.getVariablesAsReplacementMap()));
            } else if (XMLAttributes.JOB_PRIORITY.matches(attributeName)) {
                commonPropertiesHolder.setPriority(JobPriority.findPriority(replace(attributeValue,
                                                                                    commonPropertiesHolder.getVariablesAsReplacementMap())));
            } else if (XMLAttributes.COMMON_CANCEL_JOB_ON_ERROR.matches(attributeName)) {
                handleCancelJobOnErrorAttribute(commonPropertiesHolder,
                                                replace(attributeValue,
                                                        commonPropertiesHolder.getVariablesAsReplacementMap()));
            } else if (XMLAttributes.COMMON_RESTART_TASK_ON_ERROR.matches(attributeName)) {
                commonPropertiesHolder.setRestartTaskOnError(RestartMode.getMode(replace(attributeValue,
                                                                                         commonPropertiesHolder.getVariablesAsReplacementMap())));
            } else if (XMLAttributes.COMMON_TASK_RETRY_DELAY.matches(attributeName)) {
                commonPropertiesHolder.setTaskRetryDelay(Tools.formatDate(replace(attributeValue,
                                                                                  commonPropertiesHolder.getVariablesAsReplacementMap())));
            } else if (XMLAttributes.COMMON_ON_TASK_ERROR.matches(attributeName)) {
                commonPropertiesHolder.setOnTaskError(OnTaskError.getInstance(replace(attributeValue,
                                                                                      commonPropertiesHolder.getVariablesAsReplacementMap())));
            } else if (XMLAttributes.COMMON_MAX_NUMBER_OF_EXECUTION.matches(attributeName)) {
                commonPropertiesHolder.setMaxNumberOfExecution(Integer.parseInt(replace(attributeValue,
                                                                                        commonPropertiesHolder.getVariablesAsReplacementMap())));
            } else if (XMLAttributes.JOB_PROJECT_NAME.matches(attributeName)) {
                commonPropertiesHolder.setProjectName(replace(attributeValue,
                                                              commonPropertiesHolder.getVariablesAsReplacementMap()));
            } else if (XMLAttributes.JOB_TAGS.matches(attributeName)) {
                commonPropertiesHolder.setWorkflowTags(new HashSet<>(Arrays.asList(attributeValue.split("\\s*,\\s*"))));
            }
        }
    }

    private void handleCancelJobOnErrorAttribute(CommonAttribute commonPropertiesHolder, String attributeValue) {
        logger.warn(XMLAttributes.COMMON_CANCEL_JOB_ON_ERROR.getXMLName() +
                    " attribute is deprecated and no longer supported from schema 3.4+. " +
                    "Please use on task error policy to define task error behaviour. " +
                    "The attribute 'cancelJobOnError=\"true\"' is translated into " + "'onTaskError=\"cancelJob\"'.");

        if (attributeValue != null && attributeValue.equalsIgnoreCase("true")) {
            commonPropertiesHolder.setOnTaskError(OnTaskError.CANCEL_JOB);
        }
    }

    /**
     * Create a map of variables from XML variables.
     * Leave the method with the cursor at the end of 'ELEMENT_VARIABLES' tag
     *
     * @param cursorVariables the streamReader with the cursor on the 'ELEMENT_VARIABLES' tag.
     * @return the map in which the variables were added.
     * @throws JobCreationException
     */
    private Map<String, JobVariable> createUnresolvedJobVariables(XMLStreamReader cursorVariables,
            GlobalVariablesData globalVariablesData) throws JobCreationException {
        // The following initialization is to enable overriding of global variables by workflow variables
        Map<String, JobVariable> unresolvedVariablesMap = globalVariablesData.getVariables();
        try {
            int eventType;
            while (cursorVariables.hasNext()) {
                eventType = cursorVariables.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (XMLTags.VARIABLE.matches(cursorVariables.getLocalName())) {
                            Map<String, String> unresolvedAttributesAsMap = getUnresolvedAttributesAsMap(cursorVariables);

                            String name = unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_NAME.getXMLName());
                            String value = unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_VALUE.getXMLName());
                            String model = unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_MODEL.getXMLName());
                            String description = unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_DESCRIPTION.getXMLName());
                            String group = unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_GROUP.getXMLName());
                            boolean advanced = false;
                            if (unresolvedAttributesAsMap.containsKey(XMLAttributes.VARIABLE_ADVANCED.getXMLName())) {
                                advanced = Boolean.parseBoolean(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_ADVANCED.getXMLName()));
                            }
                            boolean hidden = false;
                            if (unresolvedAttributesAsMap.containsKey(XMLAttributes.VARIABLE_HIDDEN.getXMLName())) {
                                hidden = Boolean.parseBoolean(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_HIDDEN.getXMLName()));
                            }
                            unresolvedVariablesMap.put(name, new JobVariable(name,
                                                                             value,
                                                                             model,
                                                                             description,
                                                                             group,
                                                                             advanced,
                                                                             hidden));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (XMLTags.VARIABLES.matches(cursorVariables.getLocalName())) {
                            return unresolvedVariablesMap;
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
        } catch (JobCreationException jce) {
            jce.pushTag(cursorVariables.getLocalName());
            throw jce;
        } catch (Exception e) {
            String attrtmp = null;
            if (cursorVariables.isStartElement() && cursorVariables.getAttributeCount() == 1) {
                attrtmp = cursorVariables.getAttributeLocalName(0);
            }
            throw new JobCreationException(cursorVariables.getLocalName(), attrtmp, e);
        }

        return unresolvedVariablesMap;
    }

    protected Map<String, JobVariable> replaceVariablesInJobVariablesMap(Map<String, JobVariable> variablesMap,
            Map<String, String> replacementVariables) throws JobCreationException {

        LinkedHashMap<String, String> updatedReplacementVariables = new LinkedHashMap<>();
        LinkedHashMap<String, JobVariable> updatedVariablesMap = new LinkedHashMap<>(variablesMap);

        // replacements will include at first variables defined in the job
        for (JobVariable variable : updatedVariablesMap.values()) {
            updatedReplacementVariables.put(variable.getName(), variable.getValue());
        }
        if (replacementVariables != null) {
            // overwritten by variables used at job submission
            updatedReplacementVariables.putAll(replacementVariables);
        }

        for (Map.Entry<String, String> replacementVariable : updatedReplacementVariables.entrySet()) {
            String replacementVariableKey = replacementVariable.getKey();
            if (updatedVariablesMap.containsKey(replacementVariableKey)) {
                // if the variable is already defined in the job, overwrite its value by the replacement variable,
                // eventually using other variables as pattern replacements
                JobVariable jobVariable = updatedVariablesMap.get(replacementVariableKey);
                JobVariable replacedJobVariable = new JobVariable(jobVariable.getName(),
                                                                  jobVariable.getValue(),
                                                                  jobVariable.getModel(),
                                                                  jobVariable.getDescription(),
                                                                  jobVariable.getGroup(),
                                                                  jobVariable.isAdvanced(),
                                                                  jobVariable.isHidden());
                replacedJobVariable.setValue(replace(replacementVariable.getValue(), updatedReplacementVariables));
                if (replacedJobVariable.getModel() != null) {
                    // model of an existing variable can use other variables as pattern replacements
                    replacedJobVariable.setModel(replace(replacedJobVariable.getModel(), updatedReplacementVariables));
                }
                updatedVariablesMap.put(replacementVariableKey, replacedJobVariable);
            } else {
                // if the variable is not defined in the job, create a new job variable with an empty model
                updatedVariablesMap.put(replacementVariableKey,
                                        new JobVariable(replacementVariableKey,
                                                        replace(replacementVariable.getValue(),
                                                                updatedReplacementVariables),
                                                        null));
            }
        }
        return updatedVariablesMap;
    }

    /**
     * Create a map of variables from task XML variables.
     * Leave the method with the cursor at the end of 'ELEMENT_VARIABLES' tag
     *
     * @param cursorVariables the streamReader with the cursor on the 'ELEMENT_VARIABLES' tag.
     * @return the map in which the variables were added (without any resolution).
     * @throws JobCreationException
     */
    private Map<String, TaskVariable> createUnresolvedTaskVariables(XMLStreamReader cursorVariables)
            throws JobCreationException {
        LinkedHashMap<String, TaskVariable> unresolvedVariablesMap = new LinkedHashMap<>();
        try {
            int eventType;
            while (cursorVariables.hasNext()) {
                eventType = cursorVariables.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (XMLTags.VARIABLE.matches(cursorVariables.getLocalName())) {
                            Map<String, String> unresolvedAttributesAsMap = getUnresolvedAttributesAsMap(cursorVariables);

                            TaskVariable taskVariable = new TaskVariable();
                            String name = unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_NAME.getXMLName());
                            taskVariable.setName(name);
                            taskVariable.setValue(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_VALUE.getXMLName()));
                            taskVariable.setModel(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_MODEL.getXMLName()));
                            taskVariable.setDescription(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_DESCRIPTION.getXMLName()));
                            taskVariable.setGroup(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_GROUP.getXMLName()));
                            if (unresolvedAttributesAsMap.containsKey(XMLAttributes.VARIABLE_ADVANCED.getXMLName())) {
                                taskVariable.setAdvanced(Boolean.parseBoolean(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_ADVANCED.getXMLName())));
                            }
                            if (unresolvedAttributesAsMap.containsKey(XMLAttributes.VARIABLE_HIDDEN.getXMLName())) {
                                taskVariable.setHidden(Boolean.parseBoolean(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_HIDDEN.getXMLName())));
                            }
                            if (unresolvedAttributesAsMap.containsKey(XMLAttributes.VARIABLE_JOB_INHERITED.getXMLName())) {
                                taskVariable.setJobInherited(Boolean.valueOf(unresolvedAttributesAsMap.get(XMLAttributes.VARIABLE_JOB_INHERITED.getXMLName())));
                            }
                            unresolvedVariablesMap.put(name, taskVariable);
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (XMLTags.VARIABLES.matches(cursorVariables.getLocalName())) {
                            return unresolvedVariablesMap;
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
        } catch (JobCreationException jce) {
            jce.pushTag(cursorVariables.getLocalName());
            throw jce;
        } catch (Exception e) {
            String attrtmp = null;
            if (cursorVariables.isStartElement() && cursorVariables.getAttributeCount() == 1) {
                attrtmp = cursorVariables.getAttributeLocalName(0);
            }
            throw new JobCreationException(cursorVariables.getLocalName(), attrtmp, e);
        }

        return unresolvedVariablesMap;
    }

    protected Map<String, TaskVariable> replaceVariablesInTaskVariablesMap(Map<String, TaskVariable> variablesMap,
            Map<String, String> replacementVariables) throws JobCreationException {

        LinkedHashMap<String, String> updatedReplacementVariables = new LinkedHashMap<>();
        LinkedHashMap<String, TaskVariable> updatedVariablesMap = new LinkedHashMap<>(variablesMap);

        // replacements will include at first variables defined in the job
        for (TaskVariable variable : variablesMap.values()) {
            updatedReplacementVariables.put(variable.getName(), variable.getValue());
        }
        if (replacementVariables != null) {
            // overwritten by variables used at job submission
            updatedReplacementVariables.putAll(replacementVariables);
        }

        for (Map.Entry<String, String> replacementVariable : updatedReplacementVariables.entrySet()) {
            String replacementVariableKey = replacementVariable.getKey();
            if (updatedVariablesMap.containsKey(replacementVariableKey)) {
                // if the variable is already defined in the job, overwrite its value by the replacement variable,
                // eventually using other variables as pattern replacements
                TaskVariable taskVariable = updatedVariablesMap.get(replacementVariableKey);
                TaskVariable replacedTaskVariable = new TaskVariable(taskVariable.getName(),
                                                                     taskVariable.getValue(),
                                                                     taskVariable.getModel(),
                                                                     taskVariable.getDescription(),
                                                                     taskVariable.getGroup(),
                                                                     taskVariable.isAdvanced(),
                                                                     taskVariable.isHidden(),
                                                                     taskVariable.isJobInherited());
                replacedTaskVariable.setValue(replace(replacementVariable.getValue(), updatedReplacementVariables));
                if (replacedTaskVariable.getModel() != null) {
                    // model of an existing variable can use other variables as pattern replacements
                    replacedTaskVariable.setModel(replace(replacedTaskVariable.getModel(),
                                                          updatedReplacementVariables));
                }
                updatedVariablesMap.put(replacementVariableKey, replacedTaskVariable);
            }
        }
        // when the replacement variable is not defined as task variable the workflow, we do not add it
        return updatedVariablesMap;
    }

    private Map<String, String> getAttributesAsMap(XMLStreamReader cursorVariables,
            Map<String, String> replacementVariables) throws JobCreationException {
        final ImmutableMap.Builder<String, String> result = ImmutableMap.builder();

        for (int i = 0; i < cursorVariables.getAttributeCount(); i++) {
            result.put(cursorVariables.getAttributeLocalName(i),
                       replace(cursorVariables.getAttributeValue(i), replacementVariables));
        }

        return result.build();
    }

    private Map<String, String> getUnresolvedAttributesAsMap(XMLStreamReader cursorVariables)
            throws JobCreationException {
        final ImmutableMap.Builder<String, String> result = ImmutableMap.builder();

        for (int i = 0; i < cursorVariables.getAttributeCount(); i++) {
            result.put(cursorVariables.getAttributeLocalName(i), cursorVariables.getAttributeValue(i));
        }

        return result.build();
    }

    /**
     * Get the defined generic information of the entity.
     * Leave the method at the end of 'ELEMENT_COMMON_GENERIC_INFORMATION' tag.
     *
     * @param cursorInfo the streamReader with the cursor on the 'ELEMENT_COMMON_GENERIC_INFORMATION' tag.
     * @return the list of generic information as a hashMap.
     */
    private Map<String, String> getUnresolvedGenericInformations(XMLStreamReader cursorInfo, boolean isTask,
            GlobalVariablesData globalVariablesData) throws JobCreationException {
        // The following initializaion is to enable overridding of global generic info by workflow gi
        Map<String, String> infos = isTask ? new LinkedHashMap<>() : globalVariablesData.getGenericInformation();
        try {
            int eventType;
            while (cursorInfo.hasNext()) {
                eventType = cursorInfo.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (XMLTags.COMMON_INFO.matches(cursorInfo.getLocalName())) {
                            Map<String, String> attributesAsMap = getUnresolvedAttributesAsMap(cursorInfo);

                            String name = attributesAsMap.get(XMLAttributes.COMMON_NAME.getXMLName());
                            String value = attributesAsMap.get(XMLAttributes.COMMON_VALUE.getXMLName());

                            infos.put(name, value);
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (XMLTags.COMMON_GENERIC_INFORMATION.matches(cursorInfo.getLocalName())) {
                            return infos;
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
            return infos;
        } catch (JobCreationException jce) {
            jce.pushTag(cursorInfo.getLocalName());
            throw jce;
        } catch (Exception e) {
            String attrtmp = null;
            if (cursorInfo.isStartElement() && cursorInfo.getAttributeCount() == 1) {
                attrtmp = cursorInfo.getAttributeLocalName(0);
            }
            throw new JobCreationException(cursorInfo.getLocalName(), attrtmp, e);
        }
    }

    /**
     * Get the generic information of the entity, with variables resolved.
     *
     * @return the list of generic information as a hashMap.
     */
    private LinkedHashMap<String, String> getResolvedGenericInformations(
            Map<String, String> unresolvedGenericInformations, Map<String, String> variables)
            throws JobCreationException {
        LinkedHashMap<String, String> infos = new LinkedHashMap<>();
        for (Map.Entry<String, String> unresolvedGenericInformationValue : unresolvedGenericInformations.entrySet()) {
            infos.put(unresolvedGenericInformationValue.getKey(),
                      replace(unresolvedGenericInformationValue.getValue(), variables));
        }
        return infos;
    }

    /**
     * Get the description of the entity.
     * Leave the method with the cursor at the end of 'ELEMENT_COMMON_DESCRIPTION' tag.
     *
     * @param cursorVariables the streamReader with the cursor on the 'ELEMENT_COMMON_DESCRIPTION' tag.
     * @return the description between the tags.
     */
    private String getDescription(XMLStreamReader cursorVariables, Map<String, String> variables)
            throws JobCreationException {
        try {
            String description = "";
            //if description tag exists, then we have a characters event next.
            int eventType = cursorVariables.next();
            if (eventType == XMLEvent.CHARACTERS) {
                description = replace(cursorVariables.getText(), variables);
            } else if (eventType == XMLEvent.END_ELEMENT) {
                return description;
            }
            //go to the description END_ELEMENT
            while (cursorVariables.next() != XMLEvent.END_ELEMENT)
                ;

            return description;
        } catch (JobCreationException jce) {
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException((String) null, null, e);
        }
    }

    /**
     * Get job's SVG visualization as HTML String from Workflows's Metadata.
     * Leave the method with the cursor at the end of 'VISUALIZATION' tag.
     *
     * @param cursorMetadata the streamReader with the cursor on the 'VISUALIZATION' tag.
     * @return the Workflow's visualization HTML String between the tags.
     */
    private String getJobVisualization(XMLStreamReader cursorMetadata) throws JobCreationException {
        try {
            String visualization = "";
            //if visualization tag exists, then we have a characters event next.
            int eventType = cursorMetadata.next();
            if (eventType == XMLEvent.CHARACTERS) {
                visualization = cursorMetadata.getText();
            } else if (eventType == XMLEvent.END_ELEMENT) {
                return visualization;
            }
            //go to the description END_ELEMENT
            while (cursorMetadata.next() != XMLEvent.END_ELEMENT)
                ;

            return visualization;
        } catch (Exception e) {
            throw new JobCreationException((String) null, null, e);
        }
    }

    /**
     * Get the INPUT/OUTPUT space URL of the job.
     * Leave the method with the cursor at the end of 'ELEMENT_DS_INPUT/OUTPUTSPACE' tag.
     *
     * @param cursorVariables the streamReader with the cursor on the 'ELEMENT_DS_INPUT/OUTPUTSPACE' tag.
     * @return the INPUT/OUTPUT space URL of this tag.
     */
    private String getIOSpace(XMLStreamReader cursorVariables, Map<String, String> variables)
            throws JobCreationException {
        try {
            String url = replace(cursorVariables.getAttributeValue(0), variables);
            //go to the END_ELEMENT
            while (cursorVariables.next() != XMLEvent.END_ELEMENT)
                ;
            return url;
        } catch (JobCreationException jce) {
            throw jce;
        } catch (Exception e) {
            String temporaryAttribute = null;
            if (cursorVariables.isStartElement() && cursorVariables.getAttributeCount() == 1) {
                temporaryAttribute = cursorVariables.getAttributeLocalName(0);
            }
            throw new JobCreationException((String) null, temporaryAttribute, e);
        }
    }

    /**
     * Fill the created Job with coming tasks..
     * Leave the method with the cursor at the end of the file (nothing more has to be parsed).
     *
     * @param cursorTask the streamReader with the cursor on the first 'ELEMENT_TASK' tag.
     */
    private void fillJobWithTasks(XMLStreamReader cursorTask, Job job, Map<String, ArrayList<String>> dependencies,
            GlobalVariablesData globalVariablesData) throws JobCreationException {
        if (job == null) {
            throw new JobCreationException(XMLTags.JOB.getXMLName(), null, null);
        }

        XMLTags current = null;
        try {
            int eventType = -1;
            while (cursorTask.hasNext()) {
                //if use to keep the cursor on the task tag for the first loop
                if (eventType == -1) {
                    eventType = cursorTask.getEventType();
                } else {
                    eventType = cursorTask.next();
                }
                if (eventType == XMLEvent.START_ELEMENT && XMLTags.TASK.matches(cursorTask.getLocalName())) {
                    Task t;
                    switch (job.getType()) {
                        case TASKSFLOW:
                            current = XMLTags.TASK;
                            //create new task
                            t = createTask(cursorTask, job, dependencies, globalVariablesData);
                            //add task to the job
                            ((TaskFlowJob) job).addTask(t);
                            break;
                        case PARAMETER_SWEEPING:
                            current = XMLTags.TASK;
                            throw new RuntimeException("Job parameter sweeping is not yet implemented!");
                        default:
                            // do nothing just cope with sonarqube rule switch must have default
                    }
                } else {
                    // Leave the method with the cursor at the end of the taskflow tag
                    break;
                }
            }
        } catch (JobCreationException jce) {
            jce.pushTag(current);
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(current, null, e);
        }
    }

    /**
     * Fill the given task by the information that are at the given cursorTask.
     * Leave the method with the cursor at the end of 'ELEMENT_TASK' tag.
     *
     * @param cursorTask the streamReader with the cursor on the 'ELEMENT_TASK' tag.
     * @return The newly created task that can be any type.
     */
    private Task createTask(XMLStreamReader cursorTask, Job job, Map<String, ArrayList<String>> dependencies,
            GlobalVariablesData globalVariablesData) throws JobCreationException {
        int i = 0;
        XMLTags currentTag = null;
        String current = null;
        String taskName = null;
        try {
            Task toReturn = null;
            Task tmpTask = new Task() {
            };

            // To allow variable replacements on the task attributes, store them until task variables are parsed
            Map<String, String> delayedTaskAttributes = new LinkedHashMap<>();
            int attrLen = cursorTask.getAttributeCount();

            for (; i < attrLen; i++) {
                String attributeName = cursorTask.getAttributeLocalName(i);
                String attributeValue = cursorTask.getAttributeValue(i);
                delayedTaskAttributes.put(attributeName, attributeValue);
                if (XMLAttributes.COMMON_NAME.matches(attributeName)) {
                    // it is currently not possible to use variables in task names (due to the complexity of such feature)
                    taskName = attributeValue;
                    tmpTask.setName(taskName);
                }
            }

            // in the XML schema, description is defined after variables for tasks
            String delayedDescription = null;

            int eventType;
            boolean shouldContinue = true;
            while (shouldContinue && cursorTask.hasNext()) {
                eventType = cursorTask.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        current = cursorTask.getLocalName();
                        currentTag = null;
                        if (XMLTags.COMMON_GENERIC_INFORMATION.matches(current)) {

                            // Resolve task generic infos using job variables and job generic infos
                            Map<String, String> jobVariablesWithGenericInfos = tmpTask.getVariablesOverriden(job);
                            if (job.getGenericInformation() != null) {
                                jobVariablesWithGenericInfos.putAll(job.getGenericInformation());
                            }

                            Map<String, String> unresolvedGenericInformationDefinedInWorkflow = getUnresolvedGenericInformations(cursorTask,
                                                                                                                                 true,
                                                                                                                                 globalVariablesData);
                            tmpTask.setUnresolvedGenericInformation(unresolvedGenericInformationDefinedInWorkflow);
                            tmpTask.setGenericInformation(getResolvedGenericInformations(unresolvedGenericInformationDefinedInWorkflow,
                                                                                         jobVariablesWithGenericInfos));
                        } else if (XMLTags.VARIABLES.matches(current)) {
                            // Add resolved task variables using both job and task variables
                            // the final value of the variable can either be overwritten by a value of the job submission variables map or
                            // use in a pattern such value
                            Map<String, TaskVariable> unresolvedTaskVariablesMap = createUnresolvedTaskVariables(cursorTask);
                            tmpTask.getUnresolvedVariables().putAll(unresolvedTaskVariablesMap);

                            Map<String, TaskVariable> taskVariablesMap = replaceVariablesInTaskVariablesMap(unresolvedTaskVariablesMap,
                                                                                                            getInitialTaskVariablesOverriden(job,
                                                                                                                                             unresolvedTaskVariablesMap));
                            tmpTask.setVariables(taskVariablesMap);
                        } else if (XMLTags.COMMON_DESCRIPTION.matches(current)) {
                            delayedDescription = getDescription(cursorTask, Collections.EMPTY_MAP);
                        } else if (XMLTags.DS_INPUT_FILES.matches(current)) {
                            setIOFIles(cursorTask,
                                       XMLTags.DS_INPUT_FILES.getXMLName(),
                                       tmpTask,
                                       tmpTask.getVariablesOverriden(job));
                        } else if (XMLTags.DS_OUTPUT_FILES.matches(current)) {
                            setIOFIles(cursorTask,
                                       XMLTags.DS_OUTPUT_FILES.getXMLName(),
                                       tmpTask,
                                       tmpTask.getVariablesOverriden(job));
                        } else if (XMLTags.PARALLEL_ENV.matches(current)) {
                            tmpTask.setParallelEnvironment(createParallelEnvironment(cursorTask,
                                                                                     tmpTask.getVariablesOverriden(job)));
                        } else if (XMLTags.SCRIPT_SELECTION.matches(current)) {
                            tmpTask.setSelectionScripts(createSelectionScript(cursorTask,
                                                                              tmpTask.getVariablesOverriden(job)));
                        } else if (XMLTags.FORK_ENVIRONMENT.matches(current)) {
                            tmpTask.setForkEnvironment(createForkEnvironment(cursorTask,
                                                                             tmpTask.getVariablesOverriden(job)));
                        } else if (XMLTags.SCRIPT_PRE.matches(current)) {
                            tmpTask.setPreScript(createScript(cursorTask, tmpTask.getVariablesOverriden(job)));
                        } else if (XMLTags.SCRIPT_POST.matches(current)) {
                            tmpTask.setPostScript(createScript(cursorTask, tmpTask.getVariablesOverriden(job)));
                        } else if (XMLTags.SCRIPT_CLEANING.matches(current)) {
                            tmpTask.setCleaningScript(createScript(cursorTask, tmpTask.getVariablesOverriden(job)));
                        } else if (XMLTags.FLOW.matches(current)) {
                            tmpTask.setFlowScript(createControlFlowScript(cursorTask,
                                                                          tmpTask,
                                                                          tmpTask.getVariablesOverriden(job)));
                        } else if (XMLTags.TASK_DEPENDENCES.matches(current)) {
                            currentTag = XMLTags.TASK_DEPENDENCES;
                            dependencies.putAll(createDependences(cursorTask, tmpTask));
                        } else if (XMLTags.JAVA_EXECUTABLE.matches(current)) {
                            toReturn = new JavaTask();
                            setJavaExecutable((JavaTask) toReturn, cursorTask, tmpTask.getVariablesOverriden(job));
                        } else if (XMLTags.NATIVE_EXECUTABLE.matches(current)) {
                            toReturn = new NativeTask();
                            setNativeExecutable((NativeTask) toReturn, cursorTask);
                        } else if (XMLTags.SCRIPT_EXECUTABLE.matches(current)) {
                            toReturn = new ScriptTask();
                            ((ScriptTask) toReturn).setScript(new TaskScript(createScript(cursorTask,
                                                                                          tmpTask.getVariablesOverriden(job))));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        current = cursorTask.getLocalName();
                        if (XMLTags.TASK.matches(cursorTask.getLocalName())) {
                            shouldContinue = false;
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }

            // fill the task attributes using variable resolution
            handleTaskAttributes(tmpTask, job, delayedTaskAttributes);

            tmpTask.setDescription(replace(delayedDescription, tmpTask.getVariablesOverriden(job)));

            // check whether task properties has conflicts between "runAsMe" and "fork"
            if (tmpTask.isRunAsMe() && Boolean.FALSE.equals(tmpTask.isFork())) {
                throw new JobCreationException(String.format("The task contains conflicting properties between 'runAsMe=%s' and 'fork=%s', because 'runAsMe=true' implies 'fork=true'.",
                                                             tmpTask.isRunAsMe(),
                                                             tmpTask.isFork()));
            }
            //fill the real task with common attribute if it is a new one
            autoCopyfields(CommonAttribute.class, tmpTask, toReturn);
            autoCopyfields(Task.class, tmpTask, toReturn);
            if (toReturn != null) {
                if (toReturn.getRestartTaskOnErrorProperty().isSet()) {
                    toReturn.setRestartTaskOnError(toReturn.getRestartTaskOnError());
                }
                if (toReturn.getMaxNumberOfExecutionProperty().isSet()) {
                    toReturn.setMaxNumberOfExecution(toReturn.getMaxNumberOfExecution());
                }
            }
            return toReturn;
        } catch (JobCreationException jce) {
            jce.setTaskName(taskName);
            if (currentTag != null) {
                jce.pushTag(currentTag);
            } else {
                jce.pushTag(current);
            }
            throw jce;
        } catch (Exception e) {
            String attrtmp = null;
            if (cursorTask.isStartElement() && cursorTask.getAttributeCount() > i) {
                attrtmp = cursorTask.getAttributeLocalName(i);
            }
            if (currentTag != null) {
                throw new JobCreationException(currentTag, attrtmp, e);
            } else {
                throw new JobCreationException(current, attrtmp, e);
            }
        }
    }

    private void handleTaskAttributes(Task tmpTask, Job job, Map<String, String> delayedTaskAttributes)
            throws JobCreationException {
        for (Map.Entry<String, String> delayedAttribute : delayedTaskAttributes.entrySet()) {
            String attributeName = delayedAttribute.getKey();
            String attributeValue = delayedAttribute.getValue();

            if (XMLAttributes.COMMON_NAME.matches(attributeName)) {
                // it is currently not possible to use variables in task names (due to the complexity of such feature)
                tmpTask.setName(attributeValue);
            } else if (XMLAttributes.TASK_NB_NODES.matches(attributeName)) {
                int numberOfNodesNeeded = Integer.parseInt(replace(attributeValue, tmpTask.getVariablesOverriden(job)));
                tmpTask.setParallelEnvironment(new ParallelEnvironment(numberOfNodesNeeded));
            } else if (XMLAttributes.COMMON_CANCEL_JOB_ON_ERROR.matches(attributeName)) {
                handleCancelJobOnErrorAttribute(tmpTask, attributeValue);
            } else if (XMLAttributes.COMMON_ON_TASK_ERROR.matches(attributeName)) {
                tmpTask.setOnTaskError(OnTaskError.getInstance(replace(attributeValue,
                                                                       tmpTask.getVariablesOverriden(job))));
            } else if (XMLAttributes.COMMON_RESTART_TASK_ON_ERROR.matches(attributeName)) {
                tmpTask.setRestartTaskOnError(RestartMode.getMode(replace(attributeValue,
                                                                          tmpTask.getVariablesOverriden(job))));
            } else if (XMLAttributes.COMMON_TASK_RETRY_DELAY.matches(attributeName)) {
                tmpTask.setTaskRetryDelay(Tools.formatDate(replace(attributeValue,
                                                                   tmpTask.getVariablesOverriden(job))));
            } else if (XMLAttributes.COMMON_MAX_NUMBER_OF_EXECUTION.matches(attributeName)) {
                tmpTask.setMaxNumberOfExecution(Integer.parseInt(replace(attributeValue,
                                                                         tmpTask.getVariablesOverriden(job))));
            } else if (XMLAttributes.TASK_PRECIOUS_RESULT.matches(attributeName)) {
                tmpTask.setPreciousResult(Boolean.parseBoolean(replace(attributeValue,
                                                                       tmpTask.getVariablesOverriden(job))));
            } else if (XMLAttributes.TASK_PRECIOUS_LOGS.matches(attributeName)) {
                tmpTask.setPreciousLogs(Boolean.parseBoolean(replace(attributeValue,
                                                                     tmpTask.getVariablesOverriden(job))));
            } else if (XMLAttributes.TASK_WALLTIME.matches(attributeName)) {
                tmpTask.setWallTime(Tools.formatDate(replace(attributeValue, tmpTask.getVariablesOverriden(job))));
            } else if (XMLAttributes.TASK_RUN_AS_ME.matches(attributeName)) {
                tmpTask.setRunAsMe(Boolean.parseBoolean(replace(attributeValue, tmpTask.getVariablesOverriden(job))));
            } else if (XMLAttributes.TASK_FORK.matches(attributeName)) {
                tmpTask.setFork(Boolean.parseBoolean(replace(attributeValue, tmpTask.getVariablesOverriden(job))));
            }
        }
    }

    private Map<String, String> getInitialTaskVariablesOverriden(Job job,
            Map<String, TaskVariable> unresolvedTaskVariablesMap) {
        Map<String, String> taskVariables = new LinkedHashMap<>();
        if (job != null) {
            taskVariables.putAll(job.getVariablesAsReplacementMap());
        }
        for (TaskVariable variable : unresolvedTaskVariablesMap.values()) {
            if (!variable.isJobInherited()) {
                taskVariables.put(variable.getName(), variable.getValue());
            }
        }
        return taskVariables;
    }

    /**
     * Create the list of includes/excludes pattern for the given INPUT/OUTPUT files
     * Leave the method with the cursor at the end of 'ELEMENT_DS_INPUT/OUTPUTFILES' tag.
     *
     * @param cursorTask the streamReader with the cursor on the 'ELEMENT_DS_INPUT/OUTPUTFILES' tag.
     * @param endTag     the final tag for this tag : ELEMENT_DS_INPUTFILES or ELEMENT_DS_INPUTFILES
     * @param task       the task in which to add the input/output files selector
     * @throws JobCreationException
     */
    private void setIOFIles(XMLStreamReader cursorTask, String endTag, Task task, Map<String, String> variables)
            throws JobCreationException {
        int i = 0;
        try {
            int eventType;
            boolean shouldContinue = true;
            while (shouldContinue && cursorTask.hasNext()) {
                eventType = cursorTask.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String current = cursorTask.getLocalName();
                        if (XMLTags.DS_FILES.matches(current)) {
                            int attrLen = cursorTask.getAttributeCount();
                            FileSelector selector = null;
                            String accessMode = null;
                            for (i = 0; i < attrLen; i++) {
                                String attrName = cursorTask.getAttributeLocalName(i);
                                if (XMLAttributes.DS_INCLUDES.matches(attrName)) {
                                    if (selector == null) {
                                        selector = new FileSelector();
                                    }
                                    selector.setIncludes(cursorTask.getAttributeValue(i));
                                } else if (XMLAttributes.DS_EXCLUDES.matches(attrName)) {
                                    if (selector == null) {
                                        selector = new FileSelector();
                                    }
                                    selector.setExcludes(replace(cursorTask.getAttributeValue(i), variables));
                                } else if (XMLAttributes.DS_ACCESS_MODE.matches(attrName)) {
                                    accessMode = cursorTask.getAttributeValue(i);
                                }
                                if (selector != null && accessMode != null) {
                                    if (XMLTags.DS_INPUT_FILES.matches(endTag)) {
                                        task.addInputFiles(selector, InputAccessMode.getAccessMode(accessMode));
                                    } else {
                                        task.addOutputFiles(selector, OutputAccessMode.getAccessMode(accessMode));
                                    }
                                }
                            }
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorTask.getLocalName().equals(endTag)) {
                            shouldContinue = false;
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
        } catch (JobCreationException jce) {
            jce.pushTag(cursorTask.getLocalName());
            throw jce;
        } catch (Exception e) {
            String attrtmp = null;
            if (cursorTask.isStartElement() && cursorTask.getAttributeCount() > i) {
                attrtmp = cursorTask.getAttributeLocalName(i);
            }
            throw new JobCreationException(cursorTask.getLocalName(), attrtmp, e);
        }
    }

    /**
     * Add the dependencies to the current task.
     * Leave this method at the end of the 'ELEMENT_TASK_DEPENDENCES' tag.
     *
     * @param cursorDepends the streamReader with the cursor on the 'ELEMENT_TASK_DEPENDENCES' tag.
     * @param t             the task on which to apply the dependencies.
     */
    private Map<String, ArrayList<String>> createDependences(XMLStreamReader cursorDepends, Task t)
            throws JobCreationException {
        try {
            Map<String, ArrayList<String>> dependencies = new LinkedHashMap<>();

            ArrayList<String> depends = new ArrayList<>(0);
            int eventType;
            while (cursorDepends.hasNext()) {
                eventType = cursorDepends.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        if (XMLTags.TASK_DEPENDENCES_TASK.matches(cursorDepends.getLocalName())) {
                            depends.add(cursorDepends.getAttributeValue(0));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (XMLTags.TASK_DEPENDENCES.matches(cursorDepends.getLocalName())) {
                            dependencies.put(t.getName(), depends);
                            return dependencies;
                        }

                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
            return dependencies;
        } catch (Exception e) {
            String attrtmp = null;
            if (cursorDepends.isStartElement() && cursorDepends.getAttributeCount() == 1) {
                attrtmp = cursorDepends.getAttributeLocalName(0);
            }
            throw new JobCreationException(cursorDepends.getLocalName(), attrtmp, e);
        }
    }

    private FlowScript createControlFlowScript(XMLStreamReader cursorTask, Task tmpTask, Map<String, String> variables)
            throws JobCreationException {
        String type = null;
        String target = null;
        String targetElse = null;
        String targetJoin = null;
        int event = -1;

        for (int i = 0; i < cursorTask.getAttributeCount(); i++) {
            String attrName = cursorTask.getAttributeLocalName(i);
            if (XMLAttributes.FLOW_BLOCK.matches(attrName)) {
                tmpTask.setFlowBlock(FlowBlock.parse(replace(cursorTask.getAttributeValue(i), variables)));
            }
        }

        // <control>  =>  <if> | <loop> | <replicate>
        try {
            while (cursorTask.hasNext()) {
                event = cursorTask.next();
                if (event == XMLEvent.START_ELEMENT) {
                    break;
                } else if (event == XMLEvent.END_ELEMENT && XMLTags.FLOW.matches(cursorTask.getLocalName())) {
                    return null;
                }
            }
        } catch (Exception e) {
            throw new JobCreationException(XMLTags.FLOW.getXMLName(), null, e);
        }
        if (event != XMLEvent.START_ELEMENT) {
            throw new JobCreationException(XMLTags.FLOW.getXMLName(), null, null);
        }

        String tag = null;

        // REPLICATE: no attribute
        if (XMLTags.FLOW_REPLICATE.matches(cursorTask.getLocalName())) {
            type = FlowActionType.REPLICATE.toString();
            tag = XMLTags.FLOW_REPLICATE.getXMLName();
        }
        // IF: attributes TARGET_IF and TARGET_ELSE and TARGET_JOIN
        else if (XMLTags.FLOW_IF.matches(cursorTask.getLocalName())) {
            type = FlowActionType.IF.toString();
            tag = XMLTags.FLOW_IF.getXMLName();
            for (int i = 0; i < cursorTask.getAttributeCount(); i++) {
                String attrName = cursorTask.getAttributeLocalName(i);
                if (XMLAttributes.FLOW_TARGET.matches(attrName)) {
                    target = cursorTask.getAttributeValue(i);
                } else if (XMLAttributes.FLOW_ELSE.matches(attrName)) {
                    targetElse = cursorTask.getAttributeValue(i);
                } else if (XMLAttributes.FLOW_CONTINUATION.matches(attrName)) {
                    targetJoin = cursorTask.getAttributeValue(i);
                }
            }
        }
        // LOOP: attribute TARGET
        else if (XMLTags.FLOW_LOOP.matches(cursorTask.getLocalName())) {
            type = FlowActionType.LOOP.toString();
            tag = XMLTags.FLOW_LOOP.getXMLName();
            for (int i = 0; i < cursorTask.getAttributeCount(); i++) {
                String attrName = cursorTask.getAttributeLocalName(i);
                if (XMLAttributes.FLOW_TARGET.matches(attrName)) {
                    target = cursorTask.getAttributeValue(i);
                }
            }
        }
        FlowScript sc = null;
        Script<?> internalScript;
        try {
            internalScript = createScript(cursorTask, ScriptType.FLOW, variables);
            switch (FlowActionType.parse(type)) {
                case IF:
                    sc = FlowScript.createIfFlowScript(internalScript, target, targetElse, targetJoin);
                    break;
                case REPLICATE:
                    sc = FlowScript.createReplicateFlowScript(internalScript);
                    break;
                case LOOP:
                    sc = FlowScript.createLoopFlowScript(internalScript, target);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new JobCreationException(tag, null, e);
        }

        // </script>  -->  </if> | </replicate> | </loop>
        try {
            while (cursorTask.hasNext()) {
                event = cursorTask.next();
                if (event == XMLEvent.END_ELEMENT) {
                    break;
                }
            }
        } catch (XMLStreamException e) {
            throw new JobCreationException(tag, null, e);
        }
        if (event != XMLEvent.END_ELEMENT) {
            throw new JobCreationException(tag, null, null);
        }

        return sc;
    }

    /**
     * Creates the parallel environment from the xml descriptor.
     */
    private ParallelEnvironment createParallelEnvironment(XMLStreamReader cursorTask, Map<String, String> variables)
            throws JobCreationException {
        int event = -1;
        int nodesNumber = 0;
        TopologyDescriptor topologyDescriptor = null;

        // parallelEnvironment -> <topology>
        try {
            // cursor is parallelEnvironment
            for (int i = 0; i < cursorTask.getAttributeCount(); i++) {
                String attrName = cursorTask.getAttributeLocalName(i);
                if (XMLAttributes.TASK_NB_NODES.matches(attrName)) {
                    String value = replace(cursorTask.getAttributeValue(i), variables);
                    nodesNumber = Integer.parseInt(value);
                }
            }

            while (cursorTask.hasNext()) {
                event = cursorTask.next();
                if (event == XMLEvent.START_ELEMENT) {
                    break;
                } else if (event == XMLEvent.END_ELEMENT && XMLTags.PARALLEL_ENV.matches(cursorTask.getLocalName())) {
                    return new ParallelEnvironment(nodesNumber, TopologyDescriptor.ARBITRARY);
                }
            }

            if (XMLTags.TOPOLOGY.matches(cursorTask.getLocalName())) {
                // topology element found
                while (cursorTask.hasNext()) {
                    event = cursorTask.next();
                    if (event == XMLEvent.START_ELEMENT) {
                        break;
                    } else if (event == XMLEvent.END_ELEMENT && XMLTags.TOPOLOGY.matches(cursorTask.getLocalName())) {
                        throw new RuntimeException("Incorrect topology description");
                    }
                }

                // arbitrary : no attributes
                if (XMLTags.TOPOLOGY_ARBITRARY.matches(cursorTask.getLocalName())) {
                    topologyDescriptor = TopologyDescriptor.ARBITRARY;
                }
                // bestProximity : no attributes
                else if (XMLTags.TOPOLOGY_BEST_PROXIMITY.matches(cursorTask.getLocalName())) {
                    topologyDescriptor = TopologyDescriptor.BEST_PROXIMITY;
                }
                // thresholdProximity : elements threshold
                else if (XMLTags.TOPOLOGY_THRESHOLD_PROXIMITY.matches(cursorTask.getLocalName())) {
                    // attribute threshold
                    for (int i = 0; i < cursorTask.getAttributeCount(); i++) {
                        String attrName = cursorTask.getAttributeLocalName(i);
                        if (XMLAttributes.TOPOLOGY_THRESHOLD.matches(attrName)) {
                            String value = replace(cursorTask.getAttributeValue(i), variables);
                            long threshold = Long.parseLong(value);
                            topologyDescriptor = new ThresholdProximityDescriptor(threshold);
                        }
                    }
                }
                // singleHost : no attributes
                else if (XMLTags.TOPOLOGY_SINGLE_HOST.matches(cursorTask.getLocalName())) {
                    topologyDescriptor = TopologyDescriptor.SINGLE_HOST;
                }
                // singleHostExclusive : no attributes
                else if (XMLTags.TOPOLOGY_SINGLE_HOST_EXCLUSIVE.matches(cursorTask.getLocalName())) {
                    topologyDescriptor = TopologyDescriptor.SINGLE_HOST_EXCLUSIVE;
                }
                // multipleHostsExclusive : no attributes
                else if (XMLTags.TOPOLOGY_MULTIPLE_HOSTS_EXCLUSIVE.matches(cursorTask.getLocalName())) {
                    topologyDescriptor = TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE;
                }
                // oneNodePerHostHostsExclusive : no attributes
                else if (XMLTags.TOPOLOGY_DIFFERENT_HOSTS_EXCLUSIVE.matches(cursorTask.getLocalName())) {
                    topologyDescriptor = TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE;
                }
            }

        } catch (Exception e) {
            throw new JobCreationException(XMLTags.TOPOLOGY.getXMLName(), null, e);
        }

        return new ParallelEnvironment(nodesNumber, topologyDescriptor);
    }

    /**
     * Get the script defined at the specified cursor.
     * Leave the method with cursor at the end of the corresponding script.
     *
     * @param cursorScript the streamReader with the cursor on the corresponding script tag (pre, post, cleaning, selection, generation).
     * @param type         nature of the script
     * @return the script  defined at the specified cursor.
     */
    private Script<?> createScript(XMLStreamReader cursorScript, ScriptType type, Map<String, String> variables)
            throws JobCreationException {
        String attrtmp = null;
        String currentScriptTag = cursorScript.getLocalName();
        String current = null;
        try {
            boolean isDynamic = true;
            Script<?> toReturn = null;
            int eventType = -1;
            while (cursorScript.hasNext()) {
                if (type == ScriptType.SELECTION && eventType == -1) {
                    eventType = cursorScript.getEventType();
                } else {
                    eventType = cursorScript.next();
                }
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        current = cursorScript.getLocalName();
                        if (XMLTags.SCRIPT_CODE.matches(current)) {
                            String language = null;
                            String content = "";
                            if (cursorScript.getAttributeCount() > 0) {
                                language = cursorScript.getAttributeValue(0);
                                attrtmp = cursorScript.getAttributeLocalName(0);
                            }
                            //goto script content
                            if (cursorScript.next() == XMLEvent.CHARACTERS) {
                                content = cursorScript.getText();
                            }
                            toReturn = new SimpleScript(content, language);
                            //fast forward to the end of tag
                            while (true) {
                                int ev = cursorScript.next();
                                if (XMLTags.SCRIPT_CODE.matches(current) && ev == XMLEvent.END_ELEMENT) {
                                    break;
                                }
                            }
                        } else if (XMLTags.SCRIPT_FILE.matches(current)) {
                            String path = null;
                            String url = null;
                            String language = null;
                            for (int i = 0; i < cursorScript.getAttributeCount(); i++) {
                                attrtmp = cursorScript.getAttributeLocalName(i);
                                if (XMLAttributes.SCRIPT_URL.matches(attrtmp)) {
                                    url = replace(cursorScript.getAttributeValue(i), variables);
                                } else if (XMLAttributes.LANGUAGE.matches(attrtmp)) {
                                    language = replace(cursorScript.getAttributeValue(i), variables);
                                } else if (XMLAttributes.PATH.matches(attrtmp)) {
                                    path = checkPath(cursorScript.getAttributeValue(i), variables);
                                } else {
                                    throw new JobCreationException("Unrecognized attribute : " + attrtmp);
                                }
                            }

                            //go to the next 'arguments' start element or the 'file' end element
                            while (true) {
                                int ev = cursorScript.next();
                                if (((ev == XMLEvent.START_ELEMENT) &&
                                     XMLTags.SCRIPT_ARGUMENTS.matches(cursorScript.getLocalName())) ||
                                    (ev == XMLEvent.END_ELEMENT)) {
                                    break;
                                }
                            }

                            if (url != null) {
                                if (language != null) {
                                    toReturn = new SimpleScript(new URL(url), language, getArguments(cursorScript));
                                } else {
                                    toReturn = new SimpleScript(new URL(url), getArguments(cursorScript));
                                }
                            } else if (path != null) {
                                // language is ignored if a File is provided, the script language will be determined based on the file extension
                                toReturn = new SimpleScript(new File(path), getArguments(cursorScript));
                            } else {
                                attrtmp = null;
                                throw new JobCreationException("Invalid script file definition, one of path/url attributes must be declared");
                            }
                        } else if (XMLTags.SCRIPT_ARGUMENTS.matches(current)) {
                            toReturn = new SimpleScript(toReturn.getScript(),
                                                        toReturn.getEngineName(),
                                                        getArguments(cursorScript));
                        } else if (XMLTags.SCRIPT_SCRIPT.matches(current) && cursorScript.getAttributeCount() > 0) {
                            isDynamic = !"static".equals(cursorScript.getAttributeValue(0));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (cursorScript.getLocalName().equals(currentScriptTag)) {
                            if (type == ScriptType.SELECTION) {
                                return new SelectionScript(toReturn, isDynamic);
                            } else {
                                return toReturn;
                            }
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
            return toReturn;
        } catch (JobCreationException jce) {
            jce.pushTag(current);
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(current, attrtmp, e);
        }
    }

    /**
     * Get the selection script defined at the specified cursor.
     * Leave the method with cursor at the end of the 'ELEMENT_SCRIPT_SELECTION' script.
     *
     * @param cursorScript the streamReader with the cursor on the 'ELEMENT_SCRIPT_SELECTION' tag.
     * @return the script defined at the specified cursor.
     */
    private List<SelectionScript> createSelectionScript(XMLStreamReader cursorScript, Map<String, String> variables)
            throws JobCreationException {
        List<SelectionScript> scripts = new ArrayList<>(0);
        String selectionTag = cursorScript.getLocalName();
        String current = null;
        try {
            SelectionScript newOne;
            int eventType;
            while (cursorScript.hasNext()) {
                eventType = cursorScript.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        current = cursorScript.getLocalName();
                        if (XMLTags.SCRIPT_SCRIPT.matches(current)) {
                            newOne = (SelectionScript) createScript(cursorScript, ScriptType.SELECTION, variables);
                            scripts.add(newOne);
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        current = cursorScript.getLocalName();
                        if (current.equals(selectionTag)) {
                            return scripts.isEmpty() ? null : scripts;
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
        } catch (JobCreationException jce) {
            jce.pushTag(current);
            throw jce;
        } catch (Exception e) {
            throw new JobCreationException(current, null, e);
        }
        return scripts;
    }

    /**
     * Get the script defined at the specified cursor.
     * Leave the method with cursor at the end of the corresponding script.
     *
     * @param cursorScript the streamReader with the cursor on the corresponding script tag (env, pre, post, cleaning, generation).
     * @return the script defined at the specified cursor.
     */
    private Script<?> createScript(XMLStreamReader cursorScript, Map<String, String> variables)
            throws JobCreationException {
        try {
            return createScript(cursorScript, ScriptType.OTHER, variables);
        } catch (JobCreationException jce) {
            jce.pushTag(XMLTags.SCRIPT_SCRIPT.getXMLName());
            throw jce;
        }
    }

    /**
     * Get the arguments at the given tag and return them as a string array.
     * Leave the cursor on the end 'ELEMENT_SCRIPT_ARGUMENTS' tag.
     *
     * @param cursorArgs the streamReader with the cursor on the 'ELEMENT_SCRIPT_ARGUMENTS' tag.
     * @return the arguments as a string array, null if no args.
     */
    private String[] getArguments(XMLStreamReader cursorArgs) throws JobCreationException {
        if (XMLTags.SCRIPT_ARGUMENTS.matches(cursorArgs.getLocalName())) {
            ArrayList<String> args = new ArrayList<>(0);
            try {
                int eventType;
                while (cursorArgs.hasNext()) {
                    eventType = cursorArgs.next();
                    switch (eventType) {
                        case XMLEvent.START_ELEMENT:
                            if (XMLTags.SCRIPT_ARGUMENT.matches(cursorArgs.getLocalName())) {
                                args.add(cursorArgs.getAttributeValue(0));
                            }
                            break;
                        case XMLEvent.END_ELEMENT:
                            if (XMLTags.SCRIPT_ARGUMENTS.matches(cursorArgs.getLocalName())) {
                                return args.toArray(new String[args.size()]);
                            }
                            break;
                        default:
                            // do nothing just cope with sonarqube rule switch must have default
                    }
                }
                return args.toArray(new String[args.size()]);
            } catch (Exception e) {
                String temporaryAttribute = null;
                if (cursorArgs.isStartElement() && cursorArgs.getAttributeCount() == 1) {
                    temporaryAttribute = cursorArgs.getAttributeLocalName(0);
                }
                throw new JobCreationException(cursorArgs.getLocalName(), temporaryAttribute, e);
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
    private void setNativeExecutable(NativeTask nativeTask, XMLStreamReader cursorExec) throws JobCreationException {
        int i = 0;
        String current = null;
        try {
            //one step ahead to go to the command (static or dynamic)
            while (cursorExec.next() != XMLEvent.START_ELEMENT)
                ;

            current = cursorExec.getLocalName();
            ArrayList<String> command = new ArrayList<>(0);
            if (XMLTags.NATIVE_TASK_STATIC_COMMAND.matches(cursorExec.getLocalName())) {
                String attr_ = null;
                String current_ = null;
                try {
                    for (i = 0; i < cursorExec.getAttributeCount(); i++) {
                        String attrName = cursorExec.getAttributeLocalName(i);
                        attr_ = attrName;
                        if (XMLAttributes.TASK_COMMAND_VALUE.matches(attrName)) {
                            command.add((cursorExec.getAttributeValue(i)));
                        }
                        if (XMLAttributes.TASK_WORKDING_DIR.matches(attrName)) {
                            logger.warn(XMLAttributes.TASK_WORKDING_DIR.getXMLName() +
                                        " attribute no longer supported. Please use a forkEnvironment for defining a working directory.");
                        }
                    }

                    int eventType;
                    while (cursorExec.hasNext()) {
                        eventType = cursorExec.next();
                        switch (eventType) {
                            case XMLEvent.START_ELEMENT:
                                current_ = cursorExec.getLocalName();
                                if (XMLTags.SCRIPT_ARGUMENT.matches(cursorExec.getLocalName())) {
                                    command.add((cursorExec.getAttributeValue(0)));
                                }
                                break;
                            case XMLEvent.END_ELEMENT:
                                if (XMLTags.NATIVE_EXECUTABLE.matches(cursorExec.getLocalName())) {
                                    nativeTask.setCommandLine(command.toArray(new String[command.size()]));
                                    return;
                                }
                                break;
                            default:
                                // do nothing just cope with sonarqube rule switch must have default
                        }
                    }
                } catch (Exception e) {
                    throw new JobCreationException(current_, attr_, e);
                }
            } else {
                throw new RuntimeException("Unknown command type: " + cursorExec.getLocalName());
            }
        } catch (JobCreationException jce) {
            jce.pushTag(current);
            throw jce;
        } catch (Exception e) {
            String temporaryAttribute = null;
            if (cursorExec.isStartElement() && cursorExec.getAttributeCount() > 0) {
                temporaryAttribute = cursorExec.getAttributeLocalName(i);
            }
            throw new JobCreationException(current, temporaryAttribute, e);
        }
    }

    /**
     * Add the Java Executable to this java Task.
     * The cursor is currently at the beginning of the 'ELEMENT_JAVA_EXECUTABLE' tag.
     *
     * @param javaTask   the task in which to add the Java Executable.
     * @param cursorExec the streamReader with the cursor on the 'ELEMENT_JAVA_EXECUTABLE' tag.
     */
    private void setJavaExecutable(JavaTask javaTask, XMLStreamReader cursorExec, Map<String, String> variables)
            throws JobCreationException {
        int i = 0;
        String current = cursorExec.getLocalName();
        try {
            //parsing executable attributes
            int attrCount = cursorExec.getAttributeCount();
            for (i = 0; i < attrCount; i++) {
                String attrName = cursorExec.getAttributeLocalName(i);
                if (XMLAttributes.TASK_CLASS_NAME.matches(attrName)) {
                    javaTask.setExecutableClassName(cursorExec.getAttributeValue(i));
                }
            }
            //parsing executable tags
            int eventType;
            while (cursorExec.hasNext()) {
                eventType = cursorExec.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        current = cursorExec.getLocalName();
                        if (XMLTags.FORK_ENVIRONMENT.matches(current)) {
                            ForkEnvironment forkEnv = createForkEnvironment(cursorExec, variables);
                            javaTask.setForkEnvironment(forkEnv);
                        } else if (XMLTags.TASK_PARAMETER.matches(current)) {
                            Map<String, String> attributesAsMap = getAttributesAsMap(cursorExec, variables);

                            String name = attributesAsMap.get(XMLAttributes.VARIABLE_NAME.getXMLName());
                            String value = attributesAsMap.get(XMLAttributes.VARIABLE_VALUE.getXMLName());

                            javaTask.addArgument(replace(name, variables), value);
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (XMLTags.JAVA_EXECUTABLE.matches(cursorExec.getLocalName())) {
                            return;
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
        } catch (JobCreationException jce) {
            jce.pushTag(current);
            throw jce;
        } catch (Exception e) {
            String attrtmp = null;
            if (cursorExec.isStartElement() && cursorExec.getAttributeCount() > 0) {
                attrtmp = cursorExec.getAttributeLocalName(i);
            }
            throw new JobCreationException(current, attrtmp, e);
        }
    }

    /**
     * Create the forkEnvironment of a java task
     * The cursor is currently at the beginning of the 'FORK_ENVIRONMENT' tag.
     *
     * @param cursorExec the streamReader with the cursor on the 'FORK_ENVIRONMENT' tag.
     * @return The created ForkEnvironment
     */
    private ForkEnvironment createForkEnvironment(XMLStreamReader cursorExec, Map<String, String> variables)
            throws JobCreationException {
        ForkEnvironment forkEnv = new ForkEnvironment();
        int i = 0;
        String current = cursorExec.getLocalName();
        try {
            //parsing executable attributes
            int attrCount = cursorExec.getAttributeCount();
            for (i = 0; i < attrCount; i++) {
                String attrName = cursorExec.getAttributeLocalName(i);
                if (XMLAttributes.FORK_JAVA_HOME.matches(attrName)) {
                    forkEnv.setJavaHome(replace(cursorExec.getAttributeValue(i), variables));
                }
                if (XMLAttributes.TASK_WORKDING_DIR.matches(attrName)) {
                    forkEnv.setWorkingDir(replace(cursorExec.getAttributeValue(i), variables));
                }
            }
            //parsing executable tags
            int eventType;
            while (cursorExec.hasNext()) {
                eventType = cursorExec.next();
                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        current = cursorExec.getLocalName();
                        if (XMLTags.FORK_SYSTEM_PROPERTY.matches(current)) {
                            attrCount = cursorExec.getAttributeCount();

                            String name = null;
                            String value = null;
                            for (i = 0; i < attrCount; i++) {
                                String attrName = cursorExec.getAttributeLocalName(i);
                                if (XMLAttributes.COMMON_NAME.matches(attrName)) {
                                    name = replace(cursorExec.getAttributeValue(i), variables);
                                }
                                if (XMLAttributes.COMMON_VALUE.matches(attrName)) {
                                    value = replace(cursorExec.getAttributeValue(i), variables);
                                }
                            }

                            forkEnv.addSystemEnvironmentVariable(name, value);
                        } else if (XMLTags.FORK_JVM_ARG.matches(current)) {
                            forkEnv.addJVMArgument(replace(cursorExec.getAttributeValue(0), variables));
                        } else if (XMLTags.JOB_PATH_ELEMENT.matches(current)) {
                            forkEnv.addAdditionalClasspath(replace(cursorExec.getAttributeValue(0), variables));
                        } else if (XMLTags.SCRIPT_ENV.matches(current)) {
                            forkEnv.setEnvScript(new ForkEnvironmentScript(createScript(cursorExec, variables)));
                        }
                        break;
                    case XMLEvent.END_ELEMENT:
                        if (XMLTags.FORK_ENVIRONMENT.matches(cursorExec.getLocalName())) {
                            return forkEnv;
                        }
                        break;
                    default:
                        // do nothing just cope with sonarqube rule switch must have default
                }
            }
            return forkEnv;
        } catch (JobCreationException jce) {
            jce.pushTag(current);
            throw jce;
        } catch (Exception e) {
            String attrtmp = null;
            if (cursorExec.isStartElement() && cursorExec.getAttributeCount() > 0) {
                attrtmp = cursorExec.getAttributeLocalName(i);
            }
            throw new JobCreationException(current, attrtmp, e);
        }
    }

    /**
     * Construct the dependencies between tasks.
     *
     * @throws JobCreationException if a dependencies name is unknown.
     */
    private void makeDependences(Job job, Map<String, ArrayList<String>> dependencies) throws JobCreationException {
        if (dependencies != null && !dependencies.isEmpty() && job.getType() == JobType.TASKSFLOW) {
            TaskFlowJob tfj = (TaskFlowJob) job;
            for (Task t : tfj.getTasks()) {
                ArrayList<String> names = dependencies.get(t.getName());
                createTaskDependencies(tfj, t, names);
            }
        }
    }

    private void createTaskDependencies(TaskFlowJob tfj, Task t, ArrayList<String> dependencies)
            throws JobCreationException {
        if (dependencies != null) {
            for (String name : dependencies) {
                t.addDependence(Optional.ofNullable(tfj.getTask(name))
                                        .orElseThrow(() -> new JobCreationException("Unknown dependence: " + name)));
            }
        }
    }

    /**
     * Replace the variables inside the given string by its value if needed.<br/>
     * This method looks for ${...} pattern and replace this pattern by the corresponding user variable
     * define in the 'ELEMENT_VARIABLES' tag.
     *
     * @param str the string in which to look for.
     * @param variables a map of variables.
     * @return the string with variables replaced by values.
     * @throws JobCreationException if a Variable has not been found
     */
    private static String replace(String str, Map<String, String> variables) throws JobCreationException {

        // Include System Variables
        Map<String, String> replacements = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> o : System.getProperties().entrySet()) {
            replacements.put(o.getKey().toString(), o.getValue().toString());
        }

        // Include useful "global" scheduler variables
        replacements.put(SchedulerVars.PA_CATALOG_REST_URL.toString(),
                         PASchedulerProperties.CATALOG_REST_URL.getValueAsString());
        replacements.put(SchedulerVars.PA_SCHEDULER_REST_URL.toString(),
                         PASchedulerProperties.SCHEDULER_REST_URL.getValueAsString());
        replacements.put(SchedulerVars.PA_CLOUD_AUTOMATION_REST_URL.toString(),
                         PASchedulerProperties.CLOUD_AUTOMATION_REST_URL.getValueAsString());
        replacements.put(SchedulerVars.PA_JOB_PLANNER_REST_URL.toString(),
                         PASchedulerProperties.JOB_PLANNER_REST_URL.getValueAsString());
        replacements.put(SchedulerVars.PA_NOTIFICATION_SERVICE_REST_URL.toString(),
                         PASchedulerProperties.NOTIFICATION_SERVICE_REST_URL.getValueAsString());

        replacements.put(SchedulerVars.PA_SCHEDULER_REST_PUBLIC_URL.toString(),
                         PASchedulerProperties.SCHEDULER_REST_PUBLIC_URL.getValueAsString());
        replacements.put(SchedulerVars.PA_CATALOG_REST_PUBLIC_URL.toString(),
                         PASchedulerProperties.CATALOG_REST_PUBLIC_URL.getValueAsString());
        replacements.put(SchedulerVars.PA_CLOUD_AUTOMATION_REST_PUBLIC_URL.toString(),
                         PASchedulerProperties.CLOUD_AUTOMATION_REST_PUBLIC_URL.getValueAsString());
        replacements.put(SchedulerVars.PA_JOB_PLANNER_REST_PUBLIC_URL.toString(),
                         PASchedulerProperties.JOB_PLANNER_REST_PUBLIC_URL.getValueAsString());
        replacements.put(SchedulerVars.PA_NOTIFICATION_SERVICE_REST_PUBLIC_URL.toString(),
                         PASchedulerProperties.NOTIFICATION_SERVICE_REST_PUBLIC_URL.getValueAsString());

        // Include given variables if any
        if (variables != null) {
            for (Map.Entry<String, String> variable : variables.entrySet()) {
                String syspropWithVariableName = System.getProperty(variable.getKey());
                if (syspropWithVariableName != null) {
                    replacements.put(variable.getKey(), syspropWithVariableName);
                } else {
                    replacements.put(variable.getKey(), variable.getValue());
                }
            }
        }
        return filterAndUpdate(str, replacements);
    }

    /**
     * Replace the given file path by prepending relative root path if needed.<br/>
     * This method prepends the relative root path to the given path if it is not considered as an absolute path.
     *
     * @param path the path to be evaluated.
     * @return the same path with ${...} variables replaced and the relative path directory if this path was not absolute.
     * @throws JobCreationException if a Variable has not been found
     */
    private String checkPath(String path, Map<String, String> variables) throws JobCreationException {
        if (path == null || "".equals(path)) {
            return path;
        }
        //make variables replacement
        path = replace(path, variables);
        //prepend if file is relative
        File f = new File(path);
        return f.isAbsolute() ? path : relativePathRoot + File.separator + path;
    }

    private void displayJobInfo(Job job) {
        if (logger.isDebugEnabled()) {
            logger.debug("type: " + job.getType());
            logger.debug("name: " + job.getName());
            logger.debug("description: " + job.getDescription());
            logger.debug("projectName: " + job.getProjectName());
            logger.debug("tags: " + job.getWorkflowTags());
            logger.debug("variables: " + job.getVariables());
            logger.debug("priority: " + job.getPriority());
            logger.debug("onTaskError: " + job.getOnTaskErrorProperty().getValue().toString());
            logger.debug("restartTaskOnError: " + job.getRestartTaskOnError());
            logger.debug("TaskRetryDelay: " + job.getTaskRetryDelay());
            logger.debug("maxNumberOfExecution: " + job.getMaxNumberOfExecution());
            logger.debug("inputSpace: " + job.getInputSpace());
            logger.debug("outputSpace: " + job.getOutputSpace());
            logger.debug("genericInformation: " + job.getGenericInformation());
            logger.debug("visualization: " + job.getVisualization());
            logger.debug("jobContent: " + job.getJobContent());
            logger.debug("TASKS ------------------------------------------------");

            ArrayList<Task> tasks = job.getType().equals(JobType.TASKSFLOW) ? ((TaskFlowJob) job).getTasks()
                                                                            : new ArrayList<>();

            for (Task t : tasks) {
                logger.debug("name: " + t.getName());
                logger.debug("description: " + t.getDescription());
                logger.debug("parallel: " + t.isParallel());
                logger.debug("nbNodes: " +
                             (t.getParallelEnvironment() == null ? "1" : t.getParallelEnvironment().getNodesNumber()));
                logger.debug("onTaskError: " + t.getOnTaskErrorProperty().getValue().toString());
                logger.debug("preciousResult: " + t.isPreciousResult());
                logger.debug("preciousLogs: " + t.isPreciousLogs());
                logger.debug("restartTaskOnError: " + t.getRestartTaskOnError());
                logger.debug("taskRetryDelay: " + t.getTaskRetryDelay());
                logger.debug("maxNumberOfExecution: " + t.getMaxNumberOfExecution());
                logger.debug("walltime: " + t.getWallTime());
                logger.debug("selectionScripts: " + t.getSelectionScripts());
                logger.debug("preScript: " + t.getPreScript());
                logger.debug("postScript: " + t.getPostScript());
                logger.debug("cleaningScript: " + t.getCleaningScript());

                try {
                    logger.debug("inputFileList: length=" + t.getInputFilesList().size());
                    logger.debug("outputFileList: length=" + t.getOutputFilesList().size());
                } catch (NullPointerException ignored) {
                    // Ignore this exception
                }

                if (t.getDependencesList() != null) {
                    StringBuilder dep = new StringBuilder("dependence: ");
                    t.getDependencesList().forEach(tdep -> dep.append(tdep.getName() + " "));
                    logger.debug(dep);
                } else {
                    logger.debug("dependence: null");
                }

                logger.debug("genericInformation: " + t.getGenericInformation());
                logger.debug("variables: " + t.getVariables());

                if (t instanceof JavaTask) {
                    logger.debug("class: " + ((JavaTask) t).getExecutableClassName());
                    try {
                        logger.debug("args: " + ((JavaTask) t).getArguments());
                    } catch (Exception e) {
                        logger.debug("Cannot get args: " + e.getMessage(), e);
                    }
                    logger.debug("fork: " + t.isFork());
                    logger.debug("runAsMe: " + t.isRunAsMe());
                } else if (t instanceof NativeTask) {
                    logger.debug("commandLine: " + Arrays.toString(((NativeTask) t).getCommandLine()));
                } else if (t instanceof ScriptTask) {
                    logger.debug("script: " + ((ScriptTask) t).getScript());
                }

                ForkEnvironment forkEnvironment = t.getForkEnvironment();

                if (forkEnvironment != null) {
                    logger.debug("javaHome: " + forkEnvironment.getJavaHome());
                    logger.debug("systemEnvironment: " + forkEnvironment.getSystemEnvironment());
                    logger.debug("jvmArguments: " + forkEnvironment.getJVMArguments());
                    logger.debug("classpath: " + forkEnvironment.getAdditionalClasspath());
                    logger.debug("envScript: " + forkEnvironment.getEnvScript());
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
     * @param <T>   check type given as argument is equals or under this type.
     * @param klass the klass in which to find the fields
     * @param from  the T object in which to get the value
     * @param to    the T object in which to set the value
     */
    private static <T> void autoCopyfields(Class<T> klass, T from, T to)
            throws IllegalArgumentException, IllegalAccessException {
        for (Field f : klass.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                Object newValue = f.get(from);
                if (newValue != null || f.get(to) == null) {
                    f.set(to, newValue);
                }
            }
        }
    }

    private static void resolveCleaningScripts(TaskFlowJob job, Map<String, String> replacementVariables) {
        for (Task task : job.getTasks()) {
            Script<?> cScript = task.getCleaningScript();
            if (cScript != null) {
                filterAndUpdate(cScript, replacementVariables);
            }
        }
    }
}
