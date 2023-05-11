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
package org.ow2.proactive.scheduler.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.addons.email.exception.EmailException;
import org.ow2.proactive.resourcemanager.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.EmailConfiguration;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.SendMail;
import org.ow2.proactive.utils.PAExecutors;

import com.google.common.io.Files;


public class JobEmailNotification {

    public static final String GENERIC_INFORMATION_KEY_EMAIL = "EMAIL";

    public static final String GENERIC_INFORMATION_KEY_NOTIFICATION_EVENT = "NOTIFICATION_EVENTS";

    private static final Logger logger = Logger.getLogger(JobEmailNotification.class);

    private static final JobLogger jlogger = JobLogger.getInstance();

    private static final String SUBJECT_TEMPLATE = "ProActive Job %s : %s";

    private JobState jobState;

    private SchedulerEvent eventType;

    private SendMail sender;

    private static ExecutorService asyncMailSender;

    private SchedulerDBManager dbManager = null;

    private JobInfo jobInfo;

    public JobEmailNotification(JobState js, NotificationData<JobInfo> notification, SendMail sender) {
        this.jobState = js;
        this.eventType = notification.getEventType();
        this.sender = sender;
        this.jobInfo = notification.getData();
        initAsyncMailSender();
    }

    public JobEmailNotification(JobState js, NotificationData<JobInfo> notification) {
        this(js, notification, new SendMail());
    }

    public JobEmailNotification(JobState js, NotificationData<JobInfo> notification, SchedulerDBManager dbManager) {
        this(js, notification);
        this.dbManager = dbManager;
    }

    private static synchronized void initAsyncMailSender() {
        if (asyncMailSender == null) {
            asyncMailSender = PAExecutors.newCachedBoundedThreadPool(1,
                                                                     PASchedulerProperties.SCHEDULER_INTERNAL_POOL_NBTHREAD.getValueAsInt(),
                                                                     120L,
                                                                     TimeUnit.SECONDS,
                                                                     new NamedThreadFactory("JobEmailNotification",
                                                                                            true,
                                                                                            2));
        }
    }

