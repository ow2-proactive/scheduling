package org.objectweb.proactive.extensions.resourcemanager.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.proactive.extensions.resourcemanager.exception.RMException;


public class FileToBytesConverter {

    public static byte[] convertFileToByteArray(File file) throws RMException {
        try {
            FileInputStream GCMDInput = new FileInputStream(file);
            long size = file.length();

            byte[] GCMDeploymentContent = new byte[(new Long(size)).intValue()];
            GCMDInput.read(GCMDeploymentContent, 0, (new Long(size)).intValue());

            return GCMDeploymentContent;
        } catch (FileNotFoundException e) {
            throw new RMException(e);
        } catch (IOException e) {
            throw new RMException(e);
        }
    }

    public static void convertByteArrayToFile(byte[] array, File file) throws RMException {
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(array);
            outStream.close();
        } catch (FileNotFoundException e) {
            throw new RMException(e);
        } catch (IOException e) {
            throw new RMException(e);
        }

    }

}
