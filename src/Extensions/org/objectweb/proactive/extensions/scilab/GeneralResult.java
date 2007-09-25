package org.objectweb.proactive.extensions.scilab;

import java.io.Serializable;
import java.util.List;


public interface GeneralResult extends Serializable {
    public static final int SUCCESS = 0;
    public static final int ABORT = 1;

    public abstract int getState();

    public abstract void setState(int state);

    public abstract long getTimeExecution();

    public abstract void setTimeExecution(long timeExecution);

    public abstract String getId();

    public void add(AbstractData data);

    public List<AbstractData> getList();

    public AbstractData get(String name);

    public void setException(Exception exp);

    public void setMessage(String message);

    public boolean hasMessage();

    public String getMessage();

    public boolean isException();

    public Exception getException();
}
