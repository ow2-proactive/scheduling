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
package org.objectweb.proactive.ic2d.gui.components.dialog.model;

import org.objectweb.fractal.gui.dialog.model.TextFieldModel;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveTextFieldModel extends TextFieldModel {
    public final static int VIRTUAL_NODE = 6;
    public final static int EXPORTED_VIRTUAL_NODE = 7;
    public final static int COMPOSING_VIRTUAL_NODES = 8;

    /**
     * Constructs a new {@link ProActiveTextFieldModel} object.
     *
     * @param type code of the component's part that must be represented by this
     *      model. This code must be one of the constants defined in this class.
     */
    public ProActiveTextFieldModel(final int type) {
        super(type);
    }

    /**
     * Modifies the model on which this model is based, to reflect a change in
     * this model.
     *
     * @param s the new component's name, type, implementation... depending on the
     *      {@link #type} of this model.
     */
    @Override
    protected void setComponentText(final String s) {
        if (model == null) {
            return;
        }
        isTyping = true;
        try {
            switch (type) {
            case VIRTUAL_NODE:
                ((ProActiveComponent) model).setVirtualNode(s);
                break;
            case EXPORTED_VIRTUAL_NODE:
                ((ProActiveComponent) model).setCurrentlyEditedExportedVirtualNodeName(s);
                break;
            case COMPOSING_VIRTUAL_NODES:
                ((ProActiveComponent) model).setCurrentlyEditedComposingVirtualNodesNames(s);
                break;
            default:
            }
        } finally {
            isTyping = false;
        }
    }

    /**
     * Sets the component model on which this model is based.
     *
     * @param model a component.
     */
    @Override
    protected void setComponentModel(final Component model) {
        this.model = model;
        String s = null;
        if (model != null) {
            switch (type) {
            case VIRTUAL_NODE:
                s = ((ProActiveComponent) model).getVirtualNode();
                break;
            case EXPORTED_VIRTUAL_NODE:
                s = ((ProActiveComponent) model).getCurrentlyEditedExportedVirtualNodeName();
                break;
            case COMPOSING_VIRTUAL_NODES:
                s = ((ProActiveComponent) model).getCurrentlyEditedComposingVirtualNodesNames();
                break;
            default:
            }
        }
        componentTextChanged(s);
    }
}
