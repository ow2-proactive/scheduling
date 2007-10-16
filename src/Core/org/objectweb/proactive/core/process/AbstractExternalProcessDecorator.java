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
package org.objectweb.proactive.core.process;

import org.objectweb.proactive.core.process.filetransfer.CopyProtocol;
import org.objectweb.proactive.core.process.filetransfer.FileDependant;
import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


public abstract class AbstractExternalProcessDecorator
    extends AbstractExternalProcess implements ExternalProcessDecorator {
    protected ExternalProcess targetProcess;
    private int compositionType = APPEND_TO_COMMAND_COMPOSITION;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public AbstractExternalProcessDecorator() {
        super();
    }

    public AbstractExternalProcessDecorator(ExternalProcess targetProcess) {
        this(targetProcess, APPEND_TO_COMMAND_COMPOSITION);
    }

    public AbstractExternalProcessDecorator(ExternalProcess targetProcess,
        int compositionType) {
        super();
        setTargetProcess(targetProcess);
        this.compositionType = compositionType;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public ExternalProcess getTargetProcess() {
        return targetProcess;
    }

    public void setTargetProcess(ExternalProcess targetProcess) {
        checkStarted();
        this.targetProcess = targetProcess;
        setInputMessageLogger(targetProcess.getInputMessageLogger());
        setErrorMessageLogger(targetProcess.getErrorMessageLogger());
        setOutputMessageSink(targetProcess.getOutputMessageSink());
    }

    @Override
    public int getCompositionType() {
        return compositionType;
    }

    public void setCompositionType(int compositionType) {
        checkStarted();
        this.compositionType = compositionType;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected void toString(StringBuilder sb) {
        super.toString(sb);
        sb.append(" ---- Target Process ----- \n");
        if (targetProcess == null) {
            sb.append(" NOT DEFINED \n");
        } else {
            sb.append(targetProcess.toString());
        }
        sb.append(" -------------- \n");
    }

    @Override
    protected String buildCommand() {
        if ((compositionType == SEND_TO_OUTPUT_STREAM_COMPOSITION) ||
                (compositionType == GIVE_COMMAND_AS_PARAMETER) ||
                (compositionType == COPY_FILE_AND_APPEND_COMMAND)) {
            return internalBuildCommand();
        } else {
            if (targetProcess != null) {
                //we have to process the target command to backslash quotation mark
                //so that it is not interpreted by the current process but the target one
                //we avoid already backslashed one
                String targetCommand = targetProcess.getCommand();
                if (targetProcess.getCompositionType() == COPY_FILE_AND_APPEND_COMMAND) {
                    handleCopyFile();
                }
                return internalBuildCommand() + targetCommand;
            } else {
                return internalBuildCommand();
            }
        }
    }

    protected abstract String internalBuildCommand();

    @Override
    protected void internalStartProcess(String commandToExecute)
        throws java.io.IOException {
        super.internalStartProcess(commandToExecute);
        if (compositionType == SEND_TO_OUTPUT_STREAM_COMPOSITION) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

            // the masterProcess is started, now we feed the output with the slave command
            outputMessageSink.setMessage(targetProcess.getCommand());
        }
    }

    @Override
    protected void handleOutput(java.io.BufferedWriter out) {
        if (compositionType == SEND_TO_OUTPUT_STREAM_COMPOSITION) {
            if (outputMessageSink == null) {
                outputMessageSink = new SimpleMessageSink();
            }
        }
        super.handleOutput(out);
    }

    protected void handleCopyFile() {
        FileTransferWorkShop ftw = new FileTransferWorkShop(getFileTransferDefaultCopyProtocol());
        ftw.setFileTransferStructureDstInfo("hostname", hostname);
        ftw.setFileTransferStructureDstInfo("username", username);
        try {
            ftw.addFileTransfer(((FileDependant) targetProcess).getFileTransfertDefinition());
        } catch (ClassCastException e) {
            logger.error(
                "Unable to handle the file transfert dependant process");
            return;
        }

        CopyProtocol cp = ftw.copyProtocolFactory(getFileTransferDefaultCopyProtocol());
        cp.startFileTransfer();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //

    /**
     * Implementation of a RemoteProcessMessageLogger that feeds two MessageLoggers
     */
    public static class CompositeMessageLogger
        implements RemoteProcessMessageLogger, java.io.Serializable {
        private RemoteProcessMessageLogger messageLogger1;
        private RemoteProcessMessageLogger messageLogger2;

        public CompositeMessageLogger(
            RemoteProcessMessageLogger messageLogger1,
            RemoteProcessMessageLogger messageLogger2) {
            this.messageLogger1 = messageLogger1;
            this.messageLogger2 = messageLogger2;
        }

        public void log(String message) {
            messageLogger1.log(message);
            messageLogger2.log(message);
        }

        public void log(Throwable t) {
            messageLogger1.log(t);
            messageLogger2.log(t);
        }

        public void log(String message, Throwable t) {
            messageLogger1.log(message, t);
            messageLogger2.log(message, t);
        }
    } // end inner class CompositeMessageLogger

    /**
     * Implementation of a MessageSink that can receive one message at a time
     */
    public static class CompositeMessageSink implements MessageSink {
        private MessageSink messageSink1;
        private MessageSink messageSink2;

        public CompositeMessageSink(MessageSink messageSink1,
            MessageSink messageSink2) {
            this.messageSink1 = messageSink1;
            this.messageSink2 = messageSink2;
        }

        public synchronized String getMessage() {
            while ((!hasMessage()) && isActive()) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                }
            }
            if (messageSink1.hasMessage()) {
                return messageSink1.getMessage();
            } else if (messageSink2.hasMessage()) {
                return messageSink1.getMessage();
            }
            return null;
        }

        public synchronized void setMessage(String messageToPost) {
            messageSink1.setMessage(messageToPost);
            notifyAll();
        }

        public synchronized boolean hasMessage() {
            return messageSink1.hasMessage() || messageSink2.hasMessage();
        }

        public synchronized boolean isActive() {
            return messageSink1.isActive() || messageSink2.isActive();
        }
    } // end inner class CompositeMessageSink
}
