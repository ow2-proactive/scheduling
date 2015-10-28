package org.ow2.proactive_grid_cloud_portal.cli.cmd.scheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Stack;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractIModeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.ImodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.cli.console.JLineDevice;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;

import objectFaker.DataFaker;
import objectFaker.propertyGenerator.PrefixPropertyGenerator;


public abstract class AbstractSchedulerCommandTest {

    @Mock ApplicationContextImpl context;
    @Mock SchedulerRestClient restClient;
    @Mock SchedulerRestInterface restApi;
    @Mock Stack stack;

    protected DataFaker<JobIdData> jobIdFaker;

    protected StringBuffer userInput;

    protected ByteArrayOutputStream capturedOutput;

    protected ScriptEngine engine;


    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
        jobIdFaker = new DataFaker<JobIdData>(JobIdData.class);
        jobIdFaker.setGenerator("readableName", new PrefixPropertyGenerator("job", 1));

        when(context.getRestClient()).thenReturn(restClient);
        when(restClient.getScheduler()).thenReturn(restApi);
        when(context.resultStack()).thenReturn(stack);

        capturedOutput = new ByteArrayOutputStream();
        userInput = new StringBuffer();

        ScriptEngineManager mgr = new ScriptEngineManager();
        engine = mgr.getEngineByExtension("js");
        when(context.getEngine()).thenReturn(engine);

        when(context.getProperty(eq(AbstractIModeCommand.TERMINATE), any(Class.class), anyBoolean())).thenReturn(false);

       
        //Mockito.when(ApplicationContextImpl.currentContext()).thenReturn(context);
        ApplicationContextImpl.mockCurrentContext(context.newApplicationContextHolder());
    }


    protected void configureDevice() throws IOException{
        InputStream in = new ByteArrayInputStream(userInput.toString().getBytes());
        PrintStream out = new PrintStream(capturedOutput);
        AbstractDevice device = new JLineDevice(in, out);

        when(context.getDevice()).thenReturn(device);

        this.engine.getContext().setWriter(device.getWriter());
    }


    protected void typeLine(String line){
        userInput.append(line);
        userInput.append(System.lineSeparator());
    }


    protected abstract void executeCommandWithArgs(Object... args);


    protected void executeTest(Object... args) throws  Exception{
        configureDevice();
        executeCommandWithArgs(args);
    }

    protected void executeTestInteractive() throws Exception{
        configureDevice();
        ImodeCommand interactiveCommand = new ImodeCommand();
        interactiveCommand.execute(context);
    }
}
