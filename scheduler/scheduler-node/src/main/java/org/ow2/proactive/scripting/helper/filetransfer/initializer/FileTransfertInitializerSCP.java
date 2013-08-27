/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.filetransfer.initializer;

import org.ow2.proactive.scripting.helper.filetransfer.driver.FileTransfertDriver;
import org.ow2.proactive.scripting.helper.filetransfer.driver.SCP_Trilead_Driver;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertProtocols.Protocol;


/**
 * FileTransfertInitializerSCP...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class FileTransfertInitializerSCP implements FileTransfertInitializer {
    private String _host = "";
    private String _user = "";
    private String _pass = "";
    int compression_level = 0;

    //default scp port is 22
    private int _port = 22;

    //--FileTransfertDriverVFSSCP is the default driver
    protected Class<? extends FileTransfertDriver> _driverClass = SCP_Trilead_Driver.class;

    public FileTransfertInitializerSCP(String host, String user, String pass) {
        _host = host;
        _user = user;
        _pass = pass;
    }

    public FileTransfertInitializerSCP(String host, String user, String pass,
            Class<? extends FileTransfertDriver> driver) {
        _host = host;
        _user = user;
        _pass = pass;
        _driverClass = driver;
    }

    public FileTransfertInitializerSCP(String host, String user, String pass, int port) {
        this(host, user, pass);
        _port = port;
    }

    public FileTransfertInitializerSCP(String host, String user, String pass, int port,
            Class<? extends FileTransfertDriver> driver) {
        this(host, user, pass, port);
        _driverClass = driver;
    }

    public Class<? extends FileTransfertDriver> getDriverClass() {
        return _driverClass;
    }

    public Protocol getProtocol() {
        return Protocol.SCP;
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    public String getUser() {
        return _user;
    }

    public String getPassword() {
        return _pass;
    }

    public int getCompression_level() {
        return compression_level;
    }

    public void setCompression_level(int compression_level) {
        this.compression_level = compression_level;
    }

}
