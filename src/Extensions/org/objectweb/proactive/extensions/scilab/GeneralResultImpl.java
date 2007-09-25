package org.objectweb.proactive.extensions.scilab;

import java.util.ArrayList;
import java.util.List;


public class GeneralResultImpl implements GeneralResult {

    /**
         *
         */
    private static final long serialVersionUID = 5006133711230740830L;
    private String id;
    private int state;
    private long timeExecution;
    private String message = null;
    private Exception exp = null;
    protected ArrayList<AbstractData> listResults;

    public GeneralResultImpl() {
        // empty no arg cons
    }

    public GeneralResultImpl(String id) {
        this.id = id;
        listResults = new ArrayList<AbstractData>();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#getState()
     */
    public int getState() {
        return state;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#setState(int)
     */
    public void setState(int state) {
        this.state = state;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#getTimeExecution()
     */
    public long getTimeExecution() {
        return timeExecution;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#setTimeExecution(long)
     */
    public void setTimeExecution(long timeExecution) {
        this.timeExecution = timeExecution;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scilab.GeneralResult#getId()
     */
    public String getId() {
        return id;
    }

    public void setException(Exception exp) {
        this.exp = exp;
    }

    public boolean isException() {
        return this.exp != null;
    }

    public Exception getException() {
        return this.exp;
    }

    /**
    *
    * @param name data id
    * @return the data
    */
    public AbstractData get(String name) {
        for (AbstractData data : listResults) {
            if (data.getName().equals(name)) {
                return data;
            }
        }

        return null;
    }

    /**
    *
    * @return list of all out data
    */
    public List<AbstractData> getList() {
        return listResults;
    }

    /**
     * add an Out data
     * @param data
     */
    public void add(AbstractData data) {
        this.listResults.add(data);
    }

    public boolean hasMessage() {
        return this.message != null;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
