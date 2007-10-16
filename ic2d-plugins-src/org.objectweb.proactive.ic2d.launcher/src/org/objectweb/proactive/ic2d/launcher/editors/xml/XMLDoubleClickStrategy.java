/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.launcher.editors.xml;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;


public class XMLDoubleClickStrategy implements ITextDoubleClickStrategy {
    protected ITextViewer fText;

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //
    public void doubleClicked(ITextViewer part) {
        int pos = part.getSelectedRange().x;

        if (pos < 0) {
            return;
        }

        fText = part;

        if (!selectComment(pos)) {
            selectWord(pos);
        }
    }

    //
    // -- PROTECTED METHODS ---------------------------------------------
    //
    protected boolean selectComment(int caretPos) {
        IDocument doc = fText.getDocument();
        int startPos;
        int endPos;

        try {
            int pos = caretPos;
            char c = ' ';

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (c == '\\') {
                    pos -= 2;
                    continue;
                }
                if ((c == Character.LINE_SEPARATOR) || (c == '\"')) {
                    break;
                }
                --pos;
            }

            if (c != '\"') {
                return false;
            }

            startPos = pos;

            pos = caretPos;
            int length = doc.getLength();
            c = ' ';

            while (pos < length) {
                c = doc.getChar(pos);
                if ((c == Character.LINE_SEPARATOR) || (c == '\"')) {
                    break;
                }
                ++pos;
            }
            if (c != '\"') {
                return false;
            }

            endPos = pos;

            int offset = startPos + 1;
            int len = endPos - offset;
            fText.setSelectedRange(offset, len);
            return true;
        } catch (BadLocationException x) {
        }

        return false;
    }

    protected boolean selectWord(int caretPos) {
        IDocument doc = fText.getDocument();
        int startPos;
        int endPos;

        try {
            int pos = caretPos;
            char c;

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                --pos;
            }

            startPos = pos;

            pos = caretPos;
            int length = doc.getLength();

            while (pos < length) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                ++pos;
            }

            endPos = pos;
            selectRange(startPos, endPos);
            return true;
        } catch (BadLocationException x) {
        }

        return false;
    }

    //
    // -- PRIVATE METHODS ---------------------------------------------
    //
    private void selectRange(int startPos, int stopPos) {
        int offset = startPos + 1;
        int length = stopPos - offset;
        fText.setSelectedRange(offset, length);
    }
}
