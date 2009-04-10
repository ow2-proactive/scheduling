package functionaltests.executables;

import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class Logging extends JavaExecutable {

    public static final String MSG = "LoG";
    public static final String RESULT = "ReSuLt";

    private int numberOfLines;
    private long sleepTime;
    private String stream;

    public Logging() {
    }

    public Serializable execute(TaskResult... results) throws Throwable {

        if (this.stream.equals("out")) {
            for (int i = 0; i < numberOfLines; i++) {
                Thread.sleep(this.sleepTime);
                System.out.println(MSG);
            }
        } else {
            for (int i = 0; i < numberOfLines; i++) {
                Thread.sleep(this.sleepTime);
                System.out.println(MSG);
            }
        }

        return RESULT;
    }

    public void init(Map<String, String> args) throws Exception {
        this.numberOfLines = Integer.parseInt(args.get("lines"));
        this.sleepTime = Long.parseLong(args.get("sleep"));
        this.stream = args.get("stream");
    }

}
