package functionalTests.group.dynamicdispatch;

import java.io.Serializable;


public class Task implements Serializable {

    int taskIndex;
    int workerIndex;

    public Task() {
    }

    public Task(int index) {
        this.taskIndex = index;
    }

    public void initialize(int workerIndex) {
        if (0 == workerIndex) {
            try {
                System.out.println("sleeping...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void execute(int workerIndex) {
        this.workerIndex = workerIndex;
        System.out.println("executing task " + taskIndex + " on worker " + workerIndex);
        initialize(workerIndex);
    }

    public int getExecutionWorker() {
        return workerIndex;
    }

}
