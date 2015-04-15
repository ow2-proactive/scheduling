package functionaltests;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import java.io.*;
import java.net.URI;


/**
 * A simple java executable used by {@link RestSmartProxyTest}
 * For all files in the localspace (non recursive into folders) creates an
 * output file with the same content and the .out extension.
 *
 * @author The ProActive Team
 */
public class SimpleJavaExecutable extends JavaExecutable {

    private static final Logger log = Logger.getLogger(SimpleJavaExecutable.class.getName());

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        log.info("local space real uri: " + this.getLocalSpace().getRealURI());
        log.info("local space virtual uri: " + this.getLocalSpace().getVirtualURI());

        File localSpaceFolder = new File(URI.create(this.getLocalSpace().getRealURI()));
        log.info("Using localspace folder " + localSpaceFolder.getAbsolutePath());
        File[] files = localSpaceFolder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                log.info("Treating input file " + file.getAbsolutePath());

            } else {
                log.info(file.getAbsolutePath() + " is not a file. ");
            }

            String new_name = file.getName().replace("input", "output");
            new_name = new_name.replace(RestSmartProxyTest.INPUT_FILE_EXT, RestSmartProxyTest.OUTPUT_FILE_EXT);
            File fout = new File(file.getCanonicalFile().getParent(), new_name);
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

            log.info("Written file " + fout.getAbsolutePath());
        }

        log.info("Task End");

        return "OK";
    }

}