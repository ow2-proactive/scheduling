package org.objectweb.proactive.resourcemanager;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;


public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

    /**
     * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
     */
    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    /**
     * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
     */
    @Override
    public void initialize(IWorkbenchConfigurer configurer) {
        // To restore window preferences
        configurer.setSaveAndRestore(true);

        // Sets the look of the tabs like Eclipse 3.x
        PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
                false);
        PlatformUI.getPreferenceStore()
                .setValue(IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP, true);
        PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
                IWorkbenchPreferenceConstants.TOP_RIGHT);
    }

    /**
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
     */
    @Override
    public String getInitialWindowPerspectiveId() {
        return "org.objectweb.proactive.extensions.scheduler.gui.ResourceManagerPerspective";
    }
}
