/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.scheduler.task;

import javax.swing.JPanel;

import org.objectweb.proactive.extensions.scheduler.common.task.ResultPreview;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskId;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskLogs;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;


/**
 * Class representing the task result.
 * A task result can be an exception or an object that you have to cast into your own type.
 * Before getting the object it is recommended that you call the hadException() method.
 * It will tell you if an exception occurred in the task that generate this result.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Aug 3, 2007
 * @since ProActive 3.9
 */
public class TaskResultImpl implements TaskResult {

    /** The task identification of the result */
    private TaskId id = null;

    /** The value of the result if no exception occurred */
    public Object value = null;

    /** The exception thrown by the task */
    private Throwable exception = null;

    /** Task output */
    public TaskLogs output = null;

    /** Description definition of this result */
    private String previewerClassName = null;
    private transient ResultPreview descriptor = null;

    /** ProActive empty constructor. */
    public TaskResultImpl() {
    }

    /**
     * Return a new instance of task result represented by a task id and its result.
     *
     * @param id the identification of the task that send this result.
     * @param value the result of the task.
     */
    public TaskResultImpl(TaskId id, Object value, TaskLogs output) {
        this.id = id;
        this.value = value;
        this.output = output;
    }

    /**
     * Return a new instance of task result represented by a task id and its exception.
     *
     * @param id the identification of the task that send this result.
     * @param exception the exception that occurred in the task.
     */
    public TaskResultImpl(TaskId id, Throwable exception, TaskLogs output) {
        this.id = id;
        this.exception = exception;
        this.output = output;
    }

    /**
     * <font color='red'>** FOR INTERNAL USE ONLY **</font> - Must be called only by the scheduler to
     * improve memory usage. This method will removed the value and output of this result.
     */
    public void clean() {
        value = null;
        output = null;
    }

    /**
     * Return true if the result has been stored in database, false if not.
     * 
     * @return true if the result has been stored in database, false if not.
     */
    public boolean isInDataBase() {
        return value == null && output == null;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.task.TaskResult#hadException()
     */
    public boolean hadException() {
        return exception != null;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.task.TaskResult#getTaskId()
     */
    public TaskId getTaskId() {
        return id;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.task.TaskResult#value()
     */
    public Object value() throws Throwable {
        if (this.exception != null) {
            throw this.exception;
        } else {
            return value;
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.task.TaskResult#getException()
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.task.TaskResult#getOutput()
     */
    public TaskLogs getOuput() {
        return this.output;
    }

    /**
     *  @see org.objectweb.proactive.extensions.scheduler.common.task.TaskResult#setPreviewerClassName(String)
     */
    public void setPreviewerClassName(String descClass) {
        if (this.previewerClassName != null) {
            throw new RuntimeException("Descriptor class cannot be changed");
        } else {
            this.previewerClassName = descClass;
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.task.TaskResult#getGraphicalDescription()
     */
    public JPanel getGraphicalDescription() {
        boolean instanciation = false;

        try {
            instanciation = this.instanciateDescriptor();
        } catch (InstantiationException e) {
            return new SimpleTextPanel("[SCHEDULER] Cannot create descriptor : " + e.getMessage());
        } catch (IllegalAccessException e) {
            return new SimpleTextPanel("[SCHEDULER] Cannot create descriptor : " + e.getMessage());
        }

        if (instanciation) {
            return this.descriptor.getGraphicalDescription(this);
        } else {
            return new SimpleTextPanel(this.getTextualDescription());
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.task.TaskResult#getTextualDescription()
     */
    public String getTextualDescription() {
        boolean instanciation = false;

        try {
            instanciation = this.instanciateDescriptor();
        } catch (InstantiationException e) {
            return "[SCHEDULER] Cannot create descriptor : " + e.getMessage();
        } catch (IllegalAccessException e) {
            return "[SCHEDULER] Cannot create descriptor : " + e.getMessage();
        }

        if (instanciation) {
            return this.descriptor.getTextualDescription(this);
        } else if (!this.hadException()) {
            return "[DEFAULT DESCRIPTION] " + this.value.toString();
        } else {
            // yes, Guillaume, I know...
            return "[DEFAULT DESCRIPTION] " + this.exception.getMessage();
        }
    }

    /**
     * Create the descriptor instance if descriptor class is available.
     * @return true if the creation occurs, false otherwise
     */
    private boolean instanciateDescriptor() throws InstantiationException, IllegalAccessException {

        if (this.previewerClassName == null) {
            // no descriptor available
            return false;
        } else if (this.descriptor == null) {
            try {

                Class previewClass = Class.forName(this.previewerClassName);
                //       FIXME JFRADJ          
                //                Class previewClass = Class.forName(this.previewerClassName, true, SchedulerClassLoader
                //                        .getClassLoader(this.getClass().getClassLoader()));
                this.descriptor = (ResultPreview) previewClass.newInstance();
                return true;
            } catch (ClassNotFoundException e) {
                System.err.println("Cannot create ResultPreview : " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }
}