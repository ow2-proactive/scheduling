package org.objectweb.proactive.ic2d.p2PMonitoring.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class P2PPerspective implements IPerspectiveFactory {
    public static final String ID = "org.objectweb.proactive.ic2d.p2PMonitoring.perspectives.P2PPerspective";

    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(false);
        layout.setFixed(false);
        //layout.addActionSet(id);
        layout.addView(P2PPerspective.ID, IPageLayout.LEFT, 0.25f, editorArea);
        layout.addPerspectiveShortcut(ID);
    }
}
