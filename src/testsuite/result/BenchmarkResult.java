/*
 * Created on Jul 23, 2003
 *
 */
package testsuite.result;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import testsuite.exception.BadTypeException;

import testsuite.test.Benchmark;


/**
 * @author Alexandre di Costanzo
 *
 */
public class BenchmarkResult extends TestResult {
    private long min = -1;
    private double moy = -1;
    private double stdev = 0;
    private long max = -1;
    private long[] set = null;

    public BenchmarkResult(Benchmark test, int type, String message, long[] set)
        throws BadTypeException {
        super(test, type, message);
        this.set = set;
        long sumTest = 0;
        for (int i = 0; i < set.length; i++) {
            if ((min < 0) || (set[i] < min)) {
                min = set[i];
            }
            if (set[i] > max) {
                max = set[i];
            }
            sumTest += set[i];
        }
        moy = ((double) sumTest / set.length);
        for (int i = 0; i < set.length; i++)
            stdev += Math.pow(set[i] - moy, 2);
        stdev /= set.length;
        stdev = Math.sqrt(stdev);
    }

    public BenchmarkResult(Benchmark test, int type, String message, Throwable e)
        throws BadTypeException {
        super(test, type, message, e);
    }

    public BenchmarkResult(Benchmark test, int type, String message)
        throws BadTypeException {
        super(test, type, message);
        min = test.getResultTime();
    }

    /**
     * @see testsuite.timeResult.TestResult#toXMLNode()
     */
    public Node toXMLNode(Document document) {
        Node root = super.toXMLNode(document);

        if ((getType() == AbstractResult.RESULT) ||
                (getType() == AbstractResult.GLOBAL_RESULT)) {
            Element result = document.createElement("Benchmark");

            Element bestTime = document.createElement("BestTime");
            Text bestTimeValue = document.createTextNode(min + "");
            bestTime.appendChild(bestTimeValue);
            result.appendChild(bestTime);

            Element maxTime = document.createElement("MaxTime");
            Text maxTimeValue = document.createTextNode(max + "");
            maxTime.appendChild(maxTimeValue);
            result.appendChild(maxTime);

            Element moyTime = document.createElement("MoyTime");
            Text moyTimeValue = document.createTextNode(moy + "");
            moyTime.appendChild(moyTimeValue);
            result.appendChild(moyTime);

            Element stdevTime = document.createElement("STDEV");
            Text stdevTimeValue = document.createTextNode(stdev + "");
            stdevTime.appendChild(stdevTimeValue);
            result.appendChild(stdevTime);

            Element details = document.createElement("Details");
            for (int i = 0; i < set.length; i++) {
                Element bench = document.createElement("Bench");
                Text benchValue = document.createTextNode(set[i] + "");
                bench.appendChild(benchValue);
                details.appendChild(bench);
            }
            result.appendChild(details);

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
        ? ("Bench run in " + min + "ms\n") : "");
    }

    /**
     * @return
     */
    public double getTimeResult() {
        return min;
    }

    /**
     * @return
     */
    public long getMax() {
        return max;
    }

    /**
     * @return
     */
    public long[] getSet() {
        return set;
    }

    /**
     * @return
     */
    public double getMoy() {
        return moy;
    }

    /**
     * @return
     */
    public double getStdev() {
        return stdev;
    }
}
