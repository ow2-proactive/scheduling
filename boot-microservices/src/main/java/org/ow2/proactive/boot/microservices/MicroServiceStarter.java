package org.ow2.proactive.boot.microservices;

import java.io.*;
import java.util.StringJoiner;

import org.apache.log4j.Logger;

/**
 * Created by nebil on 09/04/18.
 */
public class IAM {

    private static final Logger logger = Logger.getLogger(IAM.class);
    private static final String separator = File.separator;
    private static final String os = System.getProperty("os.name");
    private static final String ready_indicator = "";

    /*private Properties paPropProperties;

    private List<String> paPropList;

    private OperatingSystem targetOS = OperatingSystem.UNIX;

    private boolean detached = false;

    private static String command = "${java.home}/bin/java -jar ";

    private static String microservice = "iam.war";*/

    public static void start (String PA_home, String microservice_name, boolean detached) throws InterruptedException,
            IOException {

        String command = getJavaCommand(PA_home)+getMicroservicePath(PA_home,microservice_name);

        if (detached) getDetachedCommand(command);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        logger.info("Staring boot microservice "+ microservice_name);
        Process process = processBuilder.start();
        int errCode = process.waitFor();
        System.out.println("Echo command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
        System.out.println("Echo Output:\n" + output(process.getInputStream()));
    }

    private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }

    private static String getDetachedCommand(String command) {

        StringJoiner stringJoiner = null;

        if (os.equals(OperatingSystem.UNIX)) {
            // if the system is unix-based, we need to start the process with
            // the nohup indicator it normally goes with the end of the
            // command finished with the background indicator '&' (see the end
            // of command building)

            stringJoiner = new StringJoiner("", "nohup", "");


        } else if (os.equals(OperatingSystem.WINDOWS)) {
            // Windows equivalent is to use the start command with /b option
            stringJoiner = new StringJoiner("", "start /b", "");

        }

        return stringJoiner.add(command).toString();
    }

    private static String getMicroservicePath(String PA_Home, String microservice_name){

       return PA_Home+separator+"dist"+separator+"boot"+separator+microservice_name;
    }

    private static String getJavaCommand(String PA_Home){

        return PA_Home+separator+"jre"+separator+"bin"+separator+"java  -jar ";
    }

}
