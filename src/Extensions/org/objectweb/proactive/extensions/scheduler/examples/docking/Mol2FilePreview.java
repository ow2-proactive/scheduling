/**
 * 
 */
package org.objectweb.proactive.extensions.scheduler.examples.docking;

import java.io.IOException;

import javax.swing.JPanel;

import org.objectweb.proactive.extensions.scheduler.common.task.ResultPreview;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;

/**
 * @author cdelbe
 *
 */
public class Mol2FilePreview extends ResultPreview {

	private static final String MATCH_PATTERN = "Produced output file : ";

	@Override
	public JPanel getGraphicalDescription(TaskResult r) {
		try {
			String pathToResult = this.getPathToFile(r.getOuput().getStdoutLogs(false));
			String molEditor = System.getenv("MOL_EDITOR");
			if (molEditor==null){
				// mercury is default (supposed to be in the path)
				molEditor = "mercury";
			}
			String command = molEditor + " " + pathToResult;
			try {
				Runtime.getRuntime().exec(command); 
			} catch (IOException e) {
				return new SimpleTextPanel("Unable to open external display : " + e.getMessage());
			}
			return new SimpleTextPanel("External display : " + command);
		} catch (RuntimeException e) {
			return new SimpleTextPanel("Unable to open external display : " + e.getMessage());
		}
	}

	@Override
	public String getTextualDescription(TaskResult r) {
		return "Ouput file : " + this.getPathToFile(r.getOuput().getStdoutLogs(false));
	}

	private String getPathToFile(String output) {
		int pos = output.indexOf(MATCH_PATTERN, 0);
		pos += MATCH_PATTERN.length();
		String extracted = output.substring(pos, output.indexOf(".mol2", pos));
		//extracted = ResultPreviewTool.getSystemCompliantPath(extracted);
		return extracted + ".mol2";
	}

}
