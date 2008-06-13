package functionalTests.component.collectiveitf.dynamicdispatch;

import java.io.Serializable;


public class Result implements Serializable {

    int workerIndex = -1;
    int value = -1;

    public Result(int value, int workerIndex) {
        super();
        this.value = value;
        this.workerIndex = workerIndex;
    }

    public int getWorkerIndex() {
        return workerIndex;
    }

    public int getValue() {
        return value;
    }

}
