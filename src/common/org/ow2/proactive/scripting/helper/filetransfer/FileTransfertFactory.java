package org.ow2.proactive.scripting.helper.filetransfer;

import org.ow2.proactive.scripting.helper.filetransfer.driver.*;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.*;


public class FileTransfertFactory {

    /**
     * 
     * @param myInit
     * @return
     */
    public static FileTransfertDriver getDriver(FileTransfertInitializer myInit) {

        if (myInit.getDriverClass() != null) {
            try {
                //System.out.println(myInit.getDriverClass().getName());

                FileTransfertDriver ftDriver = (FileTransfertDriver) (myInit.getDriverClass().newInstance());
                ftDriver.init(myInit);
                return ftDriver;

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    //	//TODO: to check this 
    //	public FileTransfertInitializerFTP getInitializerFTP(String host, String user, String pass, int port, Class<? extends FileTransfertDriver> driver, boolean useBinaryMode){
    //		return new FileTransfertInitializerFTP(host, user, pass, port, driver, useBinaryMode);
    //	}	

}