    public boolean doCheckAndSend(boolean withAttachment)
            throws JobEmailNotificationException, IOException, UnknownJobException, PermissionException {
        String eventFinishedWithErrorsName = SchedulerEvent.JOB_RUNNING_TO_FINISHED_WITH_ERRORS.name().toLowerCase();
        String eventFinishedWithErrorsMethod = SchedulerEvent.JOB_RUNNING_TO_FINISHED_WITH_ERRORS.toString()
                                                                                                 .toLowerCase();
        String eventFinishedName = SchedulerEvent.JOB_RUNNING_TO_FINISHED.name().toLowerCase();
        String eventFinishedMethod = SchedulerEvent.JOB_RUNNING_TO_FINISHED.toString().toLowerCase();
        String eventAbortedName = SchedulerEvent.JOB_ABORTED.name().toLowerCase();
        String eventAbortedMethod = SchedulerEvent.JOB_ABORTED.toString().toLowerCase();

        String jobStatus = jobState.getGenericInformation().get(GENERIC_INFORMATION_KEY_NOTIFICATION_EVENT);
        List<String> jobStatusList = new ArrayList<>();
        if (jobStatus != null) {
            if ("all".equals(jobStatus.toLowerCase())) {
                jobStatusList = Stream.of("JOB_CHANGE_PRIORITY",
                                          "JOB_IN_ERROR",
                                          "JOB_PAUSED",
                                          "JOB_PENDING_TO_FINISHED",
                                          "JOB_PENDING_TO_RUNNING",
                                          "JOB_RESTARTED_FROM_ERROR",
                                          "JOB_RESUMED",
                                          "JOB_RUNNING_TO_FINISHED",
                                          "JOB_SUBMITTED",
                                          "JOB_RUNNING_TO_FINISHED_WITH_ERRORS",
                                          "JOB_ABORTED")
                                      .map(status -> status.toLowerCase())
                                      .collect(Collectors.toList());
            } else {
                //get the events that require an email notification, events are comma separated and case irrelevant
                jobStatusList = Arrays.asList(jobStatus.toLowerCase().split("\\s*,\\s*"));
            }
        }

        if (!PASchedulerProperties.EMAIL_NOTIFICATIONS_ENABLED.getValueAsBoolean()) {
            logger.debug("Notification emails disabled, doing nothing");
            return false;
        }

        if ((!jobStatusList.contains(eventType.toString().toLowerCase()) &&
             !jobStatusList.contains(eventType.name().toLowerCase())) &&
            (!jobStatusList.contains(eventFinishedWithErrorsName)) &&
            (!jobStatusList.contains(eventFinishedWithErrorsMethod)) && (!jobStatusList.contains(eventAbortedName)) &&
            (!jobStatusList.contains(eventAbortedMethod))) {
            return false;
        }

        JobEmailStatus jobEmailStatus = new JobEmailStatus(jobInfo);
        switch (eventType) {
            case JOB_PENDING_TO_FINISHED:
            case JOB_RUNNING_TO_FINISHED:
                // first case: JOB_RUNNING_TO_FINISHED_WITH_ERRORS is provided and JOB_RUNNING_TO_FINISHED is not provided along with it
                if ((jobStatusList.contains(eventFinishedWithErrorsName) ||
                     jobStatusList.contains(eventFinishedWithErrorsMethod)) &&
                    (!jobStatusList.contains(eventFinishedName) && !jobStatusList.contains(eventFinishedMethod))) {
                    // check if any tasks have issues
                    jobEmailStatus.checkTasksWithErrors();
                    if (!jobEmailStatus.isWithErrors())
                        // don't send a notification if there are no errors
                        return false;
                    else {
                        // send a notification about a finished job with errors
                        sendEmail(withAttachment, jobEmailStatus);
                    }

                    // second case: JOB_RUNNING_TO_FINISHED_WITH_ERRORS is provided along with JOB_RUNNING_TO_FINISHED (e.g., the case of 'All' event)
                } else if ((jobStatusList.contains(eventFinishedWithErrorsName) ||
                            jobStatusList.contains(eventFinishedWithErrorsMethod))) {
                    // check if any tasks have issues
                    jobEmailStatus.checkTasksWithErrors();
                    // send notification according to the job finished status, with errors or not
                    sendEmail(withAttachment, jobEmailStatus);

                    // third case: check if JOB_ABORTED is activated while JOB_RUNNING_TO_FINISHED with ERRORS or not is not activated
                } else if ((jobStatusList.contains(eventAbortedName) || jobStatusList.contains(eventAbortedMethod)) &&
                           (!jobStatusList.contains(eventFinishedName) &&
                            !jobStatusList.contains(eventFinishedMethod) &&
                            !jobStatusList.contains(eventFinishedWithErrorsName) &&
                            !jobStatusList.contains(eventFinishedWithErrorsMethod))) {
                    // check job if the job is aborted according to its status Canceled, Failed or Killed
                    jobEmailStatus.checkJobAborted();
                    if (!jobEmailStatus.isAborted())
                        // don't send a notification if not aborted
                        return false;
                    else {
                        // send a notification about an aborted job
                        sendEmail(withAttachment, jobEmailStatus);
                    }

                    // last case: JOB_RUNNING_TO_FINISHED_WITH_ERRORS and JOB_ABORTED are not provided, then the notification should be sent for events having no errors or issues
                } else {
                    sendEmail(withAttachment, jobEmailStatus);
                }
                break;
            case JOB_CHANGE_PRIORITY:
            case JOB_IN_ERROR:
            case JOB_PAUSED:
            case JOB_RESTARTED_FROM_ERROR:
            case JOB_PENDING_TO_RUNNING:
            case JOB_RESUMED:
            case JOB_SUBMITTED:
                if (jobStatusList.contains(eventType.name().toLowerCase()) ||
                    jobStatusList.contains(eventType.toString().toLowerCase())) {
                    sendEmail(withAttachment, jobEmailStatus);
                }
                break;
            default:
                logger.trace("Event not in the list of email notification, doing nothing");
                return false;
        }
        return true;

    }

    private void sendEmail(boolean withAttachment, JobEmailStatus jobEmailStatus)
            throws JobEmailNotificationException, IOException, UnknownJobException, PermissionException {
        try {
            if (withAttachment) {
                String attachment = getAttachment();
                if (attachment != null) {
                    sender.sender(getTo(), getSubject(jobEmailStatus), getBody(), attachment, getAttachmentName());
                    FileUtils.deleteQuietly(new File(attachment));
                } else {
                    sender.sender(getTo(), getSubject(jobEmailStatus), getBody());
                }
            } else {
                sender.sender(getTo(), getSubject(jobEmailStatus), getBody());
            }

        } catch (EmailException e)

        {
            throw new JobEmailNotificationException(String.join(",", getTo()),
                                                    "Error sending email: " + e.getMessage(),
                                                    e);
        }
    }

    public void checkAndSendAsync(boolean withAttachment) {
        this.asyncMailSender.submit(() -> {
            try {
                boolean sent = doCheckAndSend(withAttachment);
                if (sent) {
                    jlogger.info(jobState.getId(), "sent notification email to " + getTo());
                }
            } catch (JobEmailNotificationException e) {
                jlogger.warn(jobState.getId(),
                             "failed to send email notification to " + e.getEmailTarget() + ": " + e.getMessage(),
                             e);
                logger.trace("Stack trace:", e);
            } catch (Exception e) {
                jlogger.warn(jobState.getId(), "failed to send email notification: " + e.getMessage(), e);
                logger.trace("Stack trace:", e);
            }
        });
    }

