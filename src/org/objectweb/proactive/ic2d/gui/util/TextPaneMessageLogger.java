/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.ic2d.gui.util;

import org.objectweb.proactive.ic2d.util.IC2DMessageLogger;

public class TextPaneMessageLogger implements IC2DMessageLogger {

  private javax.swing.JTextPane messageArea;
  
  private javax.swing.text.Document document;
  
  private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("HH:mm:ss");
  
  private javax.swing.text.Style regularStyle;
  private javax.swing.text.Style stackTraceStyle;
  private javax.swing.text.Style errorStyle;
  private javax.swing.text.Style threadNameStyle;
  private javax.swing.text.Style timeStampStyle;
  private static int MAX_LENGTH = 10000;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public TextPaneMessageLogger(javax.swing.JTextPane messageArea) {
    this.messageArea = messageArea;
    this.document = messageArea.getDocument();
    messageArea.setEditable(false);
    javax.swing.text.Style def = javax.swing.text.StyleContext.getDefaultStyleContext().getStyle(javax.swing.text.StyleContext.DEFAULT_STYLE);
    // regular Style
    regularStyle = messageArea.addStyle("regular", def);
    javax.swing.text.StyleConstants.setFontFamily(regularStyle, "SansSerif");
    javax.swing.text.StyleConstants.setFontSize(regularStyle, 10);
    // error Style
    errorStyle = messageArea.addStyle("error", regularStyle);
    javax.swing.text.StyleConstants.setForeground(errorStyle, java.awt.Color.red);
    // stacktrace Style
    stackTraceStyle = messageArea.addStyle("stackTrace", regularStyle);
    javax.swing.text.StyleConstants.setForeground(stackTraceStyle, java.awt.Color.lightGray);
    // threadName Style
    threadNameStyle = messageArea.addStyle("threadName", regularStyle);
    javax.swing.text.StyleConstants.setForeground(threadNameStyle, java.awt.Color.darkGray);
    javax.swing.text.StyleConstants.setItalic(threadNameStyle, true);
    // timeStamp Style
    timeStampStyle = messageArea.addStyle("timeStamp", regularStyle);
    javax.swing.text.StyleConstants.setForeground(timeStampStyle, java.awt.Color.blue);
    javax.swing.text.StyleConstants.setItalic(timeStampStyle, true);
  }

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  //
  // -- implements IC2DMessageLogger -----------------------------------------------
  //

  public void warn(String message) {
    logInternal(message, errorStyle);
    invokeDialog(message);
  }

  public void log(String message) {
    logInternal(message, regularStyle);
  }

  public void log(String message, Throwable e, boolean dialog) {
    logInternal(message, errorStyle);
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    java.io.PrintWriter pw = new java.io.PrintWriter(baos, false);
    e.printStackTrace(pw);
    pw.flush();
    logInternal(baos.toString(), stackTraceStyle);
    if (dialog)
    	invokeDialog(message);
  }

  public void log(Throwable e, boolean dialog) {
    log(e.getMessage(), e, dialog);
  }
  
  public void log(String message, Throwable e) {
    log(message, e, true);
  }

  public void log(Throwable e) {
    log(e, true);
  }
  



  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
  // make sure the caller thread (especially if it is the gui thread does not get stuck
  // with a deadlock.
  private void invokeDialog(final String message) {
    new Thread(new Runnable() {
      public void run() {
        DialogUtils.displayWarningDialog(messageArea, message);
      }
    }).start();
  }

  private void logInternal(final String message, final javax.swing.text.AttributeSet style) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        append(dateFormat.format(new java.util.Date()), timeStampStyle);
        append(" (", threadNameStyle);
        append(Thread.currentThread().getName(), threadNameStyle);
        append(") => ", threadNameStyle);
        append(message, style);
        append("\n", style);
        messageArea.setCaretPosition(document.getLength());
      }
    });
  }
  
  private void append(String str, javax.swing.text.AttributeSet style) {
    try {
      document.insertString(document.getLength(), str, style);
      int tooMuch = document.getLength() - MAX_LENGTH;
      if (tooMuch > 0)
      	document.remove(0, tooMuch);
    } catch (javax.swing.text.BadLocationException e) {
      e.printStackTrace();
    }
  }
  
}
