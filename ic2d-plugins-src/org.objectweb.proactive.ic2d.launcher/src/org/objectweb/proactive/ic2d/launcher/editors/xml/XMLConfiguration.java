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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


public class XMLConfiguration extends SourceViewerConfiguration {
    private XMLDoubleClickStrategy doubleClickStrategy;
    private XMLTagScanner tagScanner;
    private XMLScanner scanner;
    private ColorManager colorManager;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public XMLConfiguration(ColorManager colorManager) {
        this.colorManager = colorManager;
    }

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] { IDocument.DEFAULT_CONTENT_TYPE, XMLPartitionScanner.XML_COMMENT,
                XMLPartitionScanner.XML_TAG };
    }

    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
        if (doubleClickStrategy == null) {
            doubleClickStrategy = new XMLDoubleClickStrategy();
        }
        return doubleClickStrategy;
    }

    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getXMLTagScanner());
        reconciler.setDamager(dr, XMLPartitionScanner.XML_TAG);
        reconciler.setRepairer(dr, XMLPartitionScanner.XML_TAG);

        dr = new DefaultDamagerRepairer(getXMLScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(new TextAttribute(colorManager
                .getColor(IXMLColorConstants.XML_COMMENT)));
        reconciler.setDamager(ndr, XMLPartitionScanner.XML_COMMENT);
        reconciler.setRepairer(ndr, XMLPartitionScanner.XML_COMMENT);

        return reconciler;
    }

    //
    // -- PROTECTED METHODS ---------------------------------------------
    //
    protected XMLScanner getXMLScanner() {
        if (scanner == null) {
            scanner = new XMLScanner(colorManager);
            scanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager
                    .getColor(IXMLColorConstants.DEFAULT))));
        }
        return scanner;
    }

    protected XMLTagScanner getXMLTagScanner() {
        if (tagScanner == null) {
            tagScanner = new XMLTagScanner(colorManager);
            tagScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager
                    .getColor(IXMLColorConstants.TAG))));
        }
        return tagScanner;
    }
}