    private static String getFrom() throws JobEmailNotificationException {
        String from = PASchedulerProperties.EMAIL_NOTIFICATIONS_SENDER_ADDRESS.getValueAsString();
        if (from == null || from.isEmpty()) {
            throw new JobEmailNotificationException("Sender address not set in scheduler configuration");
        }
        return from;
    }

    private List<String> getTo() throws JobEmailNotificationException {
        String to = jobState.getGenericInformation().get(GENERIC_INFORMATION_KEY_EMAIL);
        if (to == null) {
            throw new JobEmailNotificationException("Recipient address is not set in generic information");
        }
        String[] toList = to.split("\\s*,\\s*");
        return Arrays.asList(toList);
    }

    private String getSubject(JobEmailStatus jobEmailStatus) {
        String event = eventType.toString();
        if (jobEmailStatus.isAborted()) {
            event = SchedulerEvent.JOB_ABORTED.toString();
        } else if (jobEmailStatus.isWithErrors()) {
            event = SchedulerEvent.JOB_RUNNING_TO_FINISHED_WITH_ERRORS.toString();
        }
        String jobID = jobState.getId().value();
        return String.format(SUBJECT_TEMPLATE, jobID, event);
    }

    private String getBody() throws IOException {
        String jobID = jobState.getId().value();
        String jobName = jobState.getName();
        String jobOwner = jobState.getOwner();
        String status = jobState.getStatus().toString();
        String hostname = "UNKNOWN";
        List<TaskState> tasks = jobState.getTasks();
        String allTaskStatusesString = String.join(System.lineSeparator(),
                                                   tasks.stream()
                                                        .map(task -> task.getId().getReadableName() + " (" +
                                                                     task.getId().toString() + ") Status: " +
                                                                     task.getStatus().toString())
                                                        .collect(Collectors.toList()));
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            logger.debug("Could not get hostname", e);
        }

        final Properties properties = EmailConfiguration.getConfiguration().getProperties();
        String templatePath = properties.getProperty(EmailConfiguration.TEMPLATE_PATH,
                                                     "config/scheduler/email.template");
        String bodyTemplate = Files.toString(new File(PASchedulerProperties.getAbsolutePath(templatePath)),
                                             Charset.defaultCharset());

        Map<String, String> values = new HashMap<>();
        values.put("JOB_ID", jobID);
        values.put("JOB_STATUS", status);
        values.put("JOB_TASKS", allTaskStatusesString);
        values.put("HOST_NAME", hostname);
        values.put("JOB_NAME", jobName);
        values.put("JOB_OWNER", jobOwner);

        // use of StrSubstitutor to replace email template parameters by job details
        String emailBody = StrSubstitutor.replace(bodyTemplate, values, "%", "%");

        return emailBody;
    }

    private String getAttachment() throws NotConnectedException, UnknownJobException, PermissionException, IOException {
        JobId jobID = jobState.getId();
        List<TaskState> tasks = jobState.getTasks();
        String attachLogPath = null;

        try {

            JobResult result = dbManager.loadJobResult(jobID);

            Stream<TaskResult> preResult = tasks.stream().map(task -> result.getAllResults()
                                                                            .get(task.getId().getReadableName()));

            Stream<TaskResult> resNonNull = preResult.filter(r -> r != null && r.getOutput() != null);

            Stream<String> resStream = resNonNull.map(taskResult -> "Task " + taskResult.getTaskId().toString() + " (" +
                                                                    taskResult.getTaskId().getReadableName() + ") :" +
                                                                    System.lineSeparator() +
                                                                    taskResult.getOutput().getAllLogs());

            String allRes = String.join(System.lineSeparator(),
                                        resStream.filter(r -> r != null).collect(Collectors.toList()));

            File file = File.createTempFile("job_logs", ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(allRes);
                attachLogPath = file.getAbsolutePath();
            } catch (IOException e) {
                jlogger.warn(jobState.getId(),
                             "Failed to create attachment for email notification: " + e.getMessage(),
                             e);
                logger.warn("Error creating attachment for email notification: " + e.getMessage(), e);

            }
        } catch (Exception e) {
            logger.warn("Error creating attachment for email notification: ", e);
        }

        return attachLogPath;
    }

    private String getAttachmentName() {
        JobId jobID = jobState.getId();
        String fileName = "job_" + jobID.value() + "_log.txt";
        return fileName;
    }

}
