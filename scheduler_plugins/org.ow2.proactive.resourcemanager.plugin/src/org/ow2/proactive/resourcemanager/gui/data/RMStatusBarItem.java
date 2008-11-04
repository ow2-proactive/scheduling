package org.ow2.proactive.resourcemanager.gui.data;

import org.eclipse.ui.texteditor.StatusLineContributionItem;


public class RMStatusBarItem extends StatusLineContributionItem {

    private static RMStatusBarItem instance;

    public RMStatusBarItem(String id) {
        super(id, true, 20);
    }

    public static RMStatusBarItem getInstance() {
        if (instance == null)
            instance = new RMStatusBarItem("");
        return instance;
    }

}
