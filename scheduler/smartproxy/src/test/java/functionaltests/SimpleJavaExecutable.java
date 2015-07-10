package functionaltests;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;


/**
 * A simple java executable used by {@link TestSmartProxy}
 * For all files in the localspace (non recursive into folders) creates an
 * output file with the same content and the .out extension.
 */
public class SimpleJavaExecutable extends JavaExecutable {

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        File localSpaceFolder = new File(".");
        System.out.println("Using localspace folder " + localSpaceFolder.getAbsolutePath());
        System.out.println(localSpaceFolder.listFiles());
        File[] inputFiles = localSpaceFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(TestSmartProxy.inputFileExt);
            }
        });

        for (File inputFile : inputFiles) {
            String outputFileName = inputFile.getName()
                    .replace("input", "output")
                    .replace(TestSmartProxy.inputFileExt, TestSmartProxy.outputFileExt);
            File outputFile = new File(outputFileName);
            FileUtils.copyFile(inputFile, outputFile);
            System.out.println("Written file " + outputFile.getAbsolutePath());
        }
        return "OK";
    }

}