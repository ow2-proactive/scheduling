/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.db;

import org.ow2.proactive.db.DatabaseManager.FilteredExceptionCallback;


/**
 * ExceptionHandler is used by database manager to handle the raised exception.<br/>
 * This class just contains a handle method that will throw an exception (or not) depending on the status
 * of the handler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.1
 */
public class DatabaseManagerExceptionHandler {

    public enum DBMEHandler {
        THROW_ALL, //do not check the filter, do not notify, just throw
        FILTER_TOP, //check filter only on the cause, notify
        FILTER_ALL, //check filter on the cause and every causes recursively, notify
        IGNORE_ALL;//do not check filter, do not throw exception
    }

    /** List of filtered exception class */
    private Class<Throwable>[] filter;
    /** State of the exception handler */
    private DBMEHandler state;
    /** callback if needed when filtered exceptions have been found, null if not used */
    private FilteredExceptionCallback callback;

    /**
     * Create a new instance of DatabaseManagerExceptionHandler with a list of exception to be filtered.<br/>
     * If filteredException is empty, the behavior of the {@link #handle(String, Throwable)} method
     * will be the {@link DBMEHandler#THROW_ALL} one.<br/>
     * <br/>
     * If filteredException contains at least one element, the behavior will depend on the state of the handler.<br/>
     * <ul>
     * <li>If the state is {@link DBMEHandler#THROW_ALL}, the {@link #handle(String, Throwable)} method
     * will always throw an exception, and the callback will never be notified.</li>
     * <li>If the state is {@link DBMEHandler#FILTER_TOP} or {@link DBMEHandler#FILTER_ALL},
     * the {@link #handle(String, Throwable)} method will not throw an exception, but the callback will be
     * notified with the raised exception as argument.<br/>
     * It remains possible to throw the passed exception at the end of the notify method. The effect will then
     * be the same as the {@link DBMEHandler#THROW_ALL} behavior.<br/>
     * For example, with a <code>"notify(e){throw e;}"</code> method, then
     * the behavior will be the same as the one if state would be {@link DBMEHandler#THROW_ALL}.
     * </ul>
     * <br/>
     * <i>Note 1</i> : The raised exception in {@link #handle(String, Throwable)} method will be
     * the message and cause wrapped in a DatabaseManagerException<br/>
     * <i>Note 2</i> : If the state is {@link DBMEHandler#IGNORE_ALL}, no exception will be raised at all and
     * the callback won't be notified
     *
     * @param filteredException an element or array of element representing the exception to be filtered
     * @param state the state of this handler, {@link DBMEHandler#THROW_ALL} by default if null is passed
     * @param callback an optional callback if a filtered exception is detected.
     */
    public DatabaseManagerExceptionHandler(Class<Throwable> filteredException[], DBMEHandler state,
            FilteredExceptionCallback callback) {
        this.filter = filteredException;
        if (state == null) {
            state = DBMEHandler.FILTER_ALL;
        }
        this.state = state;
        this.callback = callback;
    }

    /**
     * Handle the given message as an exception.<br/>
     * A call to this method will always throw a DatabaseManagerException with the given message,
     * except if the state is {@link DBMEHandler#IGNORE_ALL}. In this last case, the method will just return
     * dong nothing.
     *
     * @param message the message of the exception to handle
     */
    public void handle(String message) {
        handle(message, null);
    }

    /**
     * Handle the given message and cause as an exception.<br/>
     * <ul>
     * <li>If the state of the handler is {@link DBMEHandler#THROW_ALL}, a DatabaseManagerException is raised
     * with the given message and cause, the callback is not notified.</li>
     * <li>If the state is {@link DBMEHandler#FILTER_TOP}, it will not throw the <b>filtered</b> exception
     * (checked exception is the cause only), the callback will be notified only if the exception was in the filter.</li>
     * <li>If the state is {@link DBMEHandler#FILTER_ALL}, it will not throw the <b>filtered</b> exception
     * (checked exception is every causes recursively), the callback will be notified only if the exception was in the filter.</li>
     * <li>If the state is {@link DBMEHandler#IGNORE_ALL}, it will not throw any exception, the callback will NOT be notified.</li>
     * </ul>
     * <br/>
     * <i>Note 1</i> : The raised exception is the message and cause wrapped in a DatabaseManagerException
     * <br/>
     * <i>Note 2</i> : If the given cause does not match the filter or the state is {@link DBMEHandler#THROW_ALL},
     * the callback will not be notified and the exception will be thrown.
     *
     * @param message the message of the exception to handle
     * @param cause the cause of the exception (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown. In such a case, the call is the same as calling the {@link #handle(String)} method.)
     * @throw DatabaseManagerException as explained above
     */
    public void handle(String message, Throwable cause) {
        if (cause == null && this.state != DBMEHandler.IGNORE_ALL) {
            throw new DatabaseManagerException(message);
        }
        switch (this.state) {
            case IGNORE_ALL:
                return;
            case THROW_ALL:
                throw new DatabaseManagerException(message, cause);
            case FILTER_TOP:
            case FILTER_ALL:
                if (checkInFilter(cause)) {
                    if (callback != null) {
                        callback.notify(new DatabaseManagerException(message, cause));
                    }
                    return;
                } else {
                    throw new DatabaseManagerException(message, cause);
                }
        }
    }

    /**
     * Check if the given exception is in the filter or not, depending of
     * the state of this handler.<br/>
     * <ul>
     * <li>If the state value is {@link DBMEHandler#FILTER_ALL}, the method will check in all causes
     * recursively.</li>
     * <li>If the state value is {@link DBMEHandler#FILTER_TOP}, the method will check the given exception
     * only.</li>
     * </ul>
     * <br/>
     * Return true if the exception is in the filter, false otherwise.<br/>
     *
     * @param exception the exception to be checked
     * @return true if the exception is in the filter, false otherwise.<br/>
     */
    private boolean checkInFilter(Throwable exception) {
        if (exception == null) {
            return false;
        }
        if (filter == null || filter.length == 0) {
            return false;
        }
        do {
            Class<? extends Throwable> cclass = exception.getClass();
            for (Class<Throwable> fclass : filter) {
                if (fclass.equals(cclass)) {
                    return true;
                }
            }
            exception = exception.getCause();
        } while (state == DBMEHandler.FILTER_ALL && exception != null);
        return false;
    }

    /**
     * Get the state of the handler
     *
     * @return the state of the handler
     */
    public DBMEHandler getState() {
        return state;
    }

    /**
     * Set the state handler value to the given state value
     *
     * @param state the state handler to set
     */
    public void changeState(DBMEHandler state) {
        this.state = state;
    }

}
