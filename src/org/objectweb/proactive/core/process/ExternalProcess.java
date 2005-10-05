/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.process;

import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.util.MessageLogger;


/**
 * A class implementing this interface is able to start a UniversalProcess and to connect
 * its input/output to handlers or to close all streams.
 */
public interface ExternalProcess extends UniversalProcess {

    /**
     * Closes Input, Output, Error stream just after forking this process
     */
    public void closeStream();

    /**
     * Returns the MessageLogger handling the input stream of the process
     * @return the MessageLogger handling the input stream of the process
     */
    public MessageLogger getInputMessageLogger();

    /**
     * Returns the MessageLogger handling the error stream of the process
     * @return the MessageLogger handling the error stream of the process
     */
    public MessageLogger getErrorMessageLogger();

    /**
     * Returns the MessageSink handling the output stream of the process
     * @return the MessageSink handling the output stream of the process
     */
    public MessageSink getOutputMessageSink();

    /**
     * sets the MessageLogger handling the input stream of the process
     * @param inputMessageLogger the handler of the input stream of the process
     */
    public void setInputMessageLogger(MessageLogger inputMessageLogger);

    /**
     * sets the MessageLogger handling the error stream of the process
     * @param errorMessageLogger the handler of the error stream of the process
     */
    public void setErrorMessageLogger(MessageLogger errorMessageLogger);

    /**
     * sets the MessageSink handling the output stream of the process
     * @param outputMessageSink the handler of the output stream of the process
     */
    public void setOutputMessageSink(MessageSink outputMessageSink);

    /**
     * This method returns a single FileTransferStructure instance
     * for this process. If many invocations to this method are done,
     * the same instance of FileTransferStructure will be returned.
     * This means, that changes made to the structure will be reflected
     * on all the references obtained through this method for this process.
     * Note that different process do not share a FileTransferStructure,
     * and thus changes made to one will not reflect on the other.
     */
    public FileTransferWorkShop getFileTransferWorkShopDeploy();

    /**
     * This method returns a single FileTransferStructure instance
     * for this process. If many invocations to this method are done,
     * the same instance of FileTransferStructure will be returned.
     * This means, that changes made to the structure will be reflected
     * on all the references obtained through this method for this process.
     * Note that different process do not share a FileTransferStructure,
     * and thus changes made to one will not reflect on the other.
     */
    public FileTransferWorkShop getFileTransferWorkShopRetrieve();

    /**
     * Returns the type of this process.
     * For processes that does not reference another process, the type is NO_COMPOSITION
     * For other types are APPEND_TO_COMMAND_COMPOSITION or SEND_TO_OUTPUT_STREAM_COMPOSITION or
     * GIVE_COMMAND_AS_PARAMETER or COPY_FILE_AND_APPEND_COMMAND.
     * @return the type this process.
     */
    public int getCompositionType();
}
