/*
 * Created on Jul 24, 2003
 *
 */
package testsuite.result;

import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import testsuite.exception.BadTypeException;


/**
 * @author Alexandre di Costanzo
 *
 */
public abstract class AbstractResult {
    public static final int ERROR = 2;
    public static final int MSG = -2;
    public static final int INFO = -3;
    public static final int RESULT = -1;
    public static final int GLOBAL_RESULT = 1;
    public static final int IMP_MSG = 0;
    private int type = -2;
    private String message = "No message";
    private Throwable exception = null;
    private Calendar time = null;
    private boolean shortDateFormat = true;

    public AbstractResult(int type, String message) throws BadTypeException {
        if (!isValidType(type)) {
            throw new BadTypeException();
        }
        this.type = type;
        this.message = message;
        time = Calendar.getInstance();
    }

    public AbstractResult(int type, String message, Throwable e)
        throws BadTypeException {
        if (!isValidType(type)) {
            throw new BadTypeException();
        }
        this.type = type;
        this.message = message;
        this.exception = e;
        time = Calendar.getInstance();
    }

    private boolean isValidType(int type) {
        if ((type < 3) && (type > -4)) {
            return true;
        } else {
            return false;
        }
    }

    public abstract Node toXMLNode(Document document);

    /**
     * @see java.lang.Object#toString()
     */
    public abstract String toString();

    /**
     * @return
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * @return
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return
     */
    public boolean isShortDateFormat() {
        return shortDateFormat;
    }

    /**
     * @param shortDateFormat
     */
    public void setShortDateFormat(boolean shortDateFormat) {
        this.shortDateFormat = shortDateFormat;
    }

    /**
     * @param i
     */
    public void setType(int i) {
        type = i;
    }
}
