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
package org.objectweb.proactive.core.process.unicore;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition.FileDescription;
import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Unicore Process implementation.
 * This implementation works only for ProActive deployment, and not to submit single commands
 * @author  ProActive Team
 * @version 1.0,  2005/09/20
 * @since   ProActive 3.0
 */
public class UnicoreProcess extends AbstractExternalProcessDecorator {
    public UnicoreParameters uParam;

    public UnicoreProcess() {
        super();
        setCompositionType(GIVE_COMMAND_AS_PARAMETER);

        FILE_TRANSFER_DEFAULT_PROTOCOL = "unicore";
        //Create an UnicoreParameters instance
        uParam = new UnicoreParameters();
    }

    public UnicoreProcess(ExternalProcess targetProcess) {
        super(targetProcess);

        FILE_TRANSFER_DEFAULT_PROTOCOL = "unicore";
        uParam = new UnicoreParameters();
    }

    @Override
    protected void internalStartProcess(String commandToExecute)
        throws java.io.IOException {
        if (logger.isDebugEnabled()) {
            logger.debug(commandToExecute);
        }

        /* Depending on the system property UnicoreProActiveClient
         * can be forked or called directly.
         */
        if (!PAProperties.PA_UNICORE_FORKCLIENT.isTrue()) {
            logger.debug("Not Forking UnicoreProActiveClient");
            UnicoreProActiveClient uProClient;

            //Build a UnicoreProActive client with this parameters
            uProClient = new UnicoreProActiveClient(uParam);

            uProClient.build();
            uProClient.saveJob();
            uProClient.submitJob();
        } else {
            logger.debug("Forking UnicoreProActiveClient");
            try {
                externalProcess = Runtime.getRuntime().exec(command);
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(
                            externalProcess.getInputStream()));
                java.io.BufferedReader err = new java.io.BufferedReader(new java.io.InputStreamReader(
                            externalProcess.getErrorStream()));
                java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
                            externalProcess.getOutputStream()));
                handleProcess(in, out, err);
            } catch (java.io.IOException e) {
                isFinished = true;
                //throw e;
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String internalBuildCommand() {
        uParam.setScriptContent(targetProcess.getCommand());

        //return command for parent to execute
        return uParam.getCommandString();
    }

    @Override
    protected boolean internalFileTransferDefaultProtocol() {
        FileTransferWorkShop fts = getFileTransferWorkShopDeploy();
        FileTransferDefinition[] ftDefinitions = fts.getAllFileTransferDefinitions();

        Logger fileTransferLogger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_FILETRANSFER);

        for (int i = 0; i < ftDefinitions.length; i++) {
            //Files and Dirs
            FileDescription[] files = ftDefinitions[i].getAll();
            for (int j = 0; j < files.length; j++) {
                String fullfilename = fts.getAbsoluteSrcPath(files[j]);

                //Skipping non existant filenames
                if (!FileTransferWorkShop.isLocalReadable(fullfilename)) {
                    if (fileTransferLogger.isDebugEnabled()) {
                        fileTransferLogger.debug(
                            "Skiping. Unreadable for FileTransfer:" +
                            fullfilename);
                    }
                    continue;
                }

                StringBuilder sb = new StringBuilder();

                sb.append(fullfilename);

                if (files[j].isDir()) {
                    sb.append(fts.srcInfoParams.getFileSeparator());
                }

                sb.append(",");

                sb.append(fts.getAbsoluteDstPath(files[j]));

                if (files[j].isDir()) {
                    sb.append(fts.dstInfoParams.getFileSeparator());
                }

                if (files[j].isDir()) {
                    uParam.addDeploymentDir(sb.toString());
                } else { //isFile
                    uParam.addDeploymentFile(sb.toString());
                }

                if (fileTransferLogger.isDebugEnabled()) {
                    fileTransferLogger.debug("Unicore FileTransfer:" +
                        sb.toString());
                }
            }
        }

        //Because FileTransfer will be submited with the process,
        //we return success so no other protocols will be tried.
        return true;
    }

    public String getProcessId() {
        return "unicore_" + targetProcess.getProcessId();
    }

    public int getNodeNumber() {
        return uParam.getVsiteNodes() * uParam.getVsiteProcessors();
    }

    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    @Override
    public String getFileTransferDefaultCopyProtocol() {
        return FILE_TRANSFER_DEFAULT_PROTOCOL;
    }
}
