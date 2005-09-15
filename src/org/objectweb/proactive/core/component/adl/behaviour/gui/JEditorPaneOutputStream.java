package org.objectweb.proactive.core.component.adl.behaviour.gui;

import java.io.OutputStream;

import javax.swing.JEditorPane;

/**
 * An OutputStream that allows us to put everything written into a JEditor pane
 */
public class JEditorPaneOutputStream extends OutputStream{
	/**The pne where to print*/
	private JEditorPane editor;
	/**Current content of the editor*/
	private StringBuffer content;
	/**True if the editor is colorized*/
	private boolean colorized = false;
	/**Previous text used to restore a colorized editor*/
	private String previous_text = "";
	
	/**
	 * Constructor
	 * @param editor Editor to affect
	 */
	public JEditorPaneOutputStream(JEditorPane editor){ 
		this.editor = editor; 
		content = new StringBuffer();
	}
	
	/**Closes the stream*/
	public void close()
	{}
	
	/**Write bytes*/
	public void write(byte[] b)
	{ write(b, 0, b.length); }
	
	/**
	 * Appends byte on the String representing the content of the editor
	 */
	public void write(byte[] b, int off, int len){
		byte[] bArray = new byte[len];
		System.arraycopy(b, off, bArray, 0, len);
		content.append(new String(bArray));
	}
	
	/**
	 * Appends byte on the String representing the content of the editor
	 */
	public void write(int b){ 
	content.append(new String(new byte[] { (byte)b }));
	}
	
	/**
	 * Sets the text of the editor
	 *
	 */
	public void setText(){
		String res = content.toString();
		if(colorized)
			editor.setContentType("text/html");
		else
			editor.setContentType("text/plain");
		editor.setText(res);
	}
	
	/**
	 * Colorize the editor
	 *
	 */
	public void colorize(){
		if(!colorized)
			previous_text = editor.getText();
		colorized = true;
		String res = "<html><body>"+editor.getText();
		editor.setContentType("text/html");
		res = res.replaceAll(" ","&nbsp ");
		res = res.replaceAll("\t"," &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp ");
		res = res.replaceAll("\n","<br>");
		res = res.replaceAll("process","<font color=\"blue\">process</font>");
		res = res.replaceAll("behaviour","<font color=\"blue\">behaviour</font>");
		res = res.replaceAll("where","<font color=\"blue\">where</font>");
		res = res.replaceAll("endproc","<font color=\"blue\">endproc</font>");
		res = res.replaceAll("endspec","<font color=\"blue\">endspec</font>");
		res = res.replaceAll("endtype","<font color=\"blue\">endtype</font>");
		res = res.replaceAll("type","<font color=\"blue\">type</font>");
		res = res.replaceAll("ofsort","<font color=\"blue\">ofsort</font>");
		res = res.replaceAll("sorts","<font color=\"blue\">sorts</font>");
		res = res.replaceAll("eqns","<font color=\"blue\">eqns</font>");
		res = res.replaceAll(" is ","<font color=\"blue\">is</font>");
		res = res.replaceAll("opns","<font color=\"blue\">opns</font>");
		editor.setText(res+"</html></body>");
	}
	
	/**
	 * Uncolorize the editor
	 *
	 */
	public void uncolorize(){
		if(!colorized)
			return;
		colorized = false;
		editor.setContentType("text/plain");
		editor.setText(previous_text);
	}
	
	/**
	 * Switch between colorized and not colorized mode
	 *
	 */
	public void switch_state(){
		if(colorized)
			uncolorize();
		else
			colorize();
	}
}