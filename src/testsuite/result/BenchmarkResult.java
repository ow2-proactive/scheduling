/*
 * Created on Jul 23, 2003
 *
 */
package testsuite.result;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import testsuite.exception.BadTypeException;
import testsuite.test.Benchmark;


/**
 * @author Alexandre di Costanzo
 *
 */
public class BenchmarkResult extends TestResult {
    private double timeResult = 0.0;

    public BenchmarkResult(Benchmark test, int type, String message)
        throws BadTypeException {
        super(test, type, message);
        timeResult = test.getResultTime();
    }

    public BenchmarkResult(Benchmark test, int type, String message, Throwable e)
        throws BadTypeException {
        super(test, type, message, e);
        timeResult = test.getResultTime();
    }

    /**
     * @see testsuite.timeResult.TestResult#toXMLNode()
     */
    public Node toXMLNode(Document document) {
        Node root = super.toXMLNode(document);

        if ((getType() == AbstractResult.RESULT) ||
                (getType() == AbstractResult.GLOBAL_RESULT)) {
            Element result = document.createElement("Benchmark");
            Node resultText = document.createTextNode(timeResult + "");
            result.appendChild(resultText);
            root.appendChild(result);
        }

        return root;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString() +
        (((getType() == RESULT) || (getType() == GLOBAL_RESULT))
        ? ("Bench run in " + timeResult + "ms\n") : "");
    }

    /**
     * @return
     */
    public double getTimeResult() {
        return timeResult;
    }
}
