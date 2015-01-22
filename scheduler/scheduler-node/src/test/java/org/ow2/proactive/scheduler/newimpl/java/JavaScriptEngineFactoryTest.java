package org.ow2.proactive.scheduler.newimpl.java;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.junit.Test;

import static org.junit.Assert.*;


public class JavaScriptEngineFactoryTest {

    @Test
    public void executable_with_output_and_result() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("java");

        StringWriter output = new StringWriter();
        StringWriter error = new StringWriter();
        engine.getContext().setWriter(output);
        engine.getContext().setErrorWriter(new PrintWriter(error));

        Object result = engine.eval(WaitAndPrint.class.getName());

        assertNotEquals("", output.toString());
        assertNotEquals("", error.toString());
        assertNotEquals("", result);
        assertNotEquals("", engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).get("result"));
    }
}