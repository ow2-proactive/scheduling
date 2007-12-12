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
package org.objectweb.proactive.core.process.filetransfer;

import java.util.ArrayList;

import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition.FileDescription;


/**
 * Secure Copy Protocol module. Depends on the existence
 * of the "scp" command.
 *
 * @author  ProActive Team
 * @version 1.0,  2004/08/26
 * @since   ProActive 2.3
 */
public class SecureCopyProtocol extends AbstractCopyProtocol {
    protected String COMMAND = "scp";
    protected String PARAMS = "-pr";

    public SecureCopyProtocol(String name) {
        super(name);
    }

    /**
     * @see org.objectweb.proactive.core.process.filetransfer.CopyProtocol#startFileTransfer()
     */
    public boolean startFileTransfer() {
        int globalReturnCode = 1; //default is unsuccess

        String homocommand = buildHomonymousTransfer();
        String[] heterocommand = buildHeteronymousTransfer();

        //Put homonymous and heteronymous on the same array
        String[] allCommands = new String[(homocommand.length() > 0)
            ? (heterocommand.length + 1) : heterocommand.length];

        if (homocommand.length() > 0) {
            allCommands[allCommands.length - 1] = homocommand;
        }
        for (int i = 0; i < heterocommand.length; i++)
            allCommands[i] = heterocommand[i];

        Process[] allProcess = new Process[allCommands.length];

        // DEBUG
        if (logger.isDebugEnabled()) {
            logger.debug(homocommand);
            for (int i = 0; i < heterocommand.length; i++)
                logger.debug(heterocommand[i]);
        }

        /* TODO improove :
         *                 -The SDTOUT, STDIN, SDTERR handling and implement flag to close the sockets
         *                 -Make this a parallel copy?? (Does it make sense?)
         */
        for (int i = 0; i < allProcess.length; i++) {
            try {
                int returnCode;
                allProcess[i] = Runtime.getRuntime().exec(allCommands[i]);
                returnCode = allProcess[i].waitFor();
                if (returnCode != 0) { //it's safe to call this method because the stream is closed
                    logger.error(getErrorMessage(allProcess[i].getErrorStream()));
                } else {
                    globalReturnCode = returnCode; //One succes is good enough to return global success
                }
            } catch (Exception e) {
                logger.error("Error forking scp");
                e.printStackTrace();
            }
        }

        return globalReturnCode == 0; //0 if forked external process returned succesfully
    }

    private String buildHomonymousTransfer() {
        FileDescription[] files = getHomonymousAll();

        //No files to transfer
        if (files.length <= 0) {
            return "";
        }

        //command
        StringBuilder sb = new StringBuilder();
        sb.append(COMMAND);

        //arguments
        sb.append(" ").append(PARAMS);

        //files
        for (int i = 0; i < files.length; i++) {
            // prefix/filename
            String fullfilename = FileTransferWorkShop.buildFilePathString(srcInfoParams,
                    files[i].getSrcName());

            //Skip unreadable file
            if (!FileTransferWorkShop.isLocalReadable(fullfilename)) {
                //Skip unreadable file		
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping: " + fullfilename);
                }
                continue;
            }

            sb.append(" ");
            sb.append(fullfilename);
        }

        sb.append(" ");
        if (dstInfoParams.username.length() > 0) {
            sb.append(dstInfoParams.username).append("@");
        }
        sb.append(dstInfoParams.hostname).append(":");
        sb.append(dstInfoParams.getPrefix());

        return sb.toString();
    }

    private String[] buildHeteronymousTransfer() {
        FileDescription[] files = getHeteronymousAll();
        ArrayList<String> command = new ArrayList<String>();

        //Files & Dirs are the same
        for (int i = 0; i < files.length; i++) {
            String fullfilename = FileTransferWorkShop.buildFilePathString(srcInfoParams,
                    files[i].getSrcName());

            //Skip unreadable file
            if (!FileTransferWorkShop.isLocalReadable(fullfilename)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping: " + fullfilename);
                }
                continue;
            }

            StringBuilder sb = new StringBuilder();

            //command
            sb.append(COMMAND).append(" ").append(PARAMS).append(" ");

            //srcprefix/srcfilename
            sb.append(fullfilename);

            //hostname:destprefix/destfilename
            sb.append(" ");
            if (dstInfoParams.username.length() > 0) {
                sb.append(dstInfoParams.username).append("@");
            }
            sb.append(dstInfoParams.hostname).append(":");

            sb.append(FileTransferWorkShop.buildFilePathString(dstInfoParams,
                    files[i].getDestName()));

            command.add(sb.toString());
        }

        return command.toArray(new String[0]);
    }

    /**
     * @see org.objectweb.proactive.core.process.filetransfer.CopyProtocol#checkProtocol()
     */
    public boolean checkProtocol() {
        boolean retval = true;
        if (dstInfoParams.hostname.length() <= 0) {
            logger.error("Destination hostname not specified");
            retval = false;
        }

        return retval;
    }
}
