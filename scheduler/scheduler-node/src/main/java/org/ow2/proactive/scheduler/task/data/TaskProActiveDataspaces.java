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
package org.ow2.proactive.scheduler.task.data;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.utils.Formatter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;


public class TaskProActiveDataspaces implements TaskDataspaces {

    private static final Logger logger = Logger.getLogger(TaskProActiveDataspaces.class);

    private DataSpacesFileObject SCRATCH;
    private DataSpacesFileObject INPUT;
    private DataSpacesFileObject OUTPUT;
    private DataSpacesFileObject GLOBAL;
    private DataSpacesFileObject USER;

    private TaskId taskId;
    private NamingService namingService;

    public TaskProActiveDataspaces(TaskId taskId, NamingService namingService) throws Exception {
        this.taskId = taskId;
        this.namingService = namingService;
        initDataSpaces();
    }

    private DataSpacesFileObject createTaskIdFolder(DataSpacesFileObject space, String spaceName) {
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

    private DataSpacesFileObject resolveToExisting(DataSpacesFileObject space, String spaceName,
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
            logger.debug(spaceName + " space is " + space.getRealURI());
            logger.debug("(other available urls for " + spaceName + " space are " + space.getAllRealURIs() +
                    " )");
        }
        return space;
    }

    protected void initDataSpaces() throws Exception {
        // configure node for application
        long id = taskId.getJobId().hashCode();

        //prepare scratch, input, output
        try {
            DataSpacesNodes.configureApplication(PAActiveObject.getActiveObjectNode(PAActiveObject
                    .getStubOnThis()), id, namingService);

            SCRATCH = PADataSpaces.resolveScratchForAO();
            logger.info("SCRATCH space is " + SCRATCH.getRealURI());

        } catch (FileSystemException fse) {
            logger.error("There was a problem while initializing dataSpaces, they are not activated", fse);
            this.logDataspacesStatus(
                    "There was a problem while initializing dataSpaces, they are not activated",
                    DataspacesStatusLevel.ERROR);
            this.logDataspacesStatus(Formatter.stackTraceToString(fse), DataspacesStatusLevel.ERROR);
        }


        INPUT = initDataSpace(new Callable<DataSpacesFileObject>() {
            @Override
            public DataSpacesFileObject call() throws Exception {
                return PADataSpaces.resolveDefaultInput();
            }
        }, "INPUT", true);

        OUTPUT = initDataSpace(new Callable<DataSpacesFileObject>() {
            @Override
            public DataSpacesFileObject call() throws Exception {
                return PADataSpaces.resolveDefaultOutput();
            }
        }, "OUTPUT", false);

        GLOBAL = initDataSpace(new Callable<DataSpacesFileObject>() {
            @Override
            public DataSpacesFileObject call() throws Exception {
                return PADataSpaces.resolveOutput(SchedulerConstants.GLOBALSPACE_NAME);
            }
        }, "GLOBAL", false);

        USER = initDataSpace(new Callable<DataSpacesFileObject>() {
            @Override
            public DataSpacesFileObject call() throws Exception {
                return PADataSpaces.resolveOutput(SchedulerConstants.USERSPACE_NAME);
            }
        }, "USER", false);

    }

    private DataSpacesFileObject initDataSpace(Callable<DataSpacesFileObject> dataSpaceBuilder, String dataSpaceName, boolean input) throws Exception {
        try {
            DataSpacesFileObject result = dataSpaceBuilder.call();
            result = resolveToExisting(result, dataSpaceName, input);
            result = createTaskIdFolder(result, dataSpaceName);
            return result;
        } catch (FileSystemException fse) {
            logger.warn(dataSpaceName + " space is disabled", fse);
            this.logDataspacesStatus(dataSpaceName + " space is disabled", DataspacesStatusLevel.WARNING);
            this.logDataspacesStatus(Formatter.stackTraceToString(fse), DataspacesStatusLevel.WARNING);
        }
        return null;
    }

