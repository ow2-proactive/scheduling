/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package selectionutils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 * EngineScript provides an "EvalScript" method which is useful to test script
 * evaluation from scratch.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class EngineScript {

    private static final String propertiesFile = "propertiesFile";

    /**
     * Define supported programming languages
     */
    public enum Language {
        javascript("js"), python("py"), ruby("rb");

        private String extension;

        private Language(String ext) {
            extension = ext;
        }

        public String getExtension() {
            return extension;
        }
    };

    /**
     * Execute a script and return a result of this form : 'selected={true|false}'
     * Return null if problem occurs
     *
     * @param the path of your script
     * @param the language of your script
     * @param propertyFile the file where to find the properties to test
     * 			behavior just used for the test. The filePath (relative issue) is passed to the script as argument
     * @return the result string or null if problem occurs
     */
    public static String EvalScript(String pathFile, Language language, String propertyFile) {
        // ScriptEngineManager
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine moteur;

        // Engine selection
        switch (language) {
            case javascript:
            case python:
            case ruby:
                moteur = manager.getEngineByExtension(language.getExtension());
                break;
            default:
                System.out.println("Invalid language");
                return null;
        }

        try {
            Bindings bindings = moteur.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.clear();
            bindings.put(propertiesFile, propertyFile);
            moteur.eval(readFile(new File(pathFile)), bindings);

            return "selected=" + bindings.get("selected");
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read a file and return the content as a string
     *
     * @param f the file to read
     * @return the content of the file as a string
     */
    private static String readFile(File f) {
        try {
            // Define input/output
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
            StringWriter out = new StringWriter();

            // Write file content into output
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            // Clean
            out.close();
            in.close();
            // Return file content
            return out.toString();
        } catch (IOException ie) {
            ie.printStackTrace();
            return "none";
        } catch (Exception e) {
            e.printStackTrace();
            return "none";
        }
    }

}
