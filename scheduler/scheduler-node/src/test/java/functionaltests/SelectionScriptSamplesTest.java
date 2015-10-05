package functionaltests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scripting.InvalidScriptException;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.python.icu.impl.Assert;

/**
 * This class is in charge to evaluate all selection scripts available in
 * {@code PA_SCHEDULER_HOME/samples/scripts/selection} for checking whether they are valid or not
 * (i.e. no evaluation error and a boolean variable named "selected" is defined at the end of the evaluation).
 * <p/>
 * Selections scripts are evaluated in a forked process for having the possibility to change the current
 * working directory, thus allowing scripts to import files relative to
 * {@code PA_SCHEDULER_HOME/samples/scripts/selection}.
 * <p/>
 * The class uses parameterized Junit tests for generating independent test methods for each selection script
 * which is tested.
 */
@RunWith(Parameterized.class)
public class SelectionScriptSamplesTest {

    private static final Logger log = Logger.getLogger(SelectionScriptSamplesTest.class);

    private static final String PA_SCHEDULER_HOME = System.getProperty("pa.scheduler.home");

    private Path selectionScript;

    // used by reflection with Junit to name test method
    private String testMethodName;

    public SelectionScriptSamplesTest(Path selectionScript, String testMethodName) {
        this.selectionScript = selectionScript;
        this.testMethodName = testMethodName;
    }

    @Parameterized.Parameters(name = "{1}")
    public static Iterable<Object[]> data() throws IOException {
        final List<Path> selectionScriptsToTest = getSelectionScriptsToTest();

        Object[][] parameters = new Object[selectionScriptsToTest.size()][2];

        for (int i = 0; i < parameters.length; i++) {
            final Path selectionScript = selectionScriptsToTest.get(i);
            String fileName = selectionScript.getFileName().toString();

            parameters[i][0] = selectionScript;
            parameters[i][1] = "test" + Character.toUpperCase(fileName.charAt(0)) + fileName.substring(1);
        }

        return Arrays.asList(parameters);
    }

    @Test
    public void testSelectionScriptValidity() throws IOException, NodeException, InvalidScriptException, ClassNotFoundException, InterruptedException {
        log.info("Evaluating " + selectionScript);

        try {
            evaluateSelectionScript(selectionScript);
        } catch (ScriptEvaluationException e) {
            Assert.fail(e);
        }
    }

    public static void evaluateSelectionScript(
            Path selectionScript) throws IOException, InterruptedException, ScriptEvaluationException {
        Process process = forkEvaluation(selectionScript);

        int exitCode = process.waitFor();

        if (exitCode > 0) {
            final InputStream errorStream = process.getErrorStream();
            String errorMessage = CharStreams.toString(new InputStreamReader(errorStream, Charsets.UTF_8));
            Closeables.closeQuietly(errorStream);

            throw new ScriptEvaluationException(errorMessage);
        }
    }

    private static Process forkEvaluation(Path selectionScript) throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = SelectionScriptEvaluator.class.getCanonicalName();

        ProcessBuilder builder =
                new ProcessBuilder(
                        javaBin, "-cp", classpath,
                        className, selectionScript.toAbsolutePath().toString());

        builder.directory(new File(PA_SCHEDULER_HOME));
        return builder.start();
    }

    private static List<Path> getSelectionScriptsToTest() throws IOException {
        ImmutableList.Builder<Path> result = ImmutableList.builder();

        try (DirectoryStream<Path> directoryStream =
                     Files.newDirectoryStream(
                             Paths.get(PA_SCHEDULER_HOME, "samples", "scripts", "selection"))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)
                        && !com.google.common.io.Files.getFileExtension(
                        path.getFileName().toString()).equals("txt")) {
                    result.add(path);
                }
            }
        }

        return result.build();
    }

    private static final class ScriptEvaluationException extends Exception {

        public ScriptEvaluationException(String message) {
            super(message);
        }

    }

}