    public static String convertDataSpaceURIToFileIfPossible(String dataspaceURI, boolean errorIfNotFile) {
        URI foUri = null;
        try {
            foUri = new URI(dataspaceURI);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        String answer;
        if (foUri.getScheme() == null || foUri.getScheme().equals("file")) {
            answer = (new File(foUri)).getAbsolutePath();
        } else {
            if (errorIfNotFile) {
                throw new IllegalStateException("Space " + dataspaceURI +
                        " is not accessible via the file system.");
            }
            answer = foUri.toString();
        }
        return answer;
    }


    @Override
    public File getScratchFolder() {
        if (SCRATCH == null) {
            return new File(".");
        }

        return new File(convertDataSpaceURIToFileIfPossible(SCRATCH.getRealURI(), true));
    }

    @Override
    public String getScratchURI() {
        if (SCRATCH == null) {
            return new File(".").getAbsolutePath();
        }
        return convertDataSpaceURIToFileIfPossible(SCRATCH.getRealURI(), true);
    }

    @Override
    public String getInputURI() {
        if (INPUT == null) {
            return "";
        }
        return convertDataSpaceURIToFileIfPossible(INPUT.getRealURI(), false);
    }

    @Override
    public String getOutputURI() {
        if (OUTPUT == null) {
            return "";
        }
        return convertDataSpaceURIToFileIfPossible(OUTPUT.getRealURI(), false);
    }

    @Override
    public String getUserURI() {
        if (USER == null) {
            return "";
        }
        return convertDataSpaceURIToFileIfPossible(USER.getRealURI(), false);
    }

    @Override
    public String getGlobalURI() {
        if (GLOBAL == null) {
            return "";
        }
        return convertDataSpaceURIToFileIfPossible(GLOBAL.getRealURI(), false);
    }

    protected enum DataspacesStatusLevel {
        ERROR, WARNING, INFO
    }

    private StringBuffer clientLogs = new StringBuffer();

    protected void logDataspacesStatus(String message, DataspacesStatusLevel level) {
        final String eol = System.lineSeparator();
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
            ArrayList<DataSpacesFileObject> inResults = new ArrayList<>();
            // will contain all files coming from output space
            ArrayList<DataSpacesFileObject> outResults = new ArrayList<>();
            // will contain all files coming from global space
            ArrayList<DataSpacesFileObject> globResults = new ArrayList<>();
            // will contain all files coming from user space
            ArrayList<DataSpacesFileObject> userResults = new ArrayList<>();

            FileSystemException toBeThrown = null;

            for (InputSelector is : inputFiles) {
                org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector selector =
                        new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector();
                selector.setIncludes(is.getInputFiles().getIncludes());
                selector.setExcludes(is.getInputFiles().getExcludes());

                logger.debug("Selector used is " + selector);

                switch (is.getMode()) {
                    case TransferFromInputSpace:
                        toBeThrown =
                                copyInputDataToScratch(INPUT, "INPUT", is, selector, inResults);
                        break;
                    case TransferFromOutputSpace:
                        toBeThrown =
                                copyInputDataToScratch(OUTPUT, "OUTPUT", is, selector, outResults);
                        break;
                    case TransferFromGlobalSpace:
                        toBeThrown =
                                copyInputDataToScratch(GLOBAL, "GLOBAL", is, selector, globResults);
                        break;
                    case TransferFromUserSpace:
                        toBeThrown =
                                copyInputDataToScratch(USER, "USER", is, selector, userResults);
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

            Set<String> relPathes = new HashSet<>();

            ArrayList<DataSpacesFileObject> results = new ArrayList<>();
            results.addAll(inResults);
            results.addAll(outResults);
            results.addAll(globResults);
            results.addAll(userResults);

            ArrayList<Future> transferFutures = new ArrayList<>();
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
                                            FileSelector.SELECT_SELF);
                            return true;
                        }
                    }));

                }
                relPathes.add(relativePath);
            }

            StringBuilder exceptionMsg = new StringBuilder();
            String nl = System.lineSeparator();
            for (Future f : transferFutures) {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
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

    private FileSystemException copyInputDataToScratch(
            DataSpacesFileObject space, String spaceName, InputSelector inputSelector,
            org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector selector, List<DataSpacesFileObject> results) {

        if (!checkInputSpaceConfigured(space, spaceName, inputSelector)) {
            return null;
        }

        try {
            int oldSize = results.size();

            Utils.findFiles(space, selector, results);

            if (results.size() == oldSize) {
                // we detected that there was no new file in the list
                this.logDataspacesStatus(
                        "No file is transferred from " + spaceName + " space at " +
                                space.getRealURI() + "  for selector " + inputSelector,
                        DataspacesStatusLevel.WARNING);
                logger.warn(
                        "No file is transferred from " + spaceName + " space at "
                                + space.getRealURI() + "  for selector " + inputSelector);
            }
        } catch (FileSystemException e) {
            logger.warn("Error occurred while transferring files", e);

            this.logDataspacesStatus("Could not contact " + spaceName + " space at " + space.getRealURI() +
                            ". An error occurred while resolving selector " + inputSelector,
                    DataspacesStatusLevel.ERROR);
            this.logDataspacesStatus(Formatter.stackTraceToString(e),
                    DataspacesStatusLevel.ERROR);

            logger.error("Could not contact " + spaceName + " space at " + space.getRealURI() +
                    ". An error occurred while resolving selector " + inputSelector, e);

            return new FileSystemException("Could not contact " + spaceName + " space at " +
                    space.getRealURI() + ". An error occurred while resolving selector " + inputSelector);
        } catch (NullPointerException e) {
            // nothing to do
            return null;
        }

        return null;
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

            ArrayList<DataSpacesFileObject> results = new ArrayList<>();
            FileSystemException toBeThrown = null;

            for (OutputSelector os : outputFiles) {
                org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector selector =
                        new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector();
                selector.setIncludes(os.getOutputFiles().getIncludes());
                selector.setExcludes(os.getOutputFiles().getExcludes());

                switch (os.getMode()) {
                    case TransferToOutputSpace:
                        if (OUTPUT != null) {
                            toBeThrown = copyScratchDataToOutput(OUTPUT, "OUTPUT", os, selector, results);
                        }
                        break;
                    case TransferToGlobalSpace:
                        if (GLOBAL != null) {
                            toBeThrown = copyScratchDataToOutput(GLOBAL, "GLOBAL", os, selector, results);
                        }
                        break;
                    case TransferToUserSpace:
                        if (USER != null) {
                            toBeThrown = copyScratchDataToOutput(USER, "USER", os, selector, results);
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

    @Override
    public void cleanScratchSpace() {
        FileUtils.deleteQuietly(getScratchFolder());
    }

    private FileSystemException copyScratchDataToOutput(DataSpacesFileObject space, String spaceName, OutputSelector outputSelector,
                                                        org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector selector, List<DataSpacesFileObject> results) {
        try {
            int s = results.size();
            handleOutput(space, selector, results);
            if (results.size() == s) {
                this.logDataspacesStatus("No file is transferred to " + spaceName + " space at " +
                                space.getRealURI() + " for selector " + outputSelector,
                        DataspacesStatusLevel.WARNING);
                logger.warn("No file is transferred to " + spaceName + " space at " +
                        space.getRealURI() + " for selector " + outputSelector);
            }
        } catch (FileSystemException fse) {
            this.logDataspacesStatus("Error while transferring to " + spaceName + " space at " +
                    space.getRealURI() + " for selector " + outputSelector, DataspacesStatusLevel.ERROR);
            this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                    DataspacesStatusLevel.ERROR);
            logger.error("Error while transferring to " + spaceName + " space at " +
                    space.getRealURI() + " for selector " + outputSelector, fse);
            return fse;
        }

        return null;
    }

    /**
     * Display the content of the dataspaces status buffer on stderr if non empty.
     */
    private void displayDataspacesStatus() {
        if (this.clientLogs.length() != 0) {
            logger.warn(clientLogs);
            this.clientLogs = new StringBuffer();
        }
    }

    private void handleOutput(DataSpacesFileObject out, org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector selector,
                              List<DataSpacesFileObject> results) throws FileSystemException {
        Utils.findFiles(SCRATCH, selector, results);
        if (logger.isDebugEnabled()) {
            if (results == null || results.size() == 0) {
                logger.debug("No file found to copy from LOCAL space to OUTPUT space");
            } else {
                logger.debug("Files that will be copied from LOCAL space to OUTPUT space :");
            }
        }
        String buri = SCRATCH.getVirtualURI();
        ArrayList<Future> transferFutures = new ArrayList<>();

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
                                FileSelector.SELECT_SELF);
                        if (!finalout.resolveFile(finalRelativePath).exists()) {
                            logger.error("There was a problem during the copy of " + finaldsfo.getRealURI() + " to " + finalout.getRealURI() +
                                    "/" + finalRelativePath + ". File not present after copy.");
                            logDataspacesStatus("There was a problem during the copy of " + finaldsfo.getRealURI() + " to " + finalout.getRealURI() +
                                            "/" + finalRelativePath + ". File not present after copy.",
                                    DataspacesStatusLevel.ERROR);
                        }
                        return true;
                    }
                }));
            }
        }

        StringBuilder exceptionMsg = new StringBuilder();
        String nl = System.lineSeparator();
        for (Future f : transferFutures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
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
