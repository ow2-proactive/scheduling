package org.objectweb.proactive.extensions.scilab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public abstract class AbstractGeneralTask implements GeneralTask {
    String id;
    String job;
    String jobInit;
    protected ArrayList<String> listDataOut;

    public AbstractGeneralTask(String id) {
        this.id = id;
        this.listDataOut = new ArrayList<String>();
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void setJobInit(String jobInit) {
        this.jobInit = jobInit;
    }

    public void setJob(File fileJob) throws FileNotFoundException, IOException {
        StringBuffer strBuffer = new StringBuffer();

        FileReader reader = new FileReader(fileJob);
        int c;

        while ((c = reader.read()) != -1) {
            strBuffer.append((char) c);
        }

        this.job = strBuffer.toString();

        reader.close();
    }

    public String getId() {
        return id;
    }

    public String getJobInit() {
        return jobInit;
    }

    public ArrayList<String> getListDataOut() {
        return listDataOut;
    }

    public void setListDataOut(ArrayList<String> listDataOut) {
        this.listDataOut = listDataOut;
    }

    public void addDataOut(String data) {
        this.listDataOut.add(data);
    }
}
