package org.ow2.proactive.scripting.helper.filetransfer.initializer;

import org.ow2.proactive.scripting.helper.filetransfer.driver.FileTransfertDriver;
import org.ow2.proactive.scripting.helper.filetransfer.driver.SCP_Trilead_Driver;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertProtocols.Protocol;


public class FileTransfertInitializerSCP implements FileTransfertInitializer {
    private String _host = "";
    private String _user = "";
    private String _pass = "";

    //default scp port is 22
    private int _port = 22;

    //--FileTransfertDriverVFSSCP is the default driver
    private Class<? extends FileTransfertDriver> _driverClass = SCP_Trilead_Driver.class;

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

}
