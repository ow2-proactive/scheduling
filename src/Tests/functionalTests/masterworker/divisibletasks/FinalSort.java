package functionalTests.masterworker.divisibletasks;

import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A task which sorts the given list using Collections sort method
 *
  @author The ProActive Team 
 */
public class FinalSort implements Task<ArrayList<Integer>> {

    private ArrayList<Integer> input;

    public FinalSort(ArrayList<Integer> input) {
        this.input = input;
    }

    public ArrayList<Integer> run(WorkerMemory memory) throws Exception {
        Collections.sort(input);
        return input;
    }
}
