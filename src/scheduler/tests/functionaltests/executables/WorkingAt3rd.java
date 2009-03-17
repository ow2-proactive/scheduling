package functionaltests.executables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Serializable;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class WorkingAt3rd extends JavaExecutable {

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        String fileName = System.getProperty("java.io.tmpdir") + File.separator + "WorkingAt3rd13031984.tmp";
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
            throw new RuntimeException("WorkingAt3rd - Status : Number is " + n);
        }
        //file number is 2 or more
        boolean b = f.delete();
        return "WorkingAt3rd - Status : OK / File deleted : " + b;
    }

}
