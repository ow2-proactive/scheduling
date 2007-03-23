package org.objectweb.proactive.ic2d.infrastructuremanager.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewAdministration;
import org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewInfrastructure;
import org.objectweb.proactive.ic2d.infrastructuremanager.views.IMViewLegend;

public class IMPerspective implements IPerspectiveFactory {

	public static final String ID = "org.objectweb.proactive.ic2d.infrastructuremanager.perspectives.IMPerspective";

	/** Left folder's id. */
	public static final String FI_LEFT_ADMIN = ID + ".leftAdminFolder";
	public static final String FI_LEFT_LEGEND = FI_LEFT_ADMIN + ".leftLegendFolder";
	/** Top folder's id. */
	public static final String FI_TOP = ID + ".topFolder";
	/** Bottom folder's id. */
	public static final String FI_BOTTOM = ID + ".bottomFolder";

	public void createInitialLayout(IPageLayout layout) {
		String editorAreaId = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		
		IFolderLayout leftAdminFolder = layout.createFolder(FI_LEFT_ADMIN, IPageLayout.LEFT, 0.30f, editorAreaId);
		leftAdminFolder.addView(IMViewAdministration.ID);
		
		IFolderLayout leftLegendFolder = layout.createFolder(FI_LEFT_LEGEND, IPageLayout.BOTTOM, 0.50f, FI_LEFT_ADMIN);
		leftLegendFolder.addView(IMViewLegend.ID);
		
		IFolderLayout topFolder = layout.createFolder(FI_TOP, IPageLayout.TOP, 0.75f, editorAreaId);
		topFolder.addPlaceholder(IMViewInfrastructure.ID+":"+IMViewInfrastructure.ID+"*");

		IFolderLayout bottomFolder = layout.createFolder(FI_BOTTOM, IPageLayout.BOTTOM, 0.75f, editorAreaId);
		bottomFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
	}

}
