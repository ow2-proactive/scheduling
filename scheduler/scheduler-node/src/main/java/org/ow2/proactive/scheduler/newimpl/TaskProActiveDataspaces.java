/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.newimpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastFileSelector;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastSelector;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.utils.Formatter;
import org.apache.log4j.Logger;


public class TaskProActiveDataspaces implements TaskDataspaces {

    private static final Logger logger = Logger.getLogger(TaskProActiveDataspaces.class);

    private DataSpacesFileObject SCRATCH;
    private DataSpacesFileObject INPUT;
    private DataSpacesFileObject OUTPUT;
    private DataSpacesFileObject GLOBAL;
    private DataSpacesFileObject USER;

    private TaskId taskId;
    private NamingService namingService;

    public TaskProActiveDataspaces(TaskId taskId, NamingService namingService) {
        this.taskId = taskId;
        this.namingService = namingService;
        initDataSpaces();
    }

    protected DataSpacesFileObject createTaskIdFolder(DataSpacesFileObject space, String spaceName) {
        if (space != null) {
            String realURI = space.getRealURI();
            // Look for the TASKID pattern at the end of the dataspace URI
            if (realURI.contains(SchedulerConstants.TASKID_DIR_DEFAULT_NAME)) {
                // resolve the taskid subfolder
                DataSpacesFileObject tidOutput;
                try {
                    tidOutput = space.resolveFile(taskId.toString());
                    // create this subfolder
                    tidOutput.createFolder();
                } catch (FileSystemException e) {
                    logger.info("Error when creating the TASKID folder in " + realURI, e);
                    logger.info(spaceName + " space is disabled");
                    return null;
                }
                // assign it to the space
                space = tidOutput;
                logger.info(SchedulerConstants.TASKID_DIR_DEFAULT_NAME + " pattern found, changed " +
                  spaceName + " space to : " + space.getRealURI());
            }
        }
        return space;
    }

    protected DataSpacesFileObject resolveToExisting(DataSpacesFileObject space, String spaceName,
      boolean input) {
        if (space == null) {
            logger.info(spaceName + " space is disabled");
            return null;
        }
        // ensure that the remote folder exists (in case we didn't replace any pattern)
        try {
            space = space.ensureExistingOrSwitch(!input);
        } catch (Exception e) {
            logger.info("Error occurred when switching to alternate space root", e);
            logger.info(spaceName + " space is disabled");
            return null;
        }
        if (space == null) {
            logger.info("No existing " + spaceName + " space found");
            logger.info(spaceName + " space is disabled");
        } else {
            logger.info(spaceName + " space is " + space.getRealURI());
            logger.info("(other available urls for " + spaceName + " space are " + space.getAllRealURIs() +
              " )");
        }
        return space;
    }

    protected void initDataSpaces() {
        // configure node for application
        long id = taskId.getJobId().hashCode();

        //prepare scratch, input, output
        try {
            DataSpacesNodes.configureApplication(
              PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()), id, namingService);

            SCRATCH = PADataSpaces.resolveScratchForAO();
            logger.info("SCRATCH space is " + SCRATCH.getRealURI());

            // create a log file in local space if the node is configured
            logger.info("logfile is enabled for task " + taskId);

        } catch (Throwable t) {
            logger.error("There was a problem while initializing dataSpaces, they are not activated", t);
            return;
        }

        try {
            INPUT = PADataSpaces.resolveDefaultInput();
            INPUT = resolveToExisting(INPUT, "INPUT", true);
            INPUT = createTaskIdFolder(INPUT, "INPUT");
        } catch (Throwable t) {
            logger.warn("INPUT space is disabled");
            logger.warn("", t);
        }
        try {
            OUTPUT = PADataSpaces.resolveDefaultOutput();
            OUTPUT = resolveToExisting(OUTPUT, "OUTPUT", false);
            OUTPUT = createTaskIdFolder(OUTPUT, "OUTPUT");
        } catch (Throwable t) {
            logger.warn("OUTPUT space is disabled");
            logger.warn("", t);
        }

