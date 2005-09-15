package org.objectweb.proactive.core.component.adl.behaviour.gui;

import java.io.OutputStream;

import javax.swing.JTextArea;

/**
 * An OutputStream that allows us to put everything written into a JTextArea
 */
public class JTextAreaOutputStream extends OutputStream{
	
	/**TextArea to affect*/
	private JTextArea textArea;
	
	/**Constructor*/
	public JTextAreaOutputStream(JTextArea area)
	{ textArea = area; }
	
	/**Closes the stream*/
	public void close()
	{}
	
	/**Write bytes*/
	public void write(byte[] b)
	{ write(b, 0, b.length); }
	
	/**
	 * Append bytes on the textarea
	 */
	public void write(byte[] b, int off, int len){
		byte[] bArray = new byte[len];
		System.arraycopy(b, off, bArray, 0, len);	
		textArea.append(new String(bArray));
	}
	
	/**
	 * Append bytes on the textarea
	 */
	public void write(int b)
	{ textArea.append(new String(new byte[] { (byte)b })); }
}