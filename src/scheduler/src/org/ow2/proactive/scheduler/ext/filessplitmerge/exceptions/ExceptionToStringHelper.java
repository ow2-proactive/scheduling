//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;


public class ExceptionToStringHelper {

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

}
