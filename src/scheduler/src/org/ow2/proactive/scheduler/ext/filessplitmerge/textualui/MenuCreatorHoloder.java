//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.textualui;

import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.NotInitializedException;


public class MenuCreatorHoloder {

    private static GeneralMenuCreator menuCreator;

    public static void setMenuCreator(Class<? extends GeneralMenuCreator> mc) throws InstantiationException,
            IllegalAccessException {

        menuCreator = mc.newInstance();
    }

    public static GeneralMenuCreator getMenuCreator() throws NotInitializedException {

        if (menuCreator == null) {
            throw new NotInitializedException(
                "No menu creator has been defined. Use setPostTreatmentManager static method in class " +
                    MenuCreatorHoloder.class.getName() + "");
        }
        return menuCreator;

    }

}