        try {
            GLOBAL = PADataSpaces.resolveOutput(SchedulerConstants.GLOBALSPACE_NAME);
            GLOBAL = resolveToExisting(GLOBAL, "GLOBAL", false);
            GLOBAL = createTaskIdFolder(GLOBAL, "GLOBAL");
        } catch (Throwable t) {
            logger.warn("GLOBAL space is disabled");
            logger.warn("", t);
        }
        try {
            USER = PADataSpaces.resolveOutput(SchedulerConstants.USERSPACE_NAME);
            USER = resolveToExisting(USER, "USER", false);
            USER = createTaskIdFolder(USER, "USER");
        } catch (Throwable t) {
            logger.warn("USER space is disabled");
            logger.warn("", t);
        }
    }

    @Override
    public File getScratchFolder() {
        return new File(SCRATCH.getPath());
    }

    protected enum DataspacesStatusLevel {
        ERROR, WARNING, INFO
    }

    private StringBuffer clientLogs = new StringBuffer();

    protected void logDataspacesStatus(String message, DataspacesStatusLevel level) {
        final String eol = System.getProperty("line.separator");
        final boolean hasEol = message.endsWith(eol);
        if (level == DataspacesStatusLevel.ERROR) {
            this.clientLogs.append("[DATASPACES-ERROR] ").append(message).append(hasEol ? "" : eol);
        } else if (level == DataspacesStatusLevel.WARNING) {
            this.clientLogs.append("[DATASPACES-WARNING] ").append(message).append(hasEol ? "" : eol);
        } else if (level == DataspacesStatusLevel.INFO) {
            this.clientLogs.append("[DATASPACES-INFO] ").append(message).append(hasEol ? "" : eol);
        }
    }

    private boolean checkInputSpaceConfigured(DataSpacesFileObject space, String spaceName, InputSelector is) {
        if (space == null) {
            logger.error("Job " + spaceName +
              " space is not defined or not properly configured while input files are specified : ");

            this.logDataspacesStatus("Job " + spaceName +
                " space is not defined or not properly configured while input files are specified : ",
              DataspacesStatusLevel.ERROR);

            logger.error("--> " + is);
            this.logDataspacesStatus("--> " + is, DataspacesStatusLevel.ERROR);

            return false;
        }
        return true;
    }

    @Override
    public void copyInputDataToScratch(List<InputSelector> inputFiles) throws FileSystemException {
        try {
            if (inputFiles == null) {
                logger.debug("Input selector is empty, no file to copy");
                return;
            }

            // will contain all files coming from input space
            ArrayList<DataSpacesFileObject> inResults = new ArrayList<DataSpacesFileObject>();
            // will contain all files coming from output space
            ArrayList<DataSpacesFileObject> outResults = new ArrayList<DataSpacesFileObject>();
            // will contain all files coming from global space
            ArrayList<DataSpacesFileObject> globResults = new ArrayList<DataSpacesFileObject>();
            // will contain all files coming from user space
            ArrayList<DataSpacesFileObject> userResults = new ArrayList<DataSpacesFileObject>();

            FileSystemException toBeThrown = null;
            for (InputSelector is : inputFiles) {
                //fill fast file selector
                FastFileSelector fast = new FastFileSelector();
                fast.setIncludes(is.getInputFiles().getIncludes());
                fast.setExcludes(is.getInputFiles().getExcludes());
                fast.setCaseSensitive(is.getInputFiles().isCaseSensitive());
                switch (is.getMode()) {
                    case TransferFromInputSpace:
                        if (!checkInputSpaceConfigured(INPUT, "INPUT", is))
                            continue;

                        //search in INPUT
                        try {
                            int s = inResults.size();
                            FastSelector.findFiles(INPUT, fast, true, inResults);
                            if (s == inResults.size()) {
                                // we detected that there was no new file in the list
                                this.logDataspacesStatus("No file is transferred from INPUT space at " +
                                    INPUT.getRealURI() + "  for selector " + is,
                                  DataspacesStatusLevel.WARNING);
                                logger.warn("No file is transferred from INPUT space at " +
                                  INPUT.getRealURI() + " for selector " + is);
                            }
                        } catch (FileSystemException fse) {

                            toBeThrown = new FileSystemException("Could not contact INPUT space at " +
                              INPUT.getRealURI() + ". An error occured while resolving selector " + is);
                            this.logDataspacesStatus("Could not contact INPUT space at " +
                              INPUT.getRealURI() + " for selector " + is, DataspacesStatusLevel.ERROR);
                            this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                              DataspacesStatusLevel.ERROR);
                            logger.error("Could not contact INPUT space at " + INPUT.getRealURI() +
                              ". An error occured while resolving selector " + is, fse);
                        } catch (NullPointerException npe) {
                            // nothing to do
                        }
                        break;
                    case TransferFromOutputSpace:
                        if (!checkInputSpaceConfigured(OUTPUT, "OUTPUT", is))
                            continue;
                        //search in OUTPUT
                        try {
                            int s = outResults.size();
                            FastSelector.findFiles(OUTPUT, fast, true, outResults);
                            if (s == outResults.size()) {
                                // we detected that there was no new file in the list
                                this.logDataspacesStatus("No file is transferred from OUPUT space at " +
                                    OUTPUT.getRealURI() + "  for selector " + is,
                                  DataspacesStatusLevel.WARNING);
                                logger.warn("No file is transferred from OUPUT space at " +
                                  OUTPUT.getRealURI() + "  for selector " + is);
                            }
                        } catch (FileSystemException fse) {
                            toBeThrown = new FileSystemException("Could not contact OUTPUT space at " +
                              OUTPUT.getRealURI() + ". An error occured while resolving selector " + is);
                            this.logDataspacesStatus("Could not contact OUTPUT space at " +
                              OUTPUT.getRealURI() + " for selector " + is, DataspacesStatusLevel.ERROR);
                            this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                              DataspacesStatusLevel.ERROR);
                            logger.error("Could not contact OUTPUT space at " + OUTPUT.getRealURI() +
                              ". An error occured while resolving selector " + is, fse);
                        } catch (NullPointerException npe) {
                            // nothing to do
                        }
                        break;
                    case TransferFromGlobalSpace:
                        if (!checkInputSpaceConfigured(GLOBAL, "GLOBAL", is))
                            continue;
                        try {
                            int s = globResults.size();
                            FastSelector.findFiles(GLOBAL, fast, true, globResults);
                            if (s == globResults.size()) {
                                // we detected that there was no new file in the list
                                this.logDataspacesStatus("No file is transferred from GLOBAL space at " +
                                    GLOBAL.getRealURI() + "  for selector " + is,
                                  DataspacesStatusLevel.WARNING);
                                logger.warn("No file is transferred from GLOBAL space at " +
                                  GLOBAL.getRealURI() + "  for selector " + is);
                            }
                        } catch (FileSystemException fse) {
                            logger.info("", fse);
                            toBeThrown = new FileSystemException("Could not contact GLOBAL space at " +
                              GLOBAL.getRealURI() + ". An error occurred while resolving selector " + is);
                            this.logDataspacesStatus("Could not contact GLOBAL space at " +
                                GLOBAL.getRealURI() + ". An error occurred while resolving selector " + is,
                              DataspacesStatusLevel.ERROR);
                            this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                              DataspacesStatusLevel.ERROR);
                            logger.error("Could not contact GLOBAL space at " + GLOBAL.getRealURI() +
                              ". An error occurred while resolving selector " + is, fse);

                        } catch (NullPointerException npe) {
                            // nothing to do
                        }
                        break;
                    case TransferFromUserSpace:
                        if (!checkInputSpaceConfigured(USER, "USER", is))
                            continue;
                        try {
                            int s = userResults.size();
                            FastSelector.findFiles(USER, fast, true, userResults);
                            if (s == userResults.size()) {
                                // we detected that there was no new file in the list
                                this
                                  .logDataspacesStatus("No file is transferred from USER space at " +
                                      USER.getRealURI() + "  for selector " + is,
                                    DataspacesStatusLevel.WARNING);
                                logger.warn("No file is transferred from USER space at " + USER.getRealURI() +
                                  "  for selector " + is);
                            }
                        } catch (FileSystemException fse) {
                            logger.info("", fse);
                            toBeThrown = new FileSystemException("Could not contact USER space at " +
                              USER.getRealURI() + ". An error occurred while resolving selector " + is);
                            this.logDataspacesStatus("Could not contact USER space at " + USER.getRealURI() +
                                ". An error occurred while resolving selector " + is,
                              DataspacesStatusLevel.ERROR);
                            this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                              DataspacesStatusLevel.ERROR);
                            logger.error("Could not contact USER space at " + USER.getRealURI() +
                              ". An error occurred while resolving selector " + is, fse);

                        } catch (NullPointerException npe) {
                            // nothing to do
                        }
                        break;
                    case none:
                        //do nothing
                        break;
                }
            }

            if (toBeThrown != null) {
                throw toBeThrown;
            }

            String outuri = (OUTPUT == null) ? "" : OUTPUT.getVirtualURI();
            String globuri = (GLOBAL == null) ? "" : GLOBAL.getVirtualURI();
            String useruri = (USER == null) ? "" : USER.getVirtualURI();
            String inuri = (INPUT == null) ? "" : INPUT.getVirtualURI();

            Set<String> relPathes = new HashSet<String>();

            ArrayList<DataSpacesFileObject> results = new ArrayList<DataSpacesFileObject>();
            results.addAll(inResults);
            results.addAll(outResults);
            results.addAll(globResults);
            results.addAll(userResults);

            ArrayList<Future> transferFutures = new ArrayList<Future>();
            for (DataSpacesFileObject dsfo : results) {
                String relativePath;
                if (inResults.contains(dsfo)) {
                    relativePath = dsfo.getVirtualURI().replaceFirst(inuri + "/?", "");
                } else if (outResults.contains(dsfo)) {
                    relativePath = dsfo.getVirtualURI().replaceFirst(outuri + "/?", "");
                } else if (globResults.contains(dsfo)) {
                    relativePath = dsfo.getVirtualURI().replaceFirst(globuri + "/?", "");
                } else if (userResults.contains(dsfo)) {
                    relativePath = dsfo.getVirtualURI().replaceFirst(useruri + "/?", "");
                } else {
                    // should never happen
                    throw new IllegalStateException();
                }
                logger.debug("* " + relativePath);
                if (!relPathes.contains(relativePath)) {
                    logger.debug("------------ resolving " + relativePath);
                    final String finalRelativePath = relativePath;
                    final DataSpacesFileObject finaldsfo = dsfo;
                    transferFutures.add(executorTransfer.submit(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws FileSystemException {
                            logger.info("Copying " + finaldsfo.getRealURI() + " to " + SCRATCH.getRealURI() +
                              "/" + finalRelativePath);
                            SCRATCH
                              .resolveFile(finalRelativePath)
                              .copyFrom(
                                finaldsfo,
                                org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF);
                            return true;
                        }
                    }));

                }
                relPathes.add(relativePath);
            }

            StringBuilder exceptionMsg = new StringBuilder();
            String nl = System.getProperty("line.separator");
            for (Future f : transferFutures) {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    logger.error("", e);
                    exceptionMsg.append(StackTraceUtil.getStackTrace(e)).append(nl);
                } catch (ExecutionException e) {
                    logger.error("", e);
                    exceptionMsg.append(StackTraceUtil.getStackTrace(e)).append(nl);
                }
            }
            if (exceptionMsg.length() > 0) {
                toBeThrown = new FileSystemException(
                  "Exception(s) occurred when transferring input files : " + nl + exceptionMsg.toString());
            }

            if (toBeThrown != null) {
                throw toBeThrown;
            }
        } finally {
            // display dataspaces error and warns if any
            displayDataspacesStatus();
        }
    }
    protected ExecutorService executorTransfer = Executors.newFixedThreadPool(5,
      new NamedThreadFactory("FileTransferThreadPool"));

    private boolean checkOuputSpaceConfigured(DataSpacesFileObject space, String spaceName, OutputSelector os) {
        if (space == null) {
            logger.debug("Job " + spaceName +
              " space is not defined or not properly configured, while output files are specified :");
            this.logDataspacesStatus("Job " + spaceName +
                " space is not defined or not properly configured, while output files are specified :",
              DataspacesStatusLevel.ERROR);
            this.logDataspacesStatus("--> " + os, DataspacesStatusLevel.ERROR);
            return false;
        }
        return true;
    }

    @Override
    public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {
        try {
            if (outputFiles == null) {
                logger.debug("Output selector is empty, no file to copy");
                return;
            }

            // We check that the spaces used are properly configured, we show a message in the log output to the user if not
            for (OutputSelector os1 : outputFiles) {
                switch (os1.getMode()) {
                    case TransferToOutputSpace:
                        checkOuputSpaceConfigured(OUTPUT, "OUTPUT", os1);
                        break;
                    case TransferToGlobalSpace:
                        checkOuputSpaceConfigured(GLOBAL, "GLOBAL", os1);
                        break;
                    case TransferToUserSpace:
                        checkOuputSpaceConfigured(USER, "USER", os1);
                        break;
                }
            }

            ArrayList<DataSpacesFileObject> results = new ArrayList<DataSpacesFileObject>();
            FileSystemException toBeThrown = null;

            for (OutputSelector os : outputFiles) {
                //fill fast file selector
                FastFileSelector fast = new FastFileSelector();
                fast.setIncludes(os.getOutputFiles().getIncludes());
                fast.setExcludes(os.getOutputFiles().getExcludes());
                fast.setCaseSensitive(os.getOutputFiles().isCaseSensitive());
                switch (os.getMode()) {
                    case TransferToOutputSpace:
                        if (OUTPUT != null) {
                            try {
                                int s = results.size();
                                handleOutput(OUTPUT, fast, results);
                                if (results.size() == s) {
                                    this.logDataspacesStatus("No file is transferred to OUTPUT space at " +
                                        OUTPUT.getRealURI() + " for selector " + os,
                                      DataspacesStatusLevel.WARNING);
                                    logger.warn("No file is transferred to OUTPUT space at " +
                                      OUTPUT.getRealURI() + " for selector " + os);
                                }
                            } catch (FileSystemException fse) {
                                toBeThrown = fse;
                                this.logDataspacesStatus("Error while transferring to OUTPUT space at " +
                                  OUTPUT.getRealURI() + " for selector " + os, DataspacesStatusLevel.ERROR);
                                this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                                  DataspacesStatusLevel.ERROR);
                                logger.error("Error while transferring to OUTPUT space at " +
                                  OUTPUT.getRealURI() + " for selector " + os, fse);
                            }
                        }
                        break;
                    case TransferToGlobalSpace:
                        if (GLOBAL != null) {
                            try {
                                int s = results.size();
                                handleOutput(GLOBAL, fast, results);
                                if (results.size() == s) {
                                    this.logDataspacesStatus("No file is transferred to GLOBAL space at " +
                                        GLOBAL.getRealURI() + " for selector " + os,
                                      DataspacesStatusLevel.WARNING);
                                    logger.warn("No file is transferred to GLOBAL space at " +
                                      GLOBAL.getRealURI() + " for selector " + os);
                                }
                            } catch (FileSystemException fse) {
                                toBeThrown = fse;
                                this.logDataspacesStatus("Error while transferring to GLOBAL space at " +
                                  GLOBAL.getRealURI() + " for selector " + os, DataspacesStatusLevel.ERROR);
                                this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                                  DataspacesStatusLevel.ERROR);
                                logger.error("Error while transferring to GLOBAL space at " +
                                  GLOBAL.getRealURI() + " for selector " + os, fse);
                            }
                        }
                        break;
                    case TransferToUserSpace:
                        if (USER != null) {
                            try {
                                int s = results.size();
                                handleOutput(USER, fast, results);
                                if (results.size() == s) {
                                    this.logDataspacesStatus("No file is transferred to USER space at " +
                                        USER.getRealURI() + " for selector " + os,
                                      DataspacesStatusLevel.WARNING);
                                    logger.warn("No file is transferred to USER space at " +
                                      USER.getRealURI() + " for selector " + os);
                                }
                            } catch (FileSystemException fse) {
                                toBeThrown = fse;
                                this.logDataspacesStatus("Error while transferring to USER space at " +
                                  USER.getRealURI() + " for selector " + os, DataspacesStatusLevel.ERROR);
                                this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                                  DataspacesStatusLevel.ERROR);
                                logger.error("Error while transferring to USER space at " +
                                  USER.getRealURI() + " for selector " + os, fse);
                            }
                            break;
                        }
                    case none:
                        break;
                }
                results.clear();
            }

            if (toBeThrown != null) {
                throw toBeThrown;
            }

        } finally {
            // display dataspaces error and warns if any
            displayDataspacesStatus();
        }
    }

    /**
     * Display the content of the dataspaces status buffer on stderr if non empty.
     */
    protected void displayDataspacesStatus() {
        if (this.clientLogs.length() != 0) {
            System.err.println("");
            System.err.println(this.clientLogs);
            System.err.flush();
            this.clientLogs = new StringBuffer();
        }
    }

    private void handleOutput(DataSpacesFileObject out, FastFileSelector fast,
      ArrayList<DataSpacesFileObject> results) throws FileSystemException {
        FastSelector.findFiles(SCRATCH, fast, true, results);
        if (logger.isDebugEnabled()) {
            if (results == null || results.size() == 0) {
                logger.debug("No file found to copy from LOCAL space to OUTPUT space");
            } else {
                logger.debug("Files that will be copied from LOCAL space to OUTPUT space :");
            }
        }
        String buri = SCRATCH.getVirtualURI();
        ArrayList<Future> transferFutures = new ArrayList<Future>();

        if (results != null) {
            for (DataSpacesFileObject dsfo : results) {
                String relativePath = dsfo.getVirtualURI().replaceFirst(buri + "/?", "");
                logger.debug("* " + relativePath);

                final String finalRelativePath = relativePath;
                final DataSpacesFileObject finaldsfo = dsfo;
                final DataSpacesFileObject finalout = out;
                transferFutures.add(executorTransfer.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws FileSystemException {
                        logger.info("Copying " + finaldsfo.getRealURI() + " to " + finalout.getRealURI() +
                          "/" + finalRelativePath);

                        finalout.resolveFile(finalRelativePath).copyFrom(finaldsfo,
                          org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF);
                        return true;
                    }
                }));
            }
        }

        StringBuilder exceptionMsg = new StringBuilder();
        String nl = System.getProperty("line.separator");
        for (Future f : transferFutures) {
            try {
                f.get();
            } catch (InterruptedException e) {
                logger.error("", e);
                exceptionMsg.append(StackTraceUtil.getStackTrace(e)).append(nl);
            } catch (ExecutionException e) {
                logger.error("", e);
                exceptionMsg.append(StackTraceUtil.getStackTrace(e)).append(nl);
            }
        }
        if (exceptionMsg.length() > 0) {
            throw new FileSystemException("Exception(s) occurred when transferring input files : " + nl +
              exceptionMsg.toString());
        }
    }
}
