package functionaltests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.net.URI;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * A simple java executable used by {@link TestSchedulerProxyUIWithDSSupport}
 * For all files in the localspace (non recursive into folders) creates an
 * output file with the same content and the .out extension.
 *
 * @author esalagea
 *
 */
public class SimpleJavaExecutable extends JavaExecutable {

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {

        System.out.println("local space real uri: " + this.getLocalSpace().getRealURI());
        System.out.println("local space virtual uri: " + this.getLocalSpace().getVirtualURI());

        File localSpaceFolder = new File(URI.create(this.getLocalSpace().getRealURI()));
        System.out.println("Using localspace folder " + localSpaceFolder.getAbsolutePath());
        File[] files = localSpaceFolder.listFiles();

        for (File file : files) {

            if (file.isFile()) {
                System.out.println("Treating input file " + file.getAbsolutePath());

            } else {
                System.out.println(file.getAbsolutePath() + " is not a file. ");
            }

            File fout = new File(file.getAbsolutePath().concat(".out"));
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            BufferedWriter bw = new BufferedWriter(new FileWriter(fout));

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
            br.close();
            System.out.println("Written file " + fout.getAbsolutePath());
        }// for

        System.out.println("Task End");
        return "OK";
    }

}