package functionaltests.executables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class WorkingAt3rd extends JavaExecutable {

    private String prefix = null;
    private String suffix = null;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        String fileName = System.getProperty("java.io.tmpdir") + File.separator + prefix + suffix + ".tmp";
        File f = new File(fileName);
        //file does not exist
        if (!f.exists()) {
            f.createNewFile();
            PrintWriter pw = new PrintWriter(f);
            pw.write("1");
            pw.close();
            throw new RuntimeException("WorkingAt3rd - Status : File not found");
        }
        //file exist
        BufferedReader br = new BufferedReader(new FileReader(f));
        int n = Integer.parseInt(br.readLine());
        br.close();
        //file number is less that 2
        if (n < 2) {
            PrintWriter pw = new PrintWriter(f);
            pw.write("" + (n + 1));
            pw.close();
            if (prefix.equals("WorkingAt3rdT2_")) {
                boolean b = f.delete();
                throw new RuntimeException("WorkingAt3rd - Status : Number is " + n + " File deleted : " + b);
            } else {
                throw new RuntimeException("WorkingAt3rd - Status : Number is " + n);
            }
        }
        //file number is 2 or more
        boolean b = f.delete();
        return "WorkingAt3rd - Status : OK / File deleted : " + b;
    }

    @Override
    public void init(Map<String, String> args) throws Exception {
        prefix = args.get("prefix");
        suffix = args.get("suffix");
    }

}
