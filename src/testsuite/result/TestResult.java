/*
 * Created on Jul 23, 2003
 *
 */
package testsuite.result;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import testsuite.exception.BadTypeException;
import testsuite.test.AbstractTest;


/**
 * @author Alexandre di Costanzo
 *
 */
public class TestResult extends AbstractResult {
    private AbstractTest test = null;

    public TestResult(AbstractTest test, int type, String message)
        throws BadTypeException {
        super(type, message);
        this.test = test;
    }

    public TestResult(AbstractTest test, int type, String message, Throwable e)
        throws BadTypeException {
        super(type, message, e);
        this.test = test;
    }

    public Node toXMLNode(Document document) {
        Element root = document.createElement("Result");
        root.setAttribute("type", getType() + "");
        
        if (test != null) {
            root.setAttribute("failed", test.isFailed() + "");

            Node testName = document.createElement("TestName");
            Node testNameText = document.createTextNode(test.getName());
            testName.appendChild(testNameText);
            root.appendChild(testName);

            Node testDescription = document.createElement("TestDescription");
            Node testDescriptionText = document.createTextNode(test.getDescription());
            testDescription.appendChild(testDescriptionText);
            root.appendChild(testDescription);
        }

        Node date = time(document);
        root.appendChild(date);

        Node message = document.createElement("Message");
        Node messageText = document.createTextNode(getMessage());
        message.appendChild(messageText);
        root.appendChild(message);

        if (getException() != null) {
            Node exception = document.createElement("Exception");
            Node exceptionText = document.createTextNode(printStackTrace());
            exception.appendChild(exceptionText);
            root.appendChild(exception);
        }

        return root;
    }

    private Node time(Document document) {
        Element root = document.createElement("Date");

        root.setAttribute("day", getTime().get(Calendar.DATE) + "");
        root.setAttribute("month", getTime().get(Calendar.MONTH) + "");
        root.setAttribute("year", getTime().get(Calendar.YEAR) + "");

        Element time = document.createElement("Time");
        time.setAttribute("hour", getTime().get(Calendar.HOUR_OF_DAY) + "");
        time.setAttribute("minute", getTime().get(Calendar.MINUTE) + "");
        time.setAttribute("second", getTime().get(Calendar.SECOND) + "");
        time.setAttribute("millisecond",
            getTime().get(Calendar.MILLISECOND) + "");
        root.appendChild(time);

        return root;
    }

    private String time() {
        Date date = getTime().getTime();
        DateFormat df = (isShortDateFormat())
            ? DateFormat.getDateInstance(DateFormat.SHORT)
            : DateFormat.getDateInstance(DateFormat.FULL);
        return df.format(date) + " " + getTime().get(Calendar.HOUR_OF_DAY) +
        ":" + getTime().get(Calendar.MINUTE) + ":" +
        getTime().get(Calendar.SECOND) + "." +
        getTime().get(Calendar.MILLISECOND);
    }

    /**
     *
     */
    public String toString() {
        String res = ((test != null) ? test.getName() : "") +
            " : Empty message";

        switch (getType()) {
        case 2:
            res = time() + " [ERROR] " +
                ((test != null) ? (test.getName() + " : ") : "") +
                ((getMessage() != null) ? getMessage() : "No message") +
                ((getException() != null) ? (" :\n" + printStackTrace()) : "");
            break;
        case 0:
        case -2:
            res = time() + " [MESSAGE] " +
                ((test != null) ? (test.getName() + " : ") : "")  +
                ((getMessage() != null) ? getMessage() : "No message") +
                ((getException() != null) ? (" :\n" + getException()) : "");
            break;
        case 1:
        case -1:
            res = time() + " [RESULT] " +
                ((test != null) ? (test.getName() + " : ") : "")  +
                ((getMessage() != null) ? getMessage() : "No message") +
                (((test != null) && test.isFailed()) ? " [FAILED]" : " [SUCCESS]") +
                ((getException() != null) ? ("\nwith error :" + getException())
                                          : "");
            break;
        case -3:
            res = time() + " [INFO] " +
                ((test != null) ? (test.getName() + " : ") : "") +
                ((getMessage() != null) ? getMessage() : "No message") +
                ((getException() != null) ? (" :\n" + getException()) : "");
            break;
        }
        return res + "\n";
    }

    private String printStackTrace() {
        String res = getException() + "\n";
        StackTraceElement[] stack = getException().getStackTrace();
        for (int i = 0; i < stack.length; i++)
            res += (stack[i] + "\n");
        return res;
    }

    /**
     * @return
     */
    public AbstractTest getTest() {
        return test;
    }
}
