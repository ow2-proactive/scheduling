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
            default:}
        } finally {
            isTyping = false;
        }
    }

    /**
     * Sets the component model on which this model is based.
     *
     * @param model a component.
     */
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
            default:}
        }
        componentTextChanged(s);
    }
    
}
