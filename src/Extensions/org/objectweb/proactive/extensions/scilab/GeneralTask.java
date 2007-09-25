package org.objectweb.proactive.extensions.scilab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;


public interface GeneralTask extends Serializable {
    public void init() throws TaskException;

    public String getJob();

    public void setJob(String job);

    public void setJobInit(String jobInit);

    public void setJob(File fileJob) throws FileNotFoundException, IOException;

    public String getId();

    public String getJobInit();

    public String getLastMessage();

    public void sendListDataIn() throws TaskException;

    public List<AbstractData> receiveDataOut() throws TaskException;

    public void clearWorkspace() throws TaskException;

    public boolean execute() throws TaskException;

    public void addDataOut(String data);
}
