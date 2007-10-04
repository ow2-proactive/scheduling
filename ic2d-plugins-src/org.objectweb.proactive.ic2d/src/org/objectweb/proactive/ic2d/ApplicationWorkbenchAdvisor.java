package org.objectweb.proactive.ic2d;

import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;


public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
        IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    public void initialize(IWorkbenchConfigurer configurer) {
        // To restore window preferences
        //super.initialize(configurer);
        configurer.setSaveAndRestore(true);

        // Sets the look of the tabs like Eclipse 3.x
        PlatformUI.getPreferenceStore()
                  .setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
            false);
    }

    public String getInitialWindowPerspectiveId() {
        return DefaultPerspective.ID;
        //return "org.objectweb.proactive.ic2d.DefaultPerspective";
    }
}
