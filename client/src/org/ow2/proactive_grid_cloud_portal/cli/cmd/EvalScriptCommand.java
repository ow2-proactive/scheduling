package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;

public class EvalScriptCommand extends AbstractCommand implements Command {

    private String scriptPathname;
    private String scriptArgs;

    public EvalScriptCommand(String scriptPathname, String scriptArgs) {
        this.scriptPathname = scriptPathname;
        this.scriptArgs = scriptArgs;
    }

    @Override
    public void execute() throws Exception {
        ApplicationContext context = ApplicationContext.instance();
        Writer writer = context().getDevice().getWriter();
        ScriptEngine engine = null;
        if (context.getEngine() != null) {
            engine = context.getEngine();
        } else {
            ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByExtension("js");
            engine.getContext().setWriter(writer);
            context.setEngine(engine);
        }
        try {
            engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE)
                    .putAll(bindings(scriptArgs));
            String script = FileUtility.read(new File(scriptPathname));
            engine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace(new PrintWriter(writer, true));
        }
    }

    private Map<String, String> bindings(String bindingString) {
        Map<String, String> bindings = new HashMap<String, String>();
        String[] pairs = bindingString.split(",");
        for (String pair : pairs) {
            String[] nameValue = pair.split("=");
            bindings.put(nameValue[0], nameValue[1]);
        }
        return bindings;

    }

}
