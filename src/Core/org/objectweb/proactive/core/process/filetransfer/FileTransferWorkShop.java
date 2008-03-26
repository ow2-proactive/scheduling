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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition.FileDescription;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class stores the FileTransfer arquitecture specific
 * information. It also has a reference on the abstract
 * FileTransfer definitions.
 *
 * The tools for mergin the abstract and specific information
 * are also provided through this class.
 *
 * @author The ProActive Team
 * @version 1.0,  2005/08/26
 * @since   ProActive 2.3
 */
public class FileTransferWorkShop implements Serializable {
    private static final String PROCESSDEFAULT_KEYWORD = "processDefault";
    private static final String IMPLICIT_KEYWORD = "implicit";
    private static final String[] ALLOWED_COPY_PROTOCOLS = { PROCESSDEFAULT_KEYWORD, "scp", "unicore", "rcp",
            "nordugrid", "pftp" };
    private static final String[] URLPROTOCOLS = { "file://", "http://", "ftp://" };
    final protected static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_FILETRANSFER);

    /* Reference to filetransfer definitions */
    private HashMap<String, FileTransferDefinition> fileTransfers;

    /*Array with protocols to try*/
    private String[] copyProtocol;
    private String processDefault;
    private boolean isImplicit;
    public StructureInformation srcInfoParams;
    public StructureInformation dstInfoParams;

    /**
     * Constructs a FileTransferWorkshop.
     * @param processDefault The default type of protocols to use. Should be one of the defined in
     * the ALLOWED_COPY_PROTOCOLS
     */
    public FileTransferWorkShop(String processDefault) {
        //Verification of ilegal name for processDefault=="processDefault"
        if ((processDefault == null) || (processDefault.length() <= 0) ||
            processDefault.equalsIgnoreCase(PROCESSDEFAULT_KEYWORD)) {
            logger.error("Illegal processDefault value=" + processDefault + " in " + this.getClass() +
                ". Falling back to dummy.");
            this.processDefault = "dummy";
        } else {
            this.processDefault = processDefault;
        }

        isImplicit = false;
        fileTransfers = new HashMap<String, FileTransferDefinition>();
        copyProtocol = new String[0];
        srcInfoParams = new StructureInformation();
        dstInfoParams = new StructureInformation();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        //Queue variables
        sb.append("isImplicit     = ").append(isImplicit).append("\n");
        sb.append("processDefault = ").append(processDefault).append("\n");

        //Copy Protocol
        sb.append("copyProtocols  = ");
        for (int i = 0; i < copyProtocol.length; i++)
            sb.append(copyProtocol[i]).append(",");
        sb.append("\n");

        //SrcInfo
        sb.append("Src Information Parameters\n");
        sb.append(srcInfoParams).append("\n");

        //DstInfo
        sb.append("Dst Information Parameters\n");
        sb.append(dstInfoParams).append("\n");

        //FileTransfers
        Iterator<String> it = fileTransfers.keySet().iterator();
        while (it.hasNext()) {
            FileTransferDefinition ft = fileTransfers.get(it.next());

            sb.append(ft.toString()).append("\n");
        }

        while ((sb.length() > 0) && (sb.charAt(sb.length() - 1) == '\n'))
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Adds a File Transfer Definition to the FTW.
     * @param ft
     */
    public synchronized void addFileTransfer(FileTransferDefinition ft) {
        if (ft == null) {
            return;
        }
        if (ft.getId().equalsIgnoreCase(IMPLICIT_KEYWORD)) {
            logger.warn("Warning, ignoring addFileTransfer with keyword id=" + IMPLICIT_KEYWORD);
            return;
        }

        fileTransfers.put(ft.getId(), ft);
    }

    /**
     * Sets the copy protocol sequence, that should be executed in order to attempt
     * the FileTransfer.
     * @param copyProtocolString A coma separeted string, where each value represents
     * a protocol specified in the ALLOWED_COPY_PROTOCOLS array.
     */
    public void setFileTransferCopyProtocol(String copyProtocolString) {
        copyProtocol = copyProtocolString.split("\\s*,\\s*");
    }

    /**
     * This method returns an array of CopyProtocol instances.
     * This instances are based on the value configured through
     * the FileTransferStructure.setCopyProtocols()
     * @return An array of CopyProtocol[].
     */
    public CopyProtocol[] getCopyProtocols() {
        ArrayList<CopyProtocol> alist = new ArrayList<CopyProtocol>();

        StringBuilder skippedProtocols = new StringBuilder();
        for (int i = 0; i < copyProtocol.length; i++) {
            if (!isAllowedProtocol(copyProtocol[i])) {
                skippedProtocols.append(copyProtocol[i]).append(" ");
                continue;
            }
            alist.add(copyProtocolFactory(copyProtocol[i]));
        }

        if (skippedProtocols.length() > 0) {
            logger.warn("Unknown copyprotocols will be skipped:" + skippedProtocols.toString());
        }

        //if no Protocol is defined use the default
        if (alist.size() <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No CopyProtocols found, using default protocol:" + processDefault);
            }

            alist.add(copyProtocolFactory(PROCESSDEFAULT_KEYWORD));
        }

        return alist.toArray(new CopyProtocol[0]);
    }

    /**
     * This methods creates a CopyProtocol instance, given by it's
     * name as a parameter. If the name is unknown, then a
     * DummyCopyProtocol is returned.
     *
     * If the name of the protocol is "processDefault" then a
     * corresponding instance will be returned with the flag:
     * CopyProtocol.isDefaultProtocol() set to true.
     *
     * Note that in this last case it is possible to have a
     * DummyProtocol with the flag set to true if the factory
     * doesn't know how to make the default protocol.
     *
     * @param protocolname The name of the desired transfer protocol
     * @return An instance of a class that implements CopyProtocol.
     */
    public CopyProtocol copyProtocolFactory(String protocolname) {
        CopyProtocol cp;

        if (protocolname.equalsIgnoreCase("scp")) {
            cp = new SecureCopyProtocol(protocolname);
        } else if (protocolname.equalsIgnoreCase(PROCESSDEFAULT_KEYWORD)) {
            //Note: this will produce an infinit recursion if
            // processDefault==PROCESSDEFAULT_KEYWORD
            cp = copyProtocolFactory(processDefault); //cool recursive call
            cp.setDefaultProtocol(true);
            return cp;
        } else if (protocolname.equalsIgnoreCase("rcp")) {
            cp = new RemoteFileCopy(protocolname);
        } else {
            cp = new DummyCopyProtocol(protocolname); //pftp, unicore, nordugrid are created here
            return cp;
        }

        //Default values for almost all copy protocols (except dummies)
        cp.setFileTransferDefinitions(getAllFileTransferDefinitions());
        cp.setSrcInfo(srcInfoParams);
        cp.setDstInfo(dstInfoParams);

        return cp;
    }

    /**
     * Gives all the File Transfer Definitions mapped on to this FTW.
     * @return A File Transfer Definition Array.
     */
    public synchronized FileTransferDefinition[] getAllFileTransferDefinitions() {
        ArrayList<FileTransferDefinition> ftList = new ArrayList<FileTransferDefinition>();

        Iterator<String> it = fileTransfers.keySet().iterator();
        while (it.hasNext()) {
            FileTransferDefinition ft = fileTransfers.get(it.next());

            ftList.add(ft);
        }

        return ftList.toArray(new FileTransferDefinition[0]);
    }

    /**
     * Sets the source information for this FTW.
     * @param name  The name of the parameter from: prefix, hostname, username, password
     * @param value The value of the parameter.
     */
    public void setFileTransferStructureSrcInfo(String name, String value) {
        srcInfoParams.setInfoParameter(name, value);
    }

    /**
     * Sets the destination information for this FTW.
     * @param name  The name of the parameter from: prefix, hostname, username, password
     * @param value The value of the parameter.
     */
    public void setFileTransferStructureDstInfo(String name, String value) {
        dstInfoParams.setInfoParameter(name, value);
    }

    /**
     * Checks different things. For now, it prints a warning
     * when an empty FileTransfer definition is found.
     * @return true if everything is OK, false if there is a
     * problem.
     */
    public boolean check() {
        if (fileTransfers.size() <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No file transfer required.");
            }
            return false;
        }

        //Checking FileTransfer definitions
        boolean retval = false;
        Collection<FileTransferDefinition> list = fileTransfers.values();
        for (FileTransferDefinition ft : list) {
            if (ft.isEmpty()) {
                logger.warn("Warning: FileTransfer definition id=" + ft.getId() + " is empty or undefined.");
                continue;
            }

            //Obs: this might be a problem with Retrieve
            if (checkLocalFileExistance(ft)) {
                retval = true; //At least one file to transfer
            }
        }
        return retval;
    }

    /**
     * This method is used to determine if a direct file transfer definition
     * reference has been specified at the process level, or wether the
     * reference should be inherited from the upper levels (Virtual Node).
     * @return true if the filetransfer process level definition should inherit
     * the File Transfer Definitions from the upper levels (Virtual Node)
     */
    public boolean isImplicit() {
        return isImplicit;
    }

    public void setImplicit(boolean implicit) {
        this.isImplicit = implicit;
    }

    /**
     * Tells if a specified copy protocol is an allowed protocol
     * @param protocol
     * @return true if it is allowed, false otherwise.
     */
    public boolean isAllowedProtocol(String protocol) {
        for (int i = 0; i < ALLOWED_COPY_PROTOCOLS.length; i++)
            if (ALLOWED_COPY_PROTOCOLS[i].equalsIgnoreCase(protocol)) {
                return true;
            }

        return false;
    }

    /**
     * This method is used to get The fileDescription associated with
     * this workshop.
     * @return an array with all the FileDescriptions linked with this FTW.
     */
    public FileDescription[] getAllFileDescriptions() {
        ArrayList<FileDescription> fd = new ArrayList<FileDescription>();

        Iterator<String> it = fileTransfers.keySet().iterator();
        while (it.hasNext()) {
            FileTransferDefinition ft = fileTransfers.get(it.next());

            FileDescription[] fdesc = ft.getAllFiles();
            for (int i = 0; i < fdesc.length; i++)
                fd.add(fdesc[i]);
        }

        return fd.toArray(new FileDescription[0]);
    }

    /**
     * For a given filename, it creates the Full Path using
     * the source information of this FTW.
     * @param fileDesc
     * @return The full path String
     */
    public String getAbsoluteSrcPath(FileDescription fileDesc) {
        return buildFilePathString(srcInfoParams.getPrefix(), srcInfoParams.getFileSeparator(), fileDesc
                .getSrcName());
    }

    public String getAbsoluteDstPath(FileDescription fileDesc) {
        return buildFilePathString(dstInfoParams.getPrefix(), dstInfoParams.getFileSeparator(), fileDesc
                .getDestName());
    }

    public static String buildFilePathString(StructureInformation infoParam, String filename) {
        return buildFilePathString(infoParam.getPrefix(), infoParam.getFileSeparator(), filename);
    }

    public static String buildFilePathString(String prefix, String fileSep, String filename) {
        /*
         *BORDER CONDITIONS
         */

        //Trim white spaces
        prefix = prefix.trim();
        fileSep = fileSep.trim();
        filename = filename.trim();

        //Asign a default filesep if needed
        if (fileSep.length() <= 0) {
            fileSep = "/";
        }

        //Remove trailing slash of filename if needed
        if (filename.endsWith(fileSep)) {
            filename.substring(0, filename.length() - 1);
        }

        //Remove trailing slash from prefix if needed
        if (prefix.endsWith(fileSep)) {
            prefix.substring(0, prefix.length() - 1);
        }

        /*
         * CASES EVALUATION
         */

        // -case1: filename starts from root path, nothing to do
        // -case2: filename starts with procol "http://", "ftp://", nothing to do
        // -case3: no prefix, nothing to do
        if ((filename.charAt(0) == fileSep.charAt(0)) || begginsWithProtocol(filename) ||
            (prefix.length() <= 0)) {
            return filename;
        }

        //-case3: prefix is defined
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(fileSep).append(filename);
        return sb.toString();
    }

    public static boolean begginsWithProtocol(String s) {
        for (int i = 0; i < URLPROTOCOLS.length; i++) {
            if (s.startsWith(URLPROTOCOLS[i])) {
                return true;
            }
        }

        return false;
    }

    public boolean checkLocalFileExistance(FileTransferDefinition ft) {
        boolean atLeastOneFile = false;

        FileTransferDefinition.FileDescription[] files = ft.getAll();

        File f;
        String filefullname;
        for (int i = 0; i < files.length; i++) {
            filefullname = getAbsoluteSrcPath(files[i]);

            if (begginsWithProtocol(filefullname)) {

                /* Can't yet check existance for http:// or ftp://
                 * Therefore, we suppose the file exists.
                 * But we can check: file://
                 */
                if (!filefullname.startsWith("file://")) {
                    atLeastOneFile = true;
                    continue;
                }

                filefullname = stripProtocol(filefullname);
            }

            f = new File(filefullname);

            if (!f.exists()) {
                logger.warn("Warning, nonexistent: " + filefullname);
            } else if (!f.canRead()) {
                logger.warn("Warning, unreadble: " + filefullname);
            } else {
                atLeastOneFile = true; //at least one dir is found
            }

            if (files[i].isDir() && !f.isDirectory()) {
                logger.warn("Warning, not a directory: " + filefullname);
            }
        }

        return atLeastOneFile;
    }

    public static boolean isLocalReadable(String filenamepath) {
        if (begginsWithProtocol(filenamepath)) {
            if (!filenamepath.startsWith("file://")) {
                return false;
            }

            filenamepath = stripProtocol(filenamepath);
        }

        File f = new File(filenamepath);

        return f.canRead();
    }

    public static boolean isRemote(String filenamepath) {
        if (begginsWithProtocol(filenamepath) && !filenamepath.startsWith("file://")) {
            return true;
        }
        return false;
    }

    public static String stripProtocol(String filename) {
        int i = filename.indexOf("://");

        if ((filename.length() - i - 3) > 0) {
            return filename.substring(i + 3);
        }

        return "";
    }

    public class StructureInformation implements Serializable {

        /* FileTransferQueue specific information */
        String prefix;
        String hostname;
        String username;
        String password;
        String fileSeparator;

        public StructureInformation() {
            prefix = "";
            hostname = "";
            username = "";
            password = "";
            fileSeparator = "/";
        }

        public void setInfoParameter(String name, String value) {
            if (name.equalsIgnoreCase("prefix")) {
                value = value.trim();
                //delete ending file separators
                while ((value.length() > 0) && (value.endsWith("/") || value.endsWith("\\")))
                    value = value.substring(0, value.length() - 1);

                if (value.length() > 0) {
                    prefix = value;
                }
            } else if (name.equalsIgnoreCase("hostname")) {
                hostname = value;
            } else if (name.equalsIgnoreCase("username")) {
                username = value;
            } else if (name.equalsIgnoreCase("password")) {
                password = value;
            } else if (name.equalsIgnoreCase("fileseparator")) {
                fileSeparator = value;
            } else {
                logger.warn("Skipping:" + name + "=" + value +
                    ". Unknown FileTransfer information parameter.");
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("prefix        = ").append(prefix).append("\n");
            sb.append("hostname      = ").append(hostname).append("\n");
            sb.append("username      = ").append(username).append("\n");
            sb.append("password      = ").append(password).append("\n");
            sb.append("fileSeparator = ").append(fileSeparator);

            return sb.toString();
        }

        /**
         * @return Returns the fileSeparator.
         */
        public String getFileSeparator() {
            return fileSeparator;
        }

        /**
         * @param fileSeparator The fileSeparator to set.
         */
        public void setFileSeparator(String fileSeparator) {
            this.fileSeparator = fileSeparator;
        }

        /**
         * @return Returns the prefix.
         */
        public String getPrefix() {
            return prefix;
        }

        /**
         * @param prefix The prefix to set.
         */
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        /**
         * @return Returns the hostname.
         */
        public String getHostname() {
            return hostname;
        }

        /**
         * @return Returns the username.
         */
        public String getUsername() {
            return username;
        }

        /**
         * @param hostname The hostname to set.
         */
        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        /**
         * @param username The username to set.
         */
        public void setUsername(String username) {
            this.username = username;
        }
    }
}
