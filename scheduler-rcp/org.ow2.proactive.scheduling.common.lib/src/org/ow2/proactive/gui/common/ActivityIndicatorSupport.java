package org.ow2.proactive.gui.common;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * ActiveObjectActivityListener which displays busy cursor during the time
 * when active object call is in progress.
 * (note: busy cursor is displayed only if method call didn't finish in 1 second).  
 * 
 * @author The ProActive Team
 *
 */
public class ActivityIndicatorSupport extends ActiveObjectProxy.ActiveObjectActivityListener {

    public static final long CURSOR_DISPLAY_DELAY = 1000;

    static final String BUSYID_NAME = "SWT BusyIndicator";

    private boolean disabled;

    private boolean cursorChanged;

    private static final ScheduledThreadPoolExecutor busyCursorTimer = new ScheduledThreadPoolExecutor(1);;

    @Override
    synchronized public void onActivityStarted() {
        if (!disabled) {
            busyCursorTimer.schedule(new Runnable() {
                public void run() {
                    showBusyCursorIfNeeded();
                }
            }, CURSOR_DISPLAY_DELAY, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    synchronized public void onActivityFinished() {
        if (!disabled && cursorChanged) {
            cursorChanged = false;
            ActivityIndicatorSupport.setDefaultCursor(null);
        }
    }

    /**
     * Disable listener, when listener is disabled it
     * doesn't handle active object's events. If cursor
     * was changed to busy then default cursor is restored.
     * 
     * (it could be necessary to disable listener when
     * operation is in progress in one perspective and user switches
     * to another perspective)
     */
    synchronized public void disable() {
        disabled = true;

        if (cursorChanged) {
            cursorChanged = false;
            ActivityIndicatorSupport.setDefaultCursor(null);
        }
    }

    /**
     * Enable listener (it cancels effect of previous 'disable' call).
     * If active object call is in progress then busy cursor 
     * is displayed.
     */
    synchronized public void enable() {
        disabled = false;

        showBusyCursorIfNeeded();
    }

    synchronized public void showBusyCursorIfNeeded() {
        if (!disabled && isActivityInProgress()) {
            cursorChanged = true;
            ActivityIndicatorSupport.setBusyCursor(null);
        }
    }

    private synchronized static void setBusyCursor(Display display) {
        final Display targetDisplay = display != null ? display : Display.getDefault();
        targetDisplay.syncExec(new Runnable() {
            public void run() {
                Cursor cursor = targetDisplay.getSystemCursor(SWT.CURSOR_WAIT);
                for (Shell shell : targetDisplay.getShells()) {
                    shell.setData(BUSYID_NAME, Integer.MAX_VALUE);
                    shell.setCursor(cursor);
                }
            }
        });
    }

    private synchronized static void setDefaultCursor(Display display) {
        final Display targetDisplay = display != null ? display : Display.getDefault();
        targetDisplay.syncExec(new Runnable() {
            public void run() {
                for (Shell shell : targetDisplay.getShells()) {
                    shell.setData(BUSYID_NAME, null);
                    shell.setCursor(null);
                }
            }
        });
    }

}
